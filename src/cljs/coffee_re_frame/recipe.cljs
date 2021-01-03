(ns coffee-re-frame.recipe
  (:require
   [clojure.spec.alpha :as s]))

;; Step
(s/def :step/type keyword?)
(s/def :step/title string?)
(s/def :step/description string?)
(s/def :step/duration pos-int?)
(s/def :step/timer #{:start :stop})

(defmulti step-type :step/type)
(defmethod step-type :step.type/start [_]
  (s/keys :req [:step/type :step/title :step/description]
          :opt [:step/timer]))

(defmethod step-type :step.type/fixed [_]
  (s/keys :req [:step/type :step/title :step/description :step/duration]
          :opt [:step/timer]))

(defmethod step-type :step.type/prompt [_]
  (s/keys :req [:step/type :step/title :step/description]
          :opt [:step/timer]))

(defmethod step-type :step.type/end [_]
  (s/keys :req [:step/type :step/title :step/description]
          :opt [:step/timer]))

;; Recipe
(s/def ::name string?)
(s/def ::steps (s/coll-of ::step))
(s/def ::step (s/multi-spec step-type :step/type))
(s/def ::recipe (s/keys :req [::name ::steps]))

(defn create-v60-recipe [total-volume]
  {::name "v60"
   ::steps [{:step/type :step.type/start
             :step/title "Prepare"
             :step/description "Place your filter into the v60 brewer. Do not yet add any coffee."}

            {:step/type :step.type/prompt
             :step/title "Rinse filter"
             :step/description "Boil water. Rinse filter with a generous amount of boiled water. Discard the water."}

            {:step/type :step.type/prompt
             :step/title "Wet grounds"
             :step/description "Add coffee to the filter and use your finger to create a small hole in the center of the coffee. Then add 60g of water and immediately swirl until all ground are wet."
             :step/volume (* total-volume (/ 30 250))}

            {:step/type :step.type/fixed
             :step/title "Bloom"
             :step/description "Wait for coffee to bloom, releasing CO2."
             :step/duration 30
             :step/timer :start}

            {:step/type :step.type/fixed
             :step/title "First pour"
             :step/description "Pour water at a strong, even rate, swirling from the center ourwards. Water should enter the grounds with force but not splash."
             :step/volume (* total-volume (/ 120 250))
             :step/duration 30}

            {:step/type :step.type/fixed
             :step/title "Second pour"
             :step/description "Continue to pour but now more gently."
             :step/volume (* total-volume (/ 100 250))
             :step/duration 30}

            {:step/type :step.type/prompt
             :step/title "Stir + swirl"
             :step/description "Stir coffee and grounds with a spoon one turn in both directions, avoiding long-lasting swirling. Then pick up vessel and brewer and gently swirl several times."}

            {:step/type :step.type/prompt
             :step/title "Draw-down"
             :step/description "Wait for water level to meet the top of the grounds."}

            {:step/type :step.type/end
             :step/title "Enjoy"
             :step/description "Discard coffee grounds and enjoy."
             :step/timer :stop}]})

;; TODO write tests for this
(defn get-total-volume [recipe]
  (let [steps (::steps recipe)]
    (reduce + (map (fn [{:step/keys [volume]}] (or volume 0)) steps))))

(s/valid? ::recipe (create-v60-recipe 250))
(s/explain ::recipe (create-v60-recipe 250))
