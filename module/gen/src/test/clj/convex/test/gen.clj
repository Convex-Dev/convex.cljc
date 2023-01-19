(ns convex.test.gen

  "Testing `convex.gen`."

  {:author "Adam Helinski"}

  (:require [clojure.test                  :as T]
            [clojure.test.check.generators :as TC.gen]
            [convex.gen                    :as $.gen]
            [convex.std                    :as $.std]))


;;;;;;;;;; Helpers


(defn gen

  "Returns true if the generated value passes the given `predicate`."

  [predicate gen]

  (T/is (predicate (TC.gen/generate gen))))


;;;;;;;;;; Tests


(T/deftest cell

  (gen $.std/address?
       $.gen/address)

  (gen $.std/blob?
       ($.gen/blob))

  (gen $.std/blob?
       ($.gen/blob 2))

  (gen $.std/blob?
       ($.gen/blob 2
                   5))

  (gen $.std/blob?
       $.gen/blob-32)


  (gen $.std/boolean?
       $.gen/boolean)

  (gen $.std/char?
       $.gen/char)

  (gen $.std/char?
       $.gen/char-alphanum)

  (gen $.std/double?
       $.gen/double)

  (gen $.std/keyword?
       $.gen/keyword)

  (gen $.std/long?
       $.gen/long)

  (gen $.std/number?
       $.gen/number)

  (gen nil?
       $.gen/nothing)

  (gen $.std/string?
       ($.gen/string))

  (gen $.std/string?
       ($.gen/string 5))

  (gen $.std/string?
       ($.gen/string 5
                     10))

  (gen $.std/string?
       ($.gen/string-alphanum))

  (gen $.std/string?
       ($.gen/string-alphanum 5))

  (gen $.std/string?
       ($.gen/string-alphanum 5
                              10))

  (gen $.std/symbol?
       $.gen/symbol)

  (gen (comp not
             $.std/coll?)
       $.gen/scalar)

  (gen $.std/list?
       ($.gen/list $.gen/long))

  (gen $.std/blob-map?
       ($.gen/blob-map $.gen/address
                       $.gen/long))

  (gen $.std/map?
       ($.gen/map $.gen/long
                  $.gen/long))

  (gen $.std/set?
       ($.gen/set $.gen/long))

  (gen $.std/vector?
       ($.gen/vector $.gen/long))

  (gen $.std/cell?
       $.gen/recursive)

  (gen $.std/cell?
       $.gen/any)

  (gen $.std/list?
       $.gen/any-list)

  (gen $.std/map?
       $.gen/any-map)

  (gen $.std/set?
       $.gen/any-set)

  (gen $.std/vector?
       $.gen/any-vector)

  (gen $.std/syntax?
       ($.gen/syntax)))
