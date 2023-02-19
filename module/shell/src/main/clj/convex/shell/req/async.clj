(ns convex.shell.req.async

  "Requests for async programming.
  
   The word \"promise\" is used for any kind of async value."

  (:import (java.util.concurrent CompletableFuture))
  (:refer-clojure :exclude [take])
  (:require [convex.cell        :as $.cell]
            [convex.clj         :as $.clj]
            [convex.cvm         :as $.cvm]
            [convex.db          :as $.db]
            [convex.shell.resrc :as $.shell.resrc]
            [convex.std         :as $.std]
            [promesa.exec.csp   :as P.csp]))


;;;;;;;;;; Private


(defn- -do-promise

  ;; Unwraps a CompletableFuture from a Shell resources.

  [ctx promise f]

  (let [[ok?
         x]  ($.shell.resrc/unwrap ctx
                                   promise)]
    (if ok?
      (let [promise-2 x]
        (or (when-not (instance? CompletableFuture
                                 promise-2)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Not a promise")))
            (f promise-2)))
      (let [ctx-2 x]
        ctx-2))))


;;;;;;;;; Requests


(defn do-

  "Request for executing a CVX function in a forked context on a separate thread.
   Returns a promise."

  [ctx [f]]

  (or (when-not ($.std/fn? f)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Can only execute a function")))
      (let [ctx-2 ($.cvm/fork ctx)
            etch  ($.db/current)]
        ($.cvm/result-set ctx
                          ($.shell.resrc/create
                            (P.csp/go
                              (some-> etch
                                      ($.db/current-set))
                              (let [ctx-3 ($.cvm/invoke ctx-2
                                                        f
                                                        ($.cvm/arg+*))
                                    ex    ($.cvm/exception ctx-3)]
                                (if ex
                                  [false
                                   ex]
                                  [true
                                   ($.cvm/result ctx-3)]))))))))



(defn take

  "Request for awaiting a promise."

  [ctx [promise]]

  (-do-promise ctx
               promise
               (fn [promise-2]
                 (let [[ok?
                        x]  (deref promise-2)]
                   (if ok?
                     ($.cvm/result-set ctx
                                       x)
                     ($.cvm/exception-set ctx
                                          x))))))



(defn take-timeout

  "Like [[take]] but with a timeout."

  [ctx [promise timeout-millis timeout-val]]

  (or (when-not ($.std/long? timeout-millis)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Timeout in milliseconds must be a Long")))
      (-do-promise ctx
                   promise
                   (fn [promise-2]
                     (let [x (deref promise-2
                                    ($.clj/long timeout-millis)
                                    timeout-val)]
                       (if (identical? x
                                       timeout-val)
                         ($.cvm/result-set ctx
                                           x)
                         (let [[ok?
                                x-2] x]
                           (if ok?
                             ($.cvm/result-set ctx
                                               x-2)
                             ($.cvm/exception-set ctx
                                                  x-2)))))))))
