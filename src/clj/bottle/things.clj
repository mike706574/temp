(ns bottle.things
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [bottle.specs]))

;; protocol
(defprotocol ThingRepo
  "Stores things."
  (search [this search-term] "Searchs for things."))

;; static implementation
(defrecord StaticThingRepo [things]
  ThingRepo
  (search [this term]
    (filter #(str/includes? (:id %) term) things)))

;; constructor
(defn repo [config]
  (map->StaticThingRepo {:bottle/things config}))

;; specs
(s/def :bottle/thing-repo (partial satisfies? ThingRepo))

(s/fdef search
  :args (s/cat :repo :bottle/thing-repo
               :search-term :bottle/search-term)
  :ret (s/coll-of map?))

(s/fdef repo
  :args (s/cat :config (s/keys :req [:bottle/things]))
  :ret :bottle/thing-repo)
