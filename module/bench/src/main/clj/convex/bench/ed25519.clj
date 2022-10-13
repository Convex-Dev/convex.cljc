(ns convex.bench.ed25519

  "Comparing various Ed25519 implementations.
  
   Each implementation simply returns a vector of functions `[sign verify]`."

  (:require [convex.bench.ed25519.bouncycastle :as $.bench.ed25519.bouncycastle]
            ;;
            ;; Unavailable on Java 11
            ;  [convex.bench.ed25519.jdk          :as $.bench.ed25519.jdk]
            ;
            [convex.bench.ed25519.lazysodium   :as $.bench.ed25519.lazysodium]
            ;;
            ;; Project Panama requires Java > 18.
            ;;
            ;; It seems calling libsodium directly is the fasted options (see benchmarks below).
            ;; However we should probably wait until a long-term supported Java version is available.
            ;;
            ;  [convex.bench.ed25519.libsodium    :as $.bench.ed25519.libsodium]
            ;
            [criterium.core                    :as CT]
            [protosens.bench                   :as P.bench]))


;;;;;;;;;; Materials


(def ^bytes payload

  "Test payload."

  (byte-array 32))


;;;;;;;;;; Key pairs


;;; BouncyCastle


(let [[sign
       verify
       key-public] ($.bench.ed25519.bouncycastle/key-pair)]
  (def key-public-bouncycastle
       key-public)
  (def signature-bouncycastle
       (sign payload))
  (def sign-bouncycastle
       sign)
  (def verify-bouncycastle
       verify))


(assert (verify-bouncycastle payload
                             signature-bouncycastle))


;;; JDK default implementtion


; (let [[sign
;        verify] ($.bench.ed25519.jdk/key-pair)]
;   (def signature-jdk
;        (sign payload))
;   (def sign-jdk
;        sign)
;   (def verify-jdk
;        verify))
; 
; 
; (assert (verify-jdk payload
;                     signature-jdk))


;;; Lazysodium


(let [[sign
       verify
       key-public] ($.bench.ed25519.lazysodium/key-pair)]
  (def key-public-lazysodium
       key-public)
  (def signature-lazysodium
       (sign payload))
  (def sign-lazysodium
       sign)
  (def verify-lazysodium
       verify))


(assert (verify-lazysodium payload
                           signature-lazysodium))


;;; Libsodium
; 
; 
; (let [[sign
;        verify] ($.bench.ed25519.libsodium/key-pair)]
;   (def signature-libsodium
;        (sign payload))
;   (def sign-libsodium
;        sign)
;   (def verify-libsodium
;        verify))
; 
; 
; (assert (verify-libsodium payload
;                           signature-libsodium))


;;;;;;;;;;


(assert (= (vec signature-bouncycastle)
           (vec signature-lazysodium)
           ; (vec signature-libsodium)
           ))


(assert (= key-public-bouncycastle
           key-public-lazysodium))


;;;;;;;;;;


(defn verify--bouncy-castle-vs-lazysodium

  [& _arg]

  (doto
    (P.bench/run+ {:bouncy-castle {:f #(verify-bouncycastle payload
                                                            signature-bouncycastle)}
                   :lazysodium    {:f #(verify-lazysodium payload
                                                          signature-lazysodium)}}
                  {:samples           100
                   :warmup-jit-period (* 60000 1e3)})
    (P.bench/report)))



;;;;;;;;;;


(comment


  ;; BouncyCastle

  (-> (P.bench/run #(sign-bouncycastle payload))
      (P.bench/report))

 
  (-> (P.bench/run #(verify-bouncycastle payload
                                         signature-bouncycastle))
      (P.bench/report))


  ;; LAZYSODIUM

  (-> (P.bench/run #(CT/bench (sign-lazysodium payload)))
      (P.bench/report))

  (-> (P.bench/run #(verify-lazysodium payload
                                       signature-lazysodium))
      (P.bench/report))


  )
