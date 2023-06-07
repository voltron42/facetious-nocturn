(ns facetious-nocturn.schema
  (:require [schema.core :as s]))

(s/defschema Context
  {:last-updated s/Num
   :state s/Any})

(s/defschema Host 
  {:ip [s/Num]
   :name-tag s/Str
   :last-updated s/Num
   :state s/Any})

(s/defschema Guest 
  {:ip [s/Num]
   :name-tag s/Str
   :last-updated s/Num
   :joined s/Num
   (s/optional-key :exited) s/Num
   :state s/Any})

(s/defschema SessionId s/Str)

(s/defschema GuestKey s/Str)

(s/defschema UserData 
  {:session-id SessionId
   :last-updated s/Num
   :guest Guest
   :context Context})

(s/defschema Session 
  {:id SessionId
   :key s/Str
   :last-updated s/Num
   :host Host
   :context Context
   :guests {GuestKey Guest}})