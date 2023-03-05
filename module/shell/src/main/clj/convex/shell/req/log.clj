(ns convex.shell.req.log

  {:author "Adam Helinski"}

  (:import (java.io FileWriter))
  (:require [convex.cell        :as $.cell]
            [convex.clj         :as $.clj]
            [convex.cvm         :as $.cvm]
            [convex.shell.log   :as $.shell.log]
            [convex.shell.resrc :as $.shell.resrc]
            [taoensso.timbre    :as log]))


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
                    (when (get-in log/*config*
                                  [:appenders
                                   :cvx
                                   :enabled?])
                      ($.cell/any (:min-level log/*config*)))))



(defn level-set

  [ctx [level]]

  (if (nil? level)
    (do
      (log/swap-config! (fn [config]
                          (assoc-in config
                                    [:appenders
                                     :cvx
                                     :enabled?]
                                    false)))
      ($.cvm/result-set ctx
                        nil))
    (-do-level ctx
               level
               (fn [level-2]
                 (log/swap-config! (fn [config]
                                     (update-in config
                                                [:appenders
                                                 :cvx]
                                                merge
                                                {:enabled?  true
                                                 :min-level level-2})))
                 ($.cvm/result-set ctx
                                   level)))))



(defn log

  [ctx [level arg+]]

  (-do-level ctx
             level
             (fn [level-2]
               (log/log! level-2
                         nil
                         arg+
                         {:?line   nil
                          :?ns-str (str ($.cvm/address ctx))})
               ($.cvm/result-set ctx
                                 arg+))))



(defn out

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.shell.resrc/create ($.shell.log/out))))



(defn out-set

  [ctx [stream]]

  (let [[ok?
         x]  ($.shell.resrc/unwrap ctx
                                   stream)]
    (if ok?
      (let [stream-2 x]
        (or (when-not (instance? FileWriter
                                 stream-2)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Not an output stream")))
            (do
              ($.shell.log/out-set stream-2)
              ($.cvm/result-set ctx
                                stream))))
      (let [ctx-2 x]
        ctx-2))))
