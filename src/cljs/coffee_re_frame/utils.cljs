(ns coffee-re-frame.utils
  (:require
   [goog.string :as gstring]
   [goog.string.format]))

(defn format-time [seconds]
  (let [minutes (js/Math.floor (/ seconds 60))
        seconds (rem seconds 60)]
    (str minutes ":" (gstring/format "%02d" seconds))))
