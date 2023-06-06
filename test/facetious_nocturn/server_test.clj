(ns facetious-nocturn.server-test
  (:require [clojure.test :refer :all]
            [facetious-nocturn.server :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(deftest test-server
  (is (build-app)))