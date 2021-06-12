(ns convex.sync

  "About running Convex Lisp inputs and producing a context.
  
   This namespace is currently the core implementation of [[convex.disk]]. However, it is written
   in such a way that it is generic and could be applied to sources other than files.

   For understanding what is going on, it is best to study it alongside [[convex.disk]]. Terminology stems
   from the docstrings of [[convex.disk/load]] and [[convex.disk/watch]].
  
   The order is typically:

   - [[env]] (includes [[load]])
   - [[sync]]
   - [[exec]]
   - [[reload]] + [[unload]] when watching, followed by [[exec]]"

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [load])
  (:require [convex.cvm  :as $.cvm]))


(declare load)


;;;;;;;;;; Helpers


(defn update-error

  "Points to a `:input->error` if needed."

  [env]

  (if (seq (env :input->error))
    (assoc env
           :error
           :input->error)
    (dissoc env
            :error)))


;;;;;;;;;; Creating an execution environment


(defn env

  "Entry point, creates an executing environment given `step+` and `option+`.

   Besides what the [[convex.disk]] namespace describes, `option+` requires an additial key `:read` which
   is a function that receive an input (eg. a file path) and returns code (eg. effectively reads the file).
  
   Effectively loads all inputs using [[load]]. Commonly, [[synced]] is used after that step."

  
  ([read input+]

   (env read
        input+
        nil))


  ([read input+ option+]

   (-> option+
       (update :after-run
               #(or %
                    identity))
       (update :eval
               #(or %
                    $.cvm/eval))
       (update :init-ctx
               #(or %
                    $.cvm/ctx))
       (assoc :input+ input+
              :read   read)
       load)))


;;;;;;;;;; Reading source and handling change


(defn update-code

  ""

  [env input code]

  (assoc-in env
            [:input->code
             input]
            code))



(defn load

  "Used by [[env]] to load initial inputs.
  
   Adds to `env` 2 keys:

   | Key | Value |
   |---|---|
   | `:input->code` | Map of `input` -> `loaded code` |
   | `:input->error` | Represent failures, map of `input` -> `exception` |" 


  ([env]

   (reduce (env :read)
           env
           (env :input+)))


  ([env input]

   ((env :read)
    env
    input)))



(defn- -update-input

  ;;

  [env input]

  (-> env
      (update :input->code
              dissoc
              input)
      (update :input->error
              dissoc
              input)))



(defn reload

  ""

  [env input]

  (-> env
      (-update-input input)
      (load input)
      update-error))



(defn unload

  "Opposite of [[reload]], removes an input and code from related steps.

   Runs the result through [[update-error]]."

  [env input]

  (-> env
      (-update-input input)
      update-error))


;;;;;;;;;; Executing steps


(defn exec

  "When any code has been loaded/reloaded, executes all steps one by one.
  
   Resulting context is attached under `:ctx` unless an error occurs and figures under `:error`."

  [{:as   env
    :keys [after-run
           eval
           init-ctx
           input->code]}]

  (let [eval-2 (or eval
                   $.cvm/eval)]
    (try
      (assoc env
             :ctx
             (after-run (reduce-kv (fn [ctx input code]
                                     (try
                                       (eval-2 ctx
                                               code)
                                       (catch Throwable err
                                         (throw (ex-info "During evaluation"
                                                         {::error :eval
                                                          ::input input}
                                                         err)))))
                                   (init-ctx)
                                   input->code)))
      (catch Throwable err
        (-> env
            (assoc :error      :error-eval
                   :error-eval err)
            (dissoc :ctx)
            after-run)))))
