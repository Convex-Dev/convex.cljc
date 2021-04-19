(ns convex.lisp

  ""

  {:author "Adam Helinski"}

  (:require [clojure.core.protocols]
            [clojure.tools.reader.edn])
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
           convex.core.lang.expanders.AExpander
           (convex.core.lang.impl CoreFn
                                  Fn)
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



(defn convex->clojure

  "Translate Convex Lisp into Clojure data."

  [convex-code]

  (clojure.core.protocols/datafy convex-code))



(defn convex->edn

  "Translates Convex Lisp into an EDN string."
  
  [^ACell convex-code]

  (.ednString convex-code))



(defn read-edn

  ""

  [string]

  (clojure.tools.reader.edn/read-string {:readers {'account (fn [account]
                                                              [:convex/account
                                                               account])
                                                   'addr   (fn [address]
                                                             (list 'address
                                                                   address))
                                                   'blob   (fn [blob]
                                                             (list 'blob
                                                                   blob))
                                                   'syntax (fn [{:keys [datum]
                                                                 mta   :meta}]
                                                             (if (and (seq mta)
                                                                      (not (second mta))
                                                                      (nil? (get mta
                                                                                 :start)))
                                                               (list 'syntax
                                                                     datum
                                                                     mta)
                                                               datum))}}
                                        string))
                                        

  
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


  convex.core.data.Syntax

    (datafy [this]
      (let [mta   (.getMeta this)
            value (-> this 
                      .getValue
                      clojure.core.protocols/datafy)]
        (if (seq mta)
          (list 'syntax
                value
                (clojure.core.protocols/datafy mta))
          value)))



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


  convex.core.lang.impl.CoreFn

    (datafy [this]
      (clojure.core.protocols/datafy (.getSymbol this)))


  ;; TODO. Use EDN? Ops have protected fields meaning they cannot be readily translated.
  ;;
  convex.core.lang.impl.Fn

    (datafy [this]
      (-> this
          convex->edn
          read-edn))
  )


;;;;;;;;;; Executing code on CVM



