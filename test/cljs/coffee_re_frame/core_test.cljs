(ns coffee-re-frame.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [coffee-re-frame.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
