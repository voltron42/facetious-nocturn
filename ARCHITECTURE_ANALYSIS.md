# Architecture Analysis: facetious-nocturn vs Real-Time Alternatives

## Overview

This document analyzes facetious-nocturn's HTTP-based turn-based session architecture against real-time alternatives (Colyseus, WebSockets, Server-Sent Events), examining trade-offs for different use cases.

---

## Quick Comparison

| Aspect | facetious-nocturn (HTTP) | Colyseus (WebSocket) | SSE (Hybrid) |
|--------|---------------------------|----------------------|--------------|
| **Transport** | HTTP Request-Response | WebSocket | HTTP with SSE |
| **Connection Model** | Stateless | Persistent | Persistent (one-way) |
| **Message Direction** | Polling (client→server) | Full duplex | Push (server→client) + POST (client→server) |
| **Latency** | 200-500ms (polling) | <50ms | <100ms |
| **Complexity** | Low | High | Medium |
| **Server Resource** | Low (stateless) | High (per-connection) | Medium (per-listener) |
| **Best For** | Turn-based games | Real-time action games | Turn-based with responsive UX |

---

## Detailed Analysis

### Colyseus vs facetious-nocturn

**Colyseus:**
- **Design:** Real-time game networking framework
- **Protocol:** WebSocket (persistent bidirectional connection)
- **State Sync:** Automatic delta updates broadcast to all clients
- **Typical Latency:** <50ms
- **Use Case:** Action games, shooter games, multiplayer platformers

**facetious-nocturn:**
- **Design:** Session management for turn-based games
- **Protocol:** HTTP (request-response)
- **State Sync:** Guest polls for updates, host responds
- **Typical Latency:** 200-500ms (depends on poll frequency)
- **Use Case:** Turn-based games, async multiplayer

#### Key Differences

1. **Connection Model**
   - Colyseus: Keeps connection open 24/7
   - facetious-nocturn: Closes connection after each request

2. **Resource Usage**
   - Colyseus: Memory per active player (100 players = 100 connections)
   - facetious-nocturn: Only memory for session state itself

3. **Scaling**
   - Colyseus: Expensive at high CCU (Concurrent Users)
   - facetious-nocturn: Cheap at high CCU (stateless)

---

### The Polling Problem

#### Original Analysis: HTTP Seems Cheaper

Initial assumption: HTTP stateless servers are cheaper than persistent WebSocket connections.

**But this ignores guest polling overhead:**

```
100 guests polling every 500ms:
- 200 requests/second
- 720,000 requests/hour
- ~1.08 GB bandwidth/hour (at ~1.5KB per request)
- Each request: parsing, authentication, database query
```

#### Revised Reality: Polling Gets Expensive

**Bandwidth comparison (100 guests, 1-hour session):**
- Polling (every 500ms): 1+ GB
- WebSocket: ~100 KB
- **Winner: WebSocket (by 10,000x)**

**Compute comparison:**
- Polling: 720,000 authentication checks, database queries
- WebSocket: Persistent connection, updates on change only
- **Winner: WebSocket (drastically lower CPU)**

#### Key Insight

HTTP polling is only genuinely cheaper if polling frequency is **very low (every 10-30 seconds)**. But then player feedback lag becomes unacceptable for interactive gameplay.

**Cost-effectiveness threshold:** Once guests poll faster than every 5-10 seconds, WebSocket/SSE becomes more efficient.

---

## Push Notification Solutions

Instead of polling, facetious-nocturn could use **push notifications** to eliminate wasteful repeated requests.

### Option 1: Server-Sent Events (SSE) — RECOMMENDED

**What it is:** HTTP-based one-way push from server to client.

**How it works:**
```javascript
// CLIENT: Open persistent connection for push updates
const eventSource = new EventSource(`/stream/${sessionId}/${guestId}`);

eventSource.addEventListener('host-update', (event) => {
  const newState = JSON.parse(event.data);
  updateDisplay(newState);
});

// Client still uses POST to send moves (separate channel)
function makeMove(move) {
  fetch(`/move/${sessionId}/${guestId}`, {
    method: 'POST',
    body: JSON.stringify(move)
  });
}
```

```clojure
;; SERVER: Send events to listening guests
(defn send-event-to-guest [guest-id event-type data]
  (write-to-event-stream guest-id
    (str "event: " event-type "\n"
         "data: " (json/write-str data) "\n\n")))

;; When host makes a move, push to all guests
(defn on-host-move [session-id move]
  (let [guests (get-session-guests session-id)]
    (doseq [guest guests]
      (send-event-to-guest guest "host-move" move))))
```

**Pros:**
- ✅ Built on HTTP (no protocol switch needed)
- ✅ Simple single connection per client
- ✅ Automatic reconnect on disconnect
- ✅ Lower overhead than polling
- ✅ Still more efficient than full WebSocket
- ✅ Backward compatible with existing REST API

**Cons:**
- ❌ One-way only (needs separate POST requests for client→server)
- ❌ Less universal browser support than WebSocket (though broadly supported now)

**Bandwidth comparison:**
- Polling: 1+ GB/hour
- SSE: ~5-10 MB/hour (updates only, no headers spam)
- WebSocket: ~1-5 MB/hour

---

### Option 2: WebSocket — Full Duplex

**What it is:** Full bidirectional protocol switch.

**Pros:**
- ✅ Bidirectional (no separate POST requests needed)
- ✅ Lowest latency (<50ms)
- ✅ Most mature ecosystem

**Cons:**
- ❌ Requires protocol upgrade
- ❌ More complex connection state management
- ❌ Higher server resource usage

**This is Colyseus's approach** - overkill for turn-based but efficient if using it.

---

### Option 3: Browser Push API — Offline Only

**What it is:** Native OS notifications via service workers.

**Pros:**
- ✅ Works when browser is closed
- ✅ Native OS notifications

**Cons:**
- ❌ Requires service worker setup
- ❌ Only for notifications, not real-time state
- ❌ Overkill for active gaming sessions
- ❌ Requires user opt-in

**Use case:** "It's your turn!" notification while browser closed, but not for active gameplay.

---

## Implementation Path: Adding SSE to facetious-nocturn

### Architecture Change

**Current:**
```
POST /host          → Create session
POST /join/:id      → Join session
GET /state/:id      → Guest polls (repeated)
```

**With SSE:**
```
POST /host          → Create session
POST /join/:id      → Join session
GET /stream/:id     → Guest opens SSE stream (persistent)
POST /move/:id      → Send move (existing)
```

### Code Changes Needed

**1. Add listener registry to session-manager:**

```clojure
(defprotocol SessionManager
  ;; ... existing methods ...
  (register-listener [this session-id guest-id output-stream])
  (unregister-listener [this session-id guest-id])
  (broadcast-event [this session-id event-type data]))
```

**2. Add SSE endpoint to server:**

```clojure
(defn event-stream-handler [session-id guest-id]
  {:status 200
   :headers {"Content-Type" "text/event-stream"
             "Cache-Control" "no-cache"
             "Connection" "keep-alive"}
   :body (fn [output-stream]
           ;; Register this guest as listening
           (session-manager/register-listener session-manager
                                              session-id
                                              guest-id
                                              output-stream)
           ;; Keep connection alive
           (loop []
             (Thread/sleep 1000)
             (when (not (.isClosed output-stream))
               (recur))))})

;; Add to routes
(def routes
  [["/stream/:session-id/:guest-id"
    {:get event-stream-handler}]])
```

**3. Modify move handling to broadcast:**

```clojure
(defmethod handle-endpoint :make-move [_ {:keys [path-params body]}]
  (let [body (read-json-body body)
        session-id (get path-params :session-id)
        guest-id (get path-params :guest-id)
        move (get body "move")]
    ;; Process move
    (session-manager/update-guest session-manager session-id guest-id move)
    ;; Broadcast to other guests
    (session-manager/broadcast-event session-manager
                                      session-id
                                      "guest-move"
                                      {:guestId guest-id :move move})))
```

### Benefits of SSE Approach

| Benefit | Impact |
|---------|--------|
| **Reduces polling spam** | 720k requests/hour → single stream connection |
| **Improves latency** | 200-500ms → <100ms |
| **Lower bandwidth** | 1+ GB → 5-10 MB per active session |
| **Lower CPU** | No repeated request overhead |
| **Maintains simplicity** | Still HTTP-based, no WebSocket complexity |
| **Backward compatible** | Polling clients can still work |

---

## Decision Matrix

**Use facetious-nocturn (HTTP polling) if:**
- Turn-based game with 10-30 second check-in intervals
- Ultra-low complexity preference
- Budget extremely constrained for compute
- Asynchronous play (check once per day)

**Upgrade to SSE if:**
- Turn-based but want responsive UX (sub-second feedback)
- Eliminate polling overhead but keep HTTP model
- Players in active sessions want faster updates
- Cost vs. responsiveness trade-off acceptable

**Switch to WebSocket/Colyseus if:**
- Real-time action gameplay (platformer, shooter)
- Continuous movement/interaction needed
- <50ms latency critical
- Can justify persistent connection overhead

---

## References

- [Server-Sent Events MDN](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)
- [Colyseus Documentation](https://docs.colyseus.io/)
- [WebSocket vs HTTP Polling Performance](https://www.howtogeek.com/devops/websockets-vs-polling-whats-the-difference/)

---

## Discussion Notes

**Key insights from analysis:**
1. HTTP polling becomes inefficient very quickly once guests check every 500ms or faster
2. SSE is a practical middle-ground: keeps HTTP foundation but adds push capability
3. WebSocket is overkill for turn-based unless you want real-time state sync
4. Most cost savings from facetious-nocturn come from session architecture, not necessarily transport layer
