(ns coffee-re-frame.views
  (:require
   [re-frame.core :as re-frame]
   [coffee-re-frame.subs :as subs]
   [coffee-re-frame.events :as events]
   [coffee-re-frame.recipe :as recipe]
   ))


;; home

(defn recipe-list-item [recipe-key recipe]
  [:li
   (::recipe/name recipe)
   [:button {:on-click #(re-frame/dispatch [::events/select-recipe recipe-key])} "Select"]
   ])

(defn recipe-select []
  (let [recipes (re-frame/subscribe [::subs/recipes])]
    [:div
     [:h1 "Select a recipe"]
     (->>
      @recipes
      (map (fn [[recipe-key recipe]]
             [recipe-list-item recipe-key recipe]))
      (into [:ul]))]))

(defn recipe-title [recipe]
  [:h1 (::recipe/name recipe)])

(defn recipe-cancel-button []
  [:button {:on-click #(re-frame/dispatch [::events/select-recipe nil])} "Cancel"])

(defn recipe-step []
  (let [step (re-frame/subscribe [::subs/current-step])]
    [:h2 "Current step: " (:step/title @step)
     (if (contains? #{:step.type/start :step.type/prompt} (:step/type @step))
       [:button {:on-click #(re-frame/dispatch [::events/next-step])} "NEXT"])
     ]))

(defn recipe-session [recipe]
  (let [state (re-frame/subscribe [::subs/recipe-state])]
    [:div
     [recipe-cancel-button]
     [recipe-title recipe]
     [recipe-step]
     [:p (str (:tick @state)) " seconds"]
     [:p (str (:volume @state)) "g liquid"]
     #_[:button {:on-click #(re-frame/dispatch [::events/tick])} "Tick"]
     ]))

(defn home-panel []
  (let [recipe (re-frame/subscribe [::subs/selected-recipe])]
    [:div
     (if @recipe
       [recipe-session @recipe]
       [recipe-select])]))


;; about

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]

   [:div
    [:a {:href "#/"}
     "go to Home Page"]]])


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
