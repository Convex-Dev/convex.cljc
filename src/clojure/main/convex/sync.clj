(ns convex.sync

  "About running Convex Lisp inputs and producing a context.
  
   This namespace is currently the core implementation of [[convex.disk]]. However, it is written
   in such a way that it is generic and could be applied to sources other than files.

   For understanding what is going on, it is best to study it alongside [[convex.disk]], especially [[convex.disk/load]]."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [load])
  (:require [convex.cvm  :as $.cvm]))


(declare eval-form
         exec
         load
         unload)


;;;;;;;;;; Altering the "env" map (executing environment)


(defn assoc-code

  "Associates the given `code` to `input` in `env`."

  [env input code]

  (assoc-in env
            [:input->code
             input]
            code))



(defn assoc-err-read

  "Adds `err` as a read error that occured when reading `input`."

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


;;;;;;;;;; Dealing with inputs


(defn load

  "Reads the given `input` (or all `:inputs+` in `env`).
  
   Essentially does `((env :read) env input)`, which means that the `:read` function in `env`
   must appropriately handle read errors using [[assoc-err-read]].

   Even in case of errors, all inputs are processed."

  ([env]

   (reduce (env :read)
           env
           (env :input+)))


  ([env input]

   ((env :read)
    env
    input)))



(defn reload

  "Like [[load]] but meant to be used whenever an input is updated.

   Updates read errors."

  [env input]

  (-> env
      (unload input)
      (load input)))



(defn unload

  "Opposite of [[load]], removes code for the given `input`.

   Updates existing read errors."

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

  "Evaluates the code for all `:input+` in `env` on `ctx`, unless there is an `:error` attached."

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
