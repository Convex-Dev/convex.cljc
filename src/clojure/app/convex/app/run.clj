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


(def ctx-base

  ""

  ($.cvm/juice-refill ($.cvm/ctx)))



(defn ctx-init

  ""

  []

  ($.cvm/fork ctx-base))




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

  [env ctx form]

  (let [juice     ($.cvm/juice ctx)
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

  [env ctx form]

  (eval-trx+ (assoc env
                    :ctx
                    ctx)
             (rest form)))



(defn cvm-out

  ""

  [env ctx form]

  (let [env-2 (eval-form env
                         ctx
                         (second form))]
    (*output* ($.cvm/result (env-2 :ctx)))
    env-2))



(defn cvm-log

  ""

  [env ctx form]

  (let [cvm-sym (second form)]
    (eval-form env
               ctx
               ($.code/def cvm-sym
                           ($.cvm/log ctx)))))



(defn cvm-read

  ""

  [_env _ctx _form]

  (error "CVM special command 'cvm.read' can only be used as first transaction"))



(defn eval-trx

  ""

  [env form]

  (let [ctx         (env :ctx)
        ctx-2       ($.cvm/eval ctx
                                ($.code/do [($.code/def ($.code/symbol "cvm.juice.last")
                                                        ($.code/long (env :juice-last)))
                                            ($.code/def ($.code/symbol "cvm.trx")
                                                        ($.code/long (env :i-trx)))]))
        ctx-3       ($.cvm/expand ctx-2
                                  form)
        exception   ($.cvm/exception ctx-3)]
    (when exception
      (error (str "Exception during expansion of: "
                  form)))
    (let [form-2 ($.cvm/result ctx-3)]
      (if ($.code/list? form-2)
        (let [sym-string (str (first form-2))]
          (if (clojure.string/starts-with? sym-string
                                           "cvm.")
            (if-some [f (case sym-string
                          "cvm.do"   cvm-do
                          "cvm.log"  cvm-log
                          "cvm.out"  cvm-out
                          "cvm.read" cvm-read
                          nil)]
              (f env
                 ctx-2
                 form-2)
              (error (str "Unknown CVM special command: "
                          sym-string)))
            (eval-form env
                       ctx-2
                       form-2)))
        (eval-form env
                   ctx-2
                   form-2)))))



(defn eval-trx+

  ""

  [env form+]

  (reduce (fn [env form]
            (-> (eval-trx env
                          form)
                (update :ctx
                        $.cvm/juice-refill)))
          env
          form+))



(defn exec

  ""

  [src _option+]

  (let [form+      ($.cvm/read-many src)
        form-first (first form+)
        [ctx
         form-2+]  (if ($.code/call? form-first
                                     ($.code/symbol "cvm.read"))
                     [(:ctx ($.disk/load (map (fn [x]
                                                [(str (second x))
                                                 {:map (fn [code]
                                                         ($.code/def (first x)
                                                                     ($.code/quote code)))}])
                                              (rest form-first))
                                         {:init-ctx ctx-init}))
                      (rest form+)]
                     [(ctx-init)
                      form+])
        env        (eval-trx+ {:ctx        ctx
                               :i-trx      0
                               :juice-last 0}
                              (butlast form-2+))]
    (-> (eval-trx env
                  (last form-2+))
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
                               "eval" cmd-eval
                               "load" cmd-load
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


  )
