(ns convex.test.cvm.db

  "Testing `convex.cvm.db`."

  {:author "Adam Helinski"}

  (:require [clojure.test  :as T]
            [convex.db     :as $.db]
            [convex.cvm.db :as $.cvm.db]))


;;;;;;;;;; Setup


(def custom
     ($.db/open-temp))



(T/use-fixtures :once
                (fn [f]
                  (let [original ($.cvm.db/global)]
                    (f)
                    ($.cvm.db/local-set original)
                    ($.cvm.db/global-set original))))


;;;;;;;;;; Tests


(T/deftest global

  (T/is (= custom
           ($.cvm.db/global-set custom))
        "Set to custom")

  (T/is (= custom
           ($.cvm.db/global))
        "Custom"))



(T/deftest local

  (T/is (= custom
           ($.cvm.db/local-set custom))
        "Set to custom")

  (T/is (= custom
           ($.cvm.db/local))
        "Custom")

  (T/is (let [temp ($.db/open-temp)]
          (= temp
             ($.cvm.db/local-with temp
               (+ 2
                  2)
               ($.cvm.db/local))))
        "Macro"))

