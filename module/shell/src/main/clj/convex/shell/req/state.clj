(ns convex.shell.req.state

  "Requests relating to the global state."

  {:author "Adam Helinski"}

  (:import (convex.core State)
           (convex.core.data AccountStatus)
           (convex.core.init Init))
  (:require [convex.cell           :as $.cell]
            [convex.cvm            :as $.cvm]
            [convex.shell.ctx      :as $.shell.ctx]
            [convex.shell.fail     :as $.shell.fail]
            [convex.std            :as $.std]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(defn- -invoke

  [ctx f-cvx f-then]

  (or (when-not ($.std/fn? f-cvx)
        ($.cvm/exception-set ctx
                             ($.cell/* :ARGUMENT)
                             ($.cell/* "Can only execute a no-arg function")))
      (f-then (-> ctx
                  ($.cvm/fork)
                  ($.cvm/invoke f-cvx
                                ($.cvm/arg+*))))))



(defn- -safe

  ;; Root implementation for [[safe]] and [[tmp]].

  [ctx f select-ctx-ok]

  (-invoke ctx
           f
           (fn [ctx-2]
             (let [ex ($.cvm/exception ctx-2)]
               (if ex
                 ($.cvm/result-set ctx
                                   ($.cell/* [false
                                              ~($.shell.fail/mappify-cvm-ex ex)]))
                 ($.cvm/result-set (select-ctx-ok ctx
                                                  ctx-2)
                                   ($.cell/* [true
                                              ~($.cvm/result ctx-2)])))))))



(defn- -transplant-core

  ;; Replaces the environment and the metadata of the core account in `state`.

  [^State state env metadata]

  (.putAccount state
               Init/CORE_ADDRESS
               (-> (.getAccount state
                                Init/CORE_ADDRESS)
                   (.withEnvironment env)
                   (.withMetadata metadata))))


;;;;;;;;;;


(defn core-vanilla

  "Request for restoring genesis env and metadata in the core account in the given `state`."

  [ctx [^State state]]

  (or (when-not ($.std/state? state)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Not a State")))
      ($.cvm/result-set ctx
                        (-transplant-core state
                                          $.shell.ctx/core-env
                                          $.shell.ctx/core-meta))))



(defn do-

  "Request similar to [[safe]] but returns only a boolean (`false` in case of an exception).
  
   Avoids some overhead when dealing with exceptions (undesirable for situations like benchmarking)."

  [ctx [f]]

  (-invoke ctx
           f
           (fn [ctx-2]
             (if ($.cvm/exception? ctx-2)
               ($.cvm/result-set ctx
                                 ($.cell/* false))
               ($.cvm/result-set ctx-2
                                 ($.cell/* true))))))



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
                             ($.cell/* "All genesis keys must be 32-byte Blobs")))
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
                             ($.cell/* "Can only load a State")))
      (when-not ($.std/address? address)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Must provide a valid Address")))
      (when-not (.getAccount state
                             address)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :NOBODY)
                             ($.cell/* "Account for the requested Address does not exist in the given State")))
      (let [state-old           ($.cvm/state ctx)
            ^AccountStatus core (.getAccount state-old
                                             Init/CORE_ADDRESS)]
        (-> ctx
            ($.cvm/state-set (-transplant-core state
                                               (.getEnvironment core)
                                               (.getMetadata core)))
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
