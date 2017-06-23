(ns bottle.things-test
  (:require [bottle.things :as things]
            [clojure.test :refer [deftest testing is]]))

(def things
  [{:id "animal"}
   {:id "apple"}
   {:id "astronaut"}
   {:id "dog"}
   {:id "banana"}
   {:id "cat"}
   {:id "canine"}
   {:id "corpse"}
   {:id "rocket"}
   {:id "monster"}])

(def repo (things/repo {:bottle/things things}))

(deftest searching
  (is (= [] (things/search repo "ealknwe")))
  (is (= [{:id "animal"} {:id "monster"}] (things/search repo "m")))
  (is (= things (things/search repo ""))))
