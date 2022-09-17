(ns convex.bench.ed25519.bouncycastle

  "BouncyCastle pure Java implementations."

  (:import (java.security SecureRandom)
           (org.bouncycastle.crypto.generators Ed25519KeyPairGenerator)
           (org.bouncycastle.crypto.params Ed25519KeyGenerationParameters)
           (org.bouncycastle.crypto.signers Ed25519Signer)))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(let [generator (Ed25519KeyPairGenerator.)]
  (.init generator
         (Ed25519KeyGenerationParameters. (SecureRandom.)))
  
  (defn key-pair

    []

    (let [key-pair    (.generateKeyPair generator)
          key-private (.getPrivate key-pair)
          key-public  (.getPublic key-pair)
          signer      (Ed25519Signer.)
          sign        (fn sign [^bytes payload]
                        (.reset signer)
                        (.init signer
                               true
                               key-private)
                        (.update signer
                                 payload
                                 0
                                 (count payload))
                        (.generateSignature signer))
          verify      (fn verify [^bytes payload ^bytes signature]
                        (.reset signer)
                        (.init signer
                               false
                               key-public)
                        (.update signer
                                 payload
                                 0
                                 (count payload))
                        (.verifySignature signer
                                          signature))]
      [sign
       verify])))
