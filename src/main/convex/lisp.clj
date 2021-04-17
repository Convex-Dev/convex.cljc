(ns convex.lisp

  "# Glossary
  
   | Term | Meaning |
   |---|---|
   | DDV | Decentralized Data Object |
   | Source | Code represented as a string |
  "

  {:author "Adam Helinski"}

  (:require [clojure.core.protocols])
  (:import (convex.core.data ABlob
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
           convex.core.lang.expanders.AExpander
           convex.core.lang.Reader)
  (:refer-clojure :exclude [read-string]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Converting text to Convex Lisp


(defn read-string

  "Converts Convex Lisp **source** to **DDVs**."

  [source]

  (let [parsed (Reader/readAll source)]
    (if (second parsed)
      (.cons parsed
             (Symbol/create "do"))
      (first parsed))))


;;;;;;;;;; Converting Convex Lisp to Clojure


(extend-protocol clojure.core.protocols/Datafiable


  nil

    (datafy [_this]
      nil)


  convex.core.data.ABlob

    (datafy [this]
      (list 'blob
            (.toHexString this)))

  
  convex.core.data.Address

    (datafy [this]
      (list 'address
            (.longValue this)))


  convex.core.data.AList

    (datafy [this]
      (map clojure.core.protocols/datafy
           this))


  convex.core.data.AMap

    (datafy [this]
      (reduce (fn [hmap [k v]]
                (assoc hmap
                       (clojure.core.protocols/datafy k)
                       (clojure.core.protocols/datafy v)))
              {}
              this))


  convex.core.data.ASet

    (datafy [this]
      (into #{}
            (map clojure.core.protocols/datafy)
            this))


  convex.core.data.AString

    (datafy [this]
      (.toString this))

  
  convex.core.data.AVector

    (datafy [this]
      (mapv clojure.core.protocols/datafy
            this))


  convex.core.data.Keyword

    (datafy [this]
      (-> this
          .getName
          clojure.core.protocols/datafy
          keyword))


  convex.core.data.Symbol

    (datafy [this]
      (symbol (some-> (.getNamespace this)
                      (-> .getName
                          clojure.core.protocols/datafy))
              (-> this
                  .getName
                  clojure.core.protocols/datafy)))


  convex.core.data.prim.CVMBool

    (datafy [this]
      (.booleanValue this))


  convex.core.data.prim.CVMByte

    (datafy [this]
      (.longValue this))


  convex.core.data.prim.CVMChar

    (datafy [this]
      (.longValue this))


  convex.core.data.prim.CVMDouble

    (datafy [this]
      (.doubleValue this))


  convex.core.data.prim.CVMLong

    (datafy [this]
      (.longValue this))
  )



(defn convex->clojure

  "Translate Convex **DDV** into Clojure data."

  [ddv]

  (clojure.core.protocols/datafy ddv))
