(defproject coffee-re-frame "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs "2.11.7"]
                 [reagent "0.10.0"]
                 [re-frame "1.1.2"]
                 [day8.re-frame/tracing "0.6.0"]
                 [clj-commons/secretary "1.2.4"]
                 [compojure "1.6.2"]
                 [yogthos/config "1.1.7"]
                 [ring "1.8.2"]
                 [hiccup "1.0.5"]
                 [re-frame-utils "0.1.0"]]

  :plugins [[lein-shadow "0.3.1"]
            [cider/cider-nrepl "0.27.2"]
            [lein-pdo "0.1.1"]
            [lein-shell "0.5.0"]]

  :min-lein-version "2.9.0"

  :jvm-opts ["-Xmx1G"]

  :source-paths ["src/clj" "src/cljs"]

  :resource-paths ["resources"]

  :test-paths   ["test/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :shadow-cljs {:nrepl {:port 8777}

                :builds {:app {:target :browser
                               :output-dir "resources/public/js/compiled"
                               :asset-path "/js/compiled"
                               :module-hash-names true
                               :modules {:app {:init-fn coffee-re-frame.core/init
                                               :preloads [devtools.preload
                                                          day8.re-frame-10x.preload]}}
                               :dev {:compiler-options {:closure-defines {re-frame.trace.trace-enabled? true
                                                                          day8.re-frame.tracing.trace-enabled? true}}}
                               :release {:build-options
                                         {:ns-aliases
                                          {day8.re-frame.tracing day8.re-frame.tracing-stubs}}}

                               :devtools {:watch-dir "resources/public"}
                               #_#_:devtools {:http-root "resources/public"
                                              :http-port 8280
                                              :http-handler coffee-re-frame.handler/dev-handler}}
                         :browser-test
                         {:target :browser-test
                          :ns-regexp "-test$"
                          :runner-ns shadow.test.browser
                          :test-dir "target/browser-test"
                          :devtools {:http-root "target/browser-test"
                                     :http-port 8290}}

                         :karma-test
                         {:target :karma
                          :ns-regexp "-test$"
                          :output-to "target/karma-test.js"}}}

  :shell {:commands {"karma" {:windows         ["cmd" "/c" "karma"]
                              :default-command "karma"}
                     "open"  {:windows         ["cmd" "/c" "start"]
                              :macosx          "open"
                              :linux           "xdg-open"}}}

  :aliases {"dev"          ["do"
                            ["shell" "echo" "\"DEPRECATED: Please use lein watch instead.\""]
                            ["watch-shadow"]]

            "watch" ["pdo" "server," "watch-shadow," "tailwind"]

            "watch-shadow"        ["with-profile" "dev" "do"
                                   ["shadow" "watch" "app" "browser-test" "karma-test"]]

            "tailwind" ["shell" "npm" "run" "build:css:watch"]

            "server"       ["run" "-m" "coffee-re-frame.server"]
            "build-html"       ["run" "-m" "coffee-re-frame.build-html"]

            "prod"         ["do"
                            ["shell" "echo" "\"DEPRECATED: Please use lein release instead.\""]
                            ["release"]]

            "release"      ["with-profile" "prod" "do"
                            ["shell" "npm" "run" "build:css"]
                            ["shadow" "release" "app"]]

            "build-report" ["with-profile" "prod" "do"
                            ["shadow" "run" "shadow.cljs.build-report" "app" "target/build-report.html"]
                            ["shell" "open" "target/build-report.html"]]

            "karma"        ["do"
                            ["shell" "echo" "\"DEPRECATED: Please use lein ci instead.\""]
                            ["ci"]]
            "ci"           ["with-profile" "prod" "do"
                            ["shadow" "compile" "karma-test"]
                            ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "1.0.2"]
                   [day8.re-frame/re-frame-10x "0.7.0"]]
    :source-paths ["dev"]}

   :prod {}

   :uberjar {:source-paths ["env/prod/clj"]
             :omit-source  true
             :main         coffee-re-frame.server
             :aot          [coffee-re-frame.server]
             :uberjar-name "coffee-re-frame.jar"
             :prep-tasks   ["compile" ["release"]]}}

  :prep-tasks [])
