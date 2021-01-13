(ns coffee-re-frame.components
  (:require
   [reagent.core :as reagent :refer [class-names]]))

(defn micro-header
  ([text] (micro-header {:variant :light} text))
  ([{:keys [variant as]
     :as attrs
     :or {variant :light as :p}} text]
   (let [classes (case variant
                   :dark "text-black opacity-80"
                   :light "text-white opacity-80")]
     [as (merge attrs {:class (class-names "uppercase text-xs tracking-wider font-normal" classes)}) text])))

(defn container [& children]
  (into
   [:div {:class "mx-auto bg-gray-900 text-white min-h-full"}]
   children))

(defn home-button []
  [:a {:class "text-xs tracking-wide"
       :href "#/"} "Back"])
