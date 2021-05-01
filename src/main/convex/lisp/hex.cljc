(ns convex.lisp.hex

  ""

  {:author "Adam Helinski"}

  (:require [clojure.string]
            [clojure.test.check.generators :as tc.gen]))


;;;;;;;;;;


(def regex

  "Regular expression for a hexstring of any (even) length."

  #"(?i)0x(?:(?:\d|A|B|C|D|E|F){2})*")



(def regex-32

  "Regular expression for a hexstring representing exactly 32 bytes."

  #"(?i)0x(?:\d|A|B|C|D|E|F){32}")



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



(defn pad-32

  "Pads a hexstring to 32 bytes (if needed)."

  ;; 32 bytes = 64 hex chars

  [hexstring]

  (let [delta (- 64
                 (count hexstring))]
    (if (pos? delta)
      (str (clojure.string/join (repeat delta
                                        \0))
           hexstring)
      hexstring)))
