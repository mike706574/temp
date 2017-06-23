(ns bottle.server.handler
  (:require [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [bottle.server.api.handler :as api-handler]))

(defprotocol HandlerFactory
  "Builds a request handler."
  (handler [this]))

(defrecord BottleHandlerFactory [authenticator
                                 conn-manager
                                 event-bus
                                 thing-repo
                                 user-manager]
  HandlerFactory
  (handler [this]
    (api-handler/handler this)))

(defn factory
  [{:keys [:bottle/event-content-type]}]
  (component/using
   (map->BottleHandlerFactory {:event-content-type event-content-type})
   [:conn-manager :thing-repo :user-manager :authenticator]))
