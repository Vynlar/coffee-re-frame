(ns coffee-re-frame.db-test
  (:require [coffee-re-frame.db :as db]
            [coffee-re-frame.recipe :as recipe]
            [cljs.test :as t :refer-macros [deftest testing is]]))


(deftest db-test
  (testing "select-recipe"
    (let [{:keys [recipe-state selected-recipe]} (db/select-recipe db/default-db :v60)]
      (is (= selected-recipe :v60))
      (is (= recipe-state db/default-recipe-state))))

  (testing "get-current-recipe"
    (let [db (db/select-recipe db/default-db :v60)]
      (is (= (db/get-current-recipe db)
             (get-in db [:recipes :v60])))))

  (testing "get-current-step"
    (let [db (db/select-recipe db/default-db :v60)]
      (is (= (db/get-current-step db)
             (get-in db [:recipes :v60 ::recipe/steps 0]))))))
