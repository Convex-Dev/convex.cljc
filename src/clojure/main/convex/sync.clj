(ns convex.sync

  "About running Convex Lisp input and producing a context.
  
   This namespace is currently the core implementation of [[convex.disk]]. However, it is written
   in such a way that it is generic and could be applied to sources other than files.

   For understanding what is going on, it is best to study it alongsied [[convex.disk]]."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [load
                            sync])
  (:require [convex.cvm :as $.cvm]))


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

  ""

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

  ""


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

  ""

  
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
                             (throw (IllegalArgumentException. "Read function is mandatory")))))))


;;;;;;;;;; Reading source and handling change


(defn load

  ""


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

  ""

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

  ""

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

  ""

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
