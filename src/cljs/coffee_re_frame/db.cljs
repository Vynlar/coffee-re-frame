(ns coffee-re-frame.db
  (:require
   [coffee-re-frame.recipe :as recipe]))

(def default-db
  {:selected-recipe nil
   :recipes {}
   :recipe-state nil
   :wake-lock nil
   :recipe-setup {:volume 250
                  :max-volume 1000
                  :quick-options [[250 "1 cup"] [500 "2 cups"]]
                  :last-size nil
                  :type :normal}})

(def default-recipe-state {:tick 0
                           :step-index 0
                           :current-step-tick 0
                           :volume 0})

(defn get-current-recipe [db]
  (get (:recipes db) (:selected-recipe db)))

(defn get-current-step [db]
  (let [{::recipe/keys [steps]} (get-current-recipe db)
        step-index (-> db :recipe-state :step-index)]
    (get steps step-index)))

(defn select-recipe [db recipe-key]
  (-> db
      (assoc :selected-recipe recipe-key)
      (assoc :recipe-state default-recipe-state)))
