(ns convex.app.run

  ""

  {:author "Adam Helinski"}

  (:gen-class)
  (:require [clojure.tools.cli]
            [convex.code        :as $.code]
            [convex.cvm         :as $.cvm]
            [convex.disk        :as $.disk]))


;;;;;;;;;; Helpers


(defn error

  ""

  [message]

  (println message)
  #_(System/exit 42))



(defn cvm-print

  ""

  [cvm-object]

  (println (str cvm-object)))



(def kw-convex

  ""

  ($.code/keyword "convex"))



(defn process-config

  ""

  [result]

  (let [run (get result
                 ($.code/keyword "run"))]
    (when-not ($.code/vector? run)
      (error "`:run` argument must be a vector"))
    (-> (map (fn [cvm-string]
               (when-not ($.code/string? cvm-string)
                 (error (str "Should be file path to run, not: "
                             cvm-string)))
               [(str cvm-string)])
             run)
        $.disk/load
        :ctx
        $.cvm/result
        cvm-print)))



(defn exec

  ""

  [src _option+]

  (let [ctx       ($.cvm/eval ($.cvm/ctx)
                              ($.cvm/read src))
        exception ($.cvm/exception ctx)]
    (if exception
      (println exception)
      (let [result ($.cvm/result ctx)]
        (if (and ($.code/map? result)
                 (contains? result
                            kw-convex))
          (process-config result)
          (cvm-print result))))))


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


(def cli-option+

  ""

  [])



(defn -main

  ""

  [& arg+]

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
                     command)))))


;;;;;;;;;;


(comment


  (-main "load"
         "src/convex/dev/app/run.cvx")


  )
