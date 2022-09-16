(ns convex.bench.ed25519.native

  "JDK implementation."

  (:import (java.security KeyPair
                          KeyPairGenerator
                          Signature)))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;

(let [^KeyPairGenerator generator (KeyPairGenerator/getInstance "Ed25519")
      ^Signature        signer    (Signature/getInstance "Ed25519")]

  (defn key-pair
  
    []

    (let [^KeyPair key-pair    (.generateKeyPair generator)
                   key-private (.getPrivate key-pair)
                   key-public  (.getPublic key-pair)
                   sign        (fn sign [^bytes payload]
                                 (doto
                                   signer
                                   (.initSign key-private)
                                   (.update payload))
                                 (.sign signer))
                   verify      (fn verify [^bytes payload ^bytes signature]
                                 (doto
                                   signer
                                   (.initVerify key-public)
                                   (.update payload))
                                 (.verify signer
                                          signature))]
      [sign
       verify])))
