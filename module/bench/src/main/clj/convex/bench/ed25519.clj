(ns convex.bench.ed25519

  "Comparing various Ed25519 implementations.
  
   Each implementation simply returns a vector of functions `[sign verify]`."

  (:require [convex.bench.ed25519.native     :as $.bench.ed25519.native]
            [convex.bench.ed25519.lazysodium :as $.bench.ed25519.lazysodium]
            [criterium.core                  :as CT]))


;;;;;;;;;;


(def ^bytes payload

  "Test payload."

  (byte-array (range 32)))


;;;;;;;;;;


(let [[sign
       verify] ($.bench.ed25519.native/key-pair)]
  (def signature-native
       (sign payload))
  (def sign-native
       sign)
  (def verify-native
       verify))


(assert (verify-native payload
                       signature-native))


;;;


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


;;;;;;;;;;


(comment


  ;; Benchmarked on 2022-09-16 with a Macbook Pro M1 Max 64GB
  ;;
  ;;   Sign  : LazySodium 6.03x
  ;;   Verify: LazySodium 2.95x


  (CT/bench (sign-native payload))

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


  (CT/bench (verify-native payload
                           signature-native))
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


  )
