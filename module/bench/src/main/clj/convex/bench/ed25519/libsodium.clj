(ns convex.bench.ed25519.libsodium

  "FFI over Libsodium using Project Panama via a Clojure library.
  
   Requires Java 18."

  (:import (jdk.incubator.foreign MemorySegment
                                  ValueLayout))
  (:require [coffi.mem :as ffi.mem]
            [coffi.ffi :as ffi]))


(set! *warn-on-reflection*
      true)


(ffi/load-library "./module/bench/resource/native/libsodium.dylib")


;;;;;;;;;; Bridges to C functions

(ffi/defcfn seed-key-pair
  "Initializes the key pair."
  crypto_sign_seed_keypair
  [;; Public key
   ::ffi.mem/pointer
   ;; Private key
   ::ffi.mem/pointer
   ;; Seed
   ::ffi.mem/pointer]
  ::ffi.mem/int)



(ffi/defcfn sign-detached
  "Creates signature."
  crypto_sign_detached
  [; Signature
   ::ffi.mem/pointer
   ; Signature length (pointer to long receiving the length of the signature when not nil)
   ::ffi.mem/pointer
   ; Payload
   ::ffi.mem/pointer
   ; Payload length
   ::ffi.mem/long
   ; Private key
   ::ffi.mem/pointer]
  ::ffi.mem/int)



(ffi/defcfn verify-detached
  "Verifies the signature."
  crypto_sign_verify_detached
  [;; Signature
   ::ffi.mem/pointer
   ;; Payload
   ::ffi.mem/pointer
   ;; Payload length
   ::ffi.mem/long
   ;; Public key
   ::ffi.mem/pointer]
  ;; Boolean - Verified?
  ::ffi.mem/int)


;;;;;;;;;; Public


(defn ba-java
  "Produces a Java byte array from a native byte array."
  [^MemorySegment ba-native]
  (.toArray ba-native
            ValueLayout/JAVA_BYTE))



(defn ba-native
  "Produces a native byte array from a Java byte array."
  [^bytes ba-java]
  (let [n-byte    (count ba-java)
        ba-native (ffi.mem/alloc n-byte)]
    (MemorySegment/copy ba-java
                        0
                        ba-native
                        ValueLayout/JAVA_BYTE
                        0
                        n-byte)
    ba-native))


;;;


(defn key-pair
  []
  (let [ba-seed              (ffi.mem/alloc 32)
        ;;
        ;;                   Will contain both the seed and the actual private key.
        ba-private           (ffi.mem/alloc 64)
        ;;
        ba-public            (ffi.mem/alloc 32)
        ptr-signature-length (ffi.mem/alloc ffi.mem/long-size)
        sign                 (fn sign [^bytes payload]
                               (let [ba-signature (ffi.mem/alloc 64)]
                                 (sign-detached ba-signature
                                                ptr-signature-length
                                                (ba-native payload)
                                                (count payload)
                                                ba-private)
                                 (ba-java ba-signature)))
        verify               (fn verify [^bytes payload ^bytes signature]
                               (zero? (verify-detached (ba-native signature)
                                                       (ba-native payload)
                                                       (count payload)
                                                       ba-public)))]
    (seed-key-pair ba-public
                   ba-private
                   ba-seed)
    [sign
     verify]))
