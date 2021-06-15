(ns convex.run

  ""

  {:author "Adam Helinski"}

  (:import (java.io File))
  (:refer-clojure :exclude [eval
                            load])
  (:require [clojure.string]
            [convex.code     :as $.code]
            [convex.cvm      :as $.cvm]
            [convex.sync     :as $.sync]
            [convex.watch    :as $.watch]))


(declare eval-form
         eval-trx+)


;;;;;;;;;; Miscellaneous


(def d*ctx-base

  ""

  (delay
    ($.cvm/juice-refill ($.cvm/ctx))))



(defn dep+

  ""

  [trx+]

  (let [trx-first (first trx+)]
    (when ($.code/list? trx-first)
      (let [item (first trx-first)]
        (when ($.code/symbol? item)
          (when (= (str item)
                   "cvm.read")
            (not-empty (reduce (fn [hmap x]
                                 (assoc hmap
                                        (str (first x))
                                        (str (second x))))
                               {}
                               (second trx-first)))))))))


;;;;;;;;;; Output


(defn out-default

  ""

  [x]

  (-> x
      str
      tap>))



(defn error

  ""

  [env type arg]

  (let [err (cond->
              [:cvm.error
               type]
              (some? arg)
              (conj arg))]
    ((:out env) err)
    (assoc env
           ::error
           err)))


;;;;;;;;;; Special transactions


(defn cvm-do

  ""

  [env form]

  (eval-trx+ env
             (rest form)))



(defn cvm-out

  ""

  [env form]

  (let [env-2 (eval-form env
                         (second form))]
    (if (env-2 ::error)
      env-2
      (do
        ((env-2 :out) ($.cvm/result (env-2 :ctx)))
        env-2))))



(defn cvm-log

  ""

  [env form]

  (let [cvm-sym (second form)]
    (eval-form env
               ($.code/def cvm-sym
                           ($.cvm/log (env :ctx))))))



(defn cvm-trx-map

  ""

  [env form]

  (assoc env
         :map-trx
         (second form)))



(defn cvm-read

  ""

  [env _form]

  (error env
         :read.forbidden
         "CVM special command 'cvm.read' can only be used as first transaction"))


;;;;;


(defn cvm-command

  "Special transaction"

  [trx]

  (when ($.code/list? trx)
    (let [sym-string (str (first trx))]
      (when (clojure.string/starts-with? sym-string
                                         "cvm.")
        (case sym-string
          "cvm.do"      cvm-do
          "cvm.log"     cvm-log
          "cvm.out"     cvm-out
          "cvm.read"    cvm-read
          "cvm.trx.map" cvm-trx-map
          (fn [env _trx]
            (error env
                   :cvm.unknown
                   sym-string)))))))


;;;;;;;;;; Preparing transactions


(defn expand

  ""

  [env form]

  (let [ctx-2     ($.cvm/expand (env :ctx)
                                form)
        exception ($.cvm/exception ctx-2)]
    (if exception
      (error env
             :expansion
             [form
              exception])
      (assoc env
             :ctx
             ctx-2))))



(defn inject-value+

  ""

  [env]

  (let [ctx       ($.cvm/eval (env :ctx)
                              ($.code/do [($.code/def ($.code/symbol "*cvm.juice.last*")
                                                      ($.code/long (env :juice-last)))
                                          ($.code/def ($.code/symbol "*cvm.trx.id*")
                                                      ($.code/long (env :i-trx)))]))
        exception ($.cvm/exception ctx)]
    (if exception
      (error env
             :inject-value+
             exception)
      (assoc env
             :ctx
             ctx))))


;;;;;;;;;; Evaluation


(defn eval-form

  ""

  [env form]

  (let [ctx       ($.cvm/juice-refill (env :ctx))
        juice     ($.cvm/juice ctx)
        ctx-2     ($.cvm/eval ctx
                              form)
        exception ($.cvm/exception ctx-2)]
    (if exception
      (error env
             :eval.trx
             exception)
      (-> env
          (assoc :ctx        ctx-2
                 :juice-last (- juice
                                ($.cvm/juice ctx-2)))
          (update :i-trx
                  inc)))))



(defn eval-trx

  ""

  [env trx]

  (let [env-2 (inject-value+ env)]
    (if (env-2 ::error)
      env-2
      (let [env-3 (expand env-2
                          trx)]
        (if (env-3 ::error)
          env-3
          (let [trx-2 (-> env-3
                          :ctx
                          $.cvm/result)]
            (if-some [f (cvm-command trx-2)]
              (f env-2
                 trx-2)
              (if-some [map-trx (env :map-trx)]
                (-> env-2
                    (dissoc :map-trx)
                    (eval-trx ($.code/list [map-trx
                                            trx-2]))
                    (assoc :map-trx
                           map-trx))
                (eval-form env-2
                           trx-2)))))))))



(defn eval-trx+

  ""

  
  ([env]

   (eval-trx+ env
              (env :trx+)))


  ([env trx+]

   (reduce (fn [env-2 trx]
             (let [env-3 (eval-trx env-2
                                   trx)]
               (if (env-3 ::error)
                 (reduced env-3)
                 env-3)))
           env
           trx+)))



(defn exec-trx+

  ""

  [env]

  (-> env
      (assoc :i-trx      0
             :juice-last 0)
      eval-trx+
      (dissoc :map-trx)))


;;;;;;;;;; 


(defn init

  ""

  [env]

  (update env
          :out
          #(or %
               out-default)))



(defn read-src

  ""

  [env path]

  (let [[err
         src] (try
                [nil
                 (slurp path)]
                (catch Throwable err
                  [err
                   nil]))]
    (if err
      (error env
             :main.src
             [path
              err])
      (assoc env
             :src
             src))))



(defn process-src

  ""

  [env]

  (let [[err
         trx+] (try
                 [nil
                  ($.cvm/read-many (env :src))]
                 (catch Throwable err
                   [err
                    nil]))]
    (if err
      (error env
             :read.src
             err)
      (let [dep+' (dep+ trx+)]
        (-> env
            (assoc :dep+ dep+'
                   :trx+ (cond->
                           trx+
                           (seq dep+')
                           rest))
            (dissoc :src))))))



(defn main-file

  ""

  [env path]

  (let [env-2 (-> env
                  init
                  (read-src path))]
    (if (env-2 ::error)
      env-2
      (process-src env-2))))



(defn once

  ""

  [env]

  (if (env ::error)
    env
    (if-some [dep+' (env :dep+)]
      (let [env-2 (merge env
                         ($.sync/disk ($.cvm/fork @d*ctx-base)
                                      dep+'))
            err   (env-2 ::error)]
        (if err
          (error env-2
                 :dep+
                 err)
          (exec-trx+ env-2)))
      (-> env
          (assoc :ctx
                 ($.cvm/fork @d*ctx-base))
          exec-trx+))))


;;;;;;;;;; Evaluating a given source string


(defn eval

  ""


  ([src]

   (eval nil
         src))


  ([env src]

   (-> env
       init
       (assoc :src
              src)
       process-src
       once)))


;;;;;;;;;; Load files


(defn load

  ""


  ([path]

   (load nil
         path))


  ([env path]

   (-> (main-file env
                  path)
       once)))


;;;;;;;;;; Watch files


(defn watch

  ""


  ([path]

   (watch nil
          path))


  ([env ^String path]

   (let [a*env ($.watch/init (assoc env
                                    :extra+
                                    #{(.getCanonicalPath (File. path))}))]
     (send a*env
           (fn [env]
             (assoc env
                    :on-change
                    (fn on-change [{:as       env-2
                                    dep-old+  :dep+
                                    error-dep :error}]
                      (let [env-3 (dissoc env-2
                                          ::error)]
                        (if error-dep
                          (error env-3
                                 :dep
                                 error-dep)
                          (if (or (nil? dep-old+)
                                  (seq (env-3 :extra->change)))
                            (let [env-4 (main-file env-3
                                                   path)]
                              (if (env-4 ::error)
                                env-4
                                (let [dep-new+ (env-4 :dep+)]
                                  (if (= (not-empty dep-new+)
                                         dep-old+)
                                    (-> env-4
                                        (dissoc :extra->change)
                                        $.sync/patch
                                        $.sync/eval
                                        exec-trx+)
                                    (do
                                      ($.watch/-stop env-4)
                                      ($.watch/-start a*env
                                                      (-> (select-keys env-4
                                                                       [:cycle
                                                                        :ctx-base
                                                                        :dep+
                                                                        :ms-debounce
                                                                        :on-change
                                                                        :out
                                                                        :extra+
                                                                        :trx+])
                                                          (assoc :sym->dep
                                                                 dep-new+))))))))
                            (exec-trx+ env-3))))))))
     ($.watch/start a*env)
     a*env)))


;;;;;;;;;;


(comment


  (eval "(cvm.out (+ 2 2))")



  (load "src/convex/dev/app/run.cvx")



  (def a*env
       (watch "src/convex/dev/app/run.cvx"))

  (clojure.pprint/pprint (dissoc @a*env
                                 :input->code))

  ($.watch/stop a*env)


  (agent-error a*env)


  )
