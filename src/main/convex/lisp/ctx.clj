(ns convex.lisp.ctx

  ""

  {:author "Adam Helinski"}

  (:import convex.core.Init
           convex.core.lang.Context))


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
