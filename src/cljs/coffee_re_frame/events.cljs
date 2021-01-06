(ns coffee-re-frame.events
  (:require
   [re-frame.core :as re-frame]
   [clojure.core.async :as a]
   [coffee-re-frame.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [coffee-re-frame.recipe :as recipe]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
            (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
 ::select-recipe
 (fn-traced [db [_ recipe-key volume]]
            (-> db
                (assoc-in [:recipes recipe-key] ((get recipe/recipe-constructors recipe-key) volume))
                (db/select-recipe recipe-key))))

(defn update-one-time-volume [db]
  (let [{:step/keys [volume duration]} (db/get-current-step db)]
    (if (and volume (not duration))
      (update-in db [:recipe-state :volume] + volume)
      db)))

;; Advance to next step, resetting necessary state
;; Start/stop timers
(defn handle-next-step [{:keys [db]} _]
  (let [new-db (-> db
                   (update-in [:recipe-state :step-index] inc)
                   (assoc-in [:recipe-state :current-step-tick] 0)
                   update-one-time-volume)
        new-current-step (db/get-current-step new-db)]
    (merge {:db new-db}
           (case (:step/timer new-current-step)
             :start {:interval {:action :start
                                :id :ticker
                                :interval 1000
                                :event [::tick]}}
             :stop {:interval {:action :stop
                               :id :ticker}}
             nil))))

(re-frame/reg-event-fx
 ::next-step
 handle-next-step)


(defn should-advance [db]
  (let [{:step/keys [type duration]} (db/get-current-step db)
        current-step-tick (get-in db [:recipe-state :current-step-tick])]
    (if (= type :step.type/fixed)
      (= current-step-tick duration)
      false)))

(defn increment-ticks [db]
  (-> db
      (update-in [:recipe-state :tick] inc)
      (update-in [:recipe-state :current-step-tick] inc)))

(defn update-incremental-volume [db]
  (let [{:step/keys [volume duration]} (db/get-current-step db)]
    (if (and volume duration)
      (update-in db [:recipe-state :volume] + (/ volume duration))
      db)))

;; Handle timing, sometimes advance to next step
(defn handle-tick [cofx _]
  (let [db (:db cofx)
        new-db (-> db increment-ticks update-incremental-volume)]
    {:db new-db
     :fx [(if (should-advance new-db) [:dispatch [::next-step]])]}))

(re-frame/reg-event-fx
 ::tick
 handle-tick)


#_(defmulti handle-tick get-current-step-type)
#_(defmethod handle-tick :step.type/start [recipe state]
    state)

#_(defmethod handle-tick :step.type/prompt [recipe state]
    (inc-tick state))

#_(defmethod handle-tick :step.type/fixed [recipe state]
    (inc-tick state))

#_(re-frame/reg-event-fx
   ::tick
   (fn [{:keys [db]} _]
     (let [{:keys [selected-recipe recipes recipe-state]} db
           new-recipe-state (handle-tick (selected-recipe recipes) recipe-state)]
       {:db (assoc db :recipe-state new-recipe-state)})))

#_(re-frame/reg-event-fx
   ::next-step
   (fn [{:keys [db]} _]
     (let [{:keys [recipes selected-recipe recipe-state] :as new-db}
           (-> db
               (update-in [:recipe-state :step-index] inc)
               (assoc-in [:recipe-state :step-start-time] (get-in db [:recipe-state :tick])))

           current-step (get-current-step (selected-recipe recipes) recipe-state)]
       (case (:step/timer current-step)
         :start {:db new-db
                 :interval {:action :start
                            :id :ticker
                            :interval 1000
                            :event [::tick]}}
         :stop {:db new-db
                :interval {:action :stop
                           :id :ticker}}
         {:db new-db}
         ))))
