(ns facetious-nocturn.session-manager
  (:require [facetious-nocturn.session :as session]))

(defprotocol SessionManager
  (heartbeat [this])
  (host [this initial-host-data initial-common-data])
  (join [this session-id init-data])
  (leave [this session-id guest-id])
  (kick [this session-id host-id guest-id])
  (get-for-host [this session-id host-id])
  (get-for-guest [this session-id guest-id])
  (get-common-data [this session-id])
  (update-host [this session-id host-id updates])
  (update-guest [this session-id guest-id updates])
  (close [this session-id host-id]))

(defn create-session-manager []
  (let [sessions (atom {})]
    (reify SessionManager
      (heartbeat [_]
        (mapv (fn [[_ session]] (session/pulse session)) @sessions))
      (host [_ initial-host-data initial-common-data]
        (let [session-id (str (java.util.UUID/randomUUID))
              host-id (str (java.util.UUID/randomUUID))
              session (session/create-session host-id {:host-data initial-host-data
                                                         :common-data initial-common-data})]
          (swap! sessions assoc session-id session)
          {:session-id session-id :host-id host-id}))
      (join [_ session-id init-data]
        (if-let [session (@sessions session-id)]
          (session/join session init-data)
          (throw (ex-info "Session not found" {:type :not-found}))))
      (leave [_ session-id guest-id]
        (if-let [session (@sessions session-id)]
          (session/leave session guest-id)
          (throw (ex-info "Session not found" {:type :not-found}))))
      (kick [_ session-id host-id guest-id]
        (if-let [session (@sessions session-id)]
          (session/kick session host-id guest-id)
          (throw (ex-info "Session not found" {:type :not-found}))))
      (get-for-host [_ session-id host-id]
        (if-let [session (@sessions session-id)]
          (session/get-for-host session host-id)
          (throw (ex-info "Session not found" {:type :not-found}))))
      (get-for-guest [_ session-id guest-id]
        (if-let [session (@sessions session-id)]
          (session/get-for-guest session guest-id)
          (throw (ex-info "Session not found" {:type :not-found}))))
      (get-common-data [_ session-id]
        (if-let [session (@sessions session-id)]
          (session/get-common-data session)
          (throw (ex-info "Session not found" {:type :not-found}))))
      (update-host [_ session-id host-id updates]
        (if-let [session (@sessions session-id)]
          (session/update-host session host-id updates)
          (throw (ex-info "Session not found" {:type :not-found}))))
      (update-guest [_ session-id guest-id updates]
        (if-let [session (@sessions session-id)]
          (session/update-guest session guest-id updates)
          (throw (ex-info "Session not found" {:type :not-found}))))
      (close [_ session_id host_id]
             (if-let [session (@sessions session_id)]
               (do
                 (session/close session host_id)
                 (swap! sessions dissoc session_id))
               (throw (ex-info "Session not found" {:type :not-found})))))))