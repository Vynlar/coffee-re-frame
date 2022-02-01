(ns coffee-re-frame.utils
  (:require
   [goog.string :as gstring]
   [goog.string.format]
   [cljs.core.async :refer [go]]
   [cljs.core.async.interop :refer-macros [<p!]]))

(defn format-time [seconds]
  (let [minutes (js/Math.floor (/ seconds 60))
        seconds (rem seconds 60)]
    (str minutes ":" (gstring/format "%02d" seconds))))

(def lock (atom nil))
(defn create-wakelock []
  (if (exists? js/navigator.wakeLock)
    (go
      (try
        (reset! lock (<p! (js/navigator.wakeLock.request "screen")))
        (catch ()))
    )))

(defn destroy-wakelock []
  (if :lock?
    (@lock.release [])
    (prn 0)))

(set! js/destroy_wakelock destroy-wakelock)
