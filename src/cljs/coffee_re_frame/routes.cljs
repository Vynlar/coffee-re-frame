(ns coffee-re-frame.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import [goog History]
           [goog.history EventType])
  (:require
   [secretary.core :as secretary]
   [goog.events :as gevents]
   [re-frame.core :as re-frame]
   [coffee-re-frame.events :as events]
   ))

(defn hook-browser-navigation! []
  (doto (History.)
    (gevents/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token ^js event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  ;; --------------------
  ;; define routes here
  (defroute "/" []
    (re-frame/dispatch [::events/set-active-panel :home-panel]))

  (defroute "/brew/:recipe-key/:volume" [recipe-key volume]
    (re-frame/dispatch [::events/select-recipe (keyword recipe-key) volume])
    (re-frame/dispatch [::events/set-active-panel :brew-panel]))

  (defroute "/setup/:recipe-key" [recipe-key]
    (re-frame/dispatch [::events/select-recipe (keyword recipe-key)])
    (re-frame/dispatch [::events/set-active-panel :setup-panel]))


  ;; --------------------
  (hook-browser-navigation!))
