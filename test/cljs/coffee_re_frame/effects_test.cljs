(ns coffee-re-frame.effects-test
  (:require [coffee-re-frame.effects :as effects]
            [cljs.test :as t :refer-macros [deftest testing is]]))

(defn clear-local-storage-fixture [f]
  (.clear js/localStorage)
  (f)
  (.clear js/localStorage))

(t/use-fixtures :each clear-local-storage-fixture)

(deftest store-primitives
  (let [key :my-key]
    (is (= (effects/local-storage-coeffect {} key)  {:local-storage nil}))
    (effects/local-storage-effect [:local-storage key "my value"])
    (is (= (effects/local-storage-coeffect {} key)  {:local-storage "my value"}))))

(deftest store-vectors
  (let [key :my-key]
    (is (= (effects/local-storage-coeffect {} key)  {:local-storage nil}))
    (effects/local-storage-effect [:local-storage key [1 2 3]])
    (is (= (effects/local-storage-coeffect {} key)  {:local-storage [1 2 3]}))))

(deftest store-maps
  (let [key :my-key]
    (is (= (effects/local-storage-coeffect {} key)  {:local-storage nil}))
    (effects/local-storage-effect [:local-storage key {:key "value" :another "value2"}])
    (is (= (effects/local-storage-coeffect {} key)  {:local-storage {:key "value" :another "value2"}}))))

(deftest store-nested
  (let [key :my-key]
    (is (= (effects/local-storage-coeffect {} key)  {:local-storage nil}))
    (effects/local-storage-effect [:local-storage key {:a {:nested [{:vector 3}]}}])
    (is (= (effects/local-storage-coeffect {} key)  {:local-storage {:a {:nested [{:vector 3}]}}}))))
