(ns coffee-re-frame.recipe
  (:require
   [clojure.spec.alpha :as s]))

;; Step
(s/def :step/type keyword?)
(s/def :step/title string?)
(s/def :step/duration pos-int?)
(s/def :step/next-button-text string?)
(s/def :step/timer #{:start :stop})

(defmulti step-type :step/type)
(defmethod step-type :step.type/start [_]
  (s/keys :req [:step/type :step/title]
          :opt [:step/timer :step/next-button-text]))

(defmethod step-type :step.type/fixed [_]
  (s/keys :req [:step/type :step/title :step/duration]
          :opt [:step/timer]))

(defmethod step-type :step.type/prompt [_]
  (s/keys :req [:step/type :step/title]
          :opt [:step/timer :step/next-button-text]))

(defmethod step-type :step.type/end [_]
  (s/keys :req [:step/type :step/title]
          :opt [:step/timer]))

;; Recipe
(s/def ::name string?)
(s/def ::steps (s/coll-of ::step))
(s/def ::step (s/multi-spec step-type :step/type))
(s/def ::recipe (s/keys :req [::name ::steps]))

(defn create-v60-recipe [total-volume]
  {::name "v60"
   ::steps [{:step/type :step.type/start
             :step/title "Get Ready"
             :step/next-button-text "Begin"}

            {:step/type :step.type/prompt
             :step/title "Wet the grounds"
             :step/volume (* total-volume (/ 30 250))}

            {:step/type :step.type/fixed
             :step/title "Bloom"
             :step/duration 30
             :step/timer :start}

            {:step/type :step.type/fixed
             :step/title "First Pour"
             :step/volume (* total-volume (/ 120 250))
             :step/duration 30}

            {:step/type :step.type/fixed
             :step/title "Second Pour"
             :step/volume (* total-volume (/ 100 250))
             :step/duration 30}

            {:step/type :step.type/prompt
             :step/title "Stir and swirl"}

            {:step/type :step.type/prompt
             :step/title "Drawdown"}

            {:step/type :step.type/end
             :step/title "Enjoy"
             :step/timer :stop}]})

;; TODO write tests for this
(defn get-total-volume [recipe]
  (let [steps (::steps recipe)]
    (reduce + (map (fn [{:step/keys [volume]}] (or volume 0)) steps))))

(s/valid? ::recipe (create-v60-recipe 250))
(s/explain ::recipe (create-v60-recipe 250))
