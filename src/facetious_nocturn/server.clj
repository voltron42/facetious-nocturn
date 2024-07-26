(ns facetious-nocturn.server
  (:require [clojure.tools.logging :as log]
            [compojure.api.core :as api]
            [compojure.api.sweet :as sweet]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [facetious-nocturn.schema :as s]
            [facetious-nocturn.session-manager :as sm]
            [org.httpkit.server :as server]
            [ring.util.http-response :as http]
            [ring.util.response :as resp]
            [schema.core :as schema])
  (:import [java.util NoSuchElementException]))

(defn wrap-response [action]
  (try
    (http/ok (action))
    (catch IllegalArgumentException e
      (http/bad-request (.getMessage e)))
    (catch NoSuchElementException e
      (http/not-found (.getMessage e)))))

(defn build-app []
  (let [session-manager (sm/build-session-manager)]
    (-> {:swagger
         {:ui   "/swagger/ui"
          :spec "/swagger.json"
          :data {:info {:title       "Facetious Nocturn"
                        :description "Shared Session Server"}
                 :tags [{:name "Host", :description "functions for the Session Host"}
                        {:name "Guest", :description "functions for Session Guests"}]}}}
        (sweet/api
          (api/context
            "/api/v1" []
            (api/context
              "/host-new" []
              :tags ["host"]
              (sweet/resource
                {:description ""
                 :post        {:summary    ""
                               :parameters {:body s/SessionConfig}
                               :consumes   ["application/json"]
                               :produces   ["application/json"]
                               :responses  {200 {:schema s/Session}}
                               :handler    (fn [{host-ip :remote-addr session-config :body}]
                                             (wrap-response #(sm/host-new session-manager host-ip session-config)))}}))
            (api/context
              "/reopen-session" []
              :tags ["host"]
              (sweet/resource
                {:description ""
                 :post        {:summary    ""
                               :parameters {:body s/Session}
                               :consumes   ["application/json"]
                               :produces   ["application/json"]
                               :responses  {200 {:schema s/Session}}
                               :handler    (fn [{host-ip :remote-addr session :body}]
                                             (wrap-response #(sm/reopen-session session-manager host-ip session)))}}))
            (api/context
              "/session/:session-id/:host-id/kick/:guest-key" []
              :tags ["host"]
              (sweet/resource
                {:description ""
                 :delete      {:summary    ""
                               :parameters {:path-params {:session-id schema/Str
                                                          :host-id schema/Str
                                                          :guest-key schema/Str}}
                               :consumes   ["application/json"]
                               :produces   ["application/json"]
                               :responses  {200 {:schema s/Session}}
                               :handler    (fn [{{:keys [session-id host-id guest-key]} :path-params host-ip :remote-addr}]
                                             (wrap-response #(sm/kick session-manager session-id host-id guest-key host-ip)))}}))
            (api/context
              "/session/:session-id/:host-id/close" []
              :tags ["host"]
              (sweet/resource
                {:description ""
                 :delete      {:summary    ""
                               :parameters {:path-params {:session-id schema/Str}}
                               :produces   ["application/json"]
                               :responses  {200 {:schema s/Session}}
                               :handler    (fn [{{:keys [session-id host-id]} :path-params host-ip :remote-addr}]
                                             (wrap-response #(sm/close-session session-manager host-ip session-id host-id)))}}))
            (api/context
              "/session/:session-id/host/:host-id/" []
              :tags ["host"]
              (sweet/resource
                {:description ""
                 :put        {:summary    ""
                              :parameters {:path-params {:session-id schema/Str}
                                           :body s/SessionUpdate}
                              :consumes   ["application/json"]
                              :produces   ["application/json"]
                              :responses  {200 {:schema s/HostState}}
                              :handler    (fn [{{:keys [session-id host-id]} :path-params session-update :body host-ip :remote-addr}]
                                            (wrap-response #(sm/update-session session-manager session-id host-id host-ip session-update)))}
                 :get         {:summary    ""
                               :parameters {:path-params {:session-id schema/Str}}
                               :produces   ["application/json"]
                               :responses  {200 {:schema s/HostState}}
                               :handler    (fn [{{:keys [session-id host-id]} :path-params host-ip :remote-addr}]
                                             (wrap-response #(sm/get-session session-manager session-id host-id host-ip)))}}))
            (api/context
              "/join-invited/:session-key/:guest-key" []
              :tags ["guest"]
              (sweet/resource
                {:description ""
                 :post         {:summary    ""
                                :parameters {:body s/InvitedGuest
                                             :path-params {:session-key schema/Str}}
                                :consumes   ["application/json"]
                                :produces   ["application/json"]
                                :responses  {200 {:schema s/GuestData}}
                                :handler    (fn [{{:keys [session-key guest-key]} :path-params guest :body guest-ip :remote-addr}]
                                              (wrap-response #(sm/join-invited session-manager session-key guest-key guest-ip guest)))}}))
            (api/context
              "/join-open/:session-key" []
              :tags ["guest"]
              (sweet/resource
                {:description ""
                 :post         {:summary    ""
                                :parameters {:body s/NewGuest
                                             :path-params {:session-key schema/Str}}
                                :consumes   ["application/json"]
                                :produces   ["application/json"]
                                :responses  {200 {:schema s/GuestData}}
                                :handler    (fn [{{:keys [session-key]} :path-params guest :body guest-ip :remote-addr}]
                                              (wrap-response #(sm/join-open session-manager session-key guest-ip guest)))}}))
            (api/context
              "/rejoin/:session-key/:guest-key" []
              :tags ["guest"]
              (sweet/resource
                {:description ""
                 :post         {:summary    ""
                                :parameters {:path-params {:session-key schema/Str}}
                                :consumes   ["application/json"]
                                :produces   ["application/json"]
                                :responses  {200 {:schema s/GuestData}}
                                :handler    (fn [{{:keys [session-key guest-key]} :path-params guest-ip :remote-addr}]
                                              (wrap-response #(sm/rejoin session-manager session-key guest-key guest-ip)))}}))
            (api/context
              "/session/:session-id/leave/:guest-id" []
              :tags ["guest"]
              (sweet/resource
                {:description ""
                 :delete      {:summary    ""
                               :parameters {:path-params {:session-id schema/Str}}
                               :consumes   ["application/json"]
                               :produces   ["application/json"]
                               :responses  {200 {:schema s/GuestData}}
                               :handler    (fn [{{:keys [session-id]} :path-params guest-ip :remote-addr}]
                                             (wrap-response #(sm/leave session-manager session-id guest-ip)))}}))
            (api/context
              "/session/:session-id/guest/:guest-id" []
              :tags ["guest"]
              (sweet/resource
                {:description ""
                 :put        {:summary    ""
                               :parameters {:path-params {:session-id schema/Str}
                                            :body s/GuestUpdate}
                               :consumes   ["application/json"]
                               :produces   ["application/json"]
                               :responses  {200 {:schema s/GuestData}}
                               :handler    (fn [{{:keys [session-id guest-id]} :path-params guest-update :body guest-ip :remote-addr}]
                                             (wrap-response #(sm/update-guest session-manager session-id guest-id guest-ip guest-update)))}
                 :get         {:summary    ""
                               :parameters {:path-params {:session-id schema/Str}}
                               :consumes   ["application/json"]
                               :produces   ["application/json"]
                               :responses  {200 {:schema s/GuestData}}
                               :handler    (fn [{{:keys [session-id guest-id]} :path-params guest-ip :remote-addr}]
                                             (wrap-response #(sm/get-guest session-manager session-id guest-id guest-ip)))}})))
          (sweet/GET "/" [] (resp/redirect "/index.html")))
        (sweet/routes
          (route/resources "/")
          (route/not-found "404 Not Found")))))

(defn -main [& [port]]
  (let [my-app (build-app)
        port (Integer. ^int (or port (env :port) 5001))]
    (log/info (str "port: " port))
    (server/run-server my-app {:port port
                               :join? false
                               :max-line 131072})))
