(ns coffee-re-frame.engine
  (:require
   [coffee-re-frame.db :as db]
   [coffee-re-frame.recipe :as recipe]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [re-frame.core :as re-frame]))

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
     :fx [(if (should-advance new-db) [:dispatch [::next-step]] nil)]}))

(re-frame/reg-event-fx
 ::tick
 handle-tick)

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 ::recipes
 (fn [db _]
   (into {}
         (for [[recipe-key recipe] (:recipes db)]
           [recipe-key (assoc recipe :total-volume (recipe/get-total-volume recipe))]))))

(re-frame/reg-sub
 ::recipe-state
 (fn [db _]
   (:recipe-state db)))

(re-frame/reg-sub
 ::current-step
 :<- [::selected-recipe]
 :<- [::recipe-state]
 (fn [[recipe {:keys [step-index]}] _]
   (get (::recipe/steps recipe) step-index)))

(re-frame/reg-sub
 ::next-step
 :<- [::selected-recipe]
 :<- [::recipe-state]
 (fn [[recipe {:keys [step-index]}] _]
   (get (::recipe/steps recipe) (inc step-index))))

(re-frame/reg-sub
 ::selected-recipe
 (fn [{:keys [recipes selected-recipe]} _]
   (if selected-recipe
     (selected-recipe recipes)
     nil)))

(re-frame/reg-sub
 ::selected-recipe-key
 (fn [{:keys [selected-recipe]}]
   selected-recipe))

(re-frame/reg-sub
 ::total-step-count
 :<- [::selected-recipe]
 (fn [recipe _]
   (count (::recipe/steps recipe))))

(re-frame/reg-sub
 ::step-index
 (fn [db _]
   (get-in db [:recipe-state :step-index])))

(re-frame/reg-sub
 ::recipe-progress
 :<- [::step-index]
 :<- [::total-step-count]
 (fn [[index total] _]
   {:index index
    :total total}))

(re-frame/reg-sub
 ::remaining-seconds-in-step
 :<- [::current-step]
 :<- [::recipe-state]
 (fn [[current-step recipe-state] _]
   (let [seconds-in-step (:step/duration current-step)
         current-step-tick (:current-step-tick recipe-state)]
     (- seconds-in-step current-step-tick))))

(re-frame/reg-sub
 ::total-volume
 (fn [_] [(re-frame/subscribe [::selected-recipe])])
 (fn [[recipe] _]
   (recipe/get-total-volume recipe)))
