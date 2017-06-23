(ns bottle.server.api.routes
  (:require [bottle.things :as things]
            [bottle.users :as users]
            [bottle.server.authentication :as auth]
            [bottle.server.http :refer [with-body
                                        handle-exceptions
                                        body-response
                                        not-acceptable
                                        parsed-body
                                        unsupported-media-type]]
            [clj-time.core :as time]
            [compojure.core :as compojure :refer [ANY DELETE GET PATCH POST PUT]]
            [compojure.route :as route]
            [taoensso.timbre :as log]))

(defn retrieve-things
  [{:keys [thing-repo]} request]
  (handle-exceptions request
    (or (not-acceptable request)
        (if-let [term (get-in request [:headers :term])]
          (let [things (things/search thing-repo term)]
            (Thread/sleep 300)
            (body-response 200 request things))
          (body-response 400 request {:bottle.server/message (str "Missing required query parameter: term")})))))

(defn create-token
  [{:keys [user-manager authenticator]} request]
  (try
    (or (not-acceptable request #{"text/plain"})
        (with-body [credentials :bottle/credentials request]
          (if-let [user (users/authenticate user-manager credentials)]
            {:status 201
             :headers {"Content-Type" "text/plain"}
             :body (auth/token authenticator (:bottle/username credentials))}
            {:status 401
             :headers {"Content-Type" "text/plain"}
             :body "Authentication failed."})))
    (catch Exception e
      (log/error e "An exception was thrown while processing a request.")
      {:status 500
       :headers {"Content-Type" "text/plain"}
       :body "An error occurred."})))

(defn routes
  [{:keys [user-manager authenticator] :as deps}]
  (letfn [(unauthenticated [request]
            (when-not (auth/authenticated? authenticator request)
              {:status 401}))]
    (compojure/routes
     (GET "/api/healthcheck" request {:status 200})
     (GET "/api/things" request (retrieve-things deps request))
     (POST "/api/tokens" request (create-token deps request))
     (route/not-found {:status 404}))))
