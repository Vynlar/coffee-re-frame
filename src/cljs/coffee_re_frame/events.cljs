(ns coffee-re-frame.events
  (:require
   [coffee-re-frame.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [re-frame.core :as re-frame]
   [cljs.core.async :refer [go]]
   [cljs.core.async.interop :refer-macros [<p!]]))

(def last-size-key "lastSize")

(re-frame/reg-event-fx
 ::initialize-db
 [(re-frame/inject-cofx :local-storage [:last-size last-size-key])]
 (fn-traced [{:keys [last-size]} _]
            {:db
             (cond-> db/default-db
               true (assoc-in [:recipe-setup :last-size] last-size)
               (some? last-size) (assoc-in [:recipe-setup :volume] last-size))}))

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
            (assoc db :active-panel active-panel)))

;; Recipe setup

(defn maybe-set-custom [{:keys [volume max-volume type] :as state}]
  (if (and (= type :normal) (>= volume max-volume))
    (-> state
        (assoc :type :custom))
    state))
(defn set-volume [state volume]
  (assoc state :volume (max 0 volume)))

(defn update-volume [{:keys [volume max-volume type] :as state} new-volume]
  (-> state
      (set-volume new-volume)
      maybe-set-custom))

(re-frame/reg-event-db
 :recipe-setup/set-volume
 (fn-traced [db [_ new-volume]]
            (update db :recipe-setup update-volume new-volume)))

(re-frame/reg-event-db
 :recipe-setup/increment-volume
 (fn-traced [db [_ increment]]
            (let [old-volume (get-in db [:recipe-setup :volume])]
              (update db :recipe-setup update-volume (+ old-volume increment)))))

(re-frame/reg-event-fx
 :recipe-setup/save-last-size
 (fn-traced [{:keys [db]} _]
            (let [new-last-size (get-in db [:recipe-setup :volume])]
              {:local-storage [last-size-key new-last-size]
               :db (assoc-in db [:recipe-setup :last-size] new-last-size)})))

(re-frame/reg-event-fx
 :recipe-setup/start-wakelock
 (fn-traced [_]
            {:wakelock :lock}))

(re-frame/reg-event-fx
 :recipe-setup/stop-wakelock
 (fn-traced [_]
            {:wakelock :unlock}))

(re-frame/reg-event-db
 :recipe-setup/make-custom
 (fn-traced [db _]
            (assoc-in db [:recipe-setup :type] :custom)))

(re-frame/reg-sub
 :recipe-setup/volume-type
 (fn [db _]
   (get-in db [:recipe-setup :type])))

(re-frame/reg-sub
 :recipe-setup/volume
 (fn [db _]
   (get-in db [:recipe-setup :volume])))

(re-frame/reg-sub
 :recipe-setup/max-volume
 (fn [db _]
   (get-in db [:recipe-setup :max-volume])))

(re-frame/reg-sub
 :recipe-setup/quick-options
 (fn [db _]
   (let [last-size (get-in db [:recipe-setup :last-size])]
     (cond-> db
       true (get-in [:recipe-setup :quick-options])
       (some? last-size) (conj [last-size (str "Last Size (" last-size "ml)")])))))

(comment
  (re-frame/dispatch [:recipe-setup/increment-volume -50])
  (re-frame/dispatch [:recipe-setup/set-volume 300]))
