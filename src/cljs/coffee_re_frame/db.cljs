(ns coffee-re-frame.db
  (:require
   [coffee-re-frame.recipe :as recipe]))

(def default-db
  {:selected-recipe nil
   :recipes {:v60 (recipe/create-v60-recipe 500)
             :v62 (recipe/create-v60-recipe 250)}
   :recipe-state nil})

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
