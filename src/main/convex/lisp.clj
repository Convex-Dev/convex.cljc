(ns convex.lisp

  "Reading, compiling, and executing source + conversion to EDN and Clojure data structures.
  
   The result of context operation can be retrieved using [[result]]."

  {:author "Adam Helinski"}

  (:require [clojure.core.protocols]
            [clojure.tools.reader.edn]
            [clojure.walk])
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

  "Converts Convex Lisp source to a Convex form.
  
   See [[to-clojure]], [[expand]]."

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



(defn result

  "Extracts the result (eg. after expansion, compilation, execution, ...) wrapped in a [[context]]."

  [^Context context]

  (.getResult context))


;;;;;;;;;; Compiling Convex data


(defn compile

  "Compile a form using the given context.
  
   Form is extracted from context using [[result]] if none is given and must be canonical (all items are
   fully expanded).
  
   Usually run after [[expand]].
  
   Result can be executed. See [[run]]."


  (^Context [context]

   (compile context
            (result context)))


  (^Context [^Context context canonical-form]

   (.compile context
             canonical-form)))



(defn expand

  "Expands a Convex form so that it is canonical (fully expanded and ready for compilation).

   Usually run before [[compile]] with the result from [[read]]
  
   Fake [[context]] is created if none is provided."


  (^Context [form]

   (expand (context)
           form))


  ([^Context context form]

   (.expand context
            form)))



(defn expand-compile

  "Chains [[expand]] and [[compile]] while being slightly more efficient than calling both separately.
  
   Result can be executed. See [[run]]."


  (^Context [form]

   (expand-compile (context)
                   form))


  (^Context [^Context context form]

   (.expandCompile context
                   form)))


;;;;;;;;;; Execution


(defn eval

  "Evaluates the given form after fully expanding and compiling it.
  
   Fake [[context]] is created if none is provided."


  (^Context [form]

   (eval (context)
         form))


  (^Context [context form]

   (-> context
       (expand-compile form)
       run)))



(defn query

  "Like [[run]] but the resulting state is discarded."


  (^Context [context]

   (query context
          (result context)))


  (^Context [^Context context form]

   (.query context
           form)))



(defn run

  "Runs compiled Convex code.
  
   Fetches code using [[result]] when not explicitly provided.
  
   Usually run after [[compile]]."


  (^Context [context]

   (run context
        (result context)))


  (^Context [^Context context compiled]

   (.run context
         compiled)))


;;;;;;;;;; Converting Convex Lisp to Clojure


(defn from-clojure

  "Stringifies the given clojure form and applies the result to [[read]]."

  [clojure-form]

  (-> clojure-form
      str
      read))



(defn to-clojure

  "Converts Convex data to Clojure data."

  [form]

  (clojure.core.protocols/datafy form))



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


  ;; TODO. Use EDN? Ops have protected fields meaning they cannot be readily translated.
  ;;
  convex.core.lang.impl.Fn

    (datafy [this]
      (-> this
          .toString
          read-string
          clojure.core.protocols/datafy )))


;;;;;;;;;; Dealing with EDN


(defn read-edn

  "Reads a string of Convex form expressed as EDN.
  
   Opposite of [[to-edn]]."

  [string]

  (clojure.tools.reader.edn/read-string {:readers {'account    (fn [account]
                                                                 [:convex/account
                                                                  account])
                                                   'addr       (fn [address]
                                                                 (symbol (str "#"
                                                                              address)))
                                                   'blob       (fn [blob]
                                                                 ;;
                                                                 ;; TODO. Cannot easily convert to hexstring, see #63.
                                                                 ;;
                                                                 (list 'blob
                                                                       blob))
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
                                                                   (list 'syntax
                                                                         datum
                                                                         mta)
                                                                   datum))}}
                                        string))



(defn to-edn

  "Translates a Convex form into an EDN string.
  
   Opposite of [[read-edn]]."
  
  [^ACell form]

  (.ednString form))


;;;;;;;;;; Working with Clojure forms expressing Convex Lisp code


(defn prepare-clojure

  "Prepares a Clojure form so that it can be later transformed into Convex Lisp source
   using [[str-clojure]]."

  [clojure-form]

  (clojure.walk/postwalk (fn [x]
                           (if (seq? x)
                             (condp =
                                    (first x)
                               'address (let [arg (second x)]
                                          (if (int? arg)
                                            (symbol (str "#"
                                                         (second x)))
                                            x))
                               'blob    (let [arg (second x)]
                                          (if (string? arg)
                                            (symbol (str "0x"
                                                         arg))
                                            x))
                               x)
                             (if (and (double? x)
                                      (Double/isNaN x))
                               (list 'unquote
                                     'NaN)
                               x)))
                         clojure-form))



(defn str-clojure

  "Converts a Clojure form expressing Convex Lisp code into a source string.
  
   Should be used after [[prepare-clojure]]."

  [clojure-form]

  (pr-str clojure-form))



(defn clojure->source

  "Converts a Clojure form expressing Convex Lisp code into a source string by feeding it to
   [[prepare-clojure]] and [[str-clojure]]."

  [clojure-form]

  (-> clojure-form
      prepare-clojure
      str-clojure))



(defn quote-clojure

  "Makes the given `clojure-form` quoted when converted into Convex Lisp source."

  [clojure-form]

  (list 'quote
        clojure-form))
