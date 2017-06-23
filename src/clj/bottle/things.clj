(ns bottle.things
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [bottle.specs]
            [taoensso.timbre :as log]))

(defprotocol ThingRepo
  "Stores things."
  (-search [this search-term] "Searchs for things."))

(defrecord StaticThingRepo [things]
  ThingRepo
  (-search [this term]
    (log/debug "Everything:" things)
    (filter #(str/includes? (:id %) term) things)))

(defn search [repo term]
  (log/debug (str "Searching for things containing \"" term "\"."))
  (-search repo term))

(defn repo [config]
  (map->StaticThingRepo {:things (:bottle/things config)}))

(s/def :bottle/thing-repo (partial satisfies? ThingRepo))
(s/def :bottle/search-term string?)

(s/fdef search
  :args (s/cat :repo :bottle/thing-repo
               :search-term :bottle/search-term)
  :ret (s/coll-of map?))

(s/fdef repo
  :args (s/cat :config (s/keys :req [:bottle/things]))
  :ret :bottle/thing-repo)
