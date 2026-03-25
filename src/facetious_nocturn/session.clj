(ns facetious-nocturn.session
  (:require
   [clojure.core.async :as async])
  (:import [java.util Date]))

(defprotocol Session
  (pulse [this])
  (join [this init-data])
  (leave [this guest-id])
  (kick [this host-id guest-id])
  (get-for-host [this host-id])
  (get-for-guest [this guest-id])
  (get-common-data [this])
  (update-host [this host-id updates])
  (update-guest [this guest-id updates])
  (close [this host-id]))

(defmulti update-session (fn [& [update-type]] update-type))

(defmethod update-session :join [_ state guest-id init-data]
  (assoc-in state [:guests guest-id] init-data))

(defmethod update-session :leave [_ state guest-id]
  (when-not (contains? (:guests @state) guest-id)
    (throw (ex-info "Guest not found" {:type :not-found})))
  (update-in state [:guests] dissoc guest-id))

(defmethod update-session :update-guest [_ state guest-id guest-data]
  (when-not (contains? (:guests @state) guest-id)
    (throw (ex-info "Guest not found" {:type :not-found})))
  (-> state
      (assoc-in [:guests guest-id] guest-data)
      (assoc :last-updated (Date.))))

(defmethod update-session :update-host [_ state host-data common-data]
  (-> state
      (assoc :host-data host-data)
      (assoc :common-data common-data)
      (assoc :last-updated (Date.))))

(defmethod update-session :close [_ state]
  (assoc state :is-open false))

(defn create-session [session-host-id {:keys [initial-host-data initial-common-data]}]
  (let [state (atom {:host-data initial-host-data
                     :common-data initial-common-data
                     :guests {}
                     :created-at (Date.)
                     :is-open true})
        actions (async/chan 200)
        queue-count (atom 0)
        dequeue (fn []
                  (let [action (async/<!! actions)]
                    (swap! queue-count dec)
                    action))
        enqueue (fn [& action]
                  (swap! queue-count inc)
                  (async/>!! actions action)
                  nil)]
    (async/thread
      (while (:is-open @state)
        (reset! state (apply update-session (dequeue)))))
    (reify Session
      (pulse [_]
        {:last-updated (:last-updated @state)
         :queue-count @queue-count})
      (join [_ init-data]
        (let [guest-id (str (java.util.UUID/randomUUID))]
          (enqueue :join @state guest-id init-data)
          guest-id))
      (leave [_ guest-id]
        (enqueue :leave @state guest-id)
        {:date-queued (Date.)})
      (kick [_ host-id guest-id]
        (when-not (= host-id session-host-id)
          (throw (ex-info "Only the host can kick guests" {:type :access-denied})))
        (enqueue :leave @state guest-id)
        {:date-queued (Date.)})
      (get-for-host [_ host-id]
        (when-not (= host-id session-host-id)
          (throw (ex-info "Only the host can access host data" {:type :access-denied})))
        (select-keys @state [:host-data :common-data :guests :last-updated :created-on]))
      (get-for-guest [_ guest-id]
        (when-not (contains? (:guests @state) guest-id)
          (throw (ex-info "Guest not found" {:type :not-found})))
        (let [guest-data (get-in @state [:guests guest-id])]
          (when-not guest-data
            (throw (ex-info "Guest not found" {:type :not-found})))
          (assoc (select-keys @state [:common-data :last-updated :created-on]) :guest-data guest-data)))
      (get-common-data [_]
        (select-keys  @state [:common-data :last-updated :created-on]))
      (update-host [_ host-id {:keys [host-data common-data]}]
        (when-not (= host-id session-host-id)
          (throw (ex-info "Only the host can update host data" {:type :access-denied})))
        (enqueue :update-host @state host-data common-data)
        {:date-queued (Date.)})
      (update-guest [_ guest-id {:keys [guest-data]}]
        (enqueue :update-guest @state guest-id guest-data)
        {:date-queued (Date.)})
      (close [_ host-id]
        (when-not (= host-id session-host-id)
          (throw (ex-info "Only the host can close the session" {:type :access-denied})))
        (enqueue :close @state)
        @state))))