(ns facetious-nocturn.server
  (:require [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [facetious-nocturn.session-manager :as session-manager]))

;; ============================================================================
;; State Management
;; ============================================================================

(defonce session-manager (session-manager/create-session-manager))

;; ============================================================================
;; Endpoint Handlers
;; ============================================================================

(defmulti handle-endpoint (fn [& [endpoint]] endpoint))

(defmethod handle-endpoint :host [_ {:keys [body]}]
  (session-manager/host session-manager
                        (get body "initialHostData")
                        (get body "initialCommonData")))

(defmethod handle-endpoint :join [_ {:keys [path-params body]}]
  (let [session-id (get path-params :session-id)
        initial-data (get body "initialData")]
    (session-manager/join session-manager session-id initial-data)))

(defmethod handle-endpoint :get-for-host [_ {:keys [path-params]}]
  (let [session-id (get path-params :session-id)
        host-id (get path-params :host-id)]
    (session-manager/get-for-host session-manager session-id host-id)))

(defmethod handle-endpoint :get-for-guest [_ {:keys [path-params]}]
  (let [session-id (get path-params :session-id)
        guest-id (get path-params :guest-id)]
    (session-manager/get-for-guest session-manager session-id guest-id)))

(defmethod handle-endpoint :get-common-data [_ {:keys [path-params]}]
  (let [session-id (get path-params :session-id)]
    (session-manager/get-common-data session-manager session-id)))

(defmethod handle-endpoint :update-host [_ {:keys [path-params body]}]
  (let [session-id (get path-params :session-id)
        host-id (get path-params :host-id)]
    (session-manager/update-host session-manager session-id host-id body)))

(defmethod handle-endpoint :update-guest [_ {:keys [path-params body]}]
  (let [session-id (get path-params :session-id)
        guest-id (get path-params :guest-id)]
    (session-manager/update-guest session-manager session-id guest-id body)))

(defmethod handle-endpoint :kick [_ {:keys [path-params]}]
  (let [session-id (get path-params :session-id)
        host-id (get path-params :host-id)
        guest-id (get path-params :guest-id)]
    (session-manager/kick session-manager session-id host-id guest-id)))

(defmethod handle-endpoint :leave [_ {:keys [path-params]}]
  (let [session-id (get path-params :session-id)
        guest-id (get path-params :guest-id)]
    (session-manager/leave session-manager session-id guest-id)))

(defmethod handle-endpoint :close [_ {:keys [path-params]}]
  (let [session-id (get path-params :session-id)
        host-id (get path-params :host-id)]
    (session-manager/close session-manager session-id host-id)))

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
    (handle-endpoint endpoint request)
    (catch Exception e
      (handle-error e))))

;; ============================================================================
;; Swagger/OpenAPI Metadata
;; ============================================================================

(def routes
  [;; Swagger UI and spec
   ["/swagger.json" {:get (swagger/create-swagger-handler)}]
   ["/swagger.html" {:get (swagger-ui/create-swagger-ui-handler
                           {:url "/swagger.json"})}]

   ;; API Routes with Swagger metadata
   ["/api"
    ["/sessions"
     ["/host"
      {:post
       {:summary "Initialize a new session"
        :description "Create a new shared session with optional schemas for host, guest, and common data"
        :parameters {:body {:type :object
                            :properties {:initialHostData {:type :string
                                                           :description "optional initial host data as string"}
                                         :initialCommonData {:type :string
                                                             :description "optional initial common data as string"}}}}
        :responses {200 {:description "Session created successfully"
                        :body {:type :object
                               :properties {:session-id {:type :string}
                                          :host-id {:type :string}}}}}
        :handler #(handle :host %)}}]
     
     ["/:session-id"
      {:get
       {:summary "Get common session data (host or guest)"
        :description "Retrieve common session data accessible to all participants"
        :parameters {:path {:session-id {:type :string}}}
        :responses {200 {:description "Common data retrieved"
                         :body {:type :object
                                :properties {:common-data {:type :string}}}}
                    404 {:description "Session not found"}}
        :handler #(handle :get-common-data %)}}
      ["/join"
       {:post
        {:summary "Join an existing session as guest"
         :description "Guest joins session with optional initial guest data"
         :parameters {:path {:session-id {:type :string}}
                      :body {:type :object
                             :properties {:initialData {:type :string
                                                        :description "optional json of initial guest data"}}}}
         :responses {200 {:description "Guest joined successfully"
                          :body {:type :object
                                 :properties {:guest-id {:type :string}}}}
                     404 {:description "Session not found"}}
         :handler #(handle :join %)}}]
      
      ["/:host-id"
       {:get
        {:summary "Get all session data (host only)"
         :description "Retrieve complete session state including host, common, and all guest data"
         :parameters {:path {:session-id {:type :string}
                            :host-id {:type :string}}}
         :responses {200 {:description "Session data retrieved"
                         :body {:type :object
                                :properties {:host-data {:type :string}
                                            :common-data {:type :string}
                                            :created-on {:type :string}
                                            :last-modified-on {:type :string}
                                            :guests {:type :array}}}}
                    403 {:description "Unauthorized - invalid host ID"}
                    404 {:description "Session not found"}}
         :handler #(handle :get-for-host %)}
        
        :put
        {:summary "Host updates session data"
         :description "Host can update host, common, and guest data"
         :parameters {:path {:session-id {:type :string}
                             :host-id {:type :string}}
                      :body {:type :object
                             :properties {:hostData {:type :string
                                                     :description "optional host data json as string"}
                                          :commonData {:type :string
                                                       :description "optional common data json as string"}}}}
         :responses {200 {:description "Data updated successfully"
                          :body {:type :object
                                 :properties {:date-queued {:type :string}}}}
                     403 {:description "Unauthorized - invalid host ID"}
                     404 {:description "Session not found"}}
         :handler #(handle :update-host %)}}
       
       ["/kick/:guest-id"
        {:delete
         {:summary "Remove a guest from session"
          :description "Host removes a specific guest from the session"
          :parameters {:path {:session-id {:type :string}
                             :host-id {:type :string}
                             :guest-id {:type :string}}}
          :responses {200 {:description "Guest removed successfully"
                          :body {:type :object
                                 :properties {:guests {:type :array}}}}
                     403 {:description "Unauthorized - invalid host ID"}
                     404 {:description "Session or guest not found"}}
          :handler #(handle :kick %)}}]
       
       ["/close"
        {:delete
         {:summary "Close session and remove all guests"
         :description "Close session and return final state"
          :parameters {:path {:session-id {:type :string}
                             :host-id {:type :string}}}
          :responses {200 {:description "Session closed"
                          :body {:type :object
                                 :properties {:host-data {:type :string}
                                            :common-data {:type :string}
                                            :created-on {:type :string}
                                            :last-modified-on {:type :string}
                                            :guests {:type :array}}}}
                     403 {:description "Unauthorized - invalid host ID"}
                     404 {:description "Session not found"}}
          :handler #(handle :close %)}}]]]
      
      ["/guest/:guest-id"
       {:get
        {:summary "Guest retrieves their accessible data"
         :description "Get common data and guest-specific data"
         :parameters {:path {:session-id {:type :string}
                             :guest-id {:type :string}}}
         :responses {200 {:description "Data retrieved"
                          :body {:type :object
                                 :properties {:common-data {:type :string}
                                              :guest-data {:type :string}}}}
                     404 {:description "Session or guest not found"}}
         :handler #(handle :get-for-guest %)}

        :put
        {:summary "Guest updates their data"
         :description "Guest can update their own guest data and common data"
         :parameters {:path {:session-id {:type :string}
                             :guest-id {:type :string}}
                      :body {:type :object
                             :properties {:guestData {:type :string
                                                      :description "optional guest-specific data json"}}}}
         :responses {200 {:description "Data updated successfully"
                          :body {:type :object
                                 :properties {:common-data {:type :string}
                                              :guest-data {:type :string}}}}
                     404 {:description "Session or guest not found"}}
         :handler #(handle :update-guest %)}}
       
       ["/leave"
        {:delete
         {:summary "Guest leaves the session"
          :description "Remove the guest from the session"
          :parameters {:path {:session-id {:type :string}
                              :guest-id {:type :string}}}
          :responses {200 {:description "Guest left successfully"}
                      404 {:description "Session or guest not found"}}
          :handler #(handle :leave %)}}]]]]

   ;; Health check endpoint
   ["/health"
    {:get
     {:summary "Health check"
      :description "Simple endpoint to verify server is running"
      :responses {200 {:description "Server is healthy"}}
      :handler (fn [_] {:status 200 :body {:status "ok"}})}}]
   ;; Static file handler for sample web app
   ["/app" (ring/create-file-handler)]])

;; ============================================================================
;; Ring Application
;; ============================================================================

(def app
  (ring/ring-handler
   (ring/router routes {:data {:swagger {:info {:title "Session Management API"
                                               :description "A service for managing shared sessions with role-based data visibility"
                                               :version "0.1.0"}}}})
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
  (println (str "Server started. Swagger UI available at http://localhost:" port "/swagger.html")))

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
