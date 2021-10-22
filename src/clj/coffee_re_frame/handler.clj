(ns coffee-re-frame.handler
  (:require
    [compojure.core :refer [GET defroutes]]
    [compojure.route :refer [resources not-found]]
    [ring.util.response :refer [resource-response]]
    [ring.middleware.reload :refer [wrap-reload]]
    [coffee-re-frame.build-html :refer [get-html]]
    [shadow.http.push-state :as push-state]))

(defroutes routes
  (GET "/" [] (get-html))
  (resources "/")
  (not-found "Page not found"))

(def dev-handler (-> #'routes wrap-reload push-state/handle))

(def handler routes)
