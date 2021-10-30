(ns coffee-re-frame.storage)

(defn set-item!
  [key val]
  (.setItem (.-localStorage js/window) key val))

(defn get-item
  [key]
  (.getItem (.-localStorage js/window) key))
