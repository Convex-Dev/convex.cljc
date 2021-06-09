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

  (:refer-clojure :exclude [load
                            sync])
  (:require [convex.cvm :as $.cvm]))


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


;;;;;;;;;; An input can be loaded in more than one step, hence there are `input` -> `step+` links


(defn link

  "Given steps, returns a map with:
  
   | Key | Value |
   | `input->i-step+` | Map of `input` -> `vector with indices to reladed steps` |
   | `step+` | Given argument but augmented, each step points back to its input |

   This organization assumes that a given input can be used in more than one step (eg. loadinga file
   more than once).
  
   Used by [[env]]."

  [step+]

  (reduce (fn [env [i-step [input step]]]
            (-> env
                (update-in [:input->i-step+
                            input]
                           (fnil conj
                                 [])
                          i-step)
                (update :step+
                        conj
                        (assoc step
                               :i     i-step
                               :input input))))
          {:input->i-step+ {}
           :step+          []}
          (partition 2
                     (interleave (range)
                                 step+))))



(defn sync

  "When `code` is avaiable for an `input`, it is fed through the `:map` functions of all the
   related steps.
  
   Meant to be used after [[load]] or [[reload]]."


  ([env]

   (reduce-kv sync
              env
              (env :input->code)))


  ([env input code]

   (update env
           :step+
           (fn [step+]
             (reduce (fn [step-2+ i-step]
                       (update step-2+
                               i-step
                              (fn [{:as      step
                                    map-code :map}]
                                 (assoc step
                                        :code
                                        (cond->
                                          code
                                          map-code
                                          map-code)))))
                     step+
                     (get-in env
                             [:input->i-step+
                              input]))))))


;;;;;;;;;; Creating an execution environment


(defn env

  "Entry point, creates an executing environment given `step+` and `option+`.

   Besides what the [[convex.disk]] namespace describes, `option+` requires an additial key `:read` which
   is a function that receive an input (eg. a file path) and returns code (eg. effectively reads the file).
  
   Effectively loads all inputs using [[load]]. Commonly, [[synced]] is used after that step."

  
  ([step+]

   (env step+
        nil))


  ([step+ option+]

   (-> (link step+)
       (assoc :after-run (or (:after-run option+)
                             identity)
              :eval      (or (:eval option+)
                             $.cvm/eval)
              :init-ctx  (or (:init-ctx option+)
                             $.cvm/ctx)
              :read      (or (:read option+)
                             (throw (IllegalArgumentException. "Read function is mandatory"))))
       load)))


;;;;;;;;;; Reading source and handling change


(defn load

  "Used by [[env]] to load initial inputs.
  
   Adds to `env` 2 keys:

   | Key | Value |
   |---|---|
   | `:input->code` | Map of `input` -> `loaded code` |
   | `:input->error` | Represent failures, map of `input` -> `exception` |" 


  ([env]

   (reduce load
           (assoc env
                  :input->code  {}
                  :input->error {})
           (keys (env :input->i-step+))))


  ([env input]

   (try
     (assoc-in env
               [:input->code
                input]
               ((env :read) input))
     (catch Throwable err
       (-> env
           (assoc :error
                  :input->error)
           (assoc-in [:input->error
                      input]
                     err))))))



(defn reload

  "Reloads an input and run [[sync]] that that only input.
  
   Like [[load]], updates `:input->code` and `:input->error` as needed.

   Runs the result through [[update-error]]."

  [env input]

  (try
    (let [code ((env :read) input)]
      (-> env
          (assoc-in [:input->code
                     input]
                    code)
          (update :input->error
                  dissoc
                  input)
          (sync input
                code)
          update-error))
    (catch Throwable err
      (-> env
          (assoc :error
                 :input->error)
          (update :input->code
                  dissoc
                  input)
          (assoc-in [:input->error
                     input]
                    err)))))



(defn unload

  "Opposite of [[reload]], removes an input and code from related steps.

   Runs the result through [[update-error]]."

  [env input]

  (-> env
      (update :step+
              (fn [step+]
                (reduce (fn [step-2+ i-step]
                          (update step-2+
                                  i-step
                                  dissoc
                                  :code))
                        step+
                        (get-in env
                                [:input->i-step+
                                 input]))))
      (update :input->code
              dissoc
              input)
      (update :input->error
              dissoc
              input)
      update-error))


;;;;;;;;;; Executing steps


(defn exec

  "When any code has been loaded/reloaded, executes all steps one by one.
  
   Resulting context is attached under `:ctx` unless an error occurs and figures under `:error`."

  [{:as   env
    :keys [after-run
           eval
           init-ctx
           step+]}]

  (try
    (assoc env
           :ctx
           (after-run (reduce (fn [ctx step]
                                (try
                                  ((or (step :eval)
                                       eval)
                                   ctx
                                   (step :code))
                                  (catch Throwable err
                                    (throw (ex-info "During evaluation"
                                                    {::error :eval
                                                     ::step  step}
                                                    err)))))
                              (init-ctx)
                              (eduction (filter :code)
                                        step+))))
    (catch Throwable err
      (-> env
          (assoc :error      :error-eval
                 :error-eval err)
          (dissoc :ctx)
          after-run))))
