(ns convex.lisp.form

  "Working with Clojure data representing Convex Lisp code."

  {:author "Adam Helinski"}

  (:require [clojure.string]
            [clojure.walk])
  #?(:cljs (:require-macros [convex.lisp.form :refer [templ*]])))


(declare ^:no-doc -templ*)


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



(defn src

  "Converts a Clojure form expressing Convex Lisp code into a source string."

  [form]

  (pr-str form))


;;;;;;;;;; Templating Convex Lisp code


(defn templ

  "Basic templating, walking through `code` and replacing items by following the `binding+`
   map.
  
   ```clojure
   (templ {'?x [1 2 3}
          '(conj ?x
                 4))
   ```"

  [binding+ code]

  (clojure.walk/postwalk-replace binding+
                                 code))



(defn ^:no-doc -splice

  ;; Helper for [[-templ]].

  [x+]
  
  (list* 'concat
         (map (fn [x]
                (if (and (seq? x)
                         (= (first x)
                            'clojure.core/unquote-splicing))
                  (second x)
                  [(-templ* x)]))
              x+)))



(defn- -templ*

  ;; Helper for [[templ*]].

  [form]

  (cond
    (seq? form)    (condp =
                          (first form)
                     'clojure.core/unquote          (second form)
                     'clojure.core/unquote-splicing (throw (ex-info "Can only splice inside of a collection"
                                                                    {::form form}))
                     (-splice form))
    (map? form)    `(apply hash-map
                           ~(-splice (mapcat identity
                                             form)))
    (set? form)    `(set ~(-splice form))
    (vector? form) `(vec ~(-splice form))
    :else          (if (symbol? form)
                     `(quote ~form)
                     form)))



(defmacro templ*

  "Macro for templating Convex Lisp Code.
  
   Ressembles Clojure's syntax quote but does not namespace anything.

   Unquoting and unquote-splicing for inserting Clojure values are done through the literal notation (respectively
   **~** and **~@**) whereas those same features as Convex are written via forms (respecively `(unquote x)` and
   `(unquote-splicing x)`.
  
   For example:

   ```clojure
   (let [kw :foo
         xs [2 3]
         y  42]
     (templ* [~kw 1 ~@xs 4 (unquote y)]))
   ```

   Produces the following vector:

   ```clojure
   [:foo 1 2 3 4 (unquote y)]
   ```"

  ;; Inspired by https://github.com/brandonbloom/backtick/

  [form]

  (-templ* form))
