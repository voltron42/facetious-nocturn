(defproject facetious-nocturn "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/core.async "1.6.673"]
                 [org.flatland/ordered "1.5.7"]
                 [clj-commons/secretary "1.2.4"]
                 [clj-time "0.15.2"]
                 [http-kit "2.3.0"]
                 [environ "1.1.0"]
                 [metosin/compojure-api "1.1.11"]
                 [ring/ring-mock "0.4.0"]]
  :min-lein-version "2.0.0"
  :main facetious-nocturn.server/-main
  :plugins [[environ/environ.lein "0.3.1"]
            [lein-ancient "0.6.15"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "facetious-nocturn.jar"
  :resource-paths ["resources"]
  :profiles {:production {:env {:production true}
                          :resource-paths ["resources"]}})
