(ns convex.shell.req.state

  "Requests relating to the global state."

  {:author "Adam Helinski"}

  (:import (convex.core State)
           (convex.core.init Init))
  (:require [convex.cell           :as $.cell]
            [convex.cvm            :as $.cvm]
            [convex.shell.fail     :as $.shell.fail]
            [convex.std            :as $.std]))


;;;;;;;;;;


(defn- -safe

  ;; Root implementation for [[safe]] and [[tmp]].

  [ctx f select-ctx-ok]

  (or (when-not ($.std/fn? f)
        ($.cvm/exception-set ctx
                             ($.cell/* :ARGUMENT)
                             ($.cell/* "Can only execute a no-arg function")))
      (let [ctx-2 (-> ctx
                      ($.cvm/fork)
                      ($.cvm/invoke f
                                    ($.cvm/arg+*)))
            ex    ($.cvm/exception ctx-2)]
        (if ex
          ($.cvm/result-set ctx
                            ($.cell/* [false
                                       ~($.shell.fail/mappify-cvm-ex ex)]))
          ($.cvm/result-set (select-ctx-ok ctx
                                           ctx-2)
                            ($.cell/* [true
                                       ~($.cvm/result ctx-2)]))))))


;;;;;;;;;;


(defn genesis

  "Request for generating a genesis state."

  [ctx [key+]]

  (or (when-not ($.std/vector? key+)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Genesis keys must be provided in a vector")))
      (let [n-key ($.std/count key+)]
        (or (when-not (>= n-key
                          1)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "At least 1 genesis key must be provided")))
            (when-not (= n-key
                         (count (set key+)))
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "There cannot be any duplicate genesis keys")))))
      (when (some (fn [key]
                    (or (not ($.std/blob? key))
                        (not (= ($.std/count key)
                                32))))
                  key+)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "All genesis keys must be 32-byte blobs")))
      ($.cvm/result-set ctx
                        (-> ($.cvm/ctx {:convex.cvm/genesis-key+ (map $.cell/key
                                                                      key+)})
                            ($.cvm/state)))))



(defn safe

  "Request for executing code in a safe way.
  
   In case of an exception, state is reverted."

  [ctx [f]]

  (-safe ctx
         f
         (fn [_ctx-old ctx-new]
           ctx-new)))



(defn switch

  "Request for switching a context to the given state."

  [ctx [address ^State state]]

  (or (when-not ($.std/state? state)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/string "Can only load a state")))
      (when-not ($.std/address? address)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/string "Must provide a valid address")))
      (when-not (.getAccount state
                             address)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :NOBODY)
                             ($.cell/* "Account for the request address does not exist in the given state")))
      (let [state-old ($.cvm/state ctx)]
        (-> ctx
            ($.cvm/state-set (.putAccount state
                                          Init/CORE_ADDRESS
                                          ($.cvm/account ctx
                                                         Init/CORE_ADDRESS)))
            (cond->
              (not= address
                    ($.cvm/address ctx))
              ($.cvm/fork-to address))
            ($.cvm/result-set state-old)))))



(defn tmp

  "Exactly like [[safe]] but the state is always reverted, even in case of success."

  [ctx [f]]

  (-safe ctx
         f
         (fn [ctx-old _ctx-new]
           ctx-old)))
