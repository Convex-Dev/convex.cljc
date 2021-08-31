(ns convex.sign

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.crypto AKeyPair
                               Ed25519KeyPair)
           (convex.core.data AccountKey
                             ACell
                             SignedData)
           (java.security PrivateKey
                          PublicKey)))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Creating key pairs


(defn ed25519-from

  ""

  ^AKeyPair

  [^PublicKey key-public ^PrivateKey key-private]

  (Ed25519KeyPair/create key-public
                         key-private))



(defn ed25519-gen

  ""

  ^AKeyPair

  []

  (Ed25519KeyPair/generate))


;;;;;;;;;; Retrieving keys from key pairs


(defn account-key

  ""

  ^AccountKey

  [^AKeyPair key-pair]

  (.getAccountKey key-pair))



(defn key-private

  ""

  ^PrivateKey

  [^AKeyPair key-pair]

  (.getPrivate key-pair))



(defn key-public

  ""

  ^PublicKey

  [^AKeyPair key-pair]

  (.getPublic key-pair))


;;;;;;;;;; Using key pairs


(defn sign

  ""

  ^SignedData

  [^AKeyPair key-pair ^ACell cell]

  (.signData key-pair
              cell))
