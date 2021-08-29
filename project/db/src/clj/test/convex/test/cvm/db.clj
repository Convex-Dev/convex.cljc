(ns convex.test.cvm.db

  "Testing `convex.cvm.db`."

  {:author "Adam Helinski"}

  (:import (etch EtchStore))
  (:require [clojure.test  :as T]
            [convex.db     :as $.db]
            [convex.cvm.db :as $.cvm.db]))


;;;;;;;;;; Setup


(def custom
     ($.db/open-temp))


;;;;;;;;;; Tests


(T/deftest default

  (T/is (instance? EtchStore
                   ($.cvm.db/default))))



(T/deftest global

  (T/is (= ($.cvm.db/default)
           ($.cvm.db/global))
        "Defaults to default db")

  (T/is (= custom
           ($.cvm.db/global-set custom))
        "Set to custom")

  (T/is (= custom
           ($.cvm.db/global))
        "Custom"))



(T/deftest local

  ;; Returns false because `convex.test.db` runs before this namespace and sets local databses.
  ;;
  ;; (T/is (= ($.cvm.db/default)
  ;;          ($.cvm.db/local))
  ;;       "Defaults to default db")

  (T/is (= custom
           ($.cvm.db/local-set custom))
        "Set to custom")

  (T/is (= custom
           ($.cvm.db/local))
        "Custom"))
