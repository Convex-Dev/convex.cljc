(ns convex.cvm

  "A CVM context is needed for compiling and executing Convex code.

   It can be created using [[ctx]].
  
   This namespace provide all needed utilities for such endeavours as well functions for querying and altering useful properties. 

   While the design of a context is mostly immutable, quite a few operations are mutable. Whenever a function takes a context and
   returns one, the original one must be discarded (besides in [[fork]] for obvious reasons).
  
   Such operations consume juice and lead either to a successful [[result]] or to an [[exception]]. Functions that
   do not return a context (eg. [[env]], [[juice]]) do not consume juice."

  {:author "Adam Helinski"}

  (:import (convex.core Block
                        State)
           (convex.core.data ABlobMap
                             AccountStatus
                             ACell
                             Address
                             AHashMap)
           (convex.core.data.prim CVMLong)
           (convex.core.init Init)
           (convex.core.lang AFn
                             AOp
                             Context)
           (convex.core.lang.impl ErrorValue))
  (:refer-clojure :exclude [compile
                            def
                            eval
                            time])
  (:require [convex.data :as $.data]))


(set! *warn-on-reflection*
      true)


(declare juice-set
         run
         state-set)


;;;;;;;;;; Creating a new context


(defn ctx

  "Creates a \"fake\" context. Ideal for testing and repl'ing around."


  (^Context []

   (ctx nil))

  
  (^Context [option+]

   (Context/createFake (or (:convex.cvm/state option+)
                           (Init/createState [($.data/key ($.data/blob (byte-array 32)))]))
                       (or (:convex.cvm/address option+)
                           Init/RESERVED_ADDRESS))))



(defn fork

  "Duplicates the given [[ctx]] (very cheap).

   Any operation on the returned copy has no impact on the original context.
  
   Attention, forking a `ctx` looses any attached result or exception."

  ^Context [^Context ctx]

  (.fork ctx))


;;;;;;;;;; Querying context properties


(defn- -wrap-address

  ;; Wraps `x` in an Address object if it is not already.

  ^Address

  [x]

  (cond->
    x
    (number? x)
    $.data/address))



(defn account

  "Returns the account for the given `address` (or the return value of [[address]] if none is provided)."

  
  (^AccountStatus [^Context ctx]

   (.getAccountStatus ctx))


  (^AccountStatus [^Context ctx address]

   (.getAccountStatus ctx
                      (-wrap-address address))))



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
   can be safely used.
  
   An exception code can be provided as a filter, meaning that even if an exception occured, this
   functions will return nil unless that exception has the given `code`.
  
   Also see [[code-std*]] for easily retrieving an official error code. Note that in practice, unlike the CVM
   itself or any of the core function, a user Convex function can return anything as a code."


  ([^Context ctx]

   (when (.isExceptional ctx)
     (.getExceptional ctx)))


  ([^ACell code ^Context ctx]

   (when (.isExceptional ctx)
     (let [e (.getExceptional ctx)]
       (when (= (.getCode e)
                code)
         e)))))



(defn exception?

  "Returns true if the given `ctx` is in an exceptional state.

   See [[exception]]."


  ([^Context ctx]

   (.isExceptional ctx))


  ([^ACell code ^Context ctx]

   (if (.isExceptional ctx)
     (= code
        (.getCode (.getExceptional ctx)))
     false)))



(defn juice

  "Returns the remaining amount of juice available for the executing account.
  
   Also see [[juice-set]]."

  [^Context ctx]

  (.getJuice ctx))



(defn log

  "Returns the log of `ctx` (a CVM vector of size 2 vectors containing a logging address
   and a logged value)."


  ^ABlobMap
  
  [^Context ctx]

  (.getLog ctx))



(defn result

  "Extracts the result (eg. after expansion, compilation, execution, ...) wrapped in a `ctx`.
  
   Throws if the `ctx` is in an exceptional state. See [[exception]]."

  [^Context ctx]

  (.getResult ctx))



(defn state

  "Returns the whole CVM state associated with `ctx`.
  
   Also see [[state-set]]."

  ^State

  [^Context ctx]

  (.getState ctx))



(defn time

  "Returns the current timestamp (Unix epoch in milliseconds as CVM long) assigned to the state in the given `ctx`.
  
   Also see [[timestamp-set]]."

  ^CVMLong

  [^Context ctx]

  (-> ctx
      state
      .getTimeStamp))


;;;;;;;;;; Modifying context properties


(defn account-create

  "Creates an new account, with a `key` (user) or without (actor).

   See [[convex.data/key]].
  
   Address is attached as a result in the returned context."


  (^Context [^Context ctx]

   (.createAccount ctx
                   nil))


  (^Context [^Context ctx key]

   (.createAccount ctx
                   key)))



(defn def

  "Like calling `(def sym value)` in Convex Lisp, either in the current address of the given one.

   Argument is a map of `CVM symbol` -> `CVM value`."


  (^Context [ctx sym->value]

   (convex.cvm/def ctx
                   (address ctx)
                   sym->value))


  (^Context [^Context ctx addr sym->value]

   (let [s (state ctx)
         a (.getAccount s
                        addr)]
     (if a
       (state-set ctx
                  (.putAccount s
                               addr
                               (.withEnvironment a
                                                 (reduce (fn [^AHashMap env [^ACell sym ^ACell value]]
                                                           (.assoc env
                                                                   sym
                                                                   value))
                                                         (.getEnvironment a)
                                                         sym->value))))
       ctx))))



(defn deploy

  "Deploys the given `code` as an actor.
  
   Returns a context that is either [[exception]]al or has the address of the successfully created actor
   attached as a [[result]]."

  ^Context

  [^Context ctx code]

  (.deployActor ctx
                code))



(defn exception-clear

  "Removes the currently attached exception from the given `ctx`."

  ^Context

  [^Context ctx]

  (.withException ctx
                  nil))



(defn juice-preserve

  "Executes `(f ctx)`, `f` being a function `ctx` -> `ctx`.
  
   The returned `ctx` will have the same amount of juice as the original."

  ^Context

  [ctx f]

  (let [juice- (juice ctx)]
    (.withJuice ^Context (f ctx)
                juice-)))



(defn juice-refill

  "Refills juice to maximum.

   Also see [[juice-set]]."

  ^Context

  [^Context ctx]

  (juice-set ctx
             Long/MAX_VALUE))



(defn juice-set

  "Sets the juice of the given `ctx` to the requested `amount`.
  
   Also see [[juice]], [[juice-refill]]."

  [^Context ctx amount]

  (.withJuice ctx
              amount))



(defn state-set

  "Replaces the CVM state in the `ctx` with the given one.
  
   See [[state]]."

  ^Context

  [^Context ctx state]

  (.withState ctx
              state))



(defn time-advance

  "Advances the timestamp in the state of `ctx` by `millis` milliseconds.
  
   Does not do anything if `millis` is < 0.
  
   See [[time]]."

  ^Context

  [^Context ctx millis]

  (state-set ctx
             (-> ctx
                 state
                 (.applyBlock (Block/create (long (+ (.longValue (time ctx))
                                                     millis))
                                            ($.data/key ($.data/blob (byte-array 32)))
                                            ($.data/vector [])))
                 .getState)))



(defn undef

  "Like calling `(undef sym)` in Convex Lisp, either in the current account or the given one, repeatedly
   on any CVM symbol in `sym+`."


  (^Context [ctx sym+]

   (undef ctx
          (address ctx)
          sym+))


  (^Context [^Context ctx addr sym+]

   (let [s (state ctx)
         a (.getAccount s
                        addr)]
     (if a
       (state-set ctx
                  (.putAccount s
                               addr
                               (.withEnvironment a
                                                 (reduce (fn [^AHashMap env ^ACell sym]
                                                           (.dissoc env
                                                                    sym))
                                                         (.getEnvironment a)
                                                         sym+))))
       ctx))))


;;;;;;;;;; Phase 1 & 2 - Expanding Convex objects and compiling into operations


(defn compile

  "Compiles an expanded Convex object using the given `ctx`.

   Object must be canonical (all items must be fully expanded). See [[expand]].
  
   See [[run]] for execution after compilation.

   Returns `ctx`, attached [[result]] being the compiled object."


  (^Context [ctx]

    (compile ctx
             (result ctx)))


  (^Context [^Context ctx canonical-object]

   (.compile ctx
             canonical-object)))



(defn expand

  "Expands a Convex object so that it is canonical (fully expanded and ready for compilation).

   Usually run before [[compile]] with the result from [[read]].
  
   Returns `ctx`, attached [[result]] being the expanded object."


  (^Context [ctx]

   (expand ctx
           (result ctx)))


  (^Context [^Context ctx object]

   (.expand ctx
            object)))



(defn expand-compile

  "Chains [[expand]] and [[compile]] while being slightly more efficient than calling both separately.
  
   See [[run]] for execution after compilation.

   Returns `ctx`, attached [[result]] being the compiled object."

  
  (^Context [ctx]

   (expand-compile ctx
                   (result ctx)))


  (^Context [^Context ctx object]

   (.expandCompile ctx
                   object)))


;;;;;;;;;; Pahse 3 - Executing compiled code


(defn eval

  "Evaluates the given form after fully expanding and compiling it.
  
   Returns `ctx`, attached [[result]] being the evaluated object."


  (^Context [ctx]

   (eval ctx
         (result ctx)))


  (^Context [ctx object]

   (run (expand-compile ctx
                        object))))



(defn query

  "Like [[run]] but the resulting state is discarded, as if nothing happened.

   Returns `ctx`, attached [[result]] being the evaluated object in query mode."


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
  
   Returns `ctx`, attached [[result]] being the evaluated object."


  (^Context [ctx]

   (if (exception? ctx)
     ctx
     (run ctx
          (result ctx))))


  (^Context [^Context ctx ^AOp compiled]

   (.run ctx
         compiled)))


;;;;;;;;;; Functions


(defmacro arg+*

  "See [[invoke]]."

  [& arg+]

  (let [sym-arr (gensym)]
    `(let [~sym-arr ^"[Lconvex.core.data.ACell;" (make-array ACell
                                                             ~(count arg+))]
       ~@(map (fn [i arg]
                `(aset ~sym-arr
                       ~i
                       ~arg))
              (range)
              arg+)
       ~sym-arr)))



(defn invoke

  "Invokes the given CVM `f`unction using the given `ctx`.

   `arg+` is a Java array of CVM objects. See [[arg+*]] for easily and efficiently creating one.
  
   Like other code-related functions, returns a context with either a [[result]] or an [[exception]] attached."

  ^Context

  [^Context ctx ^AFn f arg+]

  (let [ctx-2 (.invoke ctx
                       f
                       arg+)
        ex    (exception ctx-2)]
    (if ex
      (if (instance? ErrorValue
                     ex)
        ctx-2
        (exception-clear ctx-2))
      ctx-2)))
