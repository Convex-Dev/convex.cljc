(ns convex.clj

  "Working with Clojure data representing Convex Lisp code.
  
   Types that cannot be represented directly in Clojure can be created as symbol that look like the related
   Convex type when printed. Those symbols store the following metadata:

   | Key | Value |
   |---|---|
   | `:convex/raw` | Value that has been used for creating the symbol |
   | `:convex/type` | Convex type |

   For instance, `(address 42)` produces a symbol `#42` with metadata `{:convex/raw 42, :convex/type :address}`"

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [=
                            list
                            list?
                            read])
  (:require [clojure.core]))


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

  "Turns the given collection into a `(list ...)` and stores information in metadata
   like typed symbols."

  [x+]

  (let [x-2+ (vec x+)]
    (with-meta (cons 'list
                     x-2+)
               {:convex/raw  x-2+
                :convex/type :list})))



(defn literal

  "Produces a symbol from the given string with an optional type (see [[meta-type]]).
  
   A symbol prints exactly as it lookes. Hence, this is useful for [[templ*]] for including expression
   that might not be possible to write in Clojure.

   ```clojure
   (templ* (get ~(literal \"{[1] :vec, '(1) :list}\")
                [1]))
   ```"


  ([string]

   (symbol string))


  ([type string]

   (with-meta (literal string)
              {:convex/type type})))


;;;;;;;;;; Miscellaneous


(defn =

  "Substitute for `=` so that NaN equals NaN."

  [& arg+]

  (apply clojure.core/=
         (map hash
              arg+)))

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



(defn meta-type?

  ""

  [type x]

  (clojure.core/= (meta-type x)
                  type))



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

  "Is `x` an address symbol produced by [[address]]?"

  [x]

  (meta-type? :address
              x))



(defn blob?

  "Is `x` a blob symbol produced by [[blob]]?"

  [x]

  (meta-type? :blob
              x))



(defn call?

  "Is `x` a form calling `sym`?
  
   ```clojure
   (call? '-
          '(+ 2 3))  ;; False
   ```"

  [sym x]

  (and (seq? x)
       (clojure.core/= (first x)
                       sym)))



(defn list?

  "Is `x` a `(list ...)` form produced by [[list]]?"

  [x]

  (meta-type? :list
              x))



(defn quoted?

  "Is `x` a `(quote ...)` form?"

  [x]

  (call? 'quote
         x))


;;;;;;;;;; Hex-strings


(def regex-hex-string

  "Regular expression for a hexstring of any (even) length."

  #"(?i)(?:(?:\d|A|B|C|D|E|F){2})*")



(defn hex-string?

  "Is the given string a hex-string?"


  ([string]

   (boolean (re-matches regex-hex-string
                        string)))


  ([n-byte string]

   (and (clojure.core/= (count string)
                        (* 2
                           n-byte))
        (hex-string? string))))


;;;;;;;;;; Templating Convex Lisp code


(declare ^:no-doc -templ*)



(defn ^:no-doc -splice

  ;; Helper for [[-templ]].

  [x+]
  
  (list* 'concat
         (map (fn [x]
                (if (and (seq? x)
                         (clojure.core/= (first x)
                                         'clojure.core/unquote-splicing))
                  (second x)
                  [(-templ* x)]))
              x+)))



(defn- ^:no-doc -templ*

  ;; Helper for [[templ*]].

  [form]

  (cond
    (seq? form)    (condp clojure.core/=
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


;;;;;;;;;; EDN


(defn edn-reader+

  "Returns a map of EDN readers for common Convex objects.

   Meant to be used with `clojure.tools.reader.edn`.
  
   <!> Unstable, Convex EDN support is currently poor."

  []

  {'account    (fn [account]
                 [:convex/account
                  account])
   'addr       address
   'blob       blob    ;; TODO. Currently is outputted as a hex long, not a hex string.
   'context    (fn [ctx]
                 [:convex/ctx
                  ctx])
   'expander   (fn [expander]
                 [:convex/expander
                  expander])
   'signeddata (fn [hash]
                 [:convex/signed-data
                  hash])
   'syntax     (fn [{:keys [datum]
                     mta   :meta}]
                 (if (and (seq mta)
                          (not (second mta))
                          (nil? (get mta
                                     :start)))
                   (clojure.core/list 'syntax
                                      datum
                                      mta)
                   datum))})
