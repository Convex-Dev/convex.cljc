(ns convex.bench.ed25519

  "Comparing various Ed25519 implementations.
  
   Each implementation simply returns a vector of functions `[sign verify]`.


   NOTES:

     - JDK implementation does not seem to be able to generate a key pair from a seed
     - Libsodium implementation leveraging Project Panama could be more optimized"

  (:require [convex.bench.ed25519.jdk        :as $.bench.ed25519.jdk]
            [convex.bench.ed25519.lazysodium :as $.bench.ed25519.lazysodium]
            [convex.bench.ed25519.libsodium  :as $.bench.ed25519.libsodium]
            [criterium.core                  :as CT]))


;;;;;;;;;;


(def ^bytes payload

  "Test payload."

  (byte-array 32))


;;;;;;;;;; Key pairs


;;; JDK default implementtion


(let [[sign
       verify] ($.bench.ed25519.jdk/key-pair)]
  (def signature-jdk
       (sign payload))
  (def sign-jdk
       sign)
  (def verify-jdk
       verify))


(assert (verify-jdk payload
                    signature-jdk))


;;; Lazysodium


(let [[sign
       verify] ($.bench.ed25519.lazysodium/key-pair)]
  (def signature-lazysodium
       (sign payload))
  (def sign-lazysodium
       sign)
  (def verify-lazysodium
       verify))


(assert (verify-lazysodium payload
                           signature-lazysodium))


;;; Libsodium


(let [[sign
       verify] ($.bench.ed25519.libsodium/key-pair)]
  (def signature-libsodium
       (sign payload))
  (def sign-libsodium
       sign)
  (def verify-libsodium
       verify))


(assert (verify-libsodium payload
                          signature-libsodium))
(assert (= (vec signature-lazysodium)
           (vec signature-libsodium)))


;;;;;;;;;;


(comment


  ;; Benchmarked on 2022-09 with a Macbook Pro M1 Max 64GB
  ;;
  ;;   Sign  : Libsodium   4.96x  faster than LazySodium
  ;;           LazySodium  6.03x  faster than JDK
  ;;
  ;;   Verify: Libsodium   3.69x  faster than LazySodium
  ;;           LazySodium  2.95x  faster than JDK




  ;; JDK DEFAULT IMPLEMENTATION

  (CT/bench (sign-jdk payload))

  ;; Evaluation count : 146820 in 60 samples of 2447 calls.
  ;;              Execution time mean : 410,291944 µs
  ;;     Execution time std-deviation : 2,032907 µs
  ;;    Execution time lower quantile : 408,104612 µs ( 2,5%)
  ;;    Execution time upper quantile : 415,944894 µs (97,5%)
  ;;                    Overhead used : 1,835776 ns
  ;; 
  ;; Found 7 outliers in 60 samples (11,6667 %)
  ;; 	low-severe	 4 (6,6667 %)
  ;; 	low-mild	 3 (5,0000 %)
  ;;  Variance from outliers : 1,6389 % Variance is slightly inflated by outliers


  (CT/bench (verify-jdk payload
                        signature-jdk))
  ;;
  ;; Evaluation count : 143940 in 60 samples of 2399 calls.
  ;;              Execution time mean : 416,925115 µs
  ;;     Execution time std-deviation : 1,326941 µs
  ;;    Execution time lower quantile : 414,752454 µs ( 2,5%)
  ;;    Execution time upper quantile : 419,841730 µs (97,5%)
  ;;                    Overhead used : 1,835776 ns
  ;; 
  ;; Found 1 outliers in 60 samples (1,6667 %)
  ;; 	low-severe	 1 (1,6667 %)
  ;;  Variance from outliers : 1,6389 % Variance is slightly inflated by outliers




  ;; LAZYSODIUM

  (CT/bench (sign-lazysodium payload))

  ;; Evaluation count : 889380 in 60 samples of 14823 calls.
  ;;              Execution time mean : 68,003370 µs
  ;;     Execution time std-deviation : 1,449076 µs
  ;;    Execution time lower quantile : 67,444493 µs ( 2,5%)
  ;;    Execution time upper quantile : 71,434329 µs (97,5%)
  ;;                    Overhead used : 1,834467 ns
  ;; 
  ;; Found 6 outliers in 60 samples (10,0000 %)
  ;; 	low-severe	 1 (1,6667 %)
  ;; 	low-mild	 5 (8,3333 %)
  ;;  Variance from outliers : 9,4265 % Variance is slightly inflated by outliers


  (CT/bench (verify-lazysodium payload
                               signature-lazysodium))

  ;; Evaluation count : 424740 in 60 samples of 7079 calls.
  ;;              Execution time mean : 141,659244 µs
  ;;     Execution time std-deviation : 548,068110 ns
  ;;    Execution time lower quantile : 141,099886 µs ( 2,5%)
  ;;    Execution time upper quantile : 142,263666 µs (97,5%)
  ;;                    Overhead used : 1,834467 ns
  ;; 
  ;; Found 1 outliers in 60 samples (1,6667 %)
  ;; 	low-severe	 1 (1,6667 %)
  ;;  Variance from outliers : 1,6389 % Variance is slightly inflated by outliers




  ;; LIBSODIUM VIA PROJECT PANAMA

  (CT/bench (sign-libsodium payload))

  ;; Evaluation count : 4568640 in 60 samples of 76144 calls.
  ;;              Execution time mean : 13,719964 µs
  ;;     Execution time std-deviation : 1,103597 µs
  ;;    Execution time lower quantile : 13,109832 µs ( 2,5%)
  ;;    Execution time upper quantile : 16,133924 µs (97,5%)
  ;;                    Overhead used : 1,928943 ns
  ;; 
  ;; Found 13 outliers in 60 samples (21,6667 %)
  ;; 	low-severe	 13 (21,6667 %)
  ;;  Variance from outliers : 60,1361 % Variance is severely inflated by outliers

  (CT/bench (verify-libsodium payload
                              signature-libsodium))

  ;; Evaluation count : 1582860 in 60 samples of 26381 calls.
  ;;              Execution time mean : 38,492005 µs
  ;;     Execution time std-deviation : 1,808988 µs
  ;;    Execution time lower quantile : 37,745792 µs ( 2,5%)
  ;;    Execution time upper quantile : 44,397610 µs (97,5%)
  ;;                    Overhead used : 1,928943 ns
  ;; 
  ;; Found 7 outliers in 60 samples (11,6667 %)
  ;; 	low-severe	 7 (11,6667 %)
  ;;  Variance from outliers : 33,5556 % Variance is moderately inflated by outliers


  )
