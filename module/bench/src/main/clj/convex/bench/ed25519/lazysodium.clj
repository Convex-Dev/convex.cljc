(ns convex.bench.ed25519.lazysodium

  "JNA bindings for `libsodium` used by `convex.core`."

  (:import (com.goterl.lazysodium LazySodiumJava
                                  SodiumJava)
           (com.goterl.lazysodium.interfaces Sign$Native)))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(let [^Sign$Native lazysodium (LazySodiumJava. (SodiumJava.))]

  (defn key-pair
  
    []
  
    (let [ba-seed    (byte-array 32)
          ;;
          ;;         Will contain both the seed and the actual private key.
          ba-private (byte-array 64)
          ;;
          ba-public  (byte-array 32)
          sign       (fn sign [^bytes payload]
                       (let [ba-signature (byte-array 64)]
                         (.cryptoSignDetached lazysodium
                                              ba-signature
                                              payload
                                              (count payload)
                                              ba-private)
                         ba-signature))
          verify     (fn verify [^bytes payload ^bytes signature]
                       (.cryptoSignVerifyDetached lazysodium
                                                  signature
                                                  payload
                                                  (count payload)
                                                  ba-public))]
      (.cryptoSignSeedKeypair lazysodium
                              ba-public
                              ba-private
                              ba-seed)
      [sign
       verify
       (vec ba-public)])))
