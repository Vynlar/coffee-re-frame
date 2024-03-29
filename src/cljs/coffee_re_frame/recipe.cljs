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

(defn create-v60-single-recipe [total-volume]
  (let [coffee-weight (js/Math.floor (* total-volume 0.06))]
    (def water-portion (* coffee-weight (/ 10 3)))
    {::name "Hario v60"
     ::steps [{:step/type :step.type/start
               :step/title "Prepare"
               :step/description (str "Boil about " (* 2 total-volume) "ml of water. Use some of the water to pre-heat your V60 brewer, without the filter. Grind " coffee-weight "g of coffee, set it aside for later. Place your filter into the v60 brewer.")}

              {:step/type :step.type/prompt
               :step/title "Rinse filter"
               :step/description "Rinse filter with a generous amount of boiled water. Discard the water in the filter."}

              {:step/type :step.type/prompt
               :step/title "Add grounds"
               :step/description (str "Add " coffee-weight "g coffee to the filter and use your finger to create a small hole in the center of the coffee. ")}

              {:step/type :step.type/fixed
               :step/title "Wet grounds"
               :step/description (str "Gently pour " water-portion "ml of water into the middle of the gounds, spiraling outwards ensuring all grounds are wet.")
               :step/volume (* total-volume (/ 50 250))
               :step/duration 15
               :step/timer :start}

              {:step/type :step.type/fixed
               :step/title "Bloom"
               :step/description "Gently swirl the entire countainer to ensure grounds are wet, and wait for coffee to bloom, releasing CO2."
               :step/duration 30}

              {:step/type :step.type/fixed
               :step/title "first pour"
               :step/description "pour water at a strong, even rate, swirling from the center outwards. water should enter the grounds with force but not splash."
               :step/volume (* total-volume (/ 50 250))
               :step/duration 15}
              
              {:step/type :step.type/fixed
               :step/title "1st Wait"
               :step/description "Let the coffee rest"
               :step/duration 10}

              {:step/type :step.type/fixed
               :step/title "Second pour"
               :step/description "Continue to pour"
               :step/volume (* total-volume (/ 50 250))
               :step/duration 10}
              
              {:step/type :step.type/fixed
               :step/title "2nd Wait"
               :step/description "Let the coffee rest"
               :step/duration 10}
              
              {:step/type :step.type/fixed
               :step/title "Third pour"
               :step/description "Continue to pour"
               :step/volume (* total-volume (/ 50 250))
               :step/duration 10}
              
              {:step/type :step.type/fixed
               :step/title "3rd Wait"
               :step/description "Let the coffee rest"
               :step/duration 10}

              {:step/type :step.type/fixed
               :step/title "Final pour"
               :step/description "Continue to pour"
               :step/volume (* total-volume (/ 50 250))
               :step/duration 10}

              {:step/type :step.type/prompt
               :step/title "Stir + swirl"
               :step/description "Stir coffee and grounds with a spoon one turn in both directions, avoiding long-lasting swirling. Then pick up vessel and brewer and gently swirl several times."}

              {:step/type :step.type/prompt
               :step/title "Draw-down"
               :step/display :step.display/time
               :step/description "Wait for water level to meet the top of the grounds."}

              {:step/type :step.type/end
               :step/title "Enjoy"
               :step/description "Discard coffee grounds and enjoy."
               :step/display :step.display/time
               :step/timer :stop}]}))

(defn create-v60-recipe [total-volume]
  (let [coffee-weight (js/Math.floor (* total-volume 0.06))]
    {::name "Hario v60 (Multi-Cup)"
     ::steps [{:step/type :step.type/start
               :step/title "Prepare"
               :step/description (str "Grind " coffee-weight "g of coffee, set it aside for later. Place your filter into the v60 brewer.")}

              {:step/type :step.type/prompt
               :step/title "Rinse filter"
               :step/description (str "Boil about " (* 1.5 total-volume) "ml of water. Rinse filter with a generous amount of boiled water. Discard the water in the filter.")}

              {:step/type :step.type/prompt
               :step/title "Wet grounds"
               :step/description (str "Add " coffee-weight "g coffee to the filter and use your finger to create a small hole in the center of the coffee. Then add " (* coffee-weight 2) "g of water and immediately swirl until all ground are wet.")
               :step/note (str "A little more water is fine, but don't go above 3 times your grind weight (" (* coffee-weight 3) "g).")
               :step/volume (* total-volume (/ 30 250))}

              {:step/type :step.type/fixed
               :step/title "Bloom"
               :step/description "Wait for coffee to bloom, releasing CO2."
               :step/duration 30
               :step/timer :start}

              {:step/type :step.type/fixed
               :step/title "First pour"
               :step/description "Pour water at a strong, even rate, swirling from the center outwards. Water should enter the grounds with force but not splash."
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
               :step/display :step.display/time
               :step/description "Wait for water level to meet the top of the grounds."}

              {:step/type :step.type/end
               :step/title "Enjoy"
               :step/description "Discard coffee grounds and enjoy."
               :step/display :step.display/time
               :step/timer :stop}]}))

(def recipe-constructors
  {:v60-single create-v60-single-recipe
  :v60 create-v60-recipe})

(defn get-total-volume [recipe]
  (let [steps (::steps recipe)]
    (reduce + (map (fn [{:step/keys [volume]}] (or volume 0)) steps))))

(s/valid? ::recipe (create-v60-recipe 250))
(s/explain ::recipe (create-v60-recipe 250))
