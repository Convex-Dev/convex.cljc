(ns convex.lisp

  "Readind Convex source code + translating between Convex objects and Clojure data structures."

  {:author "Adam Helinski"}

  (:require [clojure.core.protocols]
            [clojure.walk]
            [convex.lisp.form        :as $.form])
  (:import (convex.core.data ABlob
                             ACell
                             Address
                             AList
                             AMap
                             ASet
                             AString
                             AVector
                             Address
                             Keyword 
                             Symbol
                             Syntax)
           (convex.core.data.prim CVMBool
                                  CVMByte
                                  CVMChar
                                  CVMDouble
                                  CVMLong)
           (convex.core.lang.impl CoreFn
                                  ErrorValue)
           convex.core.lang.Reader)
  (:refer-clojure :exclude [read]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Reading Convex Lisp source


(defn read

  "Converts Convex Lisp source to a Convex object.

   Such an object can be used as is, using its Java API. More often, is it converted to Clojure or
   compiled and executed on the CVM. See the [[convex.cvm]] namespace."

  [string]

  (let [parsed (Reader/readAll string)]
    (if (second parsed)
      (.cons parsed
             (Symbol/create "do"))
      (first parsed))))



(defn read-form

  "Stringifies the given Clojure form to Convex Lisp source and applies the result to [[read]]."

  [form]

  (-> form
      $.form/src
      read))
