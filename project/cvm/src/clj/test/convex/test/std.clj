(ns convex.test.std

  "Testing `conveX.std`."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [count
                            empty?
                            nth])
  (:require [clojure.test :as T]
            [convex.cell  :as $.cell]
            [convex.read  :as $.read]
            [convex.ref   :as $.ref]
            [convex.std   :as $.std]))


;;;;;;;;;;


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
