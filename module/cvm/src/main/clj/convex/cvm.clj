(ns convex.cvm

  "Code execution in the Convex Virtual Machine

   Altering its state and gaining insights.

   The central entity of this namespace is the execution context created by [[ctx]]. They embed a [[state]] and allow
   executing code to alter it.

   All other functions revolve around contextes. While the design of a context is mostly immutable, whenever an altering function
   is applied (eg. [[juice-set]]) or code is handled in any way (eg. [[eval]]), the old context must be discarded and only the
   returned one should be used.

   Cheap copies can be created using [[fork]].

   Actions involving code (eg. [[compile]], [[exec]], ...) return a new context which holds either a [[result]] or an [[exception]].
   Those actions always consume [[juice]].

   Given that a \"cell\" is the term reserved for CVM data and objects, execution consists of the following steps:

   | Step | Function    | Does                                                |
   |------|-------------|-----------------------------------------------------|
   | 1    | [[expand]]  | `cell` -> `canonical cell`, applies macros          |
   | 2    | [[compile]] | `canonical cell` -> `op`, preparing executable code |
   | 3    | [[exec]]    | Executes compiled code                              |

   Any cell can be applied safely to those functions, worse that can happen is nothing (e.g. providing an already compiled cell to
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
           (convex.core.lang.impl AExceptional
                                  ErrorValue)
           (convex.core.transactions ATransaction))
  (:refer-clojure :exclude [compile
                            def
                            eval
                            key
                            time])
  (:require [convex.cell :as $.cell]
            [convex.std  :as $.std]))


(set! *warn-on-reflection*
      true)


(declare juice
         juice-set
         state-set)


;;;;;;;;;; Values


(def genesis-user

  "Address of the first genesis user.
  
   More precisely, when the CVM [[state]] is created in [[ctx]].
   This behavior might change in the future.

   It receives half of the funds reserved for all users in the state."

  Init/GENESIS_ADDRESS)


;;;;;;;;;; Creating a new context


(defn ctx

  "Creates an execution context.
  
   An optional map of options may be provided:

   | Key                        | Value                                           | Default                                            |
   |----------------------------|-------------------------------------------------|----------------------------------------------------|
   | `:convex.cvm/address`      | Address of the executing account                | [[genesis-user]]                                   |
   | `:convex.cvm/genesis-key+` | Vector of keys for genesis users (at least one) | Vector with only [[fake-key]] for [[genesis-user]] |
   | `:convex.cvm/state`        | State (see [[state]])                           | Initial state with Convex actors and libraries     |
  
   More than one genesis key can be provided in order to create more users than [[genesis-user]].
   However, it is important those public keys are different otherwise an exception is thrown.

   See [[convex.cell/key]] about creating public keys."


  (^Context []

   (ctx nil))

  
  (^Context [option+]

   (Context/createFake (or (:convex.cvm/state option+)
                           (Init/createState (or (:convex.cvm/genesis-key+ option+)
                                                 [$.cell/key-fake])))
                       (or (:convex.cvm/address option+)
                           genesis-user))))



(defn fork

  "Duplicates the given `ctx` (very cheap).

   Any operation on the returned copy has no impact on the original context.
  
   Attention, forking a [[ctx]] looses any attached [[result]] or [[exception]]."

  ^Context
  
  [^Context ctx]

  (.fork ctx))



(defn fork-to

  "Duplicates the given `ctx` and switches the executing account.

   Like [[fork]] but only [[state]] and [[juice]] are preservered.
   Everything else is lost: local bindings, CVM log. CVM depth, etc."

  ^Context

  [^Context ctx address]

  (-> ctx
      (.forkWithAddress address)
      (juice-set (juice ctx))))


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



(defn actor?

  "Returns `true` if the given address point to an actor in `ctx`.
  
   An actor is an account without a public key."

  [ctx address]

  (some-> (account ctx
                   address)
          (.isActor)))



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

  "Returns the exception attached to the CVM (or nil).
  
   The CVM enters in exceptional state in case of error or particular patterns such as
   halting or doing a rollback.

   A nil result means [[result]] can be safely used on this context.
  
   An exception code can be provided as a filter, meaning that even if an exception occured, this
   functions will return nil unless that exception has the given `code`.
  
   Also see [[convex.cell/code-std*]] for easily retrieving an official error code. Note that in practice, unlike the CVM
   itself or any of the core function, a user Convex function can return anything as a code."


  (^AExceptional [^Context ctx]

   (when (.isExceptional ctx)
     (.getExceptional ctx)))


  (^AExceptional [^ACell code ^Context ctx]

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



(defn exception-code

  "Returns the code associated with the given [[exception]].
  
   Often a CVX keyword but could be any CVX value."

  ^ACell

  [^AExceptional exception]

  (.getCode exception))



(defn exception-message

  "Returns the message associated with the given [[exception]].

   Often a CVX string but could be any CVX value."

  ^ACell

  [^AExceptional exception]

  (.getMessage exception))



(defn exception-trace

  "Returns the trace associated with the given [[exception]] (CVX list of strings)."

  ^ACell

  [^ErrorValue exception]

  (.getTrace exception))



(defn juice

  "Returns the remaining amount of juice available for the executing account.
  
   Also see [[juice-set]]."

  [^Context ctx]

  (.getJuice ctx))



(defn key

  "Returns the key of the given `address`.
  
   Or the address associated with `ctx`."

  (^AccountKey [ctx]

   (.getAccountKey (account ctx)))


  (^AccountKey [ctx address]

   (.getAccountKey (account ctx
                            address))))




(defn log

  "Returns the log of `ctx`.
  
   A vector cell of size 2 vectors containing a logging address and a logged value."

  ^ABlobMap
  
  [^Context ctx]

  (.getLog ctx))



(defn look-up

  "Returns the cell associated with the given `sym`.
  
   From the environment of the given `address` (or the currently used one)."


  (^ACell [ctx sym]

   (-> ctx
       (env)
       ($.std/get sym)))


  (^ACell [ctx address sym]

   (-> ctx
       (env address)
       ($.std/get sym))))
       


(defn result

  "Extracts the result (eg. after expansion, compilation, execution, ...) wrapped in a `ctx`.
  
   Throws if the `ctx` is in an exceptional state. See [[exception]]."

  [^Context ctx]

  (.getResult ctx))



(defn state

  "Returns the whole CVM state associated with `ctx`.

   It is a special type of cell behaving like a map cell. It notably holds all accounts and can be explored
   using [[convex.std]] map functions.
  
   Also see [[state-set]]."

  ^State

  [^Context ctx]

  (.getState ctx))



(defn time

  "Returns the current timestamp assigned to the state in the given `ctx`.

   A timetamp is a Unix epoch in milliseconds (long cell);
  
   Also see [[time-set]]."

  ^CVMLong

  [^Context ctx]

  (-> ctx
      (state)
      (.getTimeStamp)))


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

   Argument is a map of `symbol cell` -> `cell`."


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
                                                 (reduce (fn [env [^ACell sym ^ACell value]]
                                                           ($.std/assoc env
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



(defn exception-set

  "Returns a `ctx` set in an exceptional state.
  
   See [[exception]]."


  (^Context [^Context ctx ^AExceptional exception]

   (.withException ctx
                   exception))


  (^Context [^Context ctx ^ACell code ^ACell message]

   (.withError ctx
               (ErrorValue/createRaw code
                                     message))))



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
   Scheduled transactions will be executed if necessary.
  
   Does not do anything if `millis` is < 0.
  
   See [[time]]."

  ^Context

  [^Context ctx millis]

  (state-set ctx
             (-> ctx
                 (state)
                 (.applyBlock (Block/create (long (+ (.longValue (time ctx))
                                                     millis))
                                            ($.cell/vector [])))
                 (.getState))))



(defn undef

  "Like calling `(undef sym)` in Convex Lisp.
  
   Either in the current account or the given one, repeatedly on any symbol cell in `sym+`."


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


  (^Context [^Context ctx cell]

   (.expand ctx
            cell)))



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

  "Expands and compiles in one go.

   More efficient than chaining [[expand]] and [[compile]] yourself."

  
  (^Context [ctx]

   (expand-compile ctx
                   (result ctx)))


  (^Context [^Context ctx cell]

   (.expandCompile ctx
                   cell)))


;;;;;;;;;; Phase 3 - Executing compiled code


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

  "Evaluates the given `cell` after forking the `ctx`.
  
   Goes efficiently through [[expand]], [[compile]], and [[exec]].

   Works with any kind of `cell` and is sufficient when there is no need for fine-grained control.

   An important difference with the aforementioned cycle is that the cell passes through `*lang*`, a function
   possibly set by the user for intercepting a cell (eg. modifying the cell and evaluating in an alternative way).

   Returns the forked `ctx` with a [[result]] or an [[exception]] in case of failure."

  (^Context [ctx]

   (eval ctx
         (result ctx)))


  (^Context [^Context ctx ^ACell cell]

   (.run ctx
         cell)))


;;;;;;;;;; Functions


(defmacro arg+*

  "Prepares arguments for invokation.
  
   See [[invoke]]."

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

   `arg+` is a Java array of cells. See [[arg+*]] for easily and efficiently creating one.
  
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


;;;;;;;;;; Transactions


(defn transact

  "Executes the given transaction.

   This is exactly what a peer does when executing a transation from a block, after
   validating its signature.

   Similar to [[eval]] but:

   - Temporarily switches to the account of the transaction
   - Executes the code in that account
   - Takes care of all the juice and memory accounting for that account

   For creating transactions, see:

   - [[convex.cell/call]]
   - [[convex.cell/invoke]]
   - [[convex.cell/transfer]]

   Returns a new `ctx` with the [[result]] or [[exception]] attached."

  [^Context ctx ^ATransaction trx]

  (let [ctx-2 (-> ctx
                  (state)
                  (.applyTransaction trx))
        ctx-3 (state-set ctx
                         (state ctx-2))]
    (if (exception? ctx-2)
      (exception-set ctx-3
                     (exception ctx-2))
      (result-set ctx-3
                  (result ctx-2)))))
