(ns convex.lisp.hex

  "Working with hexstings."

  {:author "Adam Helinski"}

  (:require [clojure.string]
            [clojure.test.check.generators :as tc.gen]))


;;;;;;;;;;


(def regex

  "Regular expression for a hexstring of any (even) length."

  #"(?i)0x(?:(?:\d|A|B|C|D|E|F){2})*")



(def regex-8

  "Regular expression for a hexstring representing exactly 8 bytes."

  #"(?i)0x(?:\d|A|B|C|D|E|F){16}")



(def regex-32

  "Regular expression for a hexstring representing exactly 32 bytes."

  #"(?i)0x(?:\d|A|B|C|D|E|F){64}")



;;;;;;;;;; 


(defn from-int

  "Converts an integer to a hexstring."

  [i]

  (let [hexstring (#?(:clj  Long/toString
                      :cljs .toString)
                   i
                   16)]
    (if (odd? (count hexstring))
      (str \0
           hexstring)
      hexstring)))



(defn pad

  "Pads a hexstring to `n` bytes (if needed)."

  ;; 1 byte = 2 hex chars

  [n hexstring]

  (let [delta (- (* 2
                    n)
                 (count hexstring))]
    (if (pos? delta)
      (str (clojure.string/join (repeat delta
                                        \0))
           hexstring)
      hexstring)))



(defn pad-8

  "Pads a hexstring to 8 bytes (if needed)."

  [hexstring]

  (pad 8
       hexstring))



(defn pad-32

  "Pads a hexstring to 32 bytes (if needed)."

  [hexstring]

  (pad 32
       hexstring))



(defn to-blob-symbol

  "Converts a hexstring to a symbol that looks like a blob."

  [hexstring]

  (symbol (str "0x"
               hexstring)))
