(ns convex.app.run

  ""

  {:author "Adam Helinski"}

  (:gen-class)
  (:require [clojure.tools.cli]
            [convex.code        :as $.code]
            [convex.cvm         :as $.cvm]
            [convex.disk        :as $.disk]))


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

  [env i-form form]

  (let [ctx         (env :ctx)
        juice-begin ($.cvm/juice ctx)
        ctx-2       (-> ctx
                        ($.cvm/eval ($.code/do [($.code/def ($.code/symbol "cvm.juice.last")
                                                            ($.code/long (env :juice-last)))
                                                ($.code/def ($.code/symbol "cvm.trx")
                                                            ($.code/long i-form))]))
                        ($.cvm/eval form))
        exception   ($.cvm/exception ctx-2)]
    (when exception
      (error (str "Exception during transaction: "
                  exception)))
    (assoc env
           :ctx        ctx-2
           :juice-last (- juice-begin
                          ($.cvm/juice ctx-2)))))



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
        env        (reduce (fn [env [i-form form]]
                              (-> (eval-form env
                                             i-form
                                             form)
                                  (update :ctx
                                          $.cvm/juice-refill)))
                            {:ctx        ctx
                             :juice-last 0}
                            (partition 2
                                       (interleave (range)
                                                   (butlast form-2+))))]
    (-> (eval-form env
                   (dec (count form-2+))
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

  (*output* [:exception.java err]))



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
