(ns convex.test.ref

  "Testing `convex.ref`."

  {:author "Adam Helinski"}

  (:require [clojure.test :as T]
            [convex.db    :as $.db]
            [convex.cell  :as $.cell]
            [convex.read  :as $.read]
            [convex.ref   :as $.ref])
  (:refer-clojure :exclude [hash
                            resolve]))


;;;;;;;;;; Setup


(def cell
     ($.read/string "{:a [42 'ok]}"))



(def cell-encoding
     ($.cell/encoding cell))



(def hash
     ($.cell/hash<-hex "0000000000000000000000000000000000000000000000000000000000000000"))


;;;


(def direct-
     ($.ref/create-direct cell))



(def soft
     ($.ref/create-soft hash))



(def soft-from-direct
     ($.ref/create-soft (.getHash direct-)))



(def hash-direct
     (.getHash direct-))


(def persisted
     ($.db/write-ref direct-))


;;;;;;;;;; Creating refs


(T/deftest create-direct

  (T/is (some? direct-)))



(T/deftest create-soft

  (T/is (some? soft)))


;;;;;;;;;; Predicates


(T/deftest direct?

  (T/is (true? ($.ref/direct? direct-))
        "Direct is direct")

  (T/is (false? ($.ref/direct? soft))
        "Soft is not direct"))



(T/deftest embedded?

  (T/is (boolean? ($.ref/embedded? direct-))))



(T/deftest missing?

  (T/is (false? ($.ref/missing? direct-))
        "Direct is never missing")

  (T/is (true? ($.ref/missing? soft))
        "Soft with wrong hash is missing")

  (T/is (false? ($.ref/missing? soft-from-direct))
        "Soft from direct cannot be missing"))



(T/deftest persisted?

  (T/is (false? ($.ref/persisted? direct-))
        "Direct not be persisted")

  (T/is (false? ($.ref/persisted? soft-from-direct))
        "Soft not yet persisted")

  (T/is (true? ($.ref/persisted? persisted))
        "Must be reported as persisted after write at setup"))


;;;;;;;;;; Data


(T/deftest encoding

  (T/is (= cell-encoding
           ($.ref/encoding direct-))
        "Direct")

  (T/is (= cell-encoding
           ($.ref/encoding soft-from-direct))
        "Direct -> Soft"))


;;;;;;;;;; Potential reads


(T/deftest direct

  (T/is ($.ref/direct? ($.ref/direct direct-))
        "Direct -> direct")

  (T/is ($.ref/direct? ($.ref/direct soft-from-direct))
        "Direct -> Soft -> Direct")

  (T/is (thrown? Exception
                 ($.ref/direct? ($.ref/direct soft)))
        "Soft -> direct"))



(T/deftest resolve

  (T/is (= cell
           ($.ref/resolve direct-))
        "Direct")

  (T/is (= cell
           ($.ref/resolve soft-from-direct))
        "Direct -> Soft")

  (T/is (thrown? Exception
                 ($.ref/resolve soft))
        "Soft"))
