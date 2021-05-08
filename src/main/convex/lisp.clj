(ns convex.lisp

  "Reading, compiling, and executing Convex Lisp source.

   Most operations of this namespace involve a \"context\". Relaties utilities
   for creating a context and extracting related information are located in the
   [[convex.lisp.ctx]] namespace while this namespace revolves around Convex Lisp code."

  {:author "Adam Helinski"}

  (:require [clojure.core.protocols]
            [clojure.walk]
            [convex.lisp.ctx         :as $.ctx]
            [convex.lisp.form        :as $.form])
  (:import convex.core.Init
           (convex.core.data ABlob
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
           convex.core.lang.impl.CoreFn
           (convex.core.lang Context
                             Reader))
  (:refer-clojure :exclude [compile
                            eval
                            read]))


(set! *warn-on-reflection*
      true)


(declare run)


;;;;;;;;;; Reading Convex Lisp source


(defn read

  "Converts Convex Lisp source to a Convex object.
  
   See [[datafy]], [[expand]], for further usage."

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
      $.form/source
      read))


;;;;;;;;;; Compiling Convex data


(defn compile

  "Compiles an expanded Convex object using the given `ctx`.
  
   Object is extracted from context using [[convex.lisp.ctx/result]] if none is given and
   must be canonical (all items are fully expanded). See [[expand]].
  
   See [[run]] for execution after compilation.

   Returns `ctx`, result being the compiled object."


  (^Context [ctx]

   (compile ctx
            ($.ctx/result ctx)))


  (^Context [^Context ctx canonical-object]

   (.compile ctx
             canonical-object)))



(defn expand

  "Expands a Convex object so that it is canonical (fully expanded and ready for compilation).

   Usually run before [[compile]] with the result from [[read]].
  
   Fake `ctx` is created if none is provided.
  
   Returns `ctx`, result being the expanded object."


  (^Context [object]

   (expand ($.ctx/create-fake)
           object))


  ([^Context ctx object]

   (.expand ctx
            object)))



(defn expand-compile

  "Chains [[expand]] and [[compile]] while being slightly more efficient than calling both separately.
  
   See [[run]] for execution after compilation.

   Fake `ctx` is created if none is provided.

   Returns `ctx`, result being the compiled object."


  (^Context [object]

   (expand-compile ($.ctx/create-fake)
                   object))


  (^Context [^Context ctx object]

   (.expandCompile ctx
                   object)))


;;;;;;;;;; Execution


(defn eval

  "Evaluates the given form after fully expanding and compiling it.
  
   Fake `ctx` is created if none is provided.

   Returns `ctx`, result being the evaluated object."


  (^Context [object]

   (eval ($.ctx/create-fake)
         object))


  (^Context [ctx object]

   (-> ctx
       (expand-compile object)
       run)))



(defn query

  "Like [[run]] but the resulting state is discarded.

   Returns `ctx`, result being the evaluated object in query mode."


  (^Context [ctx]

   (query ctx
          ($.ctx/result ctx)))


  (^Context [^Context ctx compiled]

   (.query ctx
           compiled)))



(defn run

  "Runs compiled Convex code.
  
   Fetches code using [[convex.lisp.ctxresult]] when not explicitly provided.
  
   Usually run after [[compile]].
  
   Returns `ctx`, result being the evaluated object."


  (^Context [ctx]

   (run ctx
        ($.ctx/result ctx)))


  (^Context [^Context ctx compiled]

   (.run ctx
         compiled)))


;;;;;;;;;; Converting Convex Lisp to Clojure


(defn datafy

  "Converts a Convex object into Clojure data."

  [object]

  (clojure.core.protocols/datafy object))



(extend-protocol clojure.core.protocols/Datafiable


  nil

    (datafy [_this]
      nil)


  convex.core.data.ABlob

    (datafy [this]
      (symbol (str "0x"
                   (.toHexString this))))

  
  convex.core.data.Address

    (datafy [this]
      (symbol (str "#"
                   (.longValue this))))


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
      (let [name (.getName this)
            path (some-> (.getPath this)
                         .toString)]
        (symbol (if path
                  (str path
                       \/
                       name)
                  (str name)))))


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
      (unchecked-byte (.longValue this)))


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


  ;; TODO. Use EDN? Ops have protected fields meaning they cannot be readily translated.
  ;;
  convex.core.lang.impl.Fn

    (datafy [this]
      (-> this
          .toString
          read
          clojure.core.protocols/datafy)))
