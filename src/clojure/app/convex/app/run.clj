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



(defn cvm-print

  ""

  [cvm-object]

  (println (str cvm-object)))



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
        ctx-2       (reduce (fn [ctx-2 form]
                              (let [ctx-3     ($.cvm/eval ctx-2
                                                          form)
                                    exception ($.cvm/exception ctx-3)]
                                (if exception
                                  (error (str "Exception during transaction: "
                                              exception))
                                  ($.cvm/juice-refill ctx-3))))
                            ctx
                            (butlast form-2+))]
    (-> ($.cvm/eval ctx-2
                    (last form-2+))
        $.cvm/result
        cvm-print)))


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

  (println :Exception
           err))



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
            (println (.getMessage err))
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
