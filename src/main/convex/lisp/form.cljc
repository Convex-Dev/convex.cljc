(ns convex.lisp.form

  "Working with Clojure data representing Convex Lisp code."

  {:author "Adam Helinski"}

  (:require [clojure.string]
            [clojure.walk]))


;;;;;;;;;; Literal notations for Convex objects that do not map to Clojure but can be expressed as symbols


(defn address

  "Converts `number` into a symbol that ressembles a Convex address."

  [number]

  (symbol (str "#"
               (long number))))



(defn address?

  "Is `x` a symbol that ressembles a Convex address?"

  ;; TODO. Ensures is not a qualified symbol, if new scheme that allows addresses in symbols is kept.

  [x]

  (and (symbol? x)
       (clojure.string/starts-with? (str x)
                                    "#")))



(defn blob

  "Converts `hexstring` into a symbol that ressembles a Convex blob."

  [hexstring]

  (symbol (str "0x"
               hexstring)))



(defn blob?

  "Is `x` a symbol that ressembles a Convex blob?"

  [x]

  (and (symbol? x)
       (clojure.string/starts-with? (str x)
                                    "0x")))



(defn literal

  "Transforms some forms into their literal notation:

   | Example form | Becomes |
   |---|---|
   | `(address #42)`| #42 |
   | `(blob \"11FF\")` | 0x11FF |"

  [form]

  (if (seq? form)
    (condp =
           (first form)
      'address (let [arg (second form)]
                 (if (int? arg)
                   (address arg)
                   form))
      'blob    (let [arg (second form)]
                 (if (string? arg)
                   (blob arg)
                   form))
      form)
    form))


;;;;;;;;;; Miscellaneous


(defn quoted

  "Quote the given `form` as that it will not be evaled when running as Convex Lisp."

  [form]

  (list 'quote
        form))



(defn source

  "Converts a Clojure form expressing Convex Lisp code into a source string."

  [form]

  (pr-str form))



(defn templ

  "Basic templating, walking through `code` and replacing items by following the `binding+`
   map."

  [binding+ code]

  (clojure.walk/postwalk-replace binding+
                                 code))
