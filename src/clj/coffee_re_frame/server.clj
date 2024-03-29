(ns coffee-re-frame.server
  (:require [coffee-re-frame.handler :refer [handler]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

 (defn -main [& _args]
   (let [port (or (env :port) 8280)]
     (println "Starting server on port " port)
     (run-jetty handler {:port port :join? false})))
