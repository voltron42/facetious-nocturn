(ns facetious-nocturn.schema
  (:require [clojure.spec.alpha :as spec]))

(defn is-vector-of [schema] (spec/and vector? (spec/coll-of schema)))

(spec/def :data any?)

(spec/def :forum-data any?)

(spec/def :body any?)

(spec/def :status #{"INVITED" "JOINED" "ABSENT" "WITHDRAWN"})

(spec/def :session-status #{"OPENING" "INITIALIZED" "INVITING" "ALL-JOINED" "OPEN" "CLOSING" "CLOSED"})

(spec/def :timestamp string?)
;; apply preferred date/time pattern when needed

(spec/def :name string?)

(spec/def :participant (spec/keys {:req-un [:name :status :data]}))

(spec/def :sender int?)

(spec/def :recipients
  (spec/and
    (is-vector-of int?)
    ))

(spec/def :message
  (spec/keys {:req-un [:sender :recipients :body :timestamp]}))

(spec/def :participants (is-vector-of :participant))

(spec/def :messages (is-vector-of :message))

(spec/def :host
  (spec/or :participating int?
           :isolated :participant))

(spec/def :session-status :status)

(spec/def :session
  (spec/keys
    {:req-un [:host :forum-data :participants :messages]}))
