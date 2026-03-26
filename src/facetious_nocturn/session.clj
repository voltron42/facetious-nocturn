(ns facetious-nocturn.session
  (:require
   [clojure.core.async :as async]
   [clojure.pprint :as pp])
  (:import
   [java.util Date]))

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
  (when-not (contains? (:guests state) guest-id)
    (throw (ex-info "Guest not found" {:type :not-found})))
  (update-in state [:guests] dissoc guest-id))

(defmethod update-session :update-guest [_ state guest-id guest-data]
  (when-not (contains? (:guests state) guest-id)
    (throw (ex-info "Guest not found" {:type :not-found})))
  (-> state
      (assoc-in [:guests guest-id :from-guest] guest-data)
      (assoc :last-updated (Date.))))

(defmethod update-session :update-host [_ state host-data common-data to-guests]
  (pp/pprint {:host-data host-data :common-data common-data :to-guests to-guests :guests (:guests state)})
  (-> state
      (assoc :host-data host-data)
      (assoc :common-data common-data)
      (assoc :guests (reduce-kv #(update %1 %2 assoc :to-guest %3) (:guests state) to-guests))
      (assoc :last-updated (Date.))))

(defmethod update-session :close [_ state]
  (assoc state :is-open false))

(defmethod update-session :default [& args]
  (pp/pprint {:update-session-default args}))

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
                    (pp/pprint {:dequeue action})
                    (swap! queue-count dec)
                    action))
        enqueue (fn [& action]
                  (pp/pprint {:enqueue action})
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
          (merge (select-keys @state [:common-data :last-updated :created-on]) (select-keys guest-data [:to-guest :from-guest]))))
      (get-common-data [_]
        (select-keys  @state [:common-data :last-updated :created-on]))
      (update-host [_ host-id {:keys [host-data common-data to-guests]}]
        (when-not (= host-id session-host-id)
          (throw (ex-info "Only the host can update host data" {:type :access-denied})))
        (enqueue :update-host @state host-data common-data to-guests)
        {:date-queued (Date.)})
      (update-guest [_ guest-id {:keys [from-guest]}]
        (enqueue :update-guest @state guest-id from-guest)
        {:date-queued (Date.)})
      (close [_ host-id]
        (when-not (= host-id session-host-id)
          (throw (ex-info "Only the host can close the session" {:type :access-denied})))
        (enqueue :close @state)
        @state))))