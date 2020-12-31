(ns coffee-re-frame.effects
  (:require
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
