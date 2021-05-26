(ns convex.lisp.form

  "Working with Clojure data representing Convex Lisp code.
  
   Types that cannot be represented directly in Clojure can be created as symbol that look like the related
   Convex type when printed. Those symbols store the following metadata:

   | Key | Value |
   |---|---|
   | `:convex/raw` | Value that has been used for creating the symbol |
   | `:convex/type` | Convex type |

   For instance, `(address 42)` produces a symbol `#42` with metadata `{:convex/raw 42, :convex/type :address}`"

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [empty?
                            list
                            list?])
  (:require [clojure.core]
            [clojure.string]
            [clojure.walk])
  #?(:cljs (:require-macros [convex.lisp.form :refer [templ*]])))


(declare ^:no-doc -templ*)


;;;;;;;;;; Literal notations for Convex objects that do not map to Clojure but can be expressed as symbols


(defn address

  "Converts `number` into a symbol that ressembles a Convex address."

  [number]

  (with-meta (symbol (str "#"
                          (long number)))
             {:convex/raw  number
              :convex/type :address}))



(defn blob

  "Converts `hexstring` into a symbol that ressembles a Convex blob."

  [hex-string]

  (with-meta (symbol (str "0x"
                          hex-string))
             {:convex/raw  hex-string
              :convex/type :blob}))



(defn list

  "Turns the given collection into a `(list ...)` and store information in metadata
   like typed symbols."

  [x+]

  (let [x-2+ (vec x+)]
    (with-meta (cons 'list
                     x-2+)
               {:convex/raw  x-2+
                :convex/type :list})))



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


(defn meta-raw

  "Extracts the value stored in `:convex/raw` in the metadata of `x`."

  [x]

  (-> x
      meta
      :convex/raw))



(defn meta-type

  "Extracts the type keyword stored in `:convex/type` in the metadata of `x`."

  [sym]

  (-> sym
      meta
      :convex/type))



(defn quoted

  "Quote the given `form` as that it will not be evaled when running as Convex Lisp."

  [form]

  (clojure.core/list 'quote
                     form))



(defn src

  "Converts a Clojure form expressing Convex Lisp code into a source string."

  [form]

  (pr-str form))


;;;;;;;;;; Predicates


(defn address?

  "Is `x` a symbol produced by [[symbol]]?"

  [x]

  (= (meta-type x)
     :address))



(defn blob?

  "Is `x` a symbol that ressembles a Convex blob?"

  [x]

  (= (meta-type x)
     :blob))



(defn call?

  "Is `x` a form calling `sym`?
  
   ```clojure
   (call? 'double
          '(quot 2 3))  ;; False
   ```"

  [sym x]

  (and (seq? x)
       (= (first x)
          sym)))



(defn empty?

  "Is `x` an empty collection?

   Takes care of the fact that some types are calls to their constructor function (eg. `(list :a :b :c)`)."

  [x]

  (clojure.core/empty? (cond->
                         x
                         (seq? x)
                         rest)))



(defn list?

  "Is `x` a `(list ...)` form produced by [[list]]?"

  [x]

  (= (meta-type x)
     :list))



(defn quoted?

  "Is `x` a `(quote ...)` form?"

  [x]

  (call? 'quote
         x))


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
     (templ* [~kw 1 ~@xs 4 ~y y (unquote y) (unquote-splicing vs)]))
   ```

   Produces the following vector:

   ```clojure
   [:foo 1 2 3 4 42 y (unquote y) (unquote-splicing y)]
   ```"

  ;; Inspired by https://github.com/brandonbloom/backtick/

  [form]

  (-templ* form))
