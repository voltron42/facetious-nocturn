(ns facetious-nocturn.schema
  (:require [facetious-nocturn.json :as json]
            [schema.core :as s]))

(s/defschema Context
  {:last-updated s/Num
   (s/optional-key :state) s/Any})

(s/defschema Host 
  {:id s/Str
   :ip [s/Num]
   :key s/Str
   :name-tag s/Str
   :last-updated s/Num
   (s/optional-key :state) s/Any
   (s/optional-key :guest-id) s/Str})

(s/defschema HostState
  {:session-id s/Str
   :host-id s/Str
   :name-tag s/Str
   :last-updated s/Num
   (s/optional-key :state) s/Any
   (s/optional-key :guest-id) s/Str
   (s/optional-key :context) s/Any
   :guests {:key s/Str
            :name-tag s/Str
            (s/optional-key :state) s/Any}})

(s/defschema Guest 
  {:id s/Str
   :ip [s/Num]
   :key s/Str
   :name-tag s/Str
   :last-updated s/Num
   :joined s/Num
   (s/optional-key :state) s/Any})

(s/defschema GuestData 
  {:session-id s/Str
   :last-updated s/Num
   :guest-id s/Str
   :name-tag s/Str
   (s/optional-key :state) s/Any
   (s/optional-key :context) s/Any})

(s/defschema Permissions
  (s/enum :host-only :guest-only :host-and-guest))

(s/defschema Session 
  {:id s/Str
   :key s/Str
   :last-updated s/Num
   :host-permissions Permissions
   :context-edit-permissions Permissions
   (s/optional-key :host-schema) json/$Schema
   (s/optional-key :context-schema) json/$Schema
   (s/optional-key :guest-schema) json/$Schema
   :host Host
   :context Context
   :guests {s/Str Guest}})

(s/defschema SessionConfig 
  (s/Both
   {:host-permissions Permissions
    :context-edit-permissions Permissions
    (s/optional-key :host-schema) json/$Schema
    (s/optional-key :context-schema) json/$Schema
    (s/optional-key :guest-schema) json/$Schema}
   (s/cond-pre
    {:guests [{:name-tag s/Str}]}
    {:open-invite (s/enum true)})))

(s/defschema SessionUpdate {})

(s/defschema InvitedGuest {})

(s/defschema NewGuest {})

(s/defschema GuestUpdate {})