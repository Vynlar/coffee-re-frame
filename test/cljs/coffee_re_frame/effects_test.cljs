(ns coffee-re-frame.effects-test
  (:require [coffee-re-frame.effects :as effects]
            [cljs.test :as t :refer-macros [deftest testing is]]))

(deftest local-storage-effect
  (let [key :my-key]
    (testing "primitives"
      (.clear js/localStorage)
      (is (= (effects/local-storage-coeffect {} [key key])  {:my-key nil}))
      (effects/local-storage-effect [key "my value"])
      (is (= (effects/local-storage-coeffect {} [key key])  {:my-key "my value"})))

    (testing "vectors"
      (.clear js/localStorage)
      (is (= (effects/local-storage-coeffect {} [key key])  {:my-key nil}))
      (effects/local-storage-effect [key [1 2 3]])
      (is (= (effects/local-storage-coeffect {} [key key])  {:my-key [1 2 3]})))

    (testing "maps"
      (.clear js/localStorage)
      (is (= (effects/local-storage-coeffect {} [key key])  {:my-key nil}))
      (effects/local-storage-effect [key {:key "value" :another "value2"}])
      (is (= (effects/local-storage-coeffect {} [key key])  {:my-key {:key "value" :another "value2"}})))

    (testing "nested structures"
      (.clear js/localStorage)
      (is (= (effects/local-storage-coeffect {} [key key])  {:my-key nil}))
      (effects/local-storage-effect [key {:a {:nested [{:vector 3}]}}])
      (is (= (effects/local-storage-coeffect {} [key key])  {:my-key {:a {:nested [{:vector 3}]}}})))))

(defn setup-interval []
  (let [state (atom {:dispatch [] :setInterval [] :clearInterval []})
        handler (effects/create-timer-handler
                 (fn [& args] (swap! state update :dispatch conj (vec args)))
                 (fn [& args] (swap! state update :setInterval conj (vec args)))
                 (fn [& args] (swap! state update :clearInterval conj (vec args))))]
    {:state state :handler handler}))

(deftest interval-effect
  (testing "starts a timer"
    (let [{:keys [state handler]} (setup-interval)]
      (handler {:action :start
                :id :my-timer
                :interval 1000
                :event [:my-event]})

      (is (= (count (:setInterval @state)) 1))
      (is (= (get-in @state [:setInterval 0 1]) 1000))
      (is (fn? (get-in @state [:setInterval 0 0])))
      (let [dispatcher (get-in @state [:setInterval 0 0])]
        ; Simulate a setInterval event going off
        (dispatcher)
        (is (= (get-in @state [:dispatch 0 0]) [:my-event]))
        (dispatcher)
        (is (= (get-in @state [:dispatch 1 0]) [:my-event])))

      (handler {:action :stop
                :id :my-timer})

      (is (= (count (get-in @state [:clearInterval 0])) 1)))

    (testing "stops old timer if a timer with the same id is started"
      (let [{:keys [state handler]} (setup-interval)]
        (handler {:action :start
                  :id :my-timer
                  :interval 1000
                  :event [:my-event]})

        (is (= (count (:setInterval @state)) 1))

        ;; Create a second timer with the same id
        (handler {:action :start
                  :id :my-timer
                  :interval 1000
                  :event [:my-event]})

        (is (= (count (:clearInterval @state)) 1))
        (is (= (count (:setInterval @state)) 2))

        (handler {:action :stop
                  :id :my-timer})))))
