(ns convex.cvm

  "A CVM context is needed for compiling and executing Convex code.

   It can be created using [[ctx]].
  
   This namespace provide all needed utilities for such endeavours as well few functions for
   querying useful properties, such as [[juice]].
  
   A context is mostly immutable, albeit some aspects such as juice tracking are mutable for performance
   reason. Operations that modifies a context (expansion, compilation, or any form of execution) returns
   a new instance and the old one should be discarded

   Such operations consume juice and lead either to a successful [[result]] or to an [[error]]. Functions that
   do not return a context (eg. [[env]], [[juice]]) do not consume juice.

   Result objects (Convex objects) can be datafied with [[convex.lisp/datafy]] for easy consumption from Clojure."

  {:author "Adam Helinski"}

  (:import convex.core.Init
           (convex.core.data AccountStatus
                             Address
                             AHashMap
                             Symbol)
           (convex.core.lang Context
                             Reader))
  (:refer-clojure :exclude [compile
                            eval
                            read])
  (:require [clojure.core.protocols]
            [convex.cvm.type         :as $.cvm.type]
            [convex.lisp             :as $.lisp]))


(set! *warn-on-reflection*
      true)


(declare run)


;;;;;;;;;; Creating a new context


(defn ctx

  "Creates a \"fake\" context, ideal for testing and repl'ing around."


  (^Context []

   (ctx Init/HERO))


  (^Context [account]

   (ctx (Init/createState)
        account))

  
  (^Context [state account]

   (Context/createFake state
                       account)))



(defn fork

  "Duplicates the given context (very cheap).

   Any operation on the returned copy has no impact on the original context."

  ^Context [^Context ctx]

  (.fork ctx))


;;;;;;;;;; Querying context properties


(defn account

  "Returns the account for the given `address` (or the return value of [[address]] if none is provided)."

  
  (^AccountStatus [^Context ctx]

   (.getAccountStatus ctx))


  (^AccountStatus [^Context ctx address]

    (.getAccountStatus ctx
                       (cond->
                         address
                         (number? address)
                         $.cvm.type/address))))



(defn address
  
  "Returns the executing address of the given `ctx`."

  ^Address

  [^Context ctx]

  (.getAddress ctx))



(defn env

  "Returns the environment of the executing account attached to `ctx`."


  (^AHashMap [^Context ctx]

   (.getEnvironment ctx))


  (^AHashMap [ctx address]

   (.getEnvironment (account ctx
                             address))))



(defn exception

  "The CVM enters in exceptional state in case of error or particular patterns such as
   halting or doing a rollback.

   Returns the current exception or nil if `ctx` is not in such a state meaning that [[result]]
   can be safely used."

  [^Context ctx]

  (when (.isExceptional ctx)
    (.getExceptional ctx)))



(defn exception?

  "Returns true if the given `ctx` is in an exceptional state.

   See [[exception]]."

  [^Context ctx]

  (.isExceptional ctx))



(defn juice

  "Returns the remaining amount of juice available for the executing account.
  
   See [[set-juice]]."

  [^Context ctx]

  (.getJuice ctx))



(defn log

  "Returns the log of `ctx` (a map of `address` -> `vector of values)."

  [^Context ctx]

  (.getLog ctx))



(defn result

  "Extracts the result (eg. after expansion, compilation, execution, ...) wrapped in a `ctx`.
  
   Throws if the `ctx` is in an exceptional state. See [[exception]]."

  [^Context ctx]

  (.getResult ctx))



(defn state

  "Returns the whole CVM state associated with `ctx`."

  [^Context ctx]

  (.getState ctx))


;;;;;;;;;; Modifying context properties after fork


(defn set-juice

  "Forks and sets the juice of the copied context to the requested amount"

  [^Context ctx juice]

  (.withJuice (fork ctx)
              juice))


;;;;;;;;;; Phase 1 - Reading Convex Lisp 


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
      $.lisp/src
      read))


;;;;;;;;;; Phase 2 & 3 - Expanding Convex objects and compiling into operations


(defn compile

  "Compiles an expanded Convex object using the given `ctx`.

   Object must be canonical (all items are fully expanded). See [[expand]].
  
   See [[run]] for execution after compilation.

   Returns `ctx`, result being the compiled object."


  (^Context [ctx]

    (compile ctx
             (result ctx)))


  (^Context [^Context ctx canonical-object]

   (.compile ctx
             canonical-object)))



(defn expand

  "Expands a Convex object so that it is canonical (fully expanded and ready for compilation).

   Usually run before [[compile]] with the result from [[read]].
  
   Returns `ctx`, result being the expanded object."


  (^Context [ctx]

   (expand ctx
           (result ctx)))


  (^Context [^Context ctx object]

   (.expand ctx
            object)))



(defn expand-compile

  "Chains [[expand]] and [[compile]] while being slightly more efficient than calling both separately.
  
   See [[run]] for execution after compilation.

   Returns `ctx`, result being the compiled object."

  
  (^Context [ctx]

   (expand-compile ctx
                   (result ctx)))


  (^Context [^Context ctx object]

   (.expandCompile ctx
                   object)))


;;;;;;;;;; Pahse 4 - Executing compiled code


(defn eval

  "Evaluates the given form after fully expanding and compiling it.
  
   Returns `ctx`, result being the evaluated object."


  (^Context [ctx]

   (eval ctx
         (result ctx)))


  (^Context [ctx object]

   (run (expand-compile ctx
                        object))))



(defn query

  "Like [[run]] but the resulting state is discarded.

   Returns `ctx`, result being the evaluated object in query mode."


  (^Context [ctx]

   (if (exception? ctx)
     ctx
     (query ctx
            (result ctx))))


  (^Context [^Context ctx compiled-object]

   (.query ctx
           compiled-object)))



(defn run

  "Runs compiled Convex code.
  
   Usually run after [[compile]].
  
   Returns `ctx`, result being the evaluated object."


  (^Context [ctx]

   (if (exception? ctx)
     ctx
     (run ctx
          (result ctx))))


  (^Context [^Context ctx compiled]

   (.run ctx
         compiled)))


;;;;;;;;;; Converting Convex -> Clojure


(defn as-clojure

  "Converts a Convex object into Clojure data."

  [object]

  (clojure.core.protocols/datafy object))



(extend-protocol clojure.core.protocols/Datafiable


  nil

    (datafy [_this]
      nil)


  convex.core.data.ABlob

    (datafy [this]
      ($.lisp/blob (.toHexString this)))

  
  convex.core.data.Address

    (datafy [this]
      ($.lisp/address (.longValue this)))


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
      {:convex.error/code    (clojure.core.protocols/datafy (.getCode this))
       :convex.error/message (clojure.core.protocols/datafy (.getMessage this))
       :convex.error/trace   (clojure.core.protocols/datafy (mapv clojure.core.protocols/datafy
                                                                  (.getTrace this)))})


  ;; TODO. Use EDN? Ops have protected fields meaning they cannot be readily translated.
  ;;
  convex.core.lang.impl.Fn

    (datafy [this]
      (-> this
          .toString
          read
          clojure.core.protocols/datafy)))
