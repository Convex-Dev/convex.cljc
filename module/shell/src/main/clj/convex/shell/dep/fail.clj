(ns convex.shell.dep.fail

  (:import (convex.core.lang.impl ErrorValue))
  (:require [clojure.string    :as string]
            [convex.cvm        :as $.cvm]
            [convex.shell.flow :as $.shell.flow]))


;;;;;;;;;;


(defn- -trace-ancestry

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
