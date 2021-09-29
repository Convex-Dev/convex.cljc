(ns convex.test.std

  "Testing `conveX.std`."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [assoc
                            conj
                            contains?
                            count
                            empty
                            empty?
                            get
                            nth])
  (:require [clojure.test :as T]
            [convex.cell  :as $.cell]
            [convex.ref   :as $.ref]
            [convex.std   :as $.std]))


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
