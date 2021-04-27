(ns coffee-re-frame.pages.recipe-brew
  (:require
   [re-frame.core :as re-frame]
   [coffee-re-frame.recipe :as recipe]
   [coffee-re-frame.engine :as engine]
   [coffee-re-frame.utils :as utils]
   [coffee-re-frame.components :as c]))

(defn recipe-title [recipe]
  [:h1 (::recipe/name recipe)])

(defn recipe-header [recipe]
  [:div {:class "h-12 flex items-center justify-center bg-gray-900 border-b border-gray-700"}
   [:div {:class "absolute left-4"}
    [c/home-button]]
   [recipe-title recipe]])

(defn next-step-panel []
  (let [step @(re-frame/subscribe [::engine/current-step])
        next-step @(re-frame/subscribe [::engine/next-step])
        remaining-seconds @(re-frame/subscribe [::engine/remaining-seconds-in-step])]
    [:div {:class "p-3"}
     (cond
       (contains? #{:step.type/start :step.type/prompt} (:step/type step))
       [:button {:class "bg-blue-600 py-3 px-4 text-white flex flex-col w-full rounded focus:outline-none focus:ring focus:ring-blue-400 focus:bg-blue-700"
                 :on-click #(re-frame/dispatch [::engine/next-step])}
        [c/micro-header {:variant :light} "Continue to"]
        (or (:step/title next-step) "Done")]

       (contains? #{:step.type/end} (:step/type step))
       [:a {:class "bg-blue-600 py-3 px-4 text-white flex flex-col w-full rounded focus:outline-none focus:ring focus:ring-blue-400 focus:bg-blue-700"
            :href "#/"}
        [c/micro-header {:variant :light} "Recipe complete"]
        "Start over"]

       true
       [:div {:class "bg-gray-600 py-3 px-4 text-white flex flex-col w-full rounded"}
        [c/micro-header (str "Up next in " remaining-seconds)]
        (or (:step/title next-step) "Done")])]))

(defn recipe-progress []
  (let [{:keys [index total]} @(re-frame/subscribe [::engine/recipe-progress])]
    [c/micro-header (str "Step " (str (inc index)) " of " (str total))]))

(defn recipe-step []
  (let [step @(re-frame/subscribe [::engine/current-step])]
    [:div
     [:div {:class "px-4 py-3 bg-gray-800 border-b border-gray-700"}
      [recipe-progress]
      [:h2 {:class "text-xl"}
       (:step/title step)]]
     [:div {:class "px-4 py-3"}
      (:step/description step)]
     (if (:step/note step)
       [:div {:class "px-4 py-3"}
        [:p {:class "italic inline"}
         "NOTE:\u00A0"]
        [:p {:class "inline"}
         (:step/note step)]])]))

(defn liquid-timer []
  (let [state (re-frame/subscribe [::engine/recipe-state])
        total-volume @(re-frame/subscribe [::engine/total-volume])
        current-step (re-frame/subscribe [::engine/current-step])]
    [:div {:class "bg-white text-blue-600 p-4 pb-5"}
     (if (= (:step/display @current-step) :step.display/time)
       [:div
        [c/micro-header {:variant :dark} "Brew time"]
        [:p {:class "text-5xl font-bold"}
         (utils/format-time (:tick @state))]]

       [:div
        [c/micro-header {:variant :dark} "Liquid weight"]
        [:p {:class "text-5xl font-bold"}
         (str (js/Math.round (:volume @state))) "g"
         [:span {:class "text-base font-normal text-gray-600"} "/" total-volume "g"]]])]))

(defn recipe-session [recipe]
  [:div {:class "lg:w-96 lg:h-96 lg:mx-auto relative"}
   [:div {:class "flex flex-col h-full"}
    [recipe-header recipe]
    [liquid-timer]
    [recipe-step]
    [:div {:class "fixed w-full bottom-0"}
     [next-step-panel]]]])

(defn panel []
  (let [recipe (re-frame/subscribe [::engine/selected-recipe])]
    [c/container [recipe-session @recipe]]))
