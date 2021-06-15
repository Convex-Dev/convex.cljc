(ns convex.run

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [eval
                            load])
  (:require [clojure.string]
            [convex.code     :as $.code]
            [convex.cvm      :as $.cvm]
            [convex.disk     :as $.disk]
            [convex.sync     :as $.sync]))


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
           :error
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
    (if (env-2 :error)
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
    (if (env-2 :error)
      env-2
      (let [env-3 (expand env-2
                          trx)]
        (if (env-3 :error)
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
               (if (env-3 :error)
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
      eval-trx+))


;;;;;;;;;; 


(defn main

  ""

  [env path f]

  (let [env-2 (update env
                      :out
                      #(or %
                           out-default))
        [err
         src] (try
                [nil
                 (slurp path)]
                (catch Throwable err
                  [err
                   nil]))]
    (if err
      (error env-2
             :main.src
             [path
              err])
      (f env-2
         src))))


;;;;;;;;;; Evaluating a given source string


(defn eval

  ""


  ([src]

   (eval nil
         src))


  ([env src]

   (let [trx+  ($.cvm/read-many src)
         dep+' (dep+ trx+)]
     (if (seq dep+')
       (let [env   (-> (merge env
                              ($.disk/load ($.cvm/fork @d*ctx-base)
                                           dep+'))
                       (assoc :trx+
                              (rest trx+)))
             err   (env :error)]
         (if err
           (error env
                  :dep+
                  err)
           (exec-trx+ env)))
       (-> env
           (assoc :ctx  ($.cvm/fork @d*ctx-base)
                  :trx+ trx+)
           exec-trx+)))))


;;;;;;;;;; Load files


(defn load

  ""


  ([path]

   (load nil
         path))


  ([env path]

   (main env
         path
         eval)))


;;;;;;;;;; Watch files


(let [-watcher (fn -watcher [*watcher env path trx+ dep+']
                 (swap! *watcher
                        (fn [watcher]
                          (some-> watcher
                                  $.disk/watch-stop)
                          ($.disk/watch dep+'
                                        (fn on-change [env]
                                          (let [env-2 (if (seq (env :extra->change))
                                                        (-> (let [trx+     ($.cvm/read-many (slurp path))
                                                                  dep-new+ (dep+ trx+)]
                                                              (if (= dep-new+
                                                                     (env :dep+))
                                                                (-> env
                                                                    (assoc :trx+
                                                                           (if dep-new+
                                                                             (rest trx+)
                                                                             trx+))
                                                                    $.sync/patch
                                                                    $.sync/eval)
                                                                (-watcher *watcher
                                                                          (dissoc env
                                                                                  :input->change)
                                                                          path
                                                                          trx+
                                                                          dep-new+)))
                                                            (dissoc :extra->change))
                                                        env)]
                                            (-> env-2
                                                exec-trx+
                                                (dissoc :map-trx))))
                                        (assoc env
                                               :dep+   dep+'
                                               :extra+ #{path}
                                               :trx+   (if dep+'
                                                         (rest trx+)
                                                         trx+))))))]
  (defn watch

    ""


    ([path]

     (watch nil
            path))


    ([env path]

     (main env
           path
           (fn [env-2 src]
             (let [trx+     ($.cvm/read-many src)
                   *watcher (atom nil)]
               (-watcher *watcher
                         env-2
                         path
                         trx+
                         (dep+ trx+))
               (reify

                 clojure.lang.IDeref

                   (deref [_]
                     @@*watcher)

                 java.lang.AutoCloseable

                   (close [_]
                     ($.disk/watch-stop @*watcher)))))))))


;;;;;;;;;;


(comment


  (load "src/convex/dev/app/run.cvx")

  (def w*ctx
       (watch "src/convex/dev/app/run.cvx"))

  (.close w*ctx)

  (deref w*ctx)

  )
