(ns coffee-re-frame.db
  (:require
   [coffee-re-frame.recipe :as recipe]))

(def default-db
  {:selected-recipe nil
   :recipes {:v60 recipe/v60}
   :recipe-state nil})

(def default-recipe-state {:tick 0
                           :step-index 0
                           :current-step-tick 0
                           :volume 0})
