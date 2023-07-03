(ns facetious-nocturn.server-test
  (:require [clojure.test :refer :all]
            [facetious-nocturn.server :refer :all]))

(deftest test-server
  (is (build-app)))