(ns facetious-nocturn.active-session 
  (:require [clojure.core.async :as async]))

(defprotocol ActiveSession
  (submit-request [this action])
  (get-session [this])
  (get-guest [this guest-key])
  (close [this]))

(defn build-worker [active-state session-atom action-chan is-closed?]
  (async/thread
    (while @active-state
      (let [action (async/<!! action-chan)]
        (swap! session-atom action)))
    (reset! is-closed? true)))

(defn build-active-session [init-session]
  (let [active-state (atom true)
        session-state (atom init-session)
        action-channel (async/chan 200)
        is-closed? (atom false)]
    (build-worker active-state session-state action-channel is-closed?)
    (reify ActiveSession
      (submit-request [_ action]
        (async/>!! action-channel action))
      (get-session [_]
        @session-state)
      (get-guest [_ guest-key]
        (let [{id :id context :context guests :guests} @session-state
              guest (get guests guest-key)]
          {:session-id id
           :last-modified (max (:last-modified context) (:last-modified guest))
           :guest guest
           :context context}))
      (close [_]
        (reset! active-state false)
        (async/close! action-channel)
        (while (not @is-closed?))))))
