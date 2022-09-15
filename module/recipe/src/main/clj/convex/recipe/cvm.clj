(ns convex.recipe.cvm

  "The CVM (Convex Virtual Machine) is the execution engine of the Convex network.
  
   Whenever a query or a transaction is submitted to a peer, as seen in [[convex.recipe.client]], peers
   execute code given as a cell using the CVM.

   These examples provide an overview to get an idea of what is going on.

   In essence, code undergoes the 3 steps that any true Lisp follows:

   - Expansion, executing macros : data -> data
   - Compilation                 : date -> code
   - Execution                   : code -> result

   Those steps combined together form the well-known `eval`."

  {:author "Adam Helinski"}

  (:require [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]))


;;;;;;;;;;


(def ctx

  "A `context` holds a state and allows executing operations. It is the heart of the CVM.
  
   The state is effectively the network state that is composed of many things such as all accounts on the network.

   However, when created as such, context holds a minimal state: a few required accounts, official Convex libraries and utilities."

  ($.cvm/ctx))



(def code

  "Code to execute is a cell.
  
   We use the star macro to convert the following Clojure data to an actual cell.

   See `convex.recipe.cell` for more information about cells."

  ($.cell/* (if (< 1 50)
              :lesser
              :greater)))


;;;;;;;;;;


(comment


  ;; The [[convex.cvm]] namespace explains how an execution context should be handled and hints
  ;; at why it is being "forked" below. For now, forking is a detail.


  ;; EVAL
  ;;
  ;; Executing code straightaway.
  ;;
  (-> ctx
      ($.cvm/fork)
      ($.cvm/eval code)
      ($.cvm/result))


  ;; EXPANSION
  ;;
  ;; E.g. In Convex Lisp, `if` is a macro that expands to low-level operation `cond`.
  ;;
  (def expanded
       (-> ctx
           ($.cvm/fork)
           ($.cvm/expand code)
           ($.cvm/result)))

  expanded


  ;; COMPILATION
  ;;
  ;; We can see that our expanded code compiles indeed to the `cond` low-level operation.
  ;;
  (def compiled
       (-> ctx
           ($.cvm/fork)
           ($.cvm/compile expanded)
           $.cvm/result))

  (class compiled)


  ;; EXECUTION
  ;;
  ;; Compiled code is ready for execution.
  ;;
  (-> ctx
      ($.cvm/fork)
      ($.cvm/exec compiled)
      ($.cvm/result))


  ;;
  ;; Hence, Convex is (very probably) the first decentralized Lisp ever.
  ;;
  ;; Lisp has been mentioned in a couple of projects, even Ethereum, but it most often about s-expression.
  ;; Not a full language with lambdas, macros, and eval!
  ;;


  ;; This is useful for cutting on cost.
  ;;
  ;; When possible, it is best to compile code ahead before submitting it as a transaction. Otherwise
  ;; peers have to compile it themselves and overall execution is simply more expensive.
  ;;
  (-> ctx
      ($.cvm/fork)
      ($.cvm/expand-compile code)
      ($.cvm/result))


  ;; Preparing and executing code in any way uses 'juice'.
  ;;
  ;; Juice is a computational unit. More complex operations require more 'juice'. This is how the cost of
  ;; a transaction is computed.
  ;;
  ;; Hence, when using a context directly, it must be refilled once in a while otherwise we will get
  ;; an `OutOfJuiceException`.
  ;;
  ($.cvm/juice-refill ctx)


  ;; Note that the input to all those functions is always a cell and so is the output. Even compiled code
  ;; is considered a cell and can be stored in the Etch database (see [[convex.recipe.db]] for examples).

  )
