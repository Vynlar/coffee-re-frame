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
   (into {}
         (for [[recipe-key recipe] (:recipes db)]
           [recipe-key (assoc recipe :total-volume (recipe/get-total-volume recipe))]))))

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
 ::next-step
 :<- [::selected-recipe]
 :<- [::recipe-state]
 (fn [[recipe {:keys [step-index]}] _]
   (get (::recipe/steps recipe) (inc step-index))))

(re-frame/reg-sub
 ::selected-recipe
 (fn [{:keys [recipes selected-recipe]} _]
   (if selected-recipe
     (selected-recipe recipes)
     nil)))

(re-frame/reg-sub
 ::total-step-count
 :<- [::selected-recipe]
 (fn [recipe _]
   (count (::recipe/steps recipe))))

(re-frame/reg-sub
 ::step-index
 (fn [db _]
   (get-in db [:recipe-state :step-index])))

(re-frame/reg-sub
 ::recipe-progress
 :<- [::step-index]
 :<- [::total-step-count]
 (fn [[index total] _]
   {:index index
    :total total}))

(re-frame/reg-sub
 ::remaining-seconds-in-step
 :<- [::current-step]
 :<- [::recipe-state]
 (fn [[current-step recipe-state] _]
   (let [seconds-in-step (:step/duration current-step)
         current-step-tick (:current-step-tick recipe-state)]
     (- seconds-in-step current-step-tick))))

(re-frame/reg-sub
 ::total-volume
 (fn [_] [(re-frame/subscribe [::selected-recipe])])
 (fn [[recipe] _]
   (recipe/get-total-volume recipe)))
