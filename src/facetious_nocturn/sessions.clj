(ns facetious-nocturn.sessions
  (:require [clojure.data.codec.base64 :as b64]
            [clojure.core.async :as async]))

(defprotocol ^:private Session
  (join [this participant-index])
  (mark-absent [this participant-index])
  (withdraw [this participant-index])
  (status [this])
  (list-participants [this])
  (list-participant-indicies [this])
  (participant [this participant-index])
  (post-message [this message])
  (list-messages [this])
  (list-messages-for-participant [this participant-index]))

(defprotocol ^:private SessionCache
  (put-session [this session-id session])
  (view-session [this session-id])
  (close-session [this session-id]))

(defn- init-session-cache []
  (let [cache-map (atom {})
        cache-chan (async/chan 200)]
    (async/go-loop []
                   (when-let [[session-id session] (async/<! cache-chan)]
                     (if session
                       (swap! cache-map assoc session-id session)
                       (swap! cache-map dissoc session-id))
                     (recur)))
    (reify SessionCache
      (put-session [_ session-id session]
        (async/>! cache-chan [session-id session]))
      (view-session [_ session-id]
        (get @cache-map session-id))
      (close-session [_ session-id]
        (async/>! cache-chan [session-id])))))

(defonce ^:private session-cache ^SessionCache (init-session-cache))

(defn- build-session-id [host-ip]
  (b64/encode (.getBytes (str (System/currentTimeMillis) "_" host-ip))))

(defn- wrap-as-session [session-atom]
  (let [set-status (fn [participant-index participant-status]
                     (swap! session-atom update-in [:participants participant-index] assoc :status participant-status)
                     )]
    (reify Session
      (join [_ participant-index]
        ()
        )
      (mark-absent [_ participant-index]
        )
      (withdraw [_ participant-index]
        )
      (status [_]
        (:session-status @session-atom))
      (list-participants [_]
        (:participants @session-atom))
      (list-participant-indicies [_]
        )
      (participant [_ participant-index]
        (nth (:particpants @session-atom) participant-index))
      (post-message [_ message]
        (if (empty? (:messages @session-atom))
          (swap! session-atom assoc :messages [message])
          (swap! session-atom update :messages conj message)))
      (list-messages-for-participant [_ participant-index]
        ))))

(defn- init-session [session]
  ())

(defn -open-session [host-ip session]
  (let [session-id (build-session-id host-ip)]
    (put-session session-cache session-id (atom session))
    { session-id }))

(defn -get-session [session-id]
  (wrap-as-session (view-session session-cache session-id)))

(defn -get-session-data [session-id]
  @(view-session session-cache session-id))

(defn -close-session [session-id]
  (let [session (view-session session-cache session-id)]
    (close-session session-cache session-id)
    @session))

(defn -join-session [session-id user-name])

(defn -post-message [session-id message])