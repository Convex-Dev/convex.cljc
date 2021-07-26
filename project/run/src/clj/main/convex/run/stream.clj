(ns convex.run.stream

  ""

  {:author "Adam Helinski"}

  (:import (convex.core.data.prim CVMLong)
           (java.io BufferedReader))
  (:refer-clojure :exclude [flush])
  (:require [convex.cvm     :as $.cvm]
            [convex.data    :as $.data]
            [convex.io      :as $.io]
            [convex.read    :as $.read]
            [convex.run.ctx :as $.run.ctx]
            [convex.run.err :as $.run.err]
            [convex.run.kw  :as $.run.kw]
            [convex.run.sym :as $.run.sym]
            [convex.write   :as $.write]))


(set! *warn-on-reflection*
      true)


(declare out!)


;;;;;;;;;;


(defn- -fail

  ;;

  [env id err]

  (if (= id
         (env ::stream.err))
    (if (= id
           4)  ;;  STDERR faulty, nothing can be done.
      ((env :convex.run/fatal)
       (dissoc env
               ::err)
       err)
      (out! (assoc env
                   ::stream.err
                   4)
            4
            err))
    ($.run.err/fail env
                    err)))



(defn operation

  ""

  ;; TODO. Print id in case of error?

  [env id capability f]

  (if-some [stream (get-in env
                           [:convex.run/stream+
                            id])]

    (try
      
      ($.run.ctx/def-result env
                            (f stream))
      
      (catch ClassCastException _ex
        (-fail env
               id
               ($.data/error ($.data/code-std* :ARGUMENT)
                             ($.data/string (format "Stream [%s] is missing capability: %s"
                                                    id
                                                    capability)))))

      (catch Throwable _ex
        (-fail env
               id
               ($.data/error $.run.kw/err-stream
                             ($.data/string (format "Stream [%s] failed while performing: %s" 
                                                    id
                                                    capability))))))

    (-fail env
           id
           ($.data/error $.run.kw/err-stream
                         ($.data/string (format "Stream [%s] closed or does not exist"
                                                id))))))




(defn close

  ""

  [env id]

  (operation env
             id
             "close"
             (fn [^java.lang.AutoCloseable stream]
               (.close stream)
               nil)))



(defn err

  ""

  [env]

  (let [id (-> env
               :convex.sync/ctx
               ($.cvm/env $.run.ctx/addr-env)
               ^CVMLong (.get $.run.sym/out-err)
               .longValue)]
    (-> env
        (assoc ::stream.err
               id)
        (out! id
              (env :convex.run/error))
        (dissoc :err))))



(defn flush

  ""

  [env id]

  (operation env
             id
             "flush"
             (fn [stream]
               ($.io/flush stream)
               nil)))



(defn in

  ""

  [env id]

  (operation env
             id
             "read"
             $.read/stream))


(defn in+

  ""

  [env id]

  (operation env
             id
             "read"
             $.read/stream+))



(defn in-line+

  ""

  [env id]

  (operation env
             id
             "read line"
             (fn [stream]
               (-> stream
                   BufferedReader.
                   $.read/line+))))



(defn out

  ""

  [env id cell]

  (operation env
             id
             "write"
             (fn [stream]
               ($.write/stream stream
                               cell)
               cell)))



(defn out!

  ""

  [env id cell]

  (operation env
             id
             "write flush"
             (fn [stream]
               ($.write/stream stream
                               cell)
               ($.io/newline stream)
               ($.io/flush stream)
               cell)))
