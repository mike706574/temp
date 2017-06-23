(ns bottle.server.system
  (:require [bottle.server.authentication :as auth]
            [bottle.server.connection :as conn]
            [bottle.server.handler :as handler]
            [bottle.server.service :as service]
            [bottle.things :as things]
            [bottle.users :as users]
            [bottle.util :as util]
            [clojure.spec.alpha :as s]
            [com.stuartsierra.component :as component]
            [manifold.bus :as bus]
            [taoensso.timbre :as log]
            [taoensso.timbre.appenders.core :as appenders]))

(defn configure-logging!
  [{:keys [:bottle/id :bottle/log-path] :as config}]
  (let [log-file (str log-path "/" id "-" (util/uuid))]
    (log/merge-config!
     {:appenders {:spit (appenders/spit-appender
                         {:fname log-file})}})))

;; app
(s/def :bottle/id string?)
(s/def :bottle/port integer?)
(s/def :bottle/log-path string?)
(s/def :bottle/user-manager-type #{:atomic})
(s/def :bottle/users (s/map-of :bottle/username :bottle/password))
(s/def :bottle/config (s/keys :req [:bottle/id
                                    :bottle/port
                                    :bottle/log-path
                                    :bottle/user-manager-type]
                              :opt [:bottle/users]))

(defn build
  [config]
  (log/info (str "Building " (:bottle/id config) "."))
  (configure-logging! config)
  {:thing-repo (things/repo config )
   :user-manager (users/user-manager config)
   :authenticator (auth/authenticator config)
   :conn-manager (conn/manager config)
   :handler-factory (handler/factory config)
   :app (service/aleph-service config)})

(defn system
  [config]
  (if-let [validation-failure (s/explain-data :bottle/config config)]
    (do (log/error (str "Invalid configuration:\n"
                        (util/pretty config)
                        "Validation failure:\n"
                        (util/pretty validation-failure)))
        (throw (ex-info "Invalid configuration." {:config config
                                                  :validation-failure validation-failure})))
    (build config)))

(s/fdef system
  :args (s/cat :config :bottle/config)
  :ret map?)
