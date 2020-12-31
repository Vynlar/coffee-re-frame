(ns coffee-re-frame.recipe
  (:require
   [clojure.spec.alpha :as s]))

;; Step
(s/def :step/type keyword?)
(s/def :step/title string?)
(s/def :step/duration pos-int?)
(s/def :step/timer #{:start :stop})

(defmulti step-type :step/type)
(defmethod step-type :step.type/start [_]
  (s/keys :req [:step/type :step/title]
          :opt [:step/timer]))

(defmethod step-type :step.type/fixed [_]
  (s/keys :req [:step/type :step/title :step/duration]
          :opt [:step/timer]))

(defmethod step-type :step.type/prompt [_]
  (s/keys :req [:step/type :step/title]
          :opt [:step/timer]))

(defmethod step-type :step.type/end [_]
  (s/keys :req [:step/type :step/title]
          :opt [:step/timer]))

;; Recipe
(s/def ::name string?)
(s/def ::steps (s/coll-of ::step))
(s/def ::step (s/multi-spec step-type :step/type))
(s/def ::recipe (s/keys :req [::name ::steps]))

(def v60 {::name "v60"
          ::steps [{:step/type :step.type/start
                    :step/title "Get Ready"}

                   {:step/type :step.type/prompt
                    :step/title "Wet the grounds"
                    :step/volume 30}

                   {:step/type :step.type/fixed
                    :step/title "Bloom"
                    :step/duration 5
                    :step/timer :start}

                   {:step/type :step.type/fixed
                    :step/title "First Pour"
                    :step/volume (+ 70 50)
                    :step/duration 5}

                   {:step/type :step.type/fixed
                    :step/title "Second Pour"
                    :step/volume 100
                    :step/duration 5}

                   {:step/type :step.type/prompt
                    :step/title "Stir and swirl"}

                   {:step/type :step.type/prompt
                    :step/title "Drawdown"}

                   {:step/type :step.type/end
                    :step/title "Enjoy"
                    :step/timer :stop}]})

(s/valid? ::recipe v60)
(s/explain ::recipe v60)
