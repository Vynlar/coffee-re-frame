(ns coffee-re-frame.pages.recipe-setup
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [coffee-re-frame.components :as c]))

(defn parse-number-event [event]
  (js/parseInt (.. event -target -value)))

(defn panel []
  (let [recipe-key @(re-frame/subscribe [::engine/selected-recipe-key])
        state (r/atom {:volume 250})
        max-volume 1000]
    (fn []
      [c/container
       [:form {:class "flex flex-col p-4 space-y-2"}
        [:div {:class "pb-6"}
         [c/home-button]]
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
