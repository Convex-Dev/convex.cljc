(ns convex.shell.req.cvmlog

  "Requests relating to the CVM log."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [get])
  (:require [convex.cvm :as $.cvm]))


;;;;;;;;;;


(defn clear

  "Request for clearing the CVM log."

  [ctx _arg+]

  (-> ($.cvm/ctx {:convex.cvm/address ($.cvm/address ctx)
                  :convex.cvm/state   ($.cvm/state ctx)})
      ($.cvm/result-set ($.cvm/log ctx))))



(defn get

  "Request for retrieving the CVM log."

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cvm/log ctx)))
