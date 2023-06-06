(ns facetious-nocturn.server
  (:require [clojure.logging :as log]
            [compojure.api.sweet :as sweet]
            [compojure.api.core :as api]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [ring.util.http-response :as http]
            [environ.core :refer [env]]
            [org.httpkit.server :as server]
            [schema.core :as s]))

(defn build-app []
  (-> {:swagger
       {:ui   "/swagger/ui"
        :spec "/swagger.json"
        :data {:info {:title       "Voltron 42 Webserver"
                      :description "trying something out"}
               :tags [{:name "Hello World", :description "Simple sample http routing"}]}}}
      (sweet/api
        (api/context
          "/api/v1" []
          :tags ["Hello World"]
          (api/context
            "/hi-there" []
            (sweet/resource
              {:description ""
               :get         {:summary    ""
                             :parameters {:query-params s/Any}
                             :produces   ["application/json"]
                             :responses  {200 {}}
                             :handler    (fn [{:keys [query-params]}]
                                           (http/ok query-params))}})))
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
