(ns coffee-re-frame.views
  (:require
   [re-frame.core :as re-frame]
   [coffee-re-frame.subs :as subs]
   [coffee-re-frame.events :as events]
   [coffee-re-frame.recipe :as recipe]
   ))


;; home

(defn button [attrs text]
  [:button (merge {:class "bg-yellow-700 px-3 py-1 text-white rounded shadow"} attrs) text])

(defn micro-header [text]
  [:p {:class "uppercase text-xs tracking-wider text-gray-500"} text])

(defn recipe-list-item [recipe-key recipe]
  [:li {:class "p-4 border border-gray-300 rounded space-y-2"}
   [:div
    [micro-header "Name"]
    [:h2 {:class "font-bold"} (::recipe/name recipe)]]
   [button {:on-click #(re-frame/dispatch [::events/select-recipe recipe-key])} "Start Brew"]])

(defn heading [text]
  [:h1 {:class "text-3xl font-bold"} text])

(defn recipe-select []
  (let [recipes (re-frame/subscribe [::subs/recipes])]
    [:div {:class "space-y-3"}
     [heading "Select a recipe"]
     (->>
      @recipes
      (map (fn [[recipe-key recipe]]
             [recipe-list-item recipe-key recipe]))
      (into [:ul {:class "grid md:grid-cols-2 lg:grid-cols-3 gap-3"}]))]))

(defn recipe-title [recipe]
  [heading (::recipe/name recipe)])

(defn recipe-cancel-button []
  [button {:on-click #(re-frame/dispatch [::events/select-recipe nil])} "Cancel"])

(defn next-step-button []
  (let [step @(re-frame/subscribe [::subs/current-step])]
    (if (contains? #{:step.type/start :step.type/prompt} (:step/type step))
      [button {:on-click #(re-frame/dispatch [::events/next-step])} (or (:step/next-button-text step) "Next Step")])))

(defn recipe-step []
  (let [step @(re-frame/subscribe [::subs/current-step])]
    [:div
     [micro-header "Current Step"]
     [:h2 {:class "text-xl"} (:step/title step)]]))

(defn recipe-progress []
  (let [{:keys [index total]} @(re-frame/subscribe [::subs/recipe-progress])]
    [:div "Step " (str (inc index)) " of " (str total)]))

(defn recipe-session [recipe]
  (let [state (re-frame/subscribe [::subs/recipe-state])
        total-volume @(re-frame/subscribe [::subs/total-volume])]
    [:div {:class "space-y-2"}
     [recipe-cancel-button]
     [recipe-title recipe]
     [recipe-step]
     [micro-header "Progress"]
     [recipe-progress]
     [:div
      [micro-header "Liquid weight"]
      [:p {:class "text-2xl"}
       (str (js/Math.round (:volume @state))) "g"
       [:span {:class "text-base text-gray-500"} "/" total-volume "g"]]]
     [:div
      [micro-header "Time"]
      [:p {:class "text-lg"} (str (:tick @state)) "s"]]
     [next-step-button]
     #_[:button {:on-click #(re-frame/dispatch [::events/tick])} "Tick"]
     ]))

(defn home-panel []
  (let [recipe (re-frame/subscribe [::subs/selected-recipe])]
    [:div {:class "container mx-auto px-4 pt-6"}
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
