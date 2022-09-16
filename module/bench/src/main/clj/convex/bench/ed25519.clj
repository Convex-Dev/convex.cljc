(ns convex.bench.ed25519

  "Comparing various Ed25519 implemetentations."

  (:import (java.nio.charset StandardCharsets))
  (:require [convex.bench.ed25519.native :as $.bench.ed25519.native]
            [criterium.core              :as CT]))


;;;;;;;;;;


(def ^bytes payload

  "Test payload."

  (.getBytes (str (range 100))
             StandardCharsets/UTF_8))


;;;;;;;;;;


(let [[sign
       verify] ($.bench.ed25519.native/key-pair)]
  (def signature-native
       (sign payload))
  (def sign-native
       sign)
  (def verify-native
       verify))


;;;;;;;;;;


(comment


  (CT/bench (sign-native payload))
  ;;
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


  )
