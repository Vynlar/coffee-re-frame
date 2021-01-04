(ns coffee-re-frame.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :refer [class-names]]
   [coffee-re-frame.subs :as subs]
   [coffee-re-frame.events :as events]
   [coffee-re-frame.recipe :as recipe]
   ))


;; home

(defn micro-header
  ([text] (micro-header {:variant :light} text))
  ([{:keys [variant]} text]
   (let [classes (case variant
                   :light "text-white opacity-80"
                   :dark "text-black opacity-80")]
     [:p {:class (class-names "uppercase text-xs tracking-wider" classes)} text])))

(defn recipe-list-item [recipe-key recipe]
  [:li {:class "p-4 bg-gray-800 border border-gray-600 rounded space-y-2"}
   [:div
    [micro-header "Name"]
    [:h2 {:class "font-bold"} (::recipe/name recipe)]]
   [:div
    [micro-header "Volume"]
    [:p (:total-volume recipe) "g"]]

   [:button {:on-click #(re-frame/dispatch [::events/select-recipe recipe-key])
             :class "px-3 bg-blue-600 text-white py-1 rounded"}
    "Start Brew"]])

(defn recipe-select []
  (let [recipes (re-frame/subscribe [::subs/recipes])]
    [:div {:class "space-y-3 p-4"}
     [:h1 {:class "text-3xl font-bold"} "Select recipe"]
     (->>
      @recipes
      (map (fn [[recipe-key recipe]]
             [recipe-list-item recipe-key recipe]))
      (into [:ul {:class "grid md:grid-cols-2 lg:grid-cols-3 gap-3"}]))]))

(defn recipe-title [recipe]
  [:h1 (::recipe/name recipe)])

(defn recipe-cancel-button []
  [:button {:class "text-xs tracking-wide"
            :on-click #(re-frame/dispatch [::events/select-recipe nil])} "Back"])

(defn recipe-header [recipe]
  [:div {:class "h-12 flex items-center justify-center bg-gray-900 border-b border-gray-700"}
   [:div {:class "absolute left-4"}
    [recipe-cancel-button]]
   [recipe-title recipe]])

(defn next-step-panel []
  (let [step @(re-frame/subscribe [::subs/current-step])
        next-step @(re-frame/subscribe [::subs/next-step])
        remaining-seconds @(re-frame/subscribe [::subs/remaining-seconds-in-step])]
    [:div {:class "p-3"}
     (if (contains? #{:step.type/start :step.type/prompt} (:step/type step))
       [:button {:class "bg-blue-600 py-3 px-4 text-white flex flex-col w-full rounded focus:outline-none focus:ring focus:ring-blue-400 focus:bg-blue-700"
                 :on-click #(re-frame/dispatch [::events/next-step])}
        [micro-header {:variant :light} "Continue to"]
        (or (:step/title next-step) "Done")]

       [:div {:class "bg-gray-600 py-3 px-4 text-white flex flex-col w-full rounded"}
        [micro-header (str "Up next in " remaining-seconds)]
        (or (:step/title next-step) "Done")])]))

(defn recipe-progress []
  (let [{:keys [index total]} @(re-frame/subscribe [::subs/recipe-progress])]
    [micro-header (str "Step " (str (inc index)) " of " (str total))]))

(defn recipe-step []
  (let [step @(re-frame/subscribe [::subs/current-step])]
    [:div
     [:div {:class "px-4 py-3 bg-gray-800 border-b border-gray-700"}
      [recipe-progress]
      [:h2 {:class "text-xl"}
       (:step/title step)]]
     [:div {:class "px-4 py-3"}
      (:step/description step)]]))

(defn liquid-timer []
  (let [state (re-frame/subscribe [::subs/recipe-state])
        total-volume @(re-frame/subscribe [::subs/total-volume])]
    [:div {:class "bg-white text-blue-600 p-4 pb-5"}
     [micro-header {:variant :dark} "Liquid weight"]
     [:p {:class "text-5xl font-bold"}
      (str (js/Math.round (:volume @state))) "g"
      [:span {:class "text-base font-normal text-gray-600"} "/" total-volume "g"]]]))

(defn recipe-session [recipe]
  [:div {:class "lg:w-96 lg:h-96 lg:mx-auto relative"}
   [:div {:class "flex flex-col h-screen"}
    [recipe-header recipe]
    [liquid-timer]
    [recipe-step]
    [:div {:class "mt-auto"}
     [next-step-panel]]]])

(defn home-panel []
  (let [recipe (re-frame/subscribe [::subs/selected-recipe])]
    [:div {:class "mx-auto bg-gray-900 text-white min-h-screen"}
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
