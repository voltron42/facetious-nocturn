(ns facetious-nocturn.server
  (:require
   [clojure.data.json :as json]
   [clojure.pprint :as pprint]
   [facetious-nocturn.session-manager :as session-manager]
   [reitit.ring :as ring]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

;; ============================================================================
;; utilities
;; ===========================================================================

(defn read-json-body [body]
  (let [body (slurp body)]
    (try
      (json/read-str body)
      (catch Exception e
        (throw (ex-info "Invalid JSON body" {:type :invalid-json} e))))))

;; ============================================================================
;; State Management
;; ============================================================================

(defonce session-manager (session-manager/create-session-manager))

;; ============================================================================
;; Endpoint Handlers
;; ============================================================================

(defmulti handle-endpoint (fn [& [endpoint]] endpoint))

(defmethod handle-endpoint :host [_ {:keys [body]}]
  (let [body (read-json-body body)
        {:keys [session-id host-id]} (session-manager/host session-manager
                                                           (get body "initialHostData")
                                                           (get body "initialCommonData"))]
    {:sessionId session-id
     :hostId host-id}))

(defmethod handle-endpoint :join [_ {:keys [path-params body]}]
  (let [body (read-json-body body)
        session-id (get path-params :session-id)
        initial-data (get body "initialData")
        guest-id (session-manager/join session-manager session-id initial-data)]
    {:guestId guest-id}))

(defmethod handle-endpoint :get-for-host [_ {:keys [path-params]}]
  (let [session-id (get path-params :session-id)
        host-id (get path-params :host-id)
        {:keys [host-data common-data created-at last-updated guests]} (session-manager/get-for-host session-manager session-id host-id)]
    {:hostData host-data
     :commonData common-data
     :createdAt created-at
     :lastUpdated last-updated
     :guests guests}))

(defmethod handle-endpoint :get-for-guest [_ {:keys [path-params]}]
  (let [session-id (get path-params :session-id)
        guest-id (get path-params :guest-id)
        {:keys [common-data from-guest to-guest created-at last-updated]} (session-manager/get-for-guest session-manager session-id guest-id)]
    {:commonData common-data
     :createdAt created-at
     :lastUpdated last-updated
     :fromGuest from-guest
     :toGuest to-guest}))

(defmethod handle-endpoint :get-common-data [_ {:keys [path-params]}]
  (let [session-id (get path-params :session-id)
        {:keys [common-data created-at last-updated]} (session-manager/get-common-data session-manager session-id)]
    {:commonData common-data
     :createdAt created-at
     :lastUpdated last-updated}))

(defmethod handle-endpoint :update-host [_ {:keys [path-params body]}]
  (let [body (read-json-body body)
        session-id (get path-params :session-id)
        host-id (get path-params :host-id)
        {:keys [date-queued]} (session-manager/update-host session-manager session-id host-id {:host-data (get body "hostData")
                                                                                               :common-data (get body "commonData")})]
    {:dateQueued date-queued}))

(defmethod handle-endpoint :update-guest [_ {:keys [path-params body]}]
  (let [body (read-json-body body)
        session-id (get path-params :session-id)
        guest-id (get path-params :guest-id)
        {:keys [date-queued]} (session-manager/update-guest session-manager session-id guest-id {:fromGuest (get body "fromGuest")})]
    {:dateQueued date-queued}))

(defmethod handle-endpoint :kick [_ {:keys [path-params]}]
  (let [session-id (get path-params :session-id)
        host-id (get path-params :host-id)
        guest-id (get path-params :guest-id)
        {:keys [date-queued]} (session-manager/kick session-manager session-id host-id guest-id)]
    {:dateQueued date-queued}))

(defmethod handle-endpoint :leave [_ {:keys [path-params]}]
  (let [session-id (get path-params :session-id)
        guest-id (get path-params :guest-id)
        {:keys [date-queued]} (session-manager/leave session-manager session-id guest-id)]
    {:dateQueued date-queued}))

(defmethod handle-endpoint :close [_ {:keys [path-params]}]
  (let [session-id (get path-params :session-id)
        host-id (get path-params :host-id)
        {:keys [host-data common-data created-at last-updated guests]} (session-manager/close session-manager session-id host-id)]
    {:hostData host-data
     :commonData common-data
     :createdAt created-at
     :lastUpdated last-updated
     :guests guests}))

(defmethod handle-endpoint :default [_ _]
  {:status 404 :body {:error "Endpoint not found"}})

(defmulti handle-error (fn [error] (:type (ex-data error))))

(defmethod handle-error :not-found [e]
  {:status 404 :body {:error "Resource not found" :details (ex-data e)}})

(defmethod handle-error :access-denied [e]
  {:status 403 :body {:error "Access denied" :details (ex-data e)}})

(defmethod handle-error :default [e]
  {:status 500 :body {:error "Internal server error" :details (ex-data e)}})

(defn handle [endpoint request]
  (try
    {:status 200 :body (handle-endpoint endpoint request)}
    (catch Exception e
      (handle-error e))))

;; ============================================================================
;; Swagger/OpenAPI Metadata
;; ============================================================================

(def routes
  [;; Swagger UI and spec
   ["/swagger.json" {:get {:no-doc true
                           :swagger {:info {:title "Session Management API"
                                            :description "A service for managing shared sessions with role-based data visibility"
                                            :version "0.1.0"}}
                           :handler (swagger/create-swagger-handler)}}]
   ["/swagger/ui*" {:get {:no-doc true
                          :handler (swagger-ui/create-swagger-ui-handler
                                    {:url "/swagger.json"
                                     :config {:docExpansion "none"}})}}]

   ;; API Routes with Swagger metadata
   ["/api/host"
    {:post
     {:summary "Initialize a new session"
      :description "Create a new shared session with optional schemas for host, guest, and common data"
      :swagger {:parameters [{:in :body
                              :name :body
                              :required false
                              :schema {:type :object
                                       :properties {:initialHostData {:type :object
                                                                      :description "optional initial host data"}
                                                    :initialCommonData {:type :object
                                                                        :description "optional initial common data"}}}}]
                :responses {200 {:description "Session created successfully"
                                 :schema {:type :object
                                          :properties {:sessionId {:type :string}
                                                       :hostId {:type :string}}}}}}
      :handler #(handle :host %)}}]

   ["/api/sessions/:session-id"
    {:get
     {:summary "Get common session data (host or guest)"
      :description "Retrieve common session data accessible to all participants"
      :swagger {:parameters [{:in :path
                              :name :session-id
                              :type :string
                              :required true}]
                :responses {200 {:description "Common data retrieved"
                                 :schema {:type :object
                                          :properties {:commonData {:type :object}}}}
                            404 {:description "Session not found"}}}
      :handler #(handle :get-common-data %)}}]

   ["/api/sessions/:session-id/join"
    {:post
     {:summary "Join an existing session as guest"
      :description "Guest joins session with optional initial guest data"
      :swagger {:parameters [{:in :path
                              :name :session-id
                              :type :string
                              :required true}
                             {:in :body
                              :name :body
                              :required false
                              :schema {:type :object
                                       :properties {:initialData {:type :object
                                                                  :description "optional json of initial guest data"}}}}]
                :responses {200 {:description "Guest joined successfully"
                                 :schema {:type :object
                                          :properties {:guestId {:type :string}}}}
                            404 {:description "Session not found"}}}
      :handler #(handle :join %)}}]

   ["/api/sessions/:session-id/host/:host-id"
    {:get
     {:summary "Get all session data (host only)"
      :description "Retrieve complete session state including host, common, and all guest data"
      :swagger {:parameters [{:in :path
                              :name :session-id
                              :type :string
                              :required true}
                             {:in :path
                              :name :host-id
                              :type :string
                              :required true}]
                :responses {200 {:description "Session data retrieved"
                                 :schema {:type :object
                                          :properties {:hostData {:type :object}
                                                       :commonData {:type :object}
                                                       :createdAt {:type :string}
                                                       :lastUpdated {:type :string}
                                                       :guests {:type :object
                                                                :additionalProperties {:description "guest data by id"
                                                                                       :type :object
                                                                                       :properties {:toGuest {:type :object}
                                                                                                    :fromGuest {:type :object}}}}}
                                          :example {:hostData {}
                                                    :commonData {}
                                                    :createdAt ""
                                                    :lastUpdated ""
                                                    :toGuests {"guestId1" {:toGuest {}
                                                                           :fromGuest {}}
                                                               "guestId2" {:toGuest {} 
                                                                           :fromGuest {}}}}}}
                            403 {:description "Unauthorized - invalid host ID"}
                            404 {:description "Session not found"}}}
      :handler #(handle :get-for-host %)}

     :put
     {:summary "Host updates session data"
      :description "Host can update host, common, and guest data"
      :swagger {:parameters [{:in :path
                              :name :session-id
                              :type :string
                              :required true}
                             {:in :path
                              :name :host-id
                              :type :string
                              :required true}
                             {:in :body
                              :name :body
                              :required true
                              :schema {:type :object
                                       :properties {:hostData {:type :object
                                                               :description "optional host data json"}
                                                    :commonData {:type :object
                                                                 :description "optional common data json"}
                                                    :toGuests {:type :object
                                                               :additionalProperties {:description "guest data by id"
                                                                                      :type :object
                                                                                      :properties {:toGuest {:type :object}}}
                                                               :description "optional data for guests"}}
                                       :example {:hostData {}
                                                 :commonData {}
                                                 :toGuests {"guestId1" {:toGuest {}}
                                                            "guestId2" {:toGuest {}}}}}}]
                :responses {200 {:description "Data updated successfully"
                                 :schema {:type :object
                                          :properties {:dateQueued {:type :string}}}}
                            403 {:description "Unauthorized - invalid host ID"}
                            404 {:description "Session not found"}}}
      :handler #(handle :update-host %)}}]

   ["/api/sessions/:session-id/host/:host-id/kick/:guest-id"
    {:delete
     {:summary "Remove a guest from session"
      :description "Host removes a specific guest from the session"
      :swagger {:parameters [{:in :path
                              :name :session-id
                              :type :string
                              :required true}
                             {:in :path
                              :name :host-id
                              :type :string
                              :required true}
                             {:in :path
                              :name :guest-id
                              :type :string
                              :required true}]
                :responses {200 {:description "Guest removed successfully"
                                 :schema {:type :object
                                          :properties {:dateQueued {:type :string}}}}
                            403 {:description "Unauthorized - invalid host ID"}
                            404 {:description "Session or guest not found"}}}
      :handler #(handle :kick %)}}]

   ["/api/sessions/:session-id/host/:host-id/close"
    {:delete
     {:summary "Close session and remove all guests"
      :description "Close session and return final state"
      :swagger {:parameters [{:in :path
                              :name :session-id
                              :type :string
                              :required true}
                             {:in :path
                              :name :host-id
                              :type :string
                              :required true}]
                :responses {200 {:description "Session closed"
                                 :schema {:type :object
                                          :properties {:hostData {:type :object}
                                                       :commonData {:type :object}
                                                       :createdAt {:type :string}
                                                       :lastUpdated {:type :string}
                                                       :guests {:type :object}}}}
                            403 {:description "Unauthorized - invalid host ID"}
                            404 {:description "Session not found"}}}
      :handler #(handle :close %)}}]

   ["/api/sessions/:session-id/guest/:guest-id"
    {:get
     {:summary "Guest retrieves their accessible data"
      :description "Get common data and guest-specific data"
      :swagger {:parameters [{:in :path
                              :name :session-id
                              :type :string
                              :required true}
                             {:in :path
                              :name :guest-id
                              :type :string
                              :required true}]
                :responses {200 {:description "Data retrieved"
                                 :schema {:type :object
                                          :properties {:commonData {:type :object}
                                                       :createdAt {:type :string}
                                                       :lastUpdated {:type :string}
                                                       :fromGuest {:type :object}
                                                       :toGuest {:type :object}}}}
                            404 {:description "Session or guest not found"}}}
      :handler #(handle :get-for-guest %)}

     :put
     {:summary "Guest updates their data"
      :description "Guest can update their own guest data and common data"
      :swagger {:parameters [{:in :path
                              :name :session-id
                              :type :string
                              :required true}
                             {:in :path
                              :name :guest-id
                              :type :string
                              :required true}
                             {:in :body
                              :name :body
                              :required true
                              :schema {:type :object
                                       :properties {:fromGuest {:type :object
                                                                :description "optional guest-specific data json"}}}}]
                :responses {200 {:description "Data updated successfully"
                                 :schema {:type :object
                                          :properties {:dateQueued {:type :string}}}}
                            404 {:description "Session or guest not found"}}}
      :handler #(handle :update-guest %)}}]

   ["/api/sessions/:session-id/guest/:guest-id/leave"
    {:delete
     {:summary "Guest leaves the session"
      :description "Remove the guest from the session"
      :swagger {:parameters [{:in :path
                              :name :session-id
                              :type :string
                              :required true}
                             {:in :path
                              :name :guest-id
                              :type :string
                              :required true}]
                :responses {200 {:description "Guest left successfully"
                                 :schema {:type :object
                                          :properties {:dateQueued {:type :string}}}}
                            404 {:description "Session or guest not found"}}}
      :handler #(handle :leave %)}}]

   ;; Health check endpoint
   ["/health"
    {:get
     {:summary "Health check"
      :description "Simple endpoint to verify server is running"
      :responses {200 {:description "Server is healthy"}}
      :handler (fn [_] {:status 200 :body (session-manager/heartbeat session-manager)})}}]
   ;; Static file handler for sample web app
   ["/spyfall/*" {:no-doc true
                  :handler (ring/create-resource-handler)}]])

;; ============================================================================
;; Ring Application
;; ============================================================================

(def app
  (ring/ring-handler
   (ring/router routes)
   (ring/create-default-handler)))

;; ============================================================================
;; Server Lifecycle
;; ============================================================================

(defonce server (atom nil))

(defn start-server
  "Start the Jetty HTTP server"
  [port]
  (println (str "Starting server on port " port "..."))
  (reset! server (jetty/run-jetty
                  (-> app
                      (wrap-json-body {:keywords? true})
                      (wrap-json-response))
                  {:port port :join? false}))
  (println "Server started.")
  (println (str "Swagger UI available at http://localhost:" port "/swagger/ui"))
  (println (str "Spyfall app available at http://localhost:" port "/spyfall/index.html")))

(defn stop-server
  "Stop the Jetty HTTP server"
  []
  (when @server
    (.stop @server)
    (println "Server stopped")))

(defn -main
  "Main entry point for the application"
  [& [port]]
  (let [port-num (Integer/parseInt (or port (System/getenv "PORT") "3000"))]
    (start-server port-num)))
