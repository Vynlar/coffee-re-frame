(ns coffee-re-frame.views
  (:require
   [re-frame.core :as re-frame]
   [coffee-re-frame.subs :as subs]
   [coffee-re-frame.views.recipe-list :as recipe-list]
   [coffee-re-frame.views.recipe-setup :as recipe-setup]
   [coffee-re-frame.views.recipe-session :as recipe-session]))

(defn- panels [panel-name]
  (case panel-name
    :home-panel [recipe-list/panel]
    :brew-panel [recipe-session/panel]
    :setup-panel [recipe-setup/panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
