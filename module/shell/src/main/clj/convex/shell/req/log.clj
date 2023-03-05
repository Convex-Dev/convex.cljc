(ns convex.shell.req.log

  {:author "Adam Helinski"}

  (:require [convex.cell     :as $.cell]
            [convex.clj      :as $.clj]
            [convex.cvm      :as $.cvm]
            [taoensso.timbre :as log]))


;;;;;;;;;; Private


(defn- -do-level

  ;;

  [ctx level f]

  (or (when-not (contains? #{($.cell/* :trace)
                             ($.cell/* :debug)
                             ($.cell/* :info)
                             ($.cell/* :warn)
                             ($.cell/* :error)
                             ($.cell/* :fatal)
                             ($.cell/* :report)}
                           level)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Provded log level is not valid")))
      (f ($.clj/keyword level))))


;;;;;;;;;; Requests


(defn level

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/any (:min-level log/*config*))))



(defn level-set

  [ctx [level]]

  (-do-level ctx
             level
             (fn [level-2]
               (log/set-level! level-2)
               ($.cvm/result-set ctx
                                 level))))



(defn log

  [ctx [level arg+]]

  (-do-level ctx
             level
             (fn [level-2]
               (log/log! level-2
                         nil
                         arg+
                         {:?ns-str "CONVEX-SHELL"})
               ($.cvm/result-set ctx
                                 arg+))))
