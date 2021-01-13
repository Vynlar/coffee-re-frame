(ns coffee-re-frame.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [coffee-re-frame.components :as c]
   [coffee-re-frame.subs :as subs]
   [coffee-re-frame.engine :as engine]
   [coffee-re-frame.views.recipe-list :as recipe-list]
   [coffee-re-frame.recipe :as recipe]))

(defn recipe-title [recipe]
  [:h1 (::recipe/name recipe)])

(defn home-button []
  [:a {:class "text-xs tracking-wide"
       :href "#/"} "Back"])

(defn recipe-header [recipe]
  [:div {:class "h-12 flex items-center justify-center bg-gray-900 border-b border-gray-700"}
   [:div {:class "absolute left-4"}
    [home-button]]
   [recipe-title recipe]])

(defn next-step-panel []
  (let [step @(re-frame/subscribe [::subs/current-step])
        next-step @(re-frame/subscribe [::subs/next-step])
        remaining-seconds @(re-frame/subscribe [::subs/remaining-seconds-in-step])]
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
  (let [{:keys [index total]} @(re-frame/subscribe [::subs/recipe-progress])]
    [c/micro-header (str "Step " (str (inc index)) " of " (str total))]))

(defn recipe-step []
  (let [step @(re-frame/subscribe [::subs/current-step])]
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

(defn format-time [seconds]
  (let [minutes (js/Math.floor (/ seconds 60))
        seconds (rem seconds 60)]
    (str minutes ":" seconds)))

(defn liquid-timer []
  (let [state (re-frame/subscribe [::subs/recipe-state])
        total-volume @(re-frame/subscribe [::subs/total-volume])
        current-step (re-frame/subscribe [::subs/current-step])]
    [:div {:class "bg-white text-blue-600 p-4 pb-5"}
     (if (= (:step/type @current-step) :step.type/end)
       [:div
        [c/micro-header {:variant :dark} "Brew time"]
        [:p {:class "text-5xl font-bold"}
         (format-time (:tick @state))]]

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

(defn home-panel []
  [c/container [recipe-list/recipe-select]])

(defn brew-panel []
  (let [recipe (re-frame/subscribe [::subs/selected-recipe])]
    [c/container [recipe-session @recipe]]))

(defn parse-number-event [event]
  (js/parseInt (.. event -target -value)))

(defn setup-panel []
  (let [recipe-key @(re-frame/subscribe [::subs/selected-recipe-key])
        state (r/atom {:volume 250})
        max-volume 1000]
    (fn []
      [c/container
       [:form {:class "flex flex-col p-4 space-y-2"}
        [:div {:class "pb-6"}
         [home-button]]
        [c/micro-header {:for "volume" :as :label} "How much coffee do you want to make?"]
        [:div {:class "h-16"}
         (if (:custom @state)
           [:input {:id "custom-volume"
                    :class "text-3xl font-bold bg-gray-700 border-none rounded px-2 py-1 w-full"
                    :type :number
                    :value (:volume @state)
                    :on-change #(swap! state assoc :volume (parse-number-event %))}]

           [:p {:class "text-3xl font-bold py-1"} (:volume @state)])
         [:p {:class "text-sm text-gray-500"} "milliliters"]]
        [:p {:class "italic text-sm text-gray-300"} "250ml is about a cup"]

        [:input {:id "volume"
                 :type :range
                 :step 10
                 :min 50
                 :max max-volume
                 :value (:volume @state)
                 :on-change #(swap! state (fn [s]
                                            (let [volume (parse-number-event %)]
                                              (assoc s
                                                     :custom (= volume max-volume)
                                                     :volume volume))))}]
        [:div {:class "space-y-2"}
         [c/micro-header "Quick select"]
         [:div {:class "flex space-x-2"}
          (for [[size label] [[250 "1 cup"] [500 "2 cups"] [max-volume "Custom"]]]
            [:button {:class "bg-gray-800 rounded py-2 px-4 border border-gray-700"
                      :on-click #(swap! state assoc :volume size :custom (= size max-volume))}
             label])]]

        [:div {:class "pt-4 w-full"}
         [:a {:href (str  "#/brew/" (name recipe-key) "/" (:volume @state))
              :class "bg-blue-500 py-2 px-6 rounded text-center block"} "Next"]]]])))


;; main


(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :brew-panel [brew-panel]
    :setup-panel [setup-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
