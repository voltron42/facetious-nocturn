(defproject facetious-nocturn "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [org.clojure/tools.logging "1.3.0"]
                 [org.clojure/core.async "1.6.681"]
                 [org.flatland/ordered "1.15.12"]
                 [clj-commons/secretary "1.2.4"]
                 [clj-time "0.15.2"]
                 [http-kit "2.8.0"]
                 [environ "1.2.0"]
                 [metosin/compojure-api "1.1.14"]
                 [ring/ring-mock "0.4.0"]
                 [org.sqids/sqids-clojure "1.0.15"]
                 [luposlip/json-schema "0.4.5"]]
  :min-lein-version "2.0.0"
  :main facetious-nocturn.server/-main
  :plugins [[environ/environ.lein "0.3.1"]
            [lein-ancient "0.6.15"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "facetious-nocturn.jar"
  :resource-paths ["resources"]
  :profiles {:production {:env {:production true}
                          :resource-paths ["resources"]}})
