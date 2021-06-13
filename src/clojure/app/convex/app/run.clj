(ns convex.app.run

  ""

  {:author "Adam Helinski"}

  (:gen-class)
  (:require [clojure.string]
            [clojure.tools.cli]
            [convex.code        :as $.code]
            [convex.cvm         :as $.cvm]
            [convex.disk        :as $.disk]))


(declare eval-trx+)


;;;;;;;;;; MIscellaneous


(defn dep+

  ""

  [form+]

  (let [form-first (first form+)]
    (when ($.code/list? form-first)
      (let [item (first form-first)]
        (when ($.code/symbol? item)
          (when (= (str item)
                   "cvm.read")
            (not-empty (reduce (fn [hmap x]
                                 (assoc hmap
                                        (str (first x))
                                        (str (second x))))
                               {}
                               (second form-first)))))))))
          


(def ctx-base

  ""

  ($.cvm/juice-refill ($.cvm/ctx)))



(defn error

  ""

  [message]

  (throw (ex-info message
                  {::error? true})))



(defn print-code

  ""

  [code]

  (-> code
      str
      println))



(defn ^:dynamic *output*

  ""

  [x]

  (print-code x))



(defn eval-form

  ""

  [env form]

  (let [ctx       ($.cvm/juice-refill (env :ctx))
        juice     ($.cvm/juice ctx)
        ctx-2     ($.cvm/eval ctx
                              form)
        exception ($.cvm/exception ctx-2)]
    (when exception
      (error (str "Exception during transaction: "
                  exception)))
    (-> env
        (assoc :ctx        ctx-2
               :juice-last (- juice
                              ($.cvm/juice ctx-2)))
        (update :i-trx
                inc))))



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
    (*output* ($.cvm/result (env-2 :ctx)))
    env-2))



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

  [_env _form]

  (error "CVM special command 'cvm.read' can only be used as first transaction"))




(defn cvm-command

  ""

  [form]

  (when ($.code/list? form)
    (let [sym-string (str (first form))]
      (when (clojure.string/starts-with? sym-string
                                         "cvm.")
        (case sym-string
          "cvm.do"      cvm-do
          "cvm.log"     cvm-log
          "cvm.out"     cvm-out
          "cvm.read"    cvm-read
          "cvm.trx.map" cvm-trx-map
          (error (str "Unknown CVM special command: "
                      sym-string)))))))



(defn inject-value+

  ""

  [env]

  (update env
          :ctx
          (fn [ctx]
            ($.cvm/eval ctx
                        ($.code/do [($.code/def ($.code/symbol "*cvm.juice.last*")
                                                ($.code/long (env :juice-last)))
                                    ($.code/def ($.code/symbol "*cvm.trx.id*")
                                                ($.code/long (env :i-trx)))])))))



(defn expand

  ""

  [env form]

  (let [ctx-2     ($.cvm/expand (env :ctx)
                                form)
        exception ($.cvm/exception ctx-2)]
    (when exception
      (error (str "Exception during expansion of: "
                  form)))
    (assoc env
           :ctx
           ctx-2)))



(defn eval-trx

  ""

  [env form]

  (let [env-2  (-> env
                   inject-value+
                   (expand form))
        form-2 (-> env-2
                   :ctx
                   $.cvm/result)]
    (if-some [f (cvm-command form-2)]
      (f env-2
         form-2)
      (if-some [map-trx (env :map-trx)]
        (-> env-2
            (dissoc :map-trx)
            (eval-trx ($.code/list [map-trx
                                    form-2]))
            (assoc :map-trx
                   map-trx))
        (eval-form env-2
                   form-2)))))



(defn eval-trx+

  ""

  [env form+]

  (reduce eval-trx
          env
          form+))



(defn exec

  ""

  [src _option+]

  (let [form+      ($.cvm/read-many src)
        dep+       (dep+ form+)
        [ctx
         form-2+]  (if dep+
                     [(:ctx ($.disk/load ($.cvm/fork ctx-base)
                                         dep+))
                      (rest form+)]
                     [($.cvm/fork ctx-base)
                      form+])
        env        (eval-trx+ {:ctx        ctx
                               :i-trx      0
                               :juice-last 0}
                              form-2+)]
    (-> env
        :ctx
        $.cvm/result
        *output*)))


;;;;;;;;;; Eval


(defn cmd-eval

  ""

  [arg+ option+]

  (exec (first arg+)
        option+))


;;;;;;;;;; Load files


(defn cmd-load

  ""

  [arg+ option+]

  (exec (slurp (first arg+))
        option+))


;;;;;;;;;; Watch files


(defn cmd-watch

  ""

  [arg+ _option+]

  (let [path  (first arg+)
        form+ ($.cvm/read-many (slurp path))
        -dep+ (dep+ form+)]
    ($.disk/watch ($.cvm/fork ctx-base)
                  (conj -dep+
                        ['*cvm.master*
                         path])
                  (fn on-run [{:as   env
                               :keys [ctx]}]
                    (let [ctx-2 ($.cvm/eval ctx
                                            ($.cvm/read-form '(let [trx+ *cvm.master*]
                                                                (undef *cvm.master*)
                                                                (next trx+))))
                          form+ ($.cvm/result ctx-2)]
                      (eval-trx+ (assoc env
                                        :ctx        ctx-2
                                        :i-trx      0
                                        :juice-last 0)
                                 (if (dep+ form+)
                                   (rest form+)
                                   form+)))))))


;;;;;;;;;; Main command


(defn handle-exception

  ""

  [err]

  ;(*output* [:exception.java err])
  (throw err))



(def cli-option+

  ""

  [])



(defn -main

  ""

  [& arg+]

  (try
    (let [{arg+    :arguments
           option+ :options}  (clojure.tools.cli/parse-opts arg+
                                                            cli-option+)
          command             (first arg+)
          f                   (case command
                               "eval"  cmd-eval
                               "load"  cmd-load
                               "watch" cmd-watch
                               nil)]
      (if f
        (f (rest arg+)
           option+)
        (error (format "Unknown command: %s"
                       command))))


    (catch clojure.lang.ExceptionInfo err
      (let [data (ex-data err)]
        (if (::error? data)
          (do
            (*output* [:error (.getMessage err)])
            ;(System/exit 42)
            )
          (handle-exception err))))


    (catch Throwable err
      (handle-exception err))))



;;;;;;;;;;


(comment


  (-main "load"
         "src/convex/dev/app/run.cvx")

  (.close x)
  (def x
       (-main "watch"
              "src/convex/dev/app/run.cvx"))

  (deref x)

  )
