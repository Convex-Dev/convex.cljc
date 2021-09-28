(ns convex.recipe.cvm

  "The CVM (Convex Virtual Machine) is the execution engine of the Convex network.
  
   Whenever a query or a transaction is submitted to a peer, as seen in `convex.recipe.client`, peers
   execute code given as a cell using the CVM.

   This example provides an overview to get an idea of what is going on.

   In essence, code undergoes the 3 steps that any true Lisp follows:

   - Expansion, executing macros
   - Compilation
   - Execution

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
              :smaller
              :greater)))


;;;;;;;;;;


(comment


  ;; EVAL
  ;;
  ;; Executing code straightaway.
  ;; We get the result and stringify it so it is easier to read.
  ;;
  (-> ($.cvm/eval ctx
                  code)
      $.cvm/result
      str)


  ;; EXPANSION
  ;;
  ;; In Convex, `if` is a macro that expands to low-level operation `cond`.
  ;;
  (def expanded
       (-> ($.cvm/expand ctx
                         code)
           $.cvm/result))

  (str expanded)


  ;; COMPILATION
  ;;
  ;; We can see that our expanded code compiles indeed to the `cond` low-level operation.
  ;;
  (def compiled
       (-> ($.cvm/compile ctx
                          expanded)
           $.cvm/result))

  compiled


  ;; EXECUTION
  ;;
  ;; Compile code is ready for execution.
  ;;
  (-> ($.cvm/exec ctx
                  compiled)
      $.cvm/result
      str)


  ;;
  ;; Hence, Convex is (probably) the first decentralized Lisp ever.
  ;;
  ;; Lisp has been mentioned in a couple of projects, even Ethereum, but it most often about s-expression.
  ;; Not a full language with lambdas and eval!
  ;;


  ;; This is useful for cutting on cost.
  ;;
  ;; When possible, it is best to compile code ahead before submitting it as a transaction. Otherwise
  ;; peers have to compile it themselves and overall execution is simply more expensive.
  ;;
  (-> ($.cvm/expand-compile ctx
                            code)
      $.cvm/result)


  ;; Preparing and executing code in any way uses 'juice'.
  ;;
  ;; Juice is a computational unit. More complex operations require more 'juice'. This is how the cost of
  ;; a transaction is computed.
  ;;
  ;; Hence, when using a context directly, it must be refilled once in a while otherwise we will get
  ;; an `OutOfJuiceException`.
  ;;
  ($.cvm/juice-refill ctx)


  )
