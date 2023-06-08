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

(defonce session-manager (sm/build-session-manager))

(defn wrap-response [action]
  (try
    (http/ok (action))
    (catch IllegalArgumentException e
      (http/bad-request (.getMessage e)))
    (catch NoSuchElementException e
      (http/not-found (.getMessage e)))))

(defn build-app []
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
            "/host" []
            :tags ["host"]
            (sweet/resource
             {:description ""
              :post        {:summary    ""
                            :parameters {:body s/Session}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 {:schema s/Session}}
                            :handler    (fn [{:keys [remote-addr body]}]
                                          (wrap-response #(sm/host session-manager remote-addr body)))}}))
          (api/context
            "/session/:session-id/kick/:guest-key" []
            :tags ["host"]
            (sweet/resource
             {:description ""
              :delete      {:summary    ""
                            :parameters {:path-params {:session-id schema/Str
                                                        :guest-key schema/Str}}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 {:schema s/Session}}
                            :handler    (fn [{{:keys [session-id guest-key]} :path-params host-ip :remote-addr}]
                                          (wrap-response #(sm/kick session-manager session-id host-ip guest-key)))}}))
          (api/context
            "/session/:session-id/close" []
            :tags ["host"]
            (sweet/resource
             {:description ""
              :delete      {:summary    ""
                            :parameters {:path-params {:session-id schema/Str}}
                            :produces   ["application/json"]
                            :responses  {200 {:schema s/Session}}
                            :handler    (fn [{{:keys [session-id]} :path-params host-ip :remote-addr}]
                                          (wrap-response #(sm/close session-manager host-ip session-id)))}}))
          (api/context
            "/session/:session-id/host-data" []
            :tags ["host"]
            (sweet/resource
             {:description ""
              :post        {:summary    ""
                            :parameters {:path-params {:session-id schema/Str}
                                         :body s/Session}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 {:schema s/Session}}
                            :handler    (fn [{{:keys [session-id]} :path-params session :body host-ip :remote-addr}]
                                          (wrap-response #(sm/post-session session-manager session-id host-ip session)))}
              :get         {:summary    ""
                            :parameters {:path-params {:session-id schema/Str}}
                            :produces   ["application/json"]
                            :responses  {200 {:schema s/Session}}
                            :handler    (fn [{{:keys [session-id]} :path-params host-ip :remote-addr}]
                                          (wrap-response #(sm/get-session session-manager session-id host-ip)))}}))
          (api/context
            "/join/:session-key" []
            :tags ["guest"]
            (sweet/resource
             {:description ""
              :post         {:summary    ""
                            :parameters {:body s/Guest
                                         :path-params {:session-key schema/Str}}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 {:schema s/UserData}}
                            :handler    (fn [{{:keys [session-key]} :path-params guest :body guest-ip :remote-addr}]
                                          (wrap-response #(sm/join session-manager session-key guest-ip guest)))}}))
          (api/context
            "/session/:session-id/leave" []
            :tags ["guest"]
            (sweet/resource
             {:description ""
              :delete      {:summary    ""
                            :parameters {:path-params {:session-id schema/Str}}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 {:schema s/UserData}}
                            :handler    (fn [{{:keys [session-id]} :path-params guest-ip :remote-addr}]
                                          (wrap-response #(sm/leave session-manager session-id guest-ip)))}}))
          (api/context
            "/session/:session-id/guest-data" []
            :tags ["guest"]
            (sweet/resource
             {:description ""
              :post        {:summary    ""
                            :parameters {:path-params {:session-id schema/Str}
                                         :body s/Guest}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 {:schema s/UserData}}
                            :handler    (fn [{{:keys [session-id]} :path-params user-data :body guest-ip :remote-addr}]
                                          (wrap-response #(sm/post-user-data session-manager session-id guest-ip user-data)))}
              :get         {:summary    ""
                            :parameters {:path-params {:session-id schema/Str}}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 {:schema s/UserData}}
                            :handler    (fn [{{:keys [session-id]} :path-params guest-ip :remote-addr}]
                                          (wrap-response #(sm/get-guest session-manager session-id guest-ip)))}})))
        (sweet/GET "/" [] (resp/redirect "/index.html")))
      (sweet/routes
        (route/resources "/")
        (route/not-found "404 Not Found"))))

(defn -main [& [port]]
  (let [my-app (build-app)
        port (Integer. ^int (or port (env :port) 5001))]
    (log/info (str "port: " port))
    (server/run-server my-app {:port port
                               :join? false
                               :max-line 131072})))
