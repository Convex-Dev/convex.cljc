(ns convex.bench.ed25519.bouncycastle

  "BouncyCastle pure Java implementations."

  (:import (org.bouncycastle.crypto.params Ed25519PrivateKeyParameters)
           (org.bouncycastle.crypto.signers Ed25519Signer)))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(defn key-pair

  []

  (let [key-private (Ed25519PrivateKeyParameters. (byte-array Ed25519PrivateKeyParameters/KEY_SIZE))
        key-public  (.generatePublicKey key-private)
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
     verify
     (vec (.getEncoded key-public))]))
