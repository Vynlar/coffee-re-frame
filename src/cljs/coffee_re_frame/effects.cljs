(ns coffee-re-frame.effects
  (:require
   [clojure.walk :refer [keywordize-keys]]
   [re-frame.core :as re-frame]))

(defn create-timer-handler [dispatch set-interval clear-interval]
  (let [state (atom {})]
    (fn [{:keys [action id interval event]}]
      (if (= action :start)
        (do
          (when (get @state id)
            (clear-interval (get @state id)))
          (swap! state assoc id (set-interval #(dispatch event) interval)))
        (do
          (clear-interval (get @state id))
          (swap! state dissoc id))))))

(re-frame/reg-fx
 :interval
 (create-timer-handler re-frame/dispatch js/setInterval js/clearInterval))

(defn local-storage-coeffect [coeffects key]
  (assoc coeffects :local-storage
         (let [result (js->clj (.parse js/JSON (.getItem js/localStorage (name key))))]
           (if (or (map? result) (vector? result))
             (keywordize-keys result)
             result))))

(re-frame/reg-cofx :local-storage local-storage-coeffect)

(defn local-storage-effect [[_ key value]]
  (.setItem js/localStorage (name key) (.stringify js/JSON (clj->js value))))

(re-frame/reg-fx :local-storage local-storage-effect)

(defn vibrate-effect [duration]
  (. js/navigator vibrate duration))

(re-frame/reg-fx :vibrate vibrate-effect)
