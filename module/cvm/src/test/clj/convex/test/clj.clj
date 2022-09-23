(ns convex.test.clj

  "Testing `convex.clj`."

  {:author "Adam Helinski"}

  (:require [clojure.test :as T]
            [convex.cell  :as $.cell]
            [convex.clj   :as $.clj]))


;;;;;;;;;;


(T/deftest any

  (let [cell ($.cell/address 42)]
    (T/is (= 42
             ($.clj/address cell)
             ($.clj/any cell))
          "Address"))

  (let [ba   (byte-array [1 2 3])
        cell ($.cell/blob ba)]
    (T/is (= (seq ba)
             (seq ($.clj/blob cell))
             (seq ($.clj/any cell)))
          "Blob"))

  (let [cell ($.cell/* true)]
    (T/is (= true
             ($.clj/boolean cell)
             ($.clj/any cell))
          "Boolean"))

  (let [cell ($.cell/byte 42)]
    (T/is (= 42
             ($.clj/byte cell)
             ($.clj/any cell))
          "Byte"))

  (let [cell ($.cell/* \a)]
    (T/is (= \a
             ($.clj/char cell)
             ($.clj/any cell))
          "Char"))

  (let [cell ($.cell/* 42.24)]
    (T/is (= 42.24
             ($.clj/double cell)
             ($.clj/any cell))
          "Double"))

  (let [cell ($.cell/* :foo)]
    (T/is (= :foo
             ($.clj/keyword cell)
             ($.clj/any cell))
          "Keyword"))

  (let [cell ($.cell/* (:a :b))]
    (T/is (= '(:a :b)
             ($.clj/list cell)
             ($.clj/any cell))
          "List"))

  (let [cell ($.cell/* 42)]
    (T/is (= 42
             ($.clj/long cell)
             ($.clj/any cell))
          "Long"))

  (let [cell ($.cell/* {:a :b})]
    (T/is (= {:a :b}
             ($.clj/map cell)
             ($.clj/any cell))
          "Map"))

  (let [cell ($.cell/* #{:a :b})]
    (T/is (= #{:a :b}
             ($.clj/set cell)
             ($.clj/any cell))
          "Set"))

  (let [cell ($.cell/* "test")]
    (T/is (= "test"
             ($.clj/string cell)
             ($.clj/any cell))
          "String"))

  (let [cell ($.cell/* test)]
    (T/is (= 'test
             ($.clj/symbol cell)
             ($.clj/any cell))
          "Symbol"))

  (let [cell ($.cell/syntax ($.cell/* 42))]
    (T/is (= {:meta  {}
              :value 42}
             ($.clj/syntax cell)
             ($.clj/any cell))
          "Syntax without meta"))

  (let [cell ($.cell/syntax ($.cell/* 42)
                            ($.cell/* {:a :b}))]
    (T/is (= {:meta  {:a :b}
              :value 42}
             ($.clj/syntax cell)
             ($.clj/any cell))
          "Syntax with meta"))

  (let [cell ($.cell/* [:a :b])]
    (T/is (= [:a :b]
             ($.clj/vector cell)
             ($.clj/any cell))
          "Vector")))



(T/deftest blob->hex

  (let [b ($.cell/blob (byte-array 4))
        h  "00000000"]
    (T/is (= h
             ($.clj/blob->hex b))
          "Blob converted to hex string")
    (T/is (= b
             ($.cell/blob<-hex h))
          "Hex string is correct")))
