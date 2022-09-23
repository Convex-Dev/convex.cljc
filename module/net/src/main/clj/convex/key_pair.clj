(ns convex.key-pair

  "Signing cells using public key cryptography, most notably transactions as required prior to submission.

   More precisely, is signed the hash of the encoding of the cell, producing a signed data cell.
  
   Uses Ed25519."

  {:author "Adam Helinski"}

  (:import (convex.core.crypto AKeyPair
                               ASignature
                               Ed25519KeyPair
                               Ed25519Signature)
           (convex.core.data AccountKey
                             ACell
                             Blob
                             Hash
                             SignedData)
           (java.security PrivateKey
                          PublicKey)))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Creating key pairs


(defn ed25519

  "Creates an Ed25519 key pair.

   It is generated from a [[seed]], a 32-byte blob. If not given, one is generated randomly.

   Alternatively, a [[key-public]] and a [[key-private]] retrieved from an existing key pair can
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

   An account key is a specialized cell behaving like a blob and representing the public key
   of an account."

  ^AccountKey

  [^AKeyPair key-pair]

  (.getAccountKey key-pair))



(defn hex-string

  "Returns the public key of the given `key-pair` as a hex-string (64-char string where each pair of 
   chars represents a byte in hexadecimal)."

  [^AKeyPair key-pair]

  (-> key-pair
      account-key
      .toHexString))



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

  [^Ed25519KeyPair key-pair]

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

  "Given signed data, returns the [[account-key]] of the signer.
  
   See [[sign]]."

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
      ^Ed25519Signature (.getSignature)
      (.getSignatureBlob)))



(defn verify

  "Returns true if the given `cell` has indeed been signed by the given [[account-key]].

   `signature` is the signature to verify as a blob cell."

  [^AccountKey account-key ^Blob signature ^ACell cell]

  (.checkSignature (SignedData/create account-key
                                      (ASignature/fromBlob signature)
                                      (.getRef cell))))


;;;;;;;;;; Sign and verify hashes directly


(defn sign-hash

  "Signs the given `hash` with the given `key-pair`.
   Returns the signature as a blob.

   See `convex.cell/hash` from `:module/cvm`."

  [^AKeyPair key-pair ^Hash hash]

  (-> ^Ed25519Signature (.sign key-pair
                               hash)
      (.getSignatureBlob)))



(defn verify-hash

  "Verifies that the given `signature` is indeed the given `hash` signed by the given
   [[account-key]].
  
   See [[sign-hash]]."

  [^AccountKey account-key ^Blob signature ^Hash hash]

  (-> (ASignature/fromBlob signature)
      (.verify hash
               account-key)))
