(ns coffee-re-frame.views.recipe-list
  (:require
   [coffee-re-frame.recipe :as recipe]
   [coffee-re-frame.components :as c]))

(defn recipe-list-item [recipe-key recipe]
  [:a {:class "p-4 bg-gray-800 border border-gray-600 rounded space-y-2 font-bold hover:bg-blue-500 transition transition-50"
       :href (str "#/setup/" (name recipe-key))}
   (::recipe/name recipe)])

(defn recipe-select []
  [:div {:class "space-y-3 p-4"}
   [c/micro-header "Select a brew method"]
   (into [:ul {:class "grid gap-3"}]
         (for [[recipe-key constructor] recipe/recipe-constructors]
           [recipe-list-item recipe-key (constructor 250)]))])

(defn panel []
  [c/container [recipe-select]])
