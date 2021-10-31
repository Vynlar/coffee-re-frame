(ns coffee-re-frame.pages.recipe-setup
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [coffee-re-frame.engine :as engine]
   [coffee-re-frame.components :as c]))

(defn parse-number-event [event]
  (js/parseInt (.. event -target -value)))

(defn panel []
  (let [recipe-key @(re-frame/subscribe [::engine/selected-recipe-key])
        volume (re-frame/subscribe [:recipe-setup/volume])
        volume-type (re-frame/subscribe [:recipe-setup/volume-type])
        max-volume (re-frame/subscribe [:recipe-setup/max-volume])
        quick-options (re-frame/subscribe [:recipe-setup/quick-options])]
    (fn []
      (let []
        [c/container
               [:form {:class "flex flex-col p-4 space-y-2"}
                [:div {:class "pb-6"}
                 [c/home-button]]
                [c/micro-header {:for "volume" :as :label} "How much coffee do you want to make?"]
                [:div {:class "h-16"}
                 (if (= :custom @volume-type)
                   [:input {:id "custom-volume"
                            :class "text-3xl font-bold bg-gray-700 border-none rounded px-2 py-1 w-full"
                            :type :number
                            :value @volume
                            :on-change #(re-frame/dispatch [:recipe-setup/set-volume (parse-number-event %)])}]

                   [:p {:class "text-3xl font-bold py-1"} @volume])
                 [:p {:class "text-sm text-gray-500"} "milliliters"]]
                [:p {:class "italic text-sm text-gray-300"} "250ml is about a cup"]

                [:div {:class "w-full flex flex-row space-x-2"}
                 [:button {:type "button"
                           :class "bg-blue-500 rounded-full w-12 h-12 border border-blue-700 font-bold"
                           :on-click #(re-frame/dispatch [:recipe-setup/increment-volume -50])}
                  "-50"]
                 [:input {:id "volume"
                          :class "flex-1"
                          :type :range
                          :step 10
                          :min 50
                          :max @max-volume
                          :value @volume
                          :on-change #(re-frame/dispatch [:recipe-setup/set-volume (parse-number-event %)])}]
                 [:button {:type "button"
                           :class "bg-blue-500 rounded-full w-12 h-12 border border-blue-700 font-bold"
                           :on-click #(re-frame/dispatch [:recipe-setup/increment-volume 50])}
                          "+50"]]

                [:div {:class "space-y-2"}
                 [c/micro-header "Quick select"]
                 [:div {:class "grid grid-cols-2 gap-2"}
                  (for [[size label] @quick-options]
                    ^{:key label}
                    [:button {:class "bg-gray-800 rounded py-2 px-4 border border-gray-700"
                              :on-click #(re-frame/dispatch [:recipe-setup/set-volume size])
                              :type "button"}
                     label])

                  [:button {:class "bg-gray-800 rounded py-2 px-4 border border-gray-700"
                            :on-click #(re-frame/dispatch [:recipe-setup/make-custom])
                            :type "button"}
                   "Custom"]]]



                [:div {:class "pt-4 w-full"}
                 [:a {:href (str  "#/brew/" (name recipe-key) "/" @volume)
                      :class "bg-blue-500 py-2 px-6 rounded text-center block"
                      :on-click #(re-frame/dispatch [:recipe-setup/save-last-size])}
                  "Next"]]]]))))
