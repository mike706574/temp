(ns bottle.specs
  (:require [clojure.spec.alpha :as s]))

(s/def :bottle/thing map?)
(s/def :bottle/things (s/coll-of :bottle/thing))
(s/def :bottle/search-term string?)
