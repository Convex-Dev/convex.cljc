(ns convex.run

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.lang.impl ErrorValue)
           (java.io File))
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


(defn datafy-exception

  ""

  [^ErrorValue exception]

  ($.code/map {($.code/keyword "code")    (.getCode exception)
               ($.code/keyword "message") (.getMessage exception)
               ($.code/keyword "trace")   ($.code/vector (.getTrace exception))}))



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
    ((:convex.run/out env) err)
    (assoc env
           :convex.run/error
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
    (if (env-2 :convex.run/error)
      env-2
      (do
        ((env-2 :convex.run/out) ($.cvm/result (env-2 :convex.sync/ctx)))
        env-2))))



(defn cvm-log

  ""

  [env form]

  (let [cvm-sym (second form)]
    (eval-form env
               ($.code/def cvm-sym
                           ($.cvm/log (env :convex.sync/ctx))))))



(defn cvm-trx-map

  ""

  [env form]

  (assoc env
         :convex.run/map-trx
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

  (let [ctx-2     ($.cvm/expand (env :convex.sync/ctx)
                                form)
        exception ($.cvm/exception ctx-2)]
    (if exception
      (error env
             :expansion
             [form
              exception])
      (assoc env
             :convex.sync/ctx
             ctx-2))))



(defn inject-value+

  ""

  [env]

  (let [ctx       ($.cvm/eval (env :convex.sync/ctx)
                              ($.code/do [($.code/def ($.code/symbol "*cvm.juice.last*")
                                                      ($.code/long (env :convex.run/juice-last)))
                                          ($.code/def ($.code/symbol "*cvm.trx.id*")
                                                      ($.code/long (env :convex.run/i-trx)))]))
        exception ($.cvm/exception ctx)]
    (if exception
      (error env
             :inject-value+
             exception)
      (assoc env
             :convex.sync/ctx
             ctx))))


;;;;;;;;;; Evaluation


(defn eval-form

  ""

  [env form]

  (let [ctx       ($.cvm/juice-refill (env :convex.sync/ctx))
        juice     ($.cvm/juice ctx)
        ctx-2     ($.cvm/eval ctx
                              form)
        exception ($.cvm/exception ctx-2)]
    (if exception
      (error env
             :eval.trx
             (str (datafy-exception exception)))
      (-> env
          (assoc :convex.run/juice-last (- juice
                                           ($.cvm/juice ctx-2))
                 :convex.sync/ctx       ctx-2)
          (update :convex.run/i-trx
                  inc)))))



(defn eval-trx

  ""

  [env trx]

  (let [env-2 (inject-value+ env)]
    (if (env-2 :convex.run/error)
      env-2
      (let [env-3 (expand env-2
                          trx)]
        (if (env-3 :convex.run/error)
          env-3
          (let [trx-2 (-> env-3
                          :convex.sync/ctx
                          $.cvm/result)]
            (if-some [f (cvm-command trx-2)]
              (f env-3
                 trx-2)
              (if-some [map-trx (env :convex.run/map-trx)]
                (let [env-4 (eval-form env-3
                                       ($.code/list [map-trx
                                                     ($.code/quote trx-2)]))]
                  (if (env-4 :convex.run/error)
                    env-4
                    (-> env-4
                        (dissoc :convex.run/map-trx)
                        (eval-trx (-> env-4
                                      :convex.sync/ctx
                                      $.cvm/result))
                        (assoc :convex.run/map-trx
                               map-trx))))
                (eval-form env-3
                           trx-2)))))))))



(defn eval-trx+

  ""

  
  ([env]

   (eval-trx+ env
              (env :convex.run/trx+)))


  ([env trx+]

   (reduce (fn [env-2 trx]
             (let [env-3 (eval-trx env-2
                                   trx)]
               (if (env-3 :convex.run/error)
                 (reduced env-3)
                 env-3)))
           env
           trx+)))



(defn exec-trx+

  ""

  [env]

  (-> env
      (assoc :convex.run/i-trx      0
             :convex.run/juice-last 0)
      eval-trx+
      (dissoc :convex.run/map-trx)))


;;;;;;;;;; 


(defn init

  ""

  [env]

  (update env
          :convex.run/out
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
             :convex.run/src
             src))))



(defn process-src

  ""

  [env]

  (let [[err
         trx+] (try
                 [nil
                  ($.cvm/read-many (env :convex.run/src))]
                 (catch Throwable err
                   [err
                    nil]))]
    (if err
      (error env
             :read.src
             err)
      (let [dep+' (dep+ trx+)]
        (-> env
            (assoc :convex.run/dep+ dep+'
                   :convex.run/trx+ (cond->
                                      trx+
                                      (seq dep+')
                                      rest))
            (dissoc :convex.run/src))))))



(defn main-file

  ""

  [env path]

  (let [env-2 (-> env
                  init
                  (read-src path))]
    (if (env-2 :convex.run/error)
      env-2
      (process-src env-2))))



(defn once

  ""

  [env]

  (if (env :convex.run/error)
    env
    (if-some [dep+' (env :convex.run/dep+)]
      (let [env-2    (merge env
                            ($.sync/disk ($.cvm/fork @d*ctx-base)
                                         dep+'))
            err-sync (env-2 :convex.sync/error)]
        (if err-sync
          (error env-2
                 :sync-dep+
                 err-sync)
          (exec-trx+ env-2)))
      (-> env
          (assoc :convex.sync/ctx
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
       (assoc :convex.run/src
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
                                    :convex.watch/extra+
                                    #{(.getCanonicalPath (File. path))}))]
     (send a*env
           (fn [env]
             (assoc env
                    :convex.watch/on-change
                    (fn on-change [{:as      env-2
                                    dep-old+ :convex.run/dep+
                                    err-sync :convex.sync/error}]
                      (let [env-3 (dissoc env-2
                                          :convex.run/error)]
                        (if err-sync
                          (error env-3
                                 :sync-dep+
                                 err-sync)
                          (if (or (nil? dep-old+)
                                  (seq (env-3 :convex.watch/extra->change)))
                            (let [env-4 (main-file env-3
                                                   path)]
                              (if (env-4 :convex.error/error)
                                env-4
                                (let [dep-new+ (env-4 :convex.run/dep+)]
                                  (if (= (not-empty dep-new+)
                                         dep-old+)
                                    (-> env-4
                                        (dissoc :convex.watch/extra->change)
                                        $.sync/patch
                                        $.sync/eval
                                        exec-trx+)
                                    (do
                                      ($.watch/-stop env-4)
                                      ($.watch/-start a*env
                                                      (-> (select-keys env-4
                                                                       [:convex.run/dep+
                                                                        :convex.run/out
                                                                        :convex.run/trx+
                                                                        :convex.sync/ctx-base
                                                                        :convex.watch/cycle
                                                                        :convex.watch/extra+
                                                                        :convex.watch/ms-debounce
                                                                        :convex.watch/on-change])
                                                          (assoc :convex.watch/sym->dep
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
