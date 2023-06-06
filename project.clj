(defproject facetious-nocturn "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.flatland/ordered "1.5.7"]
                 [clj-commons/secretary "1.2.4"]
                 [http-kit "2.3.0"]
                 [environ "1.1.0"]
                 [metosin/compojure-api "1.1.11"]
                 [ring/ring-mock "0.4.0"]
                 [hiccup "1.0.5"]
                 [org.clojars.pallix/batik "1.7.0"]]
  :min-lein-version "2.0.0"
  :main facetious-nocturn.server/-main
  :plugins [[environ/environ.lein "0.3.1"]
            [lein-ancient "0.6.15"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "facetious-nocturn.jar"
  :resource-paths ["resources"]
  :profiles {:production {:env {:production true}
                          :resource-paths ["resources"]}})
