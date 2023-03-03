(ns convex.shell.async

  "Helpers for async requests."

  (:import (convex.core.lang.impl ErrorValue))
  (:require [convex.cvm         :as $.cvm]
            [convex.shell.resrc :as $.shell.resrc]
            [promesa.core       :as P]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(defn failure

  "Indicating failure from an async value."

  
  ([ctx [error-code error-message]]

   (failure ctx
            error-code
            error-message))


  ([ctx ^convex.core.data.Keyword error-code ^convex.core.data.AString error-message]

   [false
    (doto
      (ErrorValue/create error-code
                         error-message)
      (.setAddress ($.cvm/address ctx)))]))



(defn success

  "Indicating success from an async value."

  [result]

  [true
   result])


;;;;;;;;;;


(defn return

  "Attaches an async value to `ctx`.
  
   Async value is produced in a delay for error handling.
   `f-catch` is used if either the delay or the async process throws." 

  [ctx d*future f-catch]

  ($.cvm/result-set ctx
                    ($.shell.resrc/create
                      (try
                        (-> @d*future
                            (P/then success)
                            (P/catch (fn [ex]
                                       (failure ctx
                                                (f-catch ex)))))
                        (catch Exception ex
                          (P/resolved (failure ctx
                                               (f-catch ex))))))))
