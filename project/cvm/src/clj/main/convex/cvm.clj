(ns convex.cvm

  "Code execution in the CVM, altering state, and gaining insights.

   Central entities of this namespaces are contextes and they can be created using [[ctx]].

   All other functions revolve around them. While the design of a context is mostly immutable, whenever an altering function
   is applied (eg. [[juice-set]]) or code is handled in any way (eg. [[eval]]), old context must be discarded and only returned
   one should be used.

   Cheap copies can be created using [[fork]].

   Actions involving code (eg. [[compile]], [[exec]], ...) return a new context which holds either a [[result]] or an [[exception]].
   Those actions always consume [[juice]].

   Given that a \"cell\" is the term reserved for CVM data and objects, execution consists of following steps:

   | Step | Function | Does |
   |---|---|---|
   | 1 | [[expand]] | `cell` -> `canonical cell`, applies macros |
   | 2 | [[compile]] | `canonical cell` -> `op`, preparing executable code |
   | 3 | [[exec]] | Executes compiled code |

   Any cell can be applied safely to those functions, worse that can happen is nothing (eg. providing an already compiled cell to
   [[compile]]).

   If fine-grained control is not needed and if source is not compiled anyways, a simpler alternative is to use [[eval]] which does
   all the job."

  {:author "Adam Helinski"}

  (:import (convex.core Block
                        State)
           (convex.core.data ABlobMap
                             AccountKey
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
                            key
                            time])
  (:require [convex.cell :as $.cell]))


(set! *warn-on-reflection*
      true)


(declare juice-set
         state-set)


;;;;;;;;;; Creating a new context


(defn ctx

  "Creates a \"fake\" context. Ideal for testing and repl'ing around.
  
   An optional map of options may be provided:

   | Key | Value | Default |
   |---|---|---|
   | `:convex.cvm/address` | Address of the executing account | Reserved address (an actor) |
   | `:convex.cvm/state` | Genesis state | Initial state with Convex actors and libraries |
   | `:convex.peer/key  | Account key for the initial peer (see [[convex.cell/account-key]]) | Fake key, all 0's |"


  (^Context []

   (ctx nil))

  
  (^Context [option+]

   (Context/createFake (or (:convex.cvm/state option+)
                           (Init/createState [(or (:convex.peer/key option+)
                                                  ($.cell/key ($.cell/blob (byte-array 32))))]))
                       (or (:convex.cvm/address option+)
                           Init/RESERVED_ADDRESS))))



(defn fork

  "Duplicates the given [[ctx]] (very cheap).

   Any operation on the returned copy has no impact on the original context.
  
   Attention, forking a `ctx` looses any attached [[result]] or [[exception]]."

  ^Context
  
  [^Context ctx]

  (.fork ctx))



(defn fork-to

  "Like [[fork]] but switches the executing account.
  
   Note: CVM log is lost."

  ^Context

  [^Context ctx address]

  (.forkWithAddress ctx
                    address))


;;;;;;;;;; Querying context properties


(defn- -wrap-address

  ;; Wraps `x` in an Address object if it is not already.

  ^Address

  [x]

  (cond->
    x
    (number? x)
    $.cell/address))



(defn account

  "Returns the account for the given `address` (or the address associated with `ctx`)."

  
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



(defn key

  "Returns the key of the given `address` (or the address associated with `ctx`)."

  (^AccountKey [ctx]

   (.getAccountKey (account ctx)))


  (^AccountKey [ctx address]

   (.getAccountKey (account ctx
                            address))))




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

   See [[convex.cell/key]].
  
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

  ^Context

  [^Context ctx amount]

  (.withJuice ctx
              amount))



(defn key-set

  "Sets `key` on the address curently associated with `ctx`."

  ^Context
  
  [^Context ctx ^AccountKey key]

  (.setAccountKey ctx
                  key))



(defn result-set

  "Attaches the given `result` to `ctx`, as if it was the result of a transaction."

  ^Context

  [^Context ctx ^ACell result]

  (.withResult ctx
               result))



(defn state-set

  "Replaces the CVM state in the `ctx` with the given one.
  
   See [[state]]."

  ^Context

  [^Context ctx ^State state]

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
                                            ($.cell/key ($.cell/blob (byte-array 32)))
                                            ($.cell/vector [])))
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


(defn expand

  "Expands `cell` into a `canonical cell` by applying macros.
  
   Fetched using [[result]] if not given.

   Returns a new `ctx` with a [[result]] ready for [[compile]] or an [[exception]] in case
   of failure."


  (^Context [ctx]

   (expand ctx
           (result ctx)))


  (^Context [^Context ctx object]

   (.expand ctx
            object)))



(defn compile

  "Compiles the `canonical-cell` into executable code.

   Fetched using [[result]] if not given.

   Returns a new `ctx` with a [[result]] ready for [[exec]] or an [[exception]] in case of
   failure."


  (^Context [ctx]

    (compile ctx
             (result ctx)))


  (^Context [^Context ctx canonical-cell]

   (.compile ctx
             canonical-cell)))



(defn expand-compile

  "Chains [[expand]] and [[compile]] in a slightly more efficient fashion than calling both separately."

  
  (^Context [ctx]

   (expand-compile ctx
                   (result ctx)))


  (^Context [^Context ctx object]

   (.expandCompile ctx
                   object)))


;;;;;;;;;; Pahse 3 - Executing compiled code


(defn exec

  "Executes compiled code.
  
   Usually run after [[compile]].
  
   Returns a new `ctx` with a [[result]] or an [[exception]] in case of failure."


  (^Context [ctx]

   (exec ctx
         (result ctx)))


  (^Context [^Context ctx ^AOp op]

   (.run ctx
         op)))


;;;


(defn eval

  "Evaluates the given `cell`, going efficiently through [[expand]], [[compile]], and [[exec]].

   Works with any kind of `cell` and is sufficient when there is no need for fine-grained control.

   An important difference with the aforementioned cycle is that the cell passes through `*lang*`, a function
   possibly set by the user for intercepting a cell (eg. modifying the cell and evaluating explicitley).

   Returns a new `ctx` with a [[result]] or an [[exception]] in case of failure."

  (^Context [ctx]

   (eval ctx
         (result ctx)))


  (^Context [^Context ctx ^ACell cell]

   (.run ctx
         cell)))


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
  
   Returns a new `ctx` with a [[result]] or an [[exception]] in case of failure."

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
