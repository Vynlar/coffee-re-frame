(ns coffee-re-frame.build-html
  (:require [clojure.java.io :as io]
            [hiccup.core :refer [html]]))

(def index-html-resource (io/resource "public/index.html"))

(defn manifest []
  (read-string (slurp (io/resource "public/js/compiled/manifest.edn"))))

(defn get-module [manifest module-id]
  (first (filter #(= (:module-id %) module-id) manifest)))

(defn module->output-name [module]
  (:output-name module))

(defn get-output-name [manifest module-id]
  (->
   manifest
   (get-module module-id)
   module->output-name))

(defn page []
  [:html
   {:lang "en"}
   [:head
    [:meta {:charset "utf-8"}]
    [:meta
     {:content "width=device-width,initial-scale=1", :name "viewport"}]
    [:link
     {:media "screen"
      :type "text/css"
      :href "/css/main.css"
      :rel "stylesheet"}]
    [:script
     {:src "https://plausible.aleixandre.dev/js/plausible.js"
      :data-domain "carafe.aleixandre.dev"
      :defer "defer"
      :async "async"}]
    [:title "Carafe"]]
   [:body.h-full.touch-manipulation
    [:noscript
     "\n      coffee-re-frame is a JavaScript app. Please enable JavaScript to continue.\n    "]
    [:div#app.h-full]
    [:script {:src (str "js/compiled/" (get-output-name (manifest) :app))}]]])

(defn get-html []
  (html (page)))

(defn -main [& args]
  (spit index-html-resource (get-html)))
