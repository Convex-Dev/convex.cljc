(ns convex.example.templ

  "Templating Convex Lisp in Clojure."

  (:require [convex.clj :as $.clj]))


;;;;;;;;;;


(comment


  ;; Form for sending some amount of coin to an address.
  ;;
  (let [addr   42
        amount 1000]
    ($.clj/templ* (let [addr (address ~addr)]
                    (transfer addr
                              ~amount)
                    (balance addr))))


  ;; Producing a vector
  ;;
  ;; `(unquote y)` is left as is because `y` is embedded in an `(unquote)` form (reserved for Convex Lisp)
  ;; instead of `~` (reserved for injecting values from Clojure).
  ;;
  ;; Similarly for `(unquote-splicing xs)`.
  ;;
  (let [kw :foo
        xs [2 3]
        y  42]
    ($.clj/templ* [~kw 1 ~@xs 4 ~y y (unquote y) (unquote-splicing xs)]))

  ;; => [:foo 1 2 3 4 42 y (unquote y) (unquote-splicing xs)]
  )
