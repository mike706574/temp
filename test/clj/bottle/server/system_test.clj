(ns bottle.server.system-test
  (:require [aleph.http :as http]
            [bottle.client :as client]
            [bottle.macros :refer [with-system unpack-response]]
            [bottle.message :as message]
            [bottle.server.system :as system]
            [bottle.things :as things]
            [bottle.util :as util :refer [map-vals]]
            [com.stuartsierra.component :as component]
            [clojure.test :refer [deftest testing is]]
            [manifold.bus :as bus]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [taoensso.timbre :as log]))

(def port 9001)
(def config {:bottle/id "bottle-server"
             :bottle/port port
             :bottle/log-path "/tmp"
             :bottle/secret-key "secret"
             :bottle/user-manager-type :atomic
             :bottle/things [{:id "animal"}
                             {:id "apple"}
                             {:id "astronaut"}
                             {:id "dog"}
                             {:id "banana"}
                             {:id "cat"}
                             {:id "canine"}
                             {:id "corpse"}
                             {:id "rocket"}
                             {:id "monster"}
                             {:id "monster"}]
             :bottle/users {"mike" "rocket"}})

(deftest simple-test
  (with-system (system/system config)
    (let [client (-> {:host (str "localhost:" port)}
                     (client/client)
                     (client/authenticate {:bottle/username "mike"
                                           :bottle/password "rocket"}))
          foo-1 {:bottle/category :foo
                 :bottle/closed? false
                 :count 4 }]
      (println client)
      ;; query
#_      (unpack-response (client/search client "a")
        (is (= 200 status))
        (is (= [] body))))))
