(ns convex.test.cell

  "Testing `convex.cell`."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [*])
  (:require [clojure.test :as T]
            [convex.cell  :as $.cell]
            [convex.read  :as $.read]))


;;;;;;;;;;


(T/deftest *

  (T/is (= ($.read/string "[:a #42]")
           ($.cell/* [:a ~($.cell/address 42)]))
        "Unquoting leaves objects as is")

  (T/is (= ($.read/string ":foo")
           ($.cell/* :foo))
        "Keyword")

  (T/is (= ($.read/string "(conj [2] :foo)")
           ($.cell/* (conj [2] :foo)))
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
