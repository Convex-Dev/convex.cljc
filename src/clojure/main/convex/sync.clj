(ns convex.sync

  "About running Convex Lisp inputs and producing a context.
  
   This namespace is currently the core implementation of [[convex.disk]]. However, it is written
   in such a way that it is generic and could be applied to sources other than files.

   For understanding what is going on, it is best to study it alongside [[convex.disk]], especially [[convex.disk/load]]."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [eval
                            load])
  (:require [convex.cvm  :as $.cvm]))


(declare load
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
                            :load)
              (update error
                      1
                      assoc
                      input
                      err)
              [:load
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

  (-> (let [error        (env :error)
            input->error (when (and error
                                    (identical? (first error)
                                                :load))
                           (not-empty (dissoc (second error)
                                              input)))]
        (if input->error
          (assoc env
                 :error
                 [:load
                  input->error])
          (dissoc env
                  :error)))
      (update :input->code
              dissoc
              input)
      (load input)))



(defn unload

  "Opposite of [[load]], removes code for the given `input`.

   This adds an error with [[assoc-err-read]] since it means the input is now missing."

  [env input]

  (-> env
      (update :input->code
              dissoc
              input)
      (assoc-err-read input
                      :unload)))


;;;;;;;;;;


(defn patch

  "Looks for changes in `:input->change` (map of `input` -> `One of #{:create :delete :modify}`) and
   applies them by using [[reload]] and [[unload]] as needed."

  ([env]

   (reduce-kv patch
              (dissoc env
                      :input->change)
              (env :input->change)))


  ([env path change]

   ((if (identical? change
                    :delete)
      unload
      reload)
    env
    path)))


;;;;;;;;;; Executing steps


(let [-eval (fn [env ctx]
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
                      (env :input+)))]

  (defn eval

    "Evaluates the code for all `:input+` in `env` on `ctx`, unless there is an `:error` attached.
    
      If `ctx` is not explicitly provided, it is fetched and forked from `:ctx-base` in the given `env`."


    ([env]

     (if (env :error)
       env
       (-eval env
              ($.cvm/fork (env :ctx-base)))))


    ([env ctx]

     (if (env :error)
       env
       (-eval env
              ctx)))))
