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

(defn local-storage-coeffect [coeffects [name-key storage-key]]
  (assoc coeffects name-key
         (let [result (js->clj (.parse js/JSON (.getItem js/localStorage (name storage-key))))]
           (if (or (map? result) (vector? result))
             (keywordize-keys result)
             result))))

(re-frame/reg-cofx :local-storage local-storage-coeffect)

(defn local-storage-effect [[key value]]
  (.setItem js/localStorage (name key) (.stringify js/JSON (clj->js value))))

(re-frame/reg-fx :local-storage local-storage-effect)

(defn vibrate-effect [duration]
  (.vibrate js/navigator duration))

(re-frame/reg-fx :vibrate vibrate-effect)

(def current-lock (atom nil))

(defn lock-brightness []
  (when-let [wakeLock (.-wakeLock js/navigator)]
    (->
     (.request wakeLock "screen")
     (.then (fn [new-lock]
              (js/console.log "Locking screen brightness: " new-lock)
              (reset! current-lock new-lock)))
     (.catch (fn [error]
               (js/console.error "Error locking screen brightness"
                                 error))))))

(defn unlock-brightness []
  (when @current-lock
    (-> (.release @current-lock)
        (.then (fn []
                 (js/console.log "Released wake lock")))
        (.catch (fn [error]
                  (js/console.log "Failed to release wake lock"
                                  error))))))

(defn wakelock-effect [action]
  (case action
    :lock (lock-brightness)
    :unlock (unlock-brightness)
    (throw "error")))

(re-frame/reg-fx :wakelock wakelock-effect)
