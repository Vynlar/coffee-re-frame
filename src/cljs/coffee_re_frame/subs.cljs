(ns coffee-re-frame.subs
  (:require
   [re-frame.core :as re-frame]
   [coffee-re-frame.recipe :as recipe]))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 ::recipes
 (fn [db _]
   (:recipes db)))

(re-frame/reg-sub
 ::recipe-state
 (fn [db _]
   (:recipe-state db)))

(re-frame/reg-sub
 ::current-step
 :<- [::selected-recipe]
 :<- [::recipe-state]
 (fn [[recipe {:keys [step-index]}] _]
   (get (::recipe/steps recipe) step-index)))

(re-frame/reg-sub
 ::selected-recipe
 (fn [{:keys [recipes selected-recipe]} _]
   (if selected-recipe
     (selected-recipe recipes)
     nil)))
