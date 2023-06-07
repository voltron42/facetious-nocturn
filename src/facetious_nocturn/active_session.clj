(ns facetious-nocturn.active-session
  (:require [org.clojure/core.async :as async]))

(defprotocol ActiveSession
  (get-session [this])
  (post-session [this session])
  (get-guest [this guest-key])
  (post-guest [this guest-key guest])
  (close [this]))

(defn build-worker [active-state session-atom action-chan]
  (async/thread
    (while @active-state
      (let []))))

(defn build-active-session [init-session]
  (let [active-state (atom true)
        session-state (atom init-session)
        action-channel (async/chan 200)]
    (build-worker active-state session-state action-channel)
    (reify ActiveSession
      (get-session [this]
        @session-state)
      (post-session [this session]
        ; todo
        )
      (get-guest [this guest-key]
        (let [{id :id context :context guests :guests} @session-state
              guest (get guests guest-key)]
          {:session-id id
           :last-modified (max (:last-modified context) (:last-modified guest))
           :guest guest
           :context context}))
      (post-guest [this guest-key guest]
        ; todo
        )
      (close [this]
        (reset! active-state false)))))