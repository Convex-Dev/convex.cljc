(ns convex.lisp

  "Reading, compiling, and executing Convex Lisp source.
  
   The result of context operation can be retrieved using [[result]]."

  {:author "Adam Helinski"}

  (:require [clojure.core.protocols]
            [clojure.walk]
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


;;;;;;;;;; Converting text to Convex Lisp


(defn read

  "Converts Convex Lisp source to a Convex object.
  
   See [[datafy]], [[expand]], for further usage."

  [string]

  (let [parsed (Reader/readAll string)]
    (if (second parsed)
      (.cons parsed
             (Symbol/create "do"))
      (first parsed))))


;;;;;;;;;; Handling a Convex context


(defn context

  "Creates a fake variant of `convex.core.Context` needed for compilation and execution."


  (^Context []

   (context Init/HERO))


  (^Context [account]

   (context Init/STATE
            account))

  
  (^Context [state account]

   (Context/createFake state
                       account)))



(defn exceptional

  "Returns the current exceptional value attached to the given `context` if it is indeed
   in an exceptional state, nil otherwise."

  [^Context context]

  (when (.isExceptional context)
    (.getExceptional context)))



(defn result

  "Extracts the result (eg. after expansion, compilation, execution, ...) wrapped in a [[context]].
  
   Throws if the [[context]] is in an exceptional state. See [[exceptional]]."

  [^Context context]

  (.getResult context))


;;;;;;;;;; Compiling Convex data


(defn compile

  "Compiles an expanded Convex object using the given `context`.
  
   Object is extracted from context using [[result]] if none is given and must be canonical (all items are
   fully expanded). See [[expand]].
  
   See [[run]] for execution after compilation.

   Returns [[context]]. See [[result]]."


  (^Context [context]

   (compile context
            (result context)))


  (^Context [^Context context canonical-object]

   (.compile context
             canonical-object)))



(defn expand

  "Expands a Convex object so that it is canonical (fully expanded and ready for compilation).

   Usually run before [[compile]] with the result from [[read]].
  
   Fake [[context]] is created if none is provided.
  
   Returns [[context]]. See [[result]]."


  (^Context [form]

   (expand (context)
           form))


  ([^Context context form]

   (.expand context
            form)))



(defn expand-compile

  "Chains [[expand]] and [[compile]] while being slightly more efficient than calling both separately.
  
   See [[run]] for execution after compilation.

   Returns [[context]]. See [[result]]."


  (^Context [form]

   (expand-compile (context)
                   form))


  (^Context [^Context context form]

   (.expandCompile context
                   form)))


;;;;;;;;;; Execution


(defn eval

  "Evaluates the given form after fully expanding and compiling it.
  
   Returns [[context]]. See [[result]]."


  (^Context [object]

   (eval (context)
         object))


  (^Context [context object]

   (-> context
       (expand-compile object)
       run)))



(defn query

  "Like [[run]] but the resulting state is discarded.
  
   Returns [[context]]. See [[result]]."


  (^Context [context]

   (query context
          (result context)))


  (^Context [^Context context compiled]

   (.query context
           compiled)))



(defn run

  "Runs compiled Convex code.
  
   Fetches code using [[result]] when not explicitly provided.
  
   Usually run after [[compile]].
  
   Returns [[context]]. See [[result]]."


  (^Context [context]

   (run context
        (result context)))


  (^Context [^Context context compiled]

   (.run context
         compiled)))


;;;;;;;;;; Converting Convex Lisp to Clojure


(defn datafy

  "Converts a Convex object into Clojure data."

  [object]

  (clojure.core.protocols/datafy object))



(defn read-form

  "Stringifies the given Clojure form and applies the result to [[read]]."

  [form]

  (-> form
      $.form/source
      read))



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
