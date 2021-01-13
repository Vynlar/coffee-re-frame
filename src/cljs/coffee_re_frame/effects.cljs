(ns coffee-re-frame.effects
  (:require
   [clojure.walk :refer [keywordize-keys]]
   [re-frame.core :as re-frame]))

(re-frame/reg-fx
 :interval
 (let [state (atom {})]
   (fn [{:keys [action id interval event]}]
     (if (= action :start)
       (swap! state assoc id (js/setInterval #(re-frame/dispatch event) interval))
       (do
         (js/clearInterval (get @state id))
         (swap! state dissoc id))))))

(defn local-storage-coeffect [coeffects key]
  (assoc coeffects :local-storage
         (let [result (js->clj (.parse js/JSON (.getItem js/localStorage (name key))))]
           (if (or (map? result) (vector? result))
             (keywordize-keys result)
             result))))

(re-frame/reg-cofx
 :local-storage local-storage-coeffect)

(defn local-storage-effect [[_ key value]]
  (.setItem js/localStorage (name key) (.stringify js/JSON (clj->js value))))

(re-frame/reg-fx
 :local-storage local-storage-effect)
