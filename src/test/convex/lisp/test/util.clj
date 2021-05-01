(ns convex.lisp.test.util

  ""
  {:author "Adam Helinski"}

  (:require [clojure.core]
            [clojure.string]
            [clojure.test.check.results      :as tc.result]
            [convex.lisp                     :as $]
            [convex.lisp.schema              :as $.schema]
            [malli.core                      :as malli]
            [malli.generator                 :as malli.gen])
  (:refer-clojure :exclude [eval]))


(declare eval-source
         registry
         schema-data-without)


;;;;;;;;;; Registry and fetching generators


(defn generator

  "Returns a generator for the given `schema`."

  [schema]

  (malli.gen/generator schema
                       {:registry registry}))



(defn generator-data-without

  "Mix between [[generator]] and [[schema-data-without]]."

  [schema+]

  (generator (schema-data-without schema+)))



(def registry

  "Malli registry for Convex."

  (-> (malli/default-schemas)
      $.schema/registry))


;;;;;;;;;; Helpers


(defn eq

  "Substitute for `=` so that NaN equals NaN."

  [& arg+]

  (apply =
         (clojure.core/map hash
              			   arg+)))



(defn eval

  "Evals the given Clojure `form` representing Convex Lisp code and returns the result
   as Clojure data."

  [form]

  (-> form
      $/clojure->source
      eval-source))



(defn eval-exceptional-source

  ""

  [source]

  (-> source
      $/read
      $/eval
      $/exceptional))



(defn eval-pred

  ""

  [core-symbol x]

  (eval ($/templ {'X   x
                  'SYM core-symbol}
                 '(SYM (quote X)))))



(defn eval-source

  "Reads Convex Lisp source, evals it and converts the result to a Clojure value."

  [source]

  (-> source
      $/read
      $/eval
      $/result
      $/to-clojure))



(defn fail

  "Returns a `test.check` error with an error message."

  [string-error]

  (reify tc.result/Result

    (pass? [_]
      false)

    (result-data [_]
      {:convex.lisp/error string-error})))



(defmacro prop+

  "Meant to be used inside a `test.check` property in order to multiplex it while keeping
   track of which \"sub-property\" failed.
  
   Tests each pair of text message and predicate. Fails with [[faill]] and the message when
   a predicate returns false.

   ```clojure
   (prop+

     \"3 must be greater than 4\"
     (< 3 4)

     \"Result must be double\"
     (double? ...))
   ```"

  [& prop-pair+]

  (assert (even? (count prop-pair+)))
  `(if-some [string-error# (cond
                            ~@(reduce (fn [acc [string-error form-test]]
                                        (conj acc
                                              (list 'not
                                                    form-test)
                                              string-error))
                                      []
                                      (partition 2
                                                 prop-pair+)))]
     (fail string-error#)
     true))



(defn schema-data-without

  "Returns the `:convex/data` schema without the schemas provided in the given set."

  [schema+]

  (into [:or]
        (filter #(not (contains? schema+
                                 %)))
        (rest (registry :convex/data))))


(defn valid?

  "Is `x` valid according to `schema`?"

  [schema x]

  (malli/validate schema
                  x
                  {:registry registry}))
