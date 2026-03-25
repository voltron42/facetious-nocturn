(defproject facetious-nocturn "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-jetty-adapter "1.15.3"]
                 [metosin/reitit "0.7.2"]
                 [ring/ring-json "0.5.1"]
                 [org.clojure/core.async "1.7.701"]
                 [org.clojure/data.json "2.5.1"]]
  :main ^:skip-aot facetious-nocturn.server
  :target-path "target/%s"
  :uberjar-name "facetious-nocturn"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
