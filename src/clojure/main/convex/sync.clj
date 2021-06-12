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


(declare eval-form
         exec
         load
         unload)


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
       (assoc :input+ input+
              :read   read)
       load)))


;;;;;;;;;; Reading source and handling change


(defn assoc-code

  ""

  [env input code]

  (assoc-in env
            [:input->code
             input]
            code))



(defn assoc-err-read

  ""

  [env input err]

  (update env
          :error
          (fn [error]
            (if (identical? (first error)
                            :input->error)
              (update error
                      1
                      assoc
                      input
                      err)
              [:input->error
               {input err}]))))



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



(defn reload

  ""

  [env input]

  (-> env
      (unload input)
      (load input)))



(defn unload

  "Opposite of [[reload]], removes an input and code from related steps.

   Runs the result through [[update-error]]."

  [env input]

  (-> (let [error (env :error)]
        (if (and error
                 (identical? (first error)
                             :input->error))
          (let [input->error-2 (dissoc (second error)
                                       input)]
            (if (seq input->error-2)
              (assoc env
                     :error
                     [:input->error
                      input->error-2])
              (dissoc env
                      :error)))
          env))
      (update :input->code
              dissoc
              input)))


;;;;;;;;;; Executing steps


(defn exec

  "When any code has been loaded/reloaded, executes all steps one by one.
  
   Resulting context is attached under `:ctx` unless an error occurs and figures under `:error`."

  [ctx env]

  (if (env :error)
    env
    (reduce (let [{:keys [input->code]} env]
              (fn [env-2 input]
                (try
                  (update env-2
                          :ctx
                          (fn [ctx]
                            ($.cvm/eval ctx
                                        (input->code input))))
                  (catch Throwable err
                    (reduced (-> env-2
                                 (assoc :error
                                        [:eval
                                         {:exception err
                                          :input     input}])
                                 (dissoc :ctx)))))))
            (assoc env
                   :ctx
                   ctx)
            (env :input+))))
