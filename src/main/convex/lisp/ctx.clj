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

  ^Context [^Context context]

  (.fork context))


;;;;;;;;;; Querying context properties


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
