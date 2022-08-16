(ns convex.test.cell

  "Testing `convex.cell`."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [*])
  (:require [clojure.test :as T]
            [convex.cell  :as $.cell]
            [convex.read  :as $.read]))


;;;;;;;;;; Printing


(T/deftest print'

  (T/is (= "#cvx #42"
           (pr-str ($.cell/address 42))))

  (T/is (= "#cvx 0x010203"
           (pr-str ($.cell/blob (byte-array [1 2 3])))))

  (T/is (= "#cvx true"
           (pr-str ($.cell/boolean true))))

  (T/is (= "#cvx 42"
           (pr-str ($.cell/byte 42))))

  (T/is (= "#cvx \\a"
           (pr-str ($.cell/char \a))))
  
  (T/is (= "#cvx 42.24"
           (pr-str ($.cell/double 42.24))))
  
  (T/is (= "#cvx :test"
           (pr-str ($.cell/keyword "test"))))
  
  (T/is (= "#cvx (:a)"
           (pr-str ($.cell/* (:a)))))
  
  (T/is (= "#cvx 42"
           (pr-str ($.cell/long 42))))
  
  (T/is (= "#cvx {:a :b}"
           (pr-str ($.cell/* {:a :b}))))
  
  (T/is (= "#cvx #{:a}"
           (pr-str ($.cell/* #{:a}))))
  
  (T/is (= "#cvx \"test\""
           (pr-str ($.cell/string "test"))))
  
  (T/is (= "#cvx test"
           (pr-str ($.cell/symbol "test"))))

  (T/is (= "#cvx ^{:a :b} 42"
           (pr-str ($.cell/syntax ($.cell/* 42)
                                  ($.cell/* {:a :b})))))
  
  (T/is (= "#cvx [:a]"
           (pr-str ($.cell/* [:a])))))


;;;;;;;;;; Conversions


(T/deftest *

  (T/is (= ($.read/string "[:a #42]")
           ($.cell/* [:a ~($.cell/address 42)]))
        "Unquoting leaves objects as is")

  (T/is (= ($.read/string ":foo")
           ($.cell/* :foo))
        "Keyword")

  (T/is (= ($.read/string "(conj [2] :foo)")
           ($.cell/* (conj [2]
                           :foo)))
        "List")

  (T/is (= ($.read/string "{:a :b}")
           ($.cell/* {:a :b}))
        "Map")

  (T/is (= ($.read/string "#{:a :b}")
           ($.cell/* #{:a :b}))
        "Set")

  (T/is (= ($.read/string "[:a :b]")
           ($.cell/* [:a :b]))
        "Vector")

  (T/is (= ($.read/string "foo")
           ($.cell/* foo))
        "Symbol")

  (T/is (= ($.read/string "true")
           ($.cell/* true))
        "Boolean")

  (T/is (= ($.read/string "\\c")
           ($.cell/* \c))
        "Char")

  (T/is (= ($.read/string "42.42")
           ($.cell/* 42.42))
        "Double")

  (T/is (= ($.read/string "42")
           ($.cell/* 42))
        "Long")

  (T/is (= ($.read/string "\"foo\"")
           ($.cell/* "foo"))
        "String"))



(T/deftest any

  (T/is (= ($.read/string "#42")
           ($.cell/any ($.cell/address 42)))
        "By default, returns object as is")

  (T/is (= ($.read/string "(+ 2 :foo)")
           ($.cell/any (cons '+
                             [2 :foo])))
        "Seq")

  (T/is (= ($.read/string ":foo")
           ($.cell/any :foo))
        "Keyword")

  (T/is (= ($.read/string "(+ 2 :foo)")
           ($.cell/any '(+ 2 :foo)))
        "List")

  (T/is (= ($.read/string "{:a :b}")
           ($.cell/any {:a :b}))
        "Map")

  (T/is (= ($.read/string "#{:a :b}")
           ($.cell/any #{:a :b}))
        "Set")

  (T/is (= ($.read/string "[:a :b]")
           ($.cell/any [:a :b]))
        "Vector")

  (T/is (= ($.read/string "foo")
           ($.cell/any 'foo))
        "Symbol")

  (T/is (= ($.read/string "true")
           ($.cell/any true))
        "Boolean")

  (T/is (= ($.read/string "\\c")
           ($.cell/any \c))
        "Char")

  (T/is (= ($.read/string "42.42")
           ($.cell/any 42.42))
        "Double")

  (T/is (= ($.read/string "42")
           ($.cell/any 42))
        "Long")

  (T/is (= ($.read/string "\"foo\"")
           ($.cell/any "foo"))
        "String"))
