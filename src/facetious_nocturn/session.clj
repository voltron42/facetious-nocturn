(ns facetious-nocturn.session
  (:require
   [clojure.core.async :as async])
  (:import [java.util Date]))

(defprotocol Session
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
        actions (async/chan 200)]
    (async/thread
      (while (:is-open @state)
        (let [action (async/<!! actions)]
          (reset! state (apply update-session action)))))
    (reify Session
      (join [_ init-data]
        (let [guest-id (str (java.util.UUID/randomUUID))]
          (async/>!! actions [:join @state guest-id init-data])
          guest-id))
      (leave [_ guest-id]
        (async/>!! actions [:leave @state guest-id]))
      (kick [_ host-id guest-id]
        (when-not (= host-id session-host-id)
          (throw (ex-info "Only the host can kick guests" {:type :access-denied})))
        (async/>!! actions [:leave @state guest-id]))
      (get-for-host [_ host-id]
        (when-not (= host-id session-host-id)
          (throw (ex-info "Only the host can access host data" {:type :access-denied})))
        (select-keys @state [:host-data :common-data :guests]))
      (get-for-guest [_ guest-id]
        (when-not (contains? (:guests @state) guest-id)
          (throw (ex-info "Guest not found" {:type :not-found})))
        (let [guest-data (get-in @state [:guests guest-id])]
          (when-not guest-data
            (throw (ex-info "Guest not found" {:type :not-found})))
          {:guest-data guest-data
           :common-data (:common-data @state)}))
      (get-common-data [_]
        {:common-data (:common-data @state)})
      (update-host [_ host-id {:keys [host-data common-data]}]
        (when-not (= host-id session-host-id)
          (throw (ex-info "Only the host can update host data" {:type :access-denied})))
        (async/>!! actions [:update-host @state host-data common-data]))
      (update-guest [_ guest-id {:keys [guest-data]}]
        (async/>!! actions [:update-guest @state guest-id guest-data]))
      (close [_ host-id]
        (when-not (= host-id session-host-id)
          (throw (ex-info "Only the host can close the session" {:type :access-denied})))
        (async/>!! actions [:close @state])))))