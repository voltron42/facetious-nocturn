(ns facetious-nocturn.schema
  (:require [schema.core :as s]))

(s/defschema Context
  {:last-updated s/Num
   :state s/Any})

(s/defschema Host 
  {:ip [s/Num]
   :key s/Str
   :name-tag s/Str
   :last-updated s/Num
   :state s/Any})

(s/defschema Guest 
  {:ip [s/Num]
   :key s/Str
   :name-tag s/Str
   :last-updated s/Num
   :joined s/Num
   :state s/Any})

(s/defschema UserData 
  {:session-id s/Str
   :last-updated s/Num
   :guest Guest
   :context Context})

(s/defschema Session 
  {:id s/Str
   :key s/Str
   :last-updated s/Num
   :host Host
   :context Context
   :guests {s/Str Guest}})