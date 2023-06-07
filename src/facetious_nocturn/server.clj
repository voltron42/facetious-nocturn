(ns facetious-nocturn.server
  (:require [clojure.tools.logging :as log]
            [clojure.pprint :as pp]
            [compojure.api.sweet :as sweet]
            [compojure.api.core :as api]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [ring.util.http-response :as http]
            [environ.core :refer [env]]
            [org.httpkit.server :as server]
            [cheshire.core :as cheshire]
            [cheshire.custom :as cust]
            [schema.core :as schema]
            [facetious-nocturn.schema :as s]
            [facetious-nocturn.session-manager :as sm]))

(cust/add-encoder java.lang.Class
                  (fn [c jsonGenerator]
                    (.writeString jsonGenerator (-> c .getSimpleName))))

(defonce session-manager (sm/build-session-manager))

(defn default-handler [req]
  (log/info (pp/pprint (:keys req)))
  (http/ok (:keys req)))

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
                            :responses  {200 s/Session}
                            :handler    default-handler}}))
          (api/context
            "/session/:session-id/kick/:guest-key" []
            :tags ["host"]
            (sweet/resource
             {:description ""
              :delete      {:summary    ""
                            :parameters {:route-params {:session-id s/SessionId
                                                        :guest-key s/GuestKey}}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 s/Session}
                            :handler    default-handler}}))
          (api/context
            "/session/:session-id/close" []
            :tags ["host"]
            (sweet/resource
             {:description ""
              :delete      {:summary    ""
                            :parameters {:route-params {:session-id s/SessionId}}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 s/Session}
                            :handler    default-handler}}))
          (api/context
            "/session/:session-id/host-data" []
            :tags ["host"]
            (sweet/resource
             {:description ""
              :post        {:summary    ""
                            :parameters {:route-params {:session-id s/SessionId}}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 s/Session}
                            :handler    default-handler}
              :get         {:summary    ""
                            :parameters {:route-params {:session-id s/SessionId}}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 s/Session}
                            :handler    default-handler}}))
          (api/context
            "/join/:session-key" []
            :tags ["host"]
            (sweet/resource
             {:description ""
              :post         {:summary    ""
                            :parameters {:body s/Guest
                                         :route-params {:session-key s/SessionKey}}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 s/UserData}
                            :handler    default-handler}}))

          (api/context
            "/session/:session-id/leave" []
            :tags ["guest"]
            (sweet/resource
             {:description ""
              :delete      {:summary    ""
                            :parameters {:route-params {:session-id s/SessionId}}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 s/UserData}
                            :handler    default-handler}}))
          (api/context
            "/session/:session-id/guest-data" []
            :tags ["guest"]
            (sweet/resource
             {:description ""
              :post        {:summary    ""
                            :parameters {:route-params {:session-id s/SessionId}}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 s/UserData}
                            :handler    default-handler}
              :get         {:summary    ""
                            :parameters {:route-params {:session-id s/SessionId}}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 s/UserData}
                            :handler    default-handler}})))
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
