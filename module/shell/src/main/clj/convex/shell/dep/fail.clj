(ns convex.shell.dep.fail

  "Builds on [[convex.shell.flow]] for returning CVM exceptions relating
   to [[convex.shell.dep]]."

  {:author "Adam Helinski"}

  (:import (convex.core.lang.impl ErrorValue))
  (:require [clojure.string    :as string]
            [convex.cvm        :as $.cvm]
            [convex.shell.flow :as $.shell.flow]))


;;;;;;;;;;


(defn- -trace-ancestry

  ;; When failing to fetch or deploy a dependency, adds its ancestry to the
  ;; exception trace (who we got there).

  [^ErrorValue ex ancestry]

  (.addTrace ex
             (str "Deploying: "
                  (string/join " <- "
                               ancestry)))
  ex)


;;;;;;;;;;


(defn rethrow-with-ancestry

  [ctx ex ancestry]
                 
  ($.shell.flow/fail ctx
                     (-trace-ancestry ex
                                      ancestry)))



(defn with-ancestry

  [ctx code message ancestry]

  (rethrow-with-ancestry ctx
                         (doto (ErrorValue/createRaw code
                                                     message)
                           (.setAddress ($.cvm/address ctx)))
                         ancestry))
