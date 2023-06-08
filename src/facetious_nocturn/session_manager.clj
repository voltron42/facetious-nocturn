(ns facetious-nocturn.session-manager
  (:require [clj-time.core :as t]
            [facetious-nocturn.active-session :as act]) 
  (:import [java.util NoSuchElementException]))

(defn- build-session-id [ip-address]
  ; todo
  (str ip-address))

(defn- build-session-key [ip-address]
  ; todo
  (str ip-address))

(defn- build-guest-key [ip-address]
  ; todo
  (str ip-address))

(defn- get-last-updated []
  (t/in-millis (t/interval (t/date-time 1970 1 1) (t/now))))

(defn- get-active-session [session-cache session-id]
  (let [session (get session-cache session-id)]
    (if (nil? session)
      (throw (NoSuchElementException. session-id))
      session)))

(defn- get-session-id [session-key-map session-key]
  (let [session-id (get session-key-map session-key)]
    (if (nil? session-id)
      (throw (NoSuchElementException. session-key))
      session-id)))

(defn- validate-host-ip [session host-ip]
  (let [host-key (build-guest-key host-ip)]
    (when (not= host-key (get-in session [:host :key]))
      (throw (IllegalArgumentException. host-ip)))))

(defn- get-valid-guest [session guest-ip]
  (let [guest (get-in session [:guests guest-ip])]
    (if (nil? guest)
      (throw (NoSuchElementException. guest-ip))
      guest)))

(defprotocol ISessionManager
  (host [this host-ip session])
  (join [this session-key guest-ip guest])
  (kick [this session-id host-ip guest-key])
  (leave [this session-id guest-ip])
  (close [this session-id host-ip])
  (get-session [this session-id host-ip])
  (post-session [this session-id host-ip session])
  (get-guest [this session-id guest-ip])
  (post-user-data [this session-id guest-ip guest]))

(defn build-session-manager []
  (let [session-cache (atom {})
        session-key-map (atom {})]
    (reify ISessionManager
      (host [_ host-ip session]
        (let [session-id (build-session-id host-ip)
              session-key (build-session-key host-ip)
              last-updated (get-last-updated)
              new-session {:id session-id
                           :key session-key
                           :host {:name-tag (-> session :host :name-tag)
                                  :key (build-guest-key host-ip)
                                  :state (-> session :host :state)
                                  :last-updated last-updated}
                           :context {:state (-> session :context :state)
                                     :last-updated last-updated}}]
          (swap! session-cache assoc session-id (act/build-active-session new-session))
          (swap! session-key-map assoc session-key session-id)
          new-session))
      (join [_ session-key guest-ip guest]
        (let [session-id (get-session-id @session-key-map session-key)
              session (get-active-session @session-cache session-id)
              last-updated (get-last-updated)
              guest-key (build-guest-key guest-ip)
              new-guest {:key guest-key
                         :name-tag (:name-tag guest)
                         :last-updated last-updated
                         :joined last-updated
                         :state (:state guest)}]
              
          (act/submit-request session #(update % :guests assoc guest-key new-guest))
          {:session-id session-id
           :guest new-guest
           :context (-> session (act/get-session) :context)}))
      (kick [_ session-id host-ip guest-key]
            (let [session (get-active-session @session-cache session-id) 
                  _ (validate-host-ip session host-ip)]
              (act/submit-request session #(update % :guests dissoc guest-key))
              (act/get-session session)))
      (leave [_ session-id guest-ip]
             (let [session (get-active-session @session-cache session-id)
                   guest-key (build-guest-key guest-ip)
                   guest (get-valid-guest session guest-key)
                   {:keys [id context]} (act/get-session session)]
               (act/submit-request session #(update % :guests dissoc guest-key))
               {:session-id id
                :guest guest
                :context context}))
      (close [_ session-id host-ip]
             (let [active-session (get-active-session @session-cache session-id) 
                   _ (validate-host-ip active-session host-ip)
                   session (act/get-session active-session)]
               (act/close active-session)
               (swap! session-cache dissoc session-id)
               (swap! session-key-map (:key session))
               session))
      (get-session [_ session-id host-ip]
                   (let [active-session (get-active-session @session-cache session-id)
                         _ (validate-host-ip active-session host-ip)]
                     (act/get-session active-session)))
      (get-guest [_ session-id guest-ip]
                 (let [session (get-active-session @session-cache session-id)
                       guest-key (build-guest-key guest-ip)
                       guest (get-valid-guest session guest-key)
                       last-updated (max (:last-updated guest) (get-in (act/get-session session) [:context :last-updated]))
                       {:keys [id context]} (act/get-session session)]
                   {:session-id id
                    :last-updated last-updated
                    :guest guest
                    :context context}))
      (post-session [_ session-id host-ip new-state]
                    (let [active-session (get-active-session @session-cache session-id)
                         _ (validate-host-ip active-session host-ip)]
                      (act/submit-request active-session
                                          (fn [old-state]
                                            (let [last-updated (get-last-updated)]
                                              (-> old-state
                                                  ; todo - update host
                                                  ; todo - update each guest
                                                  (update :context assoc 
                                                          :last-updated last-updated
                                                          :state (get-in new-state [:context :state]))
                                                  ))))
                      (act/get-session active-session)))
      (post-user-data [_ session-id guest-ip {new-guest :guest new-context :context}]
                  (let [session (get-active-session @session-cache session-id)
                        guest-key (build-guest-key guest-ip)
                        guest (get-valid-guest session guest-key)
                        last-updated (max (:last-updated guest) (get-in (act/get-session session) [:context :last-updated]))]
                    (act/submit-request session
                                        (fn [old-state]
                                          (let [last-updated (get-last-updated)]
                                            (-> old-state
                                                (update-in [:guests guest-key] assoc
                                                           :last-updated last-updated
                                                           :name-tag (:name-tag new-guest)
                                                           :state (:state new-guest))
                                                (update :context assoc 
                                                          :last-updated last-updated
                                                          :state (:state new-context))
                                                ))))
                    {:session-id session-id
                     :last-updated last-updated
                     :guest guest
                     :context (:context (act/get-session session))})))))
        
