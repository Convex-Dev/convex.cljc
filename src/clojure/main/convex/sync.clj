(ns convex.sync

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [load
                            sync])
  (:require [convex.cvm :as $.cvm]))


;;;;;;;;;; Helpers


(defn update-error

  ""

  [env]

  (if (seq (env :src->error))
    (assoc env
           :error
           :src->error)
    (dissoc env
            :error)))


;;;;;;;;;; A source file can be loaded in more than one step, hence there are `src` -> `step+` links


(defn link

  ""

  [step+]

  (reduce (fn [env [i-step [src step]]]
            (-> env
                (update-in [:src->i-step+
                            src]
                           (fnil conj
                                 [])
                          i-step)
                (update :step+
                        conj
                        (assoc step
                               :i   i-step
                               :src src))))
          {:src->i-step+ {}
           :step+         []}
          (partition 2
                     (interleave (range)
                                 step+))))



(defn sync

  ""


  ([env]

   (reduce-kv sync
              env
              (env :src->code)))


  ([env src code]

   (update env
           :step+
           (fn [step+]
             (reduce (fn [step-2+ i-step]
                       (update step-2+
                               i-step
                              (fn [{:as   step
                                     :keys [wrap]}]
                                 (assoc step
                                        :code
                                        (cond->
                                          code
                                          wrap
                                          wrap)))))
                     step+
                     (get-in env
                             [:src->i-step+
                              src]))))))


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
                  :src->code  {}
                  :src->error {})
           (keys (env :src->i-step+))))


  ([env src]

   (try
     (assoc-in env
               [:src->code
                src]
               ((env :read) src))
     (catch Throwable err
       (-> env
           (assoc :error
                  :src->error)
           (assoc-in [:src->error
                      src]
                     err))))))



(defn reload

  ""

  [env src]

  (try
    (let [code ((env :read) src)]
      (-> env
          (assoc-in [:src->code
                     src]
                    code)
          (update :src->error
                  dissoc
                  src)
          (sync src
                code)
          update-error))
    (catch Throwable err
      (-> env
          (assoc :error
                 :src->error)
          (update :src->code
                  dissoc
                  src)
          (assoc-in [:src->error
                     src]
                    err)))))



(defn unload

  ""

  [env src]

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
                                [:src->i-step+
                                 src]))))
      (update :src->code
              dissoc
              src)
      (update :src->error
              dissoc
              src)
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
