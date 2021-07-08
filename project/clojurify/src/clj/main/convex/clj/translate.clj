(ns convex.clj.translate

  "Translation of Convex data into Clojure data and EDN."

  {:author "Adam Helinski"}

  (:require [clojure.core.protocols]
            [convex.clj              :as $.clj]
            [convex.read             :as $.read]))


;;;;;;;;;; Converting Convex -> Clojure


(defn cvx->clj

  "Translates Convex data into Clojure data.
  
   See [[convex.clj]] namespace for more information on how objects that do not translate directly to Clojure look like (eg. addresses).

   Attention, one rare but existing pitfall has been detected: in Clojure, sequential data structures are comparable, not in Convex. In other words,
   the following map has 2 key-values in Convex but only 1 in Clojure (second replaces the first one):

   ```clojure
   {[1]  :vector
    '(1) :list}}
   ```"

  [object]

  (clojure.core.protocols/datafy object))



(extend-protocol clojure.core.protocols/Datafiable


  nil

    (datafy [_this]
      nil)


  convex.core.data.ABlob

    (datafy [this]
      ($.clj/blob (.toHexString this)))

  
  convex.core.data.Address

    (datafy [this]
      ($.clj/address (.longValue this)))


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
      (symbol (.getName this)))


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
      (char (.longValue this)))


  convex.core.data.prim.CVMDouble

    (datafy [this]
      (.doubleValue this))


  convex.core.data.prim.CVMLong

    (datafy [this]
      (.longValue this))


  convex.core.lang.impl.CoreFn

    (datafy [this]
      (clojure.core.protocols/datafy (.getSymbol this)))


  convex.core.lang.impl.ErrorValue

    (datafy [this]
      {:convex.exception/code    (clojure.core.protocols/datafy (.getCode this))
       :convex.exception/message (clojure.core.protocols/datafy (.getMessage this))
       :convex.exception/trace   (clojure.core.protocols/datafy (mapv clojure.core.protocols/datafy
                                                                      (.getTrace this)))})


  ;; TODO. Use EDN? Ops have protected fields meaning they cannot be readily translated.
  ;;
  convex.core.lang.impl.Fn

    (datafy [this]
      (-> this
          .toString
          $.read/string
          clojure.core.protocols/datafy)))


;;;;;;;;;; Converting Convex -> EDN


(defn cvx->edn

  "Translates Convex data into an EDN string
  
   Attention, the EDN representations of Convex objects are currently lacking and unstable."
  
  [^convex.core.data.ACell form]

  (.ednString form))
