(ns convex.lisp.form

  "Working with Clojure data representing Convex Lisp code."

  {:author "Adam Helinski"}

  (:require [clojure.walk]))


;;;;;;;;;; Working with Clojure forms expressing Convex Lisp code


(defn literal

  "Transforms some forms into their literal notation:

   | Form | Literal symbol |
   |---|---|
   | `(address #42)`| #42 |
   | `(blob \"11FF\")` | 0x11FF |"

  [form]

  (if (seq? form)
    (condp =
           (first form)
      'address (let [arg (second form)]
                 (if (int? arg)
                   (symbol (str "#"
                                arg))
                   form))
      'blob    (let [arg (second form)]
                 (if (string? arg)
                   (symbol (str "0x"
                                arg))
                   form))
      form)
    form))



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
