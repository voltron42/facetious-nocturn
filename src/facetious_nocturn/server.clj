(ns facetious-nocturn.server
  (:require [clojure.tools.logging :as log]
            [compojure.api.sweet :as sweet]
            [compojure.api.core :as api]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [ring.util.http-response :as http]
            [environ.core :refer [env]]
            [org.httpkit.server :as server]
            [facetious-nocturn.schema :as s]
            [cheshire.core :as cheshire]
            [cheshire.custom :as cust]))

(cust/add-encoder java.lang.Class
                  (fn [c jsonGenerator]
                    (.writeString jsonGenerator (-> c .getSimpleName))))



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
              :post         {:summary    ""
                            :parameters {:body s/Session}
                            :consumes   ["application/json"]
                            :produces   ["application/json"]
                            :responses  {200 s/Session}
                            :handler    (fn [{:keys [body] :as req}]
                                          (log/info (cheshire/generate-string
                                                     (:keys req)
                                                     {:pretty true}))
                                          (http/ok req))}})))
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
