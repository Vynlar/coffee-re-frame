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
 (fn-traced [db [_ recipe-key]]
            (-> db
                (assoc :selected-recipe recipe-key)
                (assoc :recipe-state db/default-recipe-state))))

(defn inc-tick [state]
  (let [new-state
        (-> state
            (update :tick inc)
            (update :current-step-tick inc))
        volume (-> state :current-step :step/volume)
        duration (-> state :current-step :step/duration)]
    (if (and duration volume)
      (update new-state :volume (fn [old-volume] (+ old-volume (/ volume duration))))
      new-state)))

(defn get-current-step [recipe {:keys [step-index]}]
  (get
   (::recipe/steps recipe)
   step-index))

;; Take the re-frame state, look up the current recipe and step, merge those into the recipe state,
;; Then call a function that takes an event type and that map and returns a map of the new recipe state and
;; a list of actions (start or stop)

(defn to-intermediate-state [{:keys [recipe-state recipes selected-recipe]}]
  (let [recipe (selected-recipe recipes)
        step (get-current-step recipe recipe-state)]
    (merge recipe-state
           {:current-step step
            :recipe recipe})))

(defn update-current-step [state]
  (assoc state :current-step (get-current-step (:recipe state) state)))

(defn update-volume [{:keys [volume current-step] :as state}]
  (if (and (= (:step/duration current-step) nil) (not= (:step/volume current-step) nil))
    (update state :volume #(+ % (:step/volume current-step)))
    state))

(defn advance-step [state]
  (-> state
      (update :step-index inc)
      (assoc :current-step-tick 0)
      update-current-step
      update-volume))

(defn check-fixed-condition [{:keys [current-step current-step-tick] :as state}]
  (if (>= current-step-tick (:step/duration current-step))
    (advance-step state)
    state))

(defn handle-transition [state action]
  (case [action (-> state :current-step :step/type)]
    [:next :step.type/start] (advance-step state)
    [:next :step.type/prompt] (advance-step state)
    [:tick :step.type/prompt] (inc-tick state)
    [:tick :step.type/fixed] (-> state
                                 inc-tick
                                 check-fixed-condition)
    state))

(defn from-intermediate-state [state]
  (dissoc state :current-step :recipe))

(defn transition-event-handler [action]
  (fn [{:keys [db]} _]
    (let [new-recipe-state (->
                            db
                            to-intermediate-state
                            (handle-transition action)
                            from-intermediate-state)
          new-db (assoc db :recipe-state new-recipe-state)
          current-step (get-current-step ((:selected-recipe db) (:recipes db)) new-recipe-state)]
      (if (= (:current-step-tick new-recipe-state) 0)
        (cond
          (= (:step/timer current-step) :start)
          {:db new-db
           :interval {:action :start
                      :id :ticker
                      :interval 1000
                      :event [::tick]}}


          (= (:step/timer current-step) :stop)
          {:db new-db
           :interval {:action :stop
                      :id :ticker}}

          true
          {:db new-db}
          )
        {:db new-db}))))

(re-frame/reg-event-fx
 ::next-step
 (transition-event-handler :next))

(re-frame/reg-event-fx
 ::tick
 (transition-event-handler :tick))


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
