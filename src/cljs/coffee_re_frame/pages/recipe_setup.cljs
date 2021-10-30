(ns coffee-re-frame.pages.recipe-setup
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [coffee-re-frame.engine :as engine]
   [coffee-re-frame.components :as c]
   [coffee-re-frame.storage :as storage]))

(defn parse-number-event [event]
  (js/parseInt (.. event -target -value)))

(defn panel []
  (let [recipe-key @(re-frame/subscribe [::engine/selected-recipe-key])
        state (r/atom {:volume 250})
        max-volume 1000
        last
          (let [n (storage/get-item "lastSize")]
            (if n [(int n) (str n " ml")] nil))]
    (fn []
      (let [volume (:volume @state)]
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
                            :value volume
                            :on-change #(swap! state assoc :volume (parse-number-event %))}]

                   [:p {:class "text-3xl font-bold py-1"} (:volume @state)])
                 [:p {:class "text-sm text-gray-500"} "milliliters"]]
                [:p {:class "italic text-sm text-gray-300"} "250ml is about a cup"]

                [:div {:class "w-full flex flex-row space-x-2"}
                 [:button {:type "button"
                           :class "bg-blue-500 rounded-full w-12 h-12 border border-blue-700 font-bold"
                           :on-click #(swap! state (fn [s]
                                                     (assoc s
                                                            :custom (>= volume max-volume)
                                                            :volume (- volume 50))))}
                          "-50"]
                 [:input {:id "volume"
                          :class "flex-1"
                          :type :range
                          :step 10
                          :min 50
                          :max max-volume
                          :value volume
                          :on-change #(swap! state (fn [s]
                                                     (let [volume (parse-number-event %)]
                                                       (assoc s
                                                              :custom (= volume max-volume)
                                                              :volume volume))))}]
                 [:button {:type "button"
                           :class "bg-blue-500 rounded-full w-12 h-12 border border-blue-700 font-bold"
                           :on-click #(swap! state (fn [s]
                                                     (assoc s
                                                            :custom (>= volume max-volume)
                                                            :volume (+ volume 50))))}
                          "+50"]]

                [:div {:class "space-y-2"}
                 [c/micro-header "Quick select"]
                 [:div {:class "flex space-x-2"}
                  (for [[size label] (keep #(if % % nil) [[250 "1 cup"] [500 "2 cups"] last [max-volume "Custom"]])]
                    ^{:key label}
                    [:button {:class "bg-gray-800 rounded py-2 px-4 border border-gray-700"
                              :on-click #(swap! state assoc :volume size :custom (= size max-volume))
                              :type "button"}
                     label])]]

                [:div {:class "pt-4 w-full"}
                 [:a {:href (str  "#/brew/" (name recipe-key) "/" (:volume @state))
                      :class "bg-blue-500 py-2 px-6 rounded text-center block"} "Next"]]]]))))
