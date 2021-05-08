(ns convex.lisp.ctx

  ""

  {:author "Adam Helinski"}

  (:import convex.core.Init
           convex.core.lang.Context)
  (:refer-clojure :exclude [compile
                            eval]))


(declare run)


;;;;;;;;;; Creating a new context


(defn create-fake

  "Creates a fake variant of `convex.core.Context` needed for compilation and execution."


  (^Context []

   (create-fake Init/HERO))


  (^Context [account]

   (create-fake Init/STATE
                account))

  
  (^Context [state account]

   (Context/createFake state
                       account)))



(defn fork

  ""

  ^Context [^Context ctx]

  (.fork ctx))


;;;;;;;;;; Querying context properties


(defn exceptional

  "Returns the current exceptional value attached to the given `ctx` if it is indeed
   in an exceptional state, nil otherwise."

  [^Context ctx]

  (when (.isExceptional ctx)
    (.getExceptional ctx)))



(defn env

  ""

  [^Context ctx]

  (.getEnvironment ctx))



(defn juice

  ""

  [^Context ctx]

  (.getJuice ctx))



(defn log

  ""

  [^Context ctx]

  (.getLog ctx))



(defn result

  "Extracts the result (eg. after expansion, compilation, execution, ...) wrapped in a `ctx`.
  
   Throws if the `ctx` is in an exceptional state. See [[exceptional]]."

  [^Context ctx]

  (.getResult ctx))



(defn state

  ""

  [^Context ctx]

  (.getState ctx))


;;;;;;;;;; Compiling Convex objects


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


;;;;;;;;;; Executing Convex objects


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

   (query ctx
          (result ctx)))


  (^Context [^Context ctx compiled-object]

   (.query ctx
           compiled-object)))



(defn run

  "Runs compiled Convex code.
  
   Usually run after [[compile]].
  
   Returns `ctx`, result being the evaluated object."


  (^Context [ctx]

   (run ctx
        (result ctx)))


  (^Context [^Context ctx compiled]

   (.run ctx
         compiled)))

