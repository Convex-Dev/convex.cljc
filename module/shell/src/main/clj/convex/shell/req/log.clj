(ns convex.shell.req.log

  (:refer-clojure :exclude [get])
  (:require [convex.cvm :as $.cvm]))


;;;;;;;;;;


(defn clear

  [ctx _arg+]

  (-> ($.cvm/ctx {:convex.cvm/address ($.cvm/address ctx)
                  :convex.cvm/state   ($.cvm/state ctx)})
      ($.cvm/result-set ($.cvm/log ctx))))



(defn get

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cvm/log ctx)))
