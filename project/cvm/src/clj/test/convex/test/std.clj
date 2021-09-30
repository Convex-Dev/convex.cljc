(ns convex.test.std

  "Testing `convex.std`."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [+
                            -
                            *
                            <
                            <=
                            ==
                            >=
                            >
                            assoc
                            concat
                            conj
                            cons
                            contains?
                            count
                            dec
                            dissoc
                            empty
                            empty?
                            find
                            get
                            hash-map
                            hash-set
                            inc
                            into
                            keys
                            list
                            merge
                            mod
                            name
                            next
                            nth
                            reverse
                            str
                            vals
                            vector
                            zero?])
  (:require [clojure.test :as T]
            [convex.cell  :as $.cell]
            [convex.ref   :as $.ref]
            [convex.std   :as $.std]))


;;;;;;;;;; Casts


(T/deftest str

  (T/is (= ($.cell/* "[:a :b]")
           ($.std/str ($.cell/* [:a :b])))))


;;;;;;;;;; Collection constructors


(let [k ($.cell/blob (byte-array [0]))]
  (T/deftest blob-map

    (T/is (= ($.cell/blob-map)
             ($.std/blob-map)))

    (T/is (= ($.cell/blob-map [[k
                                ($.cell/* :a)]])
             ($.std/blob-map k
                             ($.cell/* :a))))

    (T/is (thrown? IllegalArgumentException
                   ($.std/blob-map k)))))



(T/deftest hash-map

  (T/is (= ($.cell/* {})
           ($.std/hash-map)))

  (T/is (= ($.cell/* {:a :b})
           ($.std/hash-map ($.cell/* :a)
                           ($.cell/* :b))))

  (T/is (thrown? IllegalArgumentException
                 ($.std/hash-map ($.cell/* :a)))))



(T/deftest hash-set

  (T/is (= ($.cell/* #{})
           ($.std/hash-set)))

  (T/is (= ($.cell/* #{:a :b})
           ($.std/hash-set ($.cell/* :a)
                           ($.cell/* :b)))))



(T/deftest list

  (T/is (= ($.cell/* ())
           ($.std/list)))

  (T/is (= ($.cell/* (:a :b))
           ($.std/list ($.cell/* :a)
                       ($.cell/* :b)))))

(T/deftest vector

  (T/is (= ($.cell/* [])
           ($.std/vector)))

  (T/is (= ($.cell/* [:a :b])
           ($.std/vector ($.cell/* :a)
                         ($.cell/* :b)))))


;;;;;;;;;; Collection generics


(T/deftest into

  (T/is (let [k ($.cell/blob (byte-array [0]))]
          (= ($.std/blob-map k
                             ($.cell/* :a))
             ($.std/into ($.cell/blob-map)
                         ($.cell/* [[~k
                                     :a]])))))

  (T/is (= ($.cell/* [:foo 2 3 4])
           ($.std/into ($.cell/* [:foo])
                       (map $.std/inc)
                       ($.cell/* (1 2 3))))))



;;;;;;;;;; Comparators


(T/deftest <

  (T/is (true? ($.std/< ($.cell/* 42)
                        ($.cell/* 100))))

  (T/is (false? ($.std/< ($.cell/* 42)
                         ($.cell/* 42))))

  (T/is (false? ($.std/< ($.cell/* 42)
                         ($.cell/* 1))))

  (T/is (true? ($.std/< ($.cell/* 42.0)
                        ($.cell/* 100))))

  (T/is (false? ($.std/< ($.cell/* 42.0)
                         ($.cell/* 42))))

  (T/is (false? ($.std/< ($.cell/* 42.0)
                         ($.cell/* 1)))))



(T/deftest <=

  (T/is (true? ($.std/<= ($.cell/* 42)
                         ($.cell/* 100))))

  (T/is (true? ($.std/<= ($.cell/* 42)
                         ($.cell/* 42))))

  (T/is (false? ($.std/<= ($.cell/* 42)
                          ($.cell/* 1))))

  (T/is (true? ($.std/<= ($.cell/* 42.0)
                         ($.cell/* 100))))

  (T/is (true? ($.std/<= ($.cell/* 42.0)
                         ($.cell/* 42))))

  (T/is (false? ($.std/<= ($.cell/* 42.0)
                          ($.cell/* 1)))))



(T/deftest ==

  (T/is (true? ($.std/== ($.cell/* 42)
                         ($.cell/* 42))))

  (T/is (false? ($.std/== ($.cell/* 1)
                          ($.cell/* 42))))

  (T/is (true? ($.std/== ($.cell/* 42.0)
                         ($.cell/* 42))))

  (T/is (false? ($.std/== ($.cell/* 1.0)
                          ($.cell/* 42)))))



(T/deftest >=

  (T/is (false? ($.std/>= ($.cell/* 42)
                          ($.cell/* 100))))

  (T/is (true? ($.std/>= ($.cell/* 42)
                         ($.cell/* 42))))

  (T/is (true? ($.std/>= ($.cell/* 42)
                         ($.cell/* 1))))

  (T/is (false? ($.std/>= ($.cell/* 42.0)
                          ($.cell/* 100))))

  (T/is (true? ($.std/>= ($.cell/* 42.0)
                         ($.cell/* 42))))

  (T/is (true? ($.std/>= ($.cell/* 42.0)
                         ($.cell/* 1)))))



(T/deftest >

  (T/is (false? ($.std/> ($.cell/* 42)
                         ($.cell/* 100))))

  (T/is (false? ($.std/> ($.cell/* 42)
                         ($.cell/* 42))))

  (T/is (true? ($.std/> ($.cell/* 42)
                        ($.cell/* 1))))

  (T/is (false? ($.std/> ($.cell/* 42.0)
                         ($.cell/* 100))))

  (T/is (false? ($.std/> ($.cell/* 42.0)
                         ($.cell/* 42))))

  (T/is (true? ($.std/> ($.cell/* 42.0)
                        ($.cell/* 1)))))


;;;;;;;;;; Countable


(T/deftest count

  (T/is (= 2
           ($.std/count ($.cell/* (:a :b)))))

  (T/is (= 2
           ($.std/count ($.cell/* [:a :b]))))

  (T/is (= 2
           ($.std/count ($.cell/* {:a :b
                                   :c :d}))))

  (T/is (= 2
           ($.std/count ($.cell/* #{:a :b}))))

  (T/is (= 2
           ($.std/count ($.cell/* "ab"))))

  (T/is (= 2
           ($.std/count ($.cell/blob (byte-array [1 2]))))))



(T/deftest empty?

  (T/is ($.std/empty? ($.cell/* ())))

  (T/is (not ($.std/empty? ($.cell/* (:a)))))

  (T/is ($.std/empty? ($.cell/* [])))

  (T/is (not ($.std/empty? ($.cell/* [:a]))))

  (T/is ($.std/empty? ($.cell/* {})))

  (T/is (not ($.std/empty? ($.cell/* {:a :b}))))

  (T/is ($.std/empty? ($.cell/* #{})))

  (T/is (not ($.std/empty? ($.cell/* #{:a}))))

  (T/is ($.std/empty? ($.cell/* "")))

  (T/is (not ($.std/empty? ($.cell/* "a"))))

  (T/is ($.std/empty? ($.cell/blob (byte-array 0))))

  (T/is (not ($.std/empty? ($.cell/blob (byte-array 1))))))



(let [a ($.cell/* :a)]

  (T/deftest nth
  
    (T/is (= a
             ($.std/nth ($.cell/* (:a :b))
                        0)))

    (T/is (= a
             ($.std/nth ($.cell/* [:a :b])
                        0)))

    (T/is (= ($.cell/* [:a :b])
             ($.std/nth ($.cell/* {:a :b})
                        0)))

    (T/is (= a
             ($.std/nth ($.cell/* #{:a})
                        0)))

    (T/is (= ($.cell/* \a)
             ($.std/nth ($.cell/* "ab")
                        0)))

    (T/is (= ($.cell/byte 1)
             ($.std/nth ($.cell/blob (byte-array [1 2]))
                        0)))))



(let [a ($.cell/* :a)]

  (T/deftest nth-ref
  
    (T/is (= a
             (-> ($.std/nth-ref ($.cell/* (:a :b))
                                0)
                 $.ref/resolve)))

    (T/is (= a
             (-> ($.std/nth-ref ($.cell/* [:a :b])
                                0)
                 $.ref/resolve)))

    (T/is (= ($.cell/* [:a :b])
             (-> ($.std/nth-ref ($.cell/* {:a :b})
                                0)
                 $.ref/resolve)))

    (T/is (= a
             (-> ($.std/nth-ref ($.cell/* #{:a})
                                0)
                 $.ref/resolve)))

    (T/is (= ($.cell/* \a)
             (-> ($.std/nth-ref ($.cell/* "ab")
                                0)
                 $.ref/resolve))

    (T/is (= ($.cell/byte 1)
             (-> ($.std/nth-ref ($.cell/blob (byte-array [1 2]))
                                0)
                 $.ref/resolve))))))


;;;;;;;;;; Data structure


(T/deftest assoc

  (T/is (= ($.cell/* (:b))
           ($.std/assoc ($.cell/* (:a))
                        ($.cell/* 0)
                        ($.cell/* :b))))

  (T/is (= ($.cell/* [:b])
           ($.std/assoc ($.cell/* [:a])
                        ($.cell/* 0)
                        ($.cell/* :b))))

  (T/is (= ($.cell/* {:a :c})
           ($.std/assoc ($.cell/* {:a :b})
                        ($.cell/* :a)
                        ($.cell/* :c))))

  (T/is (= ($.cell/* #{:a :b})
           ($.std/assoc ($.cell/* #{:a})
                        ($.cell/* :b)
                        ($.cell/* true))))

  (T/is (= ($.cell/* #{:a})
           ($.std/assoc ($.cell/* #{:a :b})
                        ($.cell/* :b)
                        ($.cell/* false)))))



(T/deftest conj

  (T/is (= ($.cell/* (:b :a))
           ($.std/conj ($.cell/* (:a))
                       ($.cell/* :b))))

  (T/is (= ($.cell/* [:a :b])
           ($.std/conj ($.cell/* [:a])
                       ($.cell/* :b))))

  (T/is (= ($.cell/* {:a :b
                      :c :d})
           ($.std/conj ($.cell/* {:a :b})
                       ($.cell/* [:c :d]))))
  (T/is (= ($.cell/* #{:a :b})
           ($.std/conj ($.cell/* #{:a})
                       ($.cell/* :b)))))



(T/deftest contains?

  (T/is ($.std/contains? ($.cell/* (:a))
                         ($.cell/* 0)))

  (T/is (not ($.std/contains? ($.cell/* (:a))
                              ($.cell/* 1))))

  (T/is ($.std/contains? ($.cell/* [:a])
                         ($.cell/* 0)))

  (T/is (not ($.std/contains? ($.cell/* [:a])
                              ($.cell/* 1))))

  (T/is ($.std/contains? ($.cell/* {:a :b})
                         ($.cell/* :a)))

  (T/is (not ($.std/contains? ($.cell/* {:a :b})
                              ($.cell/* :c))))

  (T/is ($.std/contains? ($.cell/* #{:a})
                         ($.cell/* :a)))

  (T/is (not ($.std/contains? ($.cell/* #{:a})
                              ($.cell/* :b)))))



(T/deftest empty

  (T/is (= ($.cell/* ())
           ($.std/empty ($.cell/* (:a)))))

  (T/is (= ($.cell/* [])
           ($.std/empty ($.cell/* [:a]))))

  (T/is (= ($.cell/* {})
           ($.std/empty ($.cell/* {:a :b}))))

  (T/is (= ($.cell/* #{})
           ($.std/empty ($.cell/* #{:a})))))



(let [a ($.cell/* :a)
      b ($.cell/* :b)]

  (T/deftest get
  
    (T/is (= a
             ($.std/get ($.cell/* (:a))
                        ($.cell/* 0))))

    (T/is (= a
             ($.std/get ($.cell/* [:a])
                        ($.cell/* 0))))

    (T/is (= b
             ($.std/get ($.cell/* {:a :b})
                        a)))

    (T/is (= nil
             ($.std/get ($.cell/* {:a :b})
                        ($.cell/* :c))))

    (T/is (= ($.cell/* :not-found)
             ($.std/get ($.cell/* {:a :b})
                        ($.cell/* :c)
                        ($.cell/* :not-found))))

    (T/is (= ($.cell/* true)
             ($.std/get ($.cell/* #{:a})
                        a)))

    (T/is (= ($.cell/* false)
             ($.std/get ($.cell/* #{:a})
                        b)))

    (T/is (= ($.cell/* :not-found)
             ($.std/get ($.cell/* #{:a})
                        b
                        ($.cell/* :not-found))))))


;;;;;;;;;; Long


(T/deftest dec

  (T/is (= ($.cell/* 41)
           ($.std/dec ($.cell/* 42)))))



(T/deftest mod

  (T/is (= ($.cell/* 2)
           ($.std/mod ($.cell/* 2)
                      ($.cell/* 5)))))



(T/deftest inc

  (T/is (= ($.cell/* 43)
           ($.std/inc ($.cell/* 42)))))


;;;;;;;;;; Map


(T/deftest dissoc

  (T/is (= ($.cell/* {})
           ($.std/dissoc nil
                         ($.cell/* :a))))

  (T/is (= ($.cell/* {:a :b})
           ($.std/dissoc ($.cell/* {:a :b
                                    :c :d})
                         ($.cell/* :c))))

  (T/is (= ($.cell/blob-map)
           (let [b ($.cell/blob (byte-array [0]))]
             ($.std/dissoc ($.cell/blob-map [[b
                                              ($.cell/* :a)]])
                           b)))))



(T/deftest find

  (T/is (nil? ($.std/find nil
                          ($.cell/* :a))))

  (T/is (nil? ($.std/find ($.cell/* {:a :b})
                          ($.cell/* :c))))

  (T/is (= ($.cell/* [:a :b])
           ($.std/find ($.cell/* {:a :b})
                       ($.cell/* :a))))

  (T/is (nil? ($.std/find ($.cell/blob-map [[($.cell/blob (byte-array [0]))
                                             ($.cell/* :a)]])
                          ($.cell/blob (byte-array [1])))))

  (let [k ($.cell/blob (byte-array [0]))]
    (T/is (= ($.cell/* [~k
                        ~($.cell/* :a)])
             ($.std/find ($.cell/blob-map [[k
                                            ($.cell/* :a)]])
                         k)))))



(T/deftest keys

  (T/is (= ($.cell/* [])
           ($.std/keys ($.cell/* {}))))

  (T/is (= ($.cell/* [:a])
           ($.std/keys ($.cell/* {:a :b}))))

  (T/is (= ($.cell/* [])
           ($.std/keys ($.cell/blob-map))))

  (let [k ($.cell/blob (byte-array [0]))]
    (T/is (= ($.cell/* [~k])
             ($.std/keys ($.cell/blob-map [[k
                                            ($.cell/* :a)]]))))))



(T/deftest merge

  (T/is (= ($.cell/* {})
           ($.std/merge nil
                        nil)))

  (T/is (= ($.cell/* {:a :b})
           ($.std/merge ($.cell/* {:a :b})
                        nil)))

  (T/is (= ($.cell/* {:a :b})
           ($.std/merge nil
                        ($.cell/* {:a :b}))))

  (T/is (= ($.cell/* {:a :b
                      :c :d})
           ($.std/merge ($.cell/* {:a :b})
                        ($.cell/* {:c :d})))))



(T/deftest values

  (T/is (= ($.cell/* [])
           ($.std/vals ($.cell/* {}))))

  (T/is (= ($.cell/* [:b])
           ($.std/vals ($.cell/* {:a :b}))))

  (T/is (= ($.cell/* [])
           ($.std/vals ($.cell/blob-map))))

  (T/is (= ($.cell/* [:a])
           ($.std/vals ($.cell/blob-map [[($.cell/blob (byte-array [0]))
                                          ($.cell/* :a)]])))))


;;;;;;;;;; Math


(T/deftest +

  (T/is (= ($.cell/* 10)
           ($.std/+ ($.cell/* 5)
                    ($.cell/* 5))))

  (T/is (= ($.cell/* 10.0)
           ($.std/+ ($.cell/* 5.0)
                    ($.cell/* 5)))))



(T/deftest -

  (T/is (= ($.cell/* 5)
           ($.std/- ($.cell/* 10)
                    ($.cell/* 5))))

  (T/is (= ($.cell/* 5.0)
           ($.std/- ($.cell/* 10.0)
                    ($.cell/* 5)))))



(T/deftest *

  (T/is (= ($.cell/* 50)
           ($.std/* ($.cell/* 10)
                    ($.cell/* 5))))

  (T/is (= ($.cell/* 50.0)
           ($.std/* ($.cell/* 10.0)
                    ($.cell/* 5)))))



(T/deftest abs

  (T/is (= ($.cell/* 42)
           ($.std/abs ($.cell/* 42))))

  (T/is (= ($.cell/* 42)
           ($.std/abs ($.cell/* -42))))

  (T/is (= ($.cell/* 42.24)
           ($.std/abs ($.cell/* 42.24))))

  (T/is (= ($.cell/* 42.24)
           ($.std/abs ($.cell/* -42.24)))))



(T/deftest ceil

  (T/is (= ($.cell/* 42.0)
           ($.std/ceil ($.cell/* 42))))

  (T/is (= ($.cell/* 42.0)
           ($.std/ceil ($.cell/* 41.01))))

  (T/is (= ($.cell/* 42.0)
           ($.std/ceil ($.cell/* 41.99)))))



(T/deftest div

  (T/is (= ($.cell/* 5.0)
           ($.std/div ($.cell/* 10)
                      ($.cell/* 2))))

  (T/is (= ($.cell/* 4.0)
           ($.std/div ($.cell/* 24)
                      ($.cell/* 2)
                      ($.cell/* 3)))))



(T/deftest exp

  (T/is ($.std/double? ($.std/exp ($.cell/* 1)))))



(T/deftest floor

  (T/is (= ($.cell/* 42.0)
           ($.std/floor ($.cell/* 42))))

  (T/is (= ($.cell/* 42.0)
           ($.std/floor ($.cell/* 42.01))))

  (T/is (= ($.cell/* 42.0)
           ($.std/floor ($.cell/* 42.99)))))



(T/deftest nan?

  (T/is (true? ($.std/nan? ($.cell/* ##NaN))))

  (T/is (false? ($.std/nan? ($.cell/* 42.24)))))



(T/deftest pow

  (T/is (= ($.cell/* 9.0)
           ($.std/pow ($.cell/* 3)
                      ($.cell/* 2)))))



(T/deftest signum

  (T/is (= ($.cell/* 1)
           ($.std/signum ($.cell/* 42))))

  (T/is (= ($.cell/* -1)
           ($.std/signum ($.cell/* -42))))

  (T/is (= ($.cell/* 0)
           ($.std/signum ($.cell/* 0))))

  (T/is (= ($.cell/* 1.0)
           ($.std/signum ($.cell/* 42.0))))

  (T/is (= ($.cell/* -1.0)
           ($.std/signum ($.cell/* -42.0))))

  (T/is (= ($.cell/* 0.0)
           ($.std/signum ($.cell/* 0.0)))))



(T/deftest sqrt

  (T/is (= ($.cell/* 4.0)
           ($.std/sqrt ($.cell/* 16.0)))))



(T/deftest zero?

  (T/is (true? ($.std/zero? ($.cell/* 0))))

  (T/is (true? ($.std/zero? ($.cell/* 0.0))))

  (T/is (true? ($.std/zero? ($.cell/* -0.0))))

  (T/is (false? ($.std/zero? ($.cell/* 1))))

  (T/is (false? ($.std/zero? ($.cell/* [])))))



;;;;;;;;;; Sequence


(T/deftest concat

  (T/is (nil? ($.std/concat nil
                            nil)))

  (T/is (= ($.cell/* [:a])
           ($.std/concat ($.cell/* [:a])
                         nil)))

  (T/is (= ($.cell/* [:a])
           ($.std/concat nil
                         ($.cell/* [:a]))))

  (T/is (= ($.cell/* (:a :b))
           ($.std/concat ($.cell/* (:a))
                         ($.cell/* (:b)))))

  (T/is (= ($.cell/* (:a))
           ($.std/concat ($.cell/* (:a))
                         nil)))

  (T/is (= ($.cell/* (:a))
           ($.std/concat nil
                         ($.cell/* (:a)))))

  (T/is (= ($.cell/* [:a :b])
           ($.std/concat ($.cell/* [:a])
                         ($.cell/* [:b]))))

  (T/is (= ($.cell/* [:a])
           ($.std/concat ($.cell/* [:a])
                         nil)))

  (T/is (= ($.cell/* [:a])
           ($.std/concat nil
                         ($.cell/* [:a]))))

  (T/is (= ($.cell/* (:a :b))
           ($.std/concat ($.cell/* (:a))
                         ($.cell/* [:b]))))

  (T/is (= ($.cell/* [:a :b])
           ($.std/concat ($.cell/* [:a])
                         ($.cell/* (:b)))))

  (T/is (= ($.cell/* [:a [:b :c]])
           ($.std/concat ($.cell/* [:a])
                         ($.cell/* {:b :c}))))

  (T/is (= ($.cell/* [:a :b])
           ($.std/concat ($.cell/* [:a])
                         ($.cell/* #{:b})))))



(T/deftest cons

  (T/is (= ($.cell/* (:a))
           ($.std/cons ($.cell/* :a)
                       nil)))

  (T/is (= ($.cell/* (:b :a))
           ($.std/cons ($.cell/* :b)
                       ($.cell/* (:a)))))

  (T/is (= ($.cell/* (:b :a))
           ($.std/cons ($.cell/* :b)
                       ($.cell/* [:a]))))

  (T/is (= ($.cell/* (:c [:a :b]))
           ($.std/cons ($.cell/* :c)
                       ($.cell/* {:a :b}))))

  (T/is (= ($.cell/* (:b :a))
           ($.std/cons ($.cell/* :b)
                       ($.cell/* #{:a})))))



(T/deftest next

  (T/is (nil? ($.std/next ($.cell/* ()))))

  (T/is (nil? ($.std/next ($.cell/* (:a)))))

  (T/is (= ($.cell/* (:b))
           ($.std/next ($.cell/* (:a :b)))))

  (T/is (nil? ($.std/next ($.cell/* []))))

  (T/is (nil? ($.std/next ($.cell/* [:a]))))

  (T/is (= ($.cell/* [:b])
           ($.std/next ($.cell/* [:a :b]))))

  (T/is (nil? ($.std/next ($.cell/* {}))))

  (T/is (nil? ($.std/next ($.cell/* {:a :b}))))

  (T/is (not ($.std/empty? ($.std/next ($.cell/* {:a :b
                                                  :c :d})))))

  (T/is (nil? ($.std/next ($.cell/* #{}))))

  (T/is (nil? ($.std/next ($.cell/* #{:a}))))

  (T/is (not ($.std/empty? ($.std/next ($.cell/* #{:a :b}))))))



(T/deftest reverse

  (T/is (nil? ($.std/reverse nil)))

  (T/is (= ($.cell/* [:c :b :a])
           ($.std/reverse ($.cell/* (:a :b :c)))))

  (T/is (= ($.cell/* (:c :b :a))
           ($.std/reverse ($.cell/* [:a :b :c])))))


;;;;;;;;;; Set


(T/deftest difference

  (T/is (= ($.cell/* #{})
           ($.std/difference nil
                             nil)))

  (T/is (= ($.cell/* #{1})
           ($.std/difference ($.cell/* #{1})
                             nil)))
  (T/is (= ($.cell/* #{})
           ($.std/difference nil
                             ($.cell/* #{1}))))

  (T/is (= ($.cell/* #{})
           ($.std/difference ($.cell/* #{1})
                             ($.cell/* #{1}))))

  (T/is (= ($.cell/* #{1 2})
           ($.std/difference ($.cell/* #{1 2})
                             ($.cell/* #{3 4}))))

  (T/is (= ($.cell/* #{1})
           ($.std/difference ($.cell/* #{1 2})
                             ($.cell/* #{2 3})))))



(T/deftest intersection

  (T/is (= ($.cell/* #{})
           ($.std/intersection nil
                               nil)))

  (T/is (= ($.cell/* #{})
           ($.std/intersection ($.cell/* #{1})
                               nil)))

  (T/is (= ($.cell/* #{})
           ($.std/intersection nil
                               ($.cell/* #{1}))))

  (T/is (= ($.cell/* #{2})
           ($.std/intersection ($.cell/* #{1 2})
                               ($.cell/* #{2 3}))))

  (T/is (= ($.cell/* #{})
           ($.std/intersection ($.cell/* #{1 2})
                               ($.cell/* #{3 4})))))



(T/deftest subset?

  (T/is (true? ($.std/subset? nil
                              nil)))

  (T/is (true? ($.std/subset? nil
                              ($.cell/* #{1}))))

  (T/is (false? ($.std/subset? ($.cell/* #{1})
                               nil)))

  (T/is (true? ($.std/subset? ($.cell/* #{1})
                              ($.cell/* #{1 2}))))

  (T/is (false? ($.std/subset? ($.cell/* #{1})
                               ($.cell/* #{2})))))



(T/deftest union

  (T/is (= ($.cell/* #{})
           ($.std/union nil
                        nil)))

  (T/is (= ($.cell/* #{1})
           ($.std/union ($.cell/* #{1})
                        nil)))

  (T/is (= ($.cell/* #{1})
           ($.std/union nil
                        ($.cell/* #{1}))))

  (T/is (= ($.cell/* #{1 2 3})
           ($.std/union ($.cell/* #{1 2})
                        ($.cell/* #{2 3})))))


;;;;;;;;; Symbolic


(let [s ($.cell/* "test")]

  (T/deftest name
  
    (T/is (= s
             ($.std/name s)))

    (T/is (= s
             ($.std/name ($.cell/* test))))

    (T/is (= s
             ($.std/name ($.cell/* :test))))))
