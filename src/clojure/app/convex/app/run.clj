(ns convex.app.run

  ""

  {:author "Adam Helinski"}

  (:gen-class)
  (:require [clojure.tools.cli]
            [convex.code        :as $.code]
            [convex.cvm         :as $.cvm])
  )


;;;;;;;;;; Helpers


(defn exec

  ""

  
  ([src]

   (exec ($.cvm/ctx)
         src))


  ([ctx src]

   (-> ($.cvm/eval ctx
                   ($.cvm/read src))
       $.cvm/result
       str
       println)))


;;;;;;;;;; Eval


(defn cmd-eval

  ""

  [arg+ _option+]

  (exec (first arg+)))


;;;;;;;;;; Load files


(defn cmd-load

  ""

  [arg+ _option+]

  (exec (slurp (first arg+))))


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
      (do
        (println (format "Unknown command: %s"
                         command))
        #_(System/exit 42)))))
