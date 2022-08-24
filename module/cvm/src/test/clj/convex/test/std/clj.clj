(ns convex.test.std.clj

  "Checks which Clojure standard functions works with cells.

   Following useful functions from https://clojure.org/api/cheatsheet

   Comments:
   
   - Blobs cannot be converted to seqs
   - Almost all seq-related functions works since other types are converted to seqs
   - `reduce-kv` does not work on map-like cells
   - Clojure data can contain cells but cells cannot contain Clojure data"

  {:author "Adam Helinski"}

  (:require [clojure.test :as T]
            [convex.cell  :as $.cell]
            [convex.std   :as $.std]))


;;;;;;;;;; Misc


(T/deftest contains?'

  (T/is (true? (contains? ($.cell/* {:a :b})
                          ($.cell/* :a))))

  (T/is (false? (contains? ($.cell/* {:a :b})
                           ($.cell/* :c))))

  (T/is (true? (contains? ($.cell/* #{:a})
                          ($.cell/* :a))))

  (T/is (false? (contains? ($.cell/* #{:a})
                           ($.cell/* :b))))

  (let [k ($.cell/blob (byte-array [0]))]
    (T/is (true? (contains? ($.cell/blob-map [[k
                                               ($.cell/* :a)]])
                            k))))

  (T/is (false? (contains? ($.cell/blob-map [[($.cell/blob (byte-array [0]))
                                              ($.cell/* :a)]])
                           ($.cell/blob (byte-array [1]))))))


;;;;;;;;;; Reduce


(T/deftest reduce'

  (T/is (= ($.cell/* [1 2 3])
           (reduce $.std/conj
                   ($.cell/vector)
                   ($.cell/* (1 2 3))))))


;;;;;;;;;; Seq


(T/deftest keys'

  (T/is (= [($.cell/* :a)]
           (keys ($.cell/* {:a :b}))))

  (let [k ($.cell/blob (byte-array 1))]
    (T/is (= [k]
             (keys ($.cell/blob-map [[k
                                      ($.cell/* :a)]]))))))

(T/deftest seq'

  (T/is (seq? (seq ($.cell/* (:a)))))

  (T/is (= (seq [($.cell/* :a)])
           (seq ($.cell/* (:a)))))

  (T/is (seq? (seq ($.cell/* [:a]))))

  (T/is (= (seq [($.cell/* :a)])
           (seq ($.cell/* [:a]))))

  (T/is (seq? (seq ($.cell/* {:a :b}))))

  (T/is (= (seq [($.cell/* [:a :b])])
           (seq ($.cell/* {:a :b}))))

  (T/is (seq? (seq ($.cell/* #{:a}))))

  (T/is (= (seq [($.cell/* :a)])
           (seq ($.cell/* #{:a}))))

  ; (T/is (seq? (seq ($.cell/blob (byte-array 1)))))

  (T/is (seq? (seq ($.cell/blob-map [[($.cell/blob (byte-array 1))
                                      ($.cell/* :a)]]))))

  (let [k ($.cell/blob (byte-array 1))]
    (T/is (= (seq [($.cell/* [~k
                              :a])])
             (seq ($.cell/blob-map [[k
                                    ( $.cell/* :a)]]))))))



(T/deftest vals'

  (T/is (= [($.cell/* :b)]
           (vals ($.cell/* {:a :b}))))

  (T/is (= [($.cell/* :a)]
           (vals ($.cell/blob-map [[($.cell/blob (byte-array 1))
                                    ($.cell/* :a)]])))))


;;;;;;;;;; Transducers


(T/deftest sequence'

  (T/is (= [($.cell/* 43)]
           (sequence (map $.std/inc)
                     ($.cell/* (42)))))

  (T/is (= [($.cell/* 43)]
           (sequence (map $.std/inc)
                     ($.cell/* [42]))))

  (T/is (= [($.cell/* 43)]
           (sequence (map (fn [[_k v]]
                            ($.std/inc v)))
                     ($.cell/* {:a 42}))))

  (T/is (= [($.cell/* 43)]
           (sequence (map $.std/inc)
                     ($.cell/* [42]))))

  (T/is (= [($.cell/* 43)]
           (sequence (map $.std/inc)
                     ($.cell/* #{42}))))

  ; (T/is (= [($.cell/* 42)]
  ;          (sequence (map identity)
  ;                    ($.cell/blob (byte-array [42])))))

  (T/is (= [($.cell/* 43)]
           (sequence (map (fn [[_k v]]
                            ($.std/inc v)))
                     ($.cell/blob-map [[($.cell/blob (byte-array [0]))
                                        ($.cell/* 42)]])))))



(T/deftest transduce'

  (T/is (= ($.cell/* [2 3 4])
           (transduce (map $.std/inc)
                      $.std/conj
                      ($.cell/vector)
                      ($.cell/* [1 2 3])))))
