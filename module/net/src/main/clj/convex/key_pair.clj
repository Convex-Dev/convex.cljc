(ns convex.key-pair

  "Signing cells using public key cryptography, most notably transactions.

   More precisely, is signed the hash of the encoding of the cell, producing a signed data cell.
  
   Uses [Ed25519](https://ed25519.cr.yp.to).
  
   ---
  
   By default, Convex uses a pure Java implementation of Ed25519.

   When running a peer, which requires intensive signature validation, it is advised switching to
   the native LibSodium implementation.

   Follow instruction to switch, best done when starting your application:
  
     https://github.com/Convex-Dev/convex/tree/main/convex-sodium"

  {:author "Adam Helinski"}

  (:import (convex.core.crypto AKeyPair
                               ASignature)
           (convex.core.data AccountKey
                             ACell
                             Blob
                             SignedData)
           (java.security PrivateKey
                          PublicKey))
  (:require [convex.cell :as $.cell]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Creating key pairs


(defn ed25519

  "Creates an Ed25519 key pair.

   It is generated from a [[seed]], a 32-byte blob. If not given, one is generated randomly.

   Alternatively, a [[key-public]] and a [[key-private]] retrieved from an existing key pair can
   be provided."


  (^AKeyPair []

   (AKeyPair/generate))


  (^AKeyPair [^Blob seed]

   (AKeyPair/create seed))


  (^AKeyPair [^PublicKey key-public ^PrivateKey key-private]

   (AKeyPair/create key-public
                    key-private)))


;;;;;;;;;; Retrieving keys from key pairs


(defn account-key

  "Returns the account key of the given `key-pair`.

   An account key is a specialized cell behaving like a blob and representing the public key
   of an account."

  ^AccountKey

  [^AKeyPair key-pair]

  (.getAccountKey key-pair))



(defn hex-string

  "Returns the public key of the given `key-pair` as a hex-string.
   
   64-char string where each pair of chars represents a byte in hexadecimal."

  [^AKeyPair key-pair]

  (-> key-pair
      (account-key)
      (.toHexString)))



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

  "Returns the seed of the given `key-pair`.

   Attention, this is very sensitive information since it allows rebuilding the key-pair using [[ed25519]]."

  ^Blob

  [^AKeyPair key-pair]

  (.getSeed key-pair))


;;;;;;;;;; Sign cells and verify signatures


(defn sign

  "Returns the given `cell` as data signed by `key-pair`. That value is a cell itself
   and can be stored in Etch if required (see the `convex.db` namespace from `:module/cvm`).

   `signed->...` functions allows for extracting information from signed data.

   Most useful for signing transactions.
   See [[convex.client/transact]]."

  ^SignedData

  [^AKeyPair key-pair ^ACell cell]

  (.signData key-pair
             cell))



(defn signed->account-key

  "Given signed data, returns the account key of the signer.

   See [[account-key]], [[sign]]."

  ^AccountKey

  [^SignedData signed]

  (.getAccountKey signed))



(defn signed->cell

  "Given signed data, returns the cell that was signed.

   See [[sign]]."

  ^ACell

  [^SignedData signed]

  (-> signed
      (.getDataRef)
      (.getValue)))


(defn signed->signature

  "Given signed data, returns the signature as a blob cell.

   See [[sign]]."

  ^Blob

  [^SignedData signed]

  (-> signed
      (.getSignature)
      (.getBytes)
      ($.cell/blob)))



(defn verify

  "Returns true if the given `cell` has indeed been signed by the given [[account-key]].

   `signature` is the signature to verify as a blob cell."

  [^AccountKey account-key ^Blob signature ^ACell cell]

  (.checkSignature (SignedData/create account-key
                                      (ASignature/fromBlob signature)
                                      (.getRef cell))))


;;;;;;;;;; Miscellaneous


(defn key-pair?

  "Returns `true` is `x` is a key pair."

  [x]

  (instance? AKeyPair
             x))
