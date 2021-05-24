(ns convex.lisp.gen

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [boolean
                            char
                            double
                            keyword
                            list
                            long
                            map
                            set
                            symbol
                            vector])
  (:require [clojure.core]
            [clojure.set]
            [clojure.string]
            [clojure.test.check.generators :as TC.gen]
            [convex.lisp.form              :as $.form]))


;;;;;;;;;;


(def address

  ""

  (TC.gen/fmap $.form/address
               (TC.gen/large-integer* {:min 0})))



(def boolean

  ""

  TC.gen/boolean)



(def char

  ""

  TC.gen/char)



(def double

  ""

  TC.gen/double)



(def long

  ""

  TC.gen/large-integer)



(def number

  ""

  (TC.gen/one-of [double
                  long]))



(defn number-bounded

  ""

  [option+]

  (TC.gen/one-of [(TC.gen/double* option+)
                  (TC.gen/large-integer* option+)]))



(def nothing

  ""

  (TC.gen/return nil))



(def hex-digit

  ""

  ;; TODO. Case insensitive.

  (TC.gen/elements [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \A \B \C \D \E \F]))



(def hex-string

  ""

  (TC.gen/fmap (fn [digit+]
                 (let [n (count digit+)]
                   (clojure.string/join (if (even? n)
                                          digit+
                                          (conj digit+
                                                (nth digit+
                                                     (rand-int n)))))))
               (TC.gen/vector hex-digit)))



(def hex-string-8

  "Compatible with addresses."

  (TC.gen/fmap (fn [[x x+]]
                 (clojure.string/join (cons x
                                            x+)))
               (TC.gen/tuple (TC.gen/elements [\0 \1 \2 \3 \4 \5 \6 \7])
                             (TC.gen/vector hex-digit
                                            15))))



(def hex-string-32

  ""

  (TC.gen/fmap clojure.string/join
               (TC.gen/vector hex-digit
                              64)))



(def blob

  ""

  (TC.gen/fmap $.form/blob
               hex-string))



(def blob-8

  ""

  (TC.gen/fmap $.form/blob
               hex-string-8))



(def blob-32

  ""

  (TC.gen/fmap $.form/blob
               hex-string-32))




(def string
  
  ""

  ;; TOOD. Not only alphanumeric, but Convex does not escape all special chars yet.

  TC.gen/string-alphanumeric)



(def string-symbolic

  ""

  ;; TODO. Not only alphanumeric.

  (TC.gen/fmap (fn [[ch-1 ch+]]
                 (clojure.string/join (cons ch-1
                                            ch+)))
               (TC.gen/tuple TC.gen/char-alpha
                             (TC.gen/vector TC.gen/char-alphanumeric
                                            0
                                            63))))



(def keyword

  ""

  (TC.gen/fmap clojure.core/keyword
               string-symbolic))



(defn quoted

  ""

  [gen]

  (TC.gen/fmap #(clojure.core/list 'quote
                                   %)
               gen))



(def symbol

  ""

  (TC.gen/fmap clojure.core/symbol
               string-symbolic))



(def symbol-ns

  ""

  (TC.gen/fmap (fn [[namespace- name-]]
                 (clojure.core/symbol namespace-
                                      name-))
               (TC.gen/tuple (TC.gen/one-of [(TC.gen/fmap str
                                                          address)
                                              string-symbolic])
                             string-symbolic)))



(def symbol-quoted

  ""

  (quoted symbol))



(def symbol-ns-quoted

  ""

  (quoted symbol-ns))



(def scalar

  ""

  (TC.gen/one-of [address
                  blob
                  boolean
                  char
                  double
                  keyword
                  long
                  nothing
                  string
                  symbol-quoted
                  symbol-ns-quoted]))



(def recursive

  ""

  (TC.gen/recursive-gen (fn [gen-inner]
                          (let [gen-vector (TC.gen/vector gen-inner)]
                            (TC.gen/one-of [(TC.gen/fmap #(cons 'list
                                                                %)
                                                         gen-vector)
                                            (TC.gen/scale #(quot %
                                                                 2)
                                                          (TC.gen/map gen-inner
                                                                      gen-inner))
                                            (TC.gen/set gen-inner)
                                            gen-vector])))
                        scalar))



(defn ^:no-doc -wrap-in-coll

  ;;

  [x]

  (case (rand-int 4)
    0 (clojure.core/list 'list
                         x)
    1 {x x}
    2 #{x}
    3 [x]))



(def collection

  ""

  (TC.gen/fmap (fn [x]
                 (if (coll? x)
                   (if (seq? x)
                     (if (= (first x)
                            'list)
                       x
                       (-wrap-in-coll x))
                     x)
                   (-wrap-in-coll x)))
               recursive))



(def list

  ""
  
  (TC.gen/fmap (fn [x]
                 (cond
                   (map? x)    (cons 'list
                                     (reduce-kv conj
                                                []
                                                x))
                   (set? x)    (cons 'list
                                     x)
                   (seq? x)    (if (= (first x)
                                      'list)
                                 x
                                 (clojure.core/list 'list
                                                    x))
                   (vector? x) (cons 'list
                                     x)
                   :else       (clojure.core/list 'list
                                                  x)))
               collection))



(defn ^:no-doc -to-map

  ;;

  [sq]

  (into {}
        (clojure.core/map vec)
        (partition 2
                   2
                   sq
                   sq)))



(def map

  ""
  
  (TC.gen/fmap (fn [x]
                 (cond
                   (map? x)    x
                   (set? x)    (-to-map x)
                   (seq? x)    (if (= (first x)
                                      'list)
                                 (-to-map (rest x))
                                 {x x})
                   (vector? x) (-to-map x)
                   :else       {x x}))
               collection))



(def set

  ""
  
  (TC.gen/fmap (fn [x]
                 (cond
                   (map? x)    (reduce-kv conj
                                          #{}
                                          x)
                   (set? x)    x
                   (seq? x)    (if (= (first x)
                                      'list)
                                 (clojure.core/set (rest x))
                                 #{x})
                   (vector? x) (clojure.core/set x)
                   :else       #{x}))
               collection))



(def vector

  ""
  
  (TC.gen/fmap (fn [x]
                 (cond
                   (map? x)    (reduce-kv conj
                                          []
                                          x)
                   (set? x)    (vec x)
                   (seq? x)    (if (= (first x)
                                      'list)
                                 (vec (rest x))
                                 [x])
                   (vector? x) x
                   :else       [x]))
               collection))



(def sequential


  ""

  (TC.gen/one-of [list
                  vector]))



(def any

  ""

  (TC.gen/frequency [[10 recursive]
                     [9  scalar]]))



(defn any-but

  ""

  [exclusion-set]

  (TC.gen/one-of (vec (clojure.set/difference #{address
                                                blob
                                                boolean
                                                char
                                                double
                                                keyword
                                                list
                                                long
                                                nothing
                                                map
                                                set
                                                string
                                                symbol-quoted
                                                symbol-ns-quoted
                                                vector}
                                              exclusion-set))))


;;;;;;;;;;

     
(comment

  (TC.gen/generate (any-but #{vector})
                   200)


  )
