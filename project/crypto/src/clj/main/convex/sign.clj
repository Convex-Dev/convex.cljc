(ns convex.sign

  "Signing cells using public key cryptography, most notably transactions as required prior to submission.

   More precisely, is signed the hash of the encoding of the cell, producing a signed data cell.
  
   Uses Ed25519."

  {:author "Adam Helinski"}

  (:import (convex.core.crypto AKeyPair
                               Ed25519KeyPair)
           (convex.core.data AccountKey
                             ACell
                             Blob
                             SignedData)
           (java.security PrivateKey
                          PublicKey)))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Creating key pairs


(defn ed25519

  "Creates an Ed25519 key pair.

   It is generated from a [[seed]], a 32-byte blob. If not given, one is generated randomly.

   Alternatively, a [[public-key]] and a [[private-key]] retrieved from an existing key pair can
   be provided."


  (^AKeyPair []

   (Ed25519KeyPair/generate))


  (^AKeyPair [^Blob seed]

   (Ed25519KeyPair/create seed))


  (^AKeyPair [^PublicKey key-public ^PrivateKey key-private]

   (Ed25519KeyPair/create key-public
                          key-private)))


;;;;;;;;;; Retrieving keys from key pairs


(defn account-key

  "Returns the account key of the given `key-pair`.
  
   This is effectively the public key presented as a cell."

  ^AccountKey

  [^AKeyPair key-pair]

  (.getAccountKey key-pair))



(defn key-private

  "Returns the `java.security.PrivateKey` of the given `key-pair`."

  ^PrivateKey

  [^AKeyPair key-pair]

  (.getPrivate key-pair))



(defn key-public

  "Returns the `java.security.PublicKey` of the given `key-pair`."

  ^PublicKey

  [^AKeyPair key-pair]

  (.getPublic key-pair))



(defn seed

  "Returns the seed of"

  ^Blob

  [^AKeyPair key-pair]

  (.getSeed key-pair))


;;;;;;;;;; Using key pairs


(defn signed

  "Returns the given `cell` signed by `key-pair`."

  ^SignedData

  [^AKeyPair key-pair ^ACell cell]

  (.signData key-pair
             cell))
