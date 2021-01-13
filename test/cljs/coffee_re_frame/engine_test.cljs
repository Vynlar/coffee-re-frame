(ns coffee-re-frame.engine-test
  (:require [coffee-re-frame.engine :as engine]
            [coffee-re-frame.db :as db]
            [coffee-re-frame.recipe :as recipe]
            [cljs.test :as t :refer-macros [deftest testing is]]))

(deftest engine-test
  (testing "should-advance"
    (let [test-recipe {::recipe/name "Test Recipe"
                       ::recipe/steps
                       [{:step/type :step.type/start
                         :step/title "Start"}

                        {:step/type :step.type/fixed
                         :step/title "30 seconds"
                         :step/duration 30}]}
          db (-> db/default-db
                 (update :recipes #(assoc % :test test-recipe))
                 (db/select-recipe :test))]

      (is (= (-> db
                 (assoc-in [:recipe-state :step-index] 1)
                 (assoc-in [:recipe-state :current-step-tick] 30)
                 engine/should-advance)
             true))

      (is (= (-> db
                 (assoc-in [:recipe-state :step-index] 1)
                 (assoc-in [:recipe-state :current-step-tick] 29)
                 engine/should-advance)
             false))

      (is (= (-> db
                 (assoc-in [:recipe-state :step-index] 0)
                 (assoc-in [:recipe-state :current-step-tick] 30)
                 engine/should-advance)
             false))))

  (testing "handle-next-step"
    (let [db (-> db/default-db
                 (db/select-recipe :v60)
                 (assoc-in [:recipe-state :current-step-tick] 30))
          result (engine/handle-next-step {:db db} [::engine/next-step])]
      (is (= (get-in result [:db :recipe-state :step-index]) 1))
      (is (= (get-in result [:db :recipe-state :current-step-tick]) 0)))))
