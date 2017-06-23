(ns bottle.client
  (:require [aleph.http :as http]
            [manifold.stream :as s]
            [bottle.message :as message]
            [bottle.users :as users]))

(def content-type "application/transit+json")

(defn receive!
  ([conn]
   (receive! conn 100))
  ([conn timeout]
   (let [out @(s/try-take! conn :drained timeout :timeout)]
     (if (contains? #{:drained :timeout} out) out (message/decode content-type out)))))

(defn flush!
  [conn]
  (loop [out :continue]
    (when (not= out :timeout)
      (recur @(s/try-take! conn :drained 10 :timeout)))))

(defn send!
  [conn message]
  (s/put! conn (message/encode content-type message)))

(defn parse
  [response]
  (let [response-content-type (get-in response [:headers "content-type"])]
    (if (and (contains? response :body) (= response-content-type content-type))
      (update response :body (comp (partial message/decode content-type)))
      response)))

(defn add-user!
  [system username password]
  (users/add! (:user-manager system) {:bottle/username username
                                      :bottle/password password}))

(defn http-url [host] (str "http://" host))
(defn ws-url [host] (str "ws://" host))

(defn connect!
  ([host token]
   (connect! host token nil))
  ([host token category]
   (let [endpoint-url (str (ws-url host) "/api/websocket")
         url (if category
               (str endpoint-url "/" (name category))
               endpoint-url)
         url (str url "?token=" token)
         conn @(http/websocket-client url)]
     conn)))

(defprotocol Client
  (authenticate [this credentials])
  (search [this term]))

(defrecord ServiceClient [host token]
  Client
  (authenticate [this credentials]
    (let [response @(http/post (str (http-url host) "/api/tokens")
                               {:headers {"Content-Type" "application/json"
                                          "Accept" "text/plain"}
                                :body (String. (message/encode "application/json" credentials) "UTF-8")
                                :throw-exceptions false})]

      (if (= (:status response) 201)
        (assoc this :token (-> response :body slurp))
        (println (slurp (:body response))))))

  (search [this term]
    (parse @(http/get (str (http-url host) "/api/things")
                      {:headers {"Accept" "application/json"
                                 "Authorization" (str "Token " token)}
                       :query-params {"term" (name term)}
                       :throw-exceptions false}))))

(defn client
  [{:keys [host]}]
  (map->ServiceClient {:host host}))
