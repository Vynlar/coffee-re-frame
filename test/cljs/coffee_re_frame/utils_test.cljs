(ns coffee-re-frame.utils-test
  (:require [coffee-re-frame.utils :as utils]
            [cljs.test :as t :refer-macros [deftest is]]))

(deftest format-time-test
  (is (= (utils/format-time 0) "0:00"))
  (is (= (utils/format-time 30) "0:30"))
  (is (= (utils/format-time 60) "1:00"))
  (is (= (utils/format-time 65) "1:05"))
  (is (= (utils/format-time 75) "1:15"))
  (is (= (utils/format-time 140) "2:20")))
