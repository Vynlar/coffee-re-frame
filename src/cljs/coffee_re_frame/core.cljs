(ns coffee-re-frame.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [coffee-re-frame.events :as events]
   [coffee-re-frame.effects :as effects]
   [coffee-re-frame.routes :as routes]
   [coffee-re-frame.views :as views]
   [coffee-re-frame.config :as config]
   ))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (routes/app-routes)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
