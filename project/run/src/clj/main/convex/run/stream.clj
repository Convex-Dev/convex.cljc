(ns convex.run.stream

  ""

  {:author "Adam Helinski"}

  (:import (java.io BufferedReader))
  (:refer-clojure :exclude [flush])
  (:require [convex.data    :as $.data]
            [convex.io      :as $.io]
            [convex.read    :as $.read]
            [convex.run.ctx :as $.run.ctx]
            [convex.run.err :as $.run.err]
            [convex.run.kw  :as $.run.kw]
            [convex.write   :as $.write]))


;;;;;;;;;; Errors


(defn err-stream-not-found

  ""

  [env]

  ($.run.err/signal env
                    $.run.kw/err-stream
                    ($.data/string "Stream closed or does not exist")))

;;;;;;;;;;


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
        ($.run.err/signal env
                          ($.data/code-std* :ARGUMENT)
                          ($.data/string (str "Stream is missing capability: "
                                              capability))))

      (catch Throwable _ex
        ($.run.err/signal env
                          $.run.kw/err-stream
                          ($.data/string (str "Stream failed while performing: "
                                              capability)))))
    (err-stream-not-found env)))






(defn flush

  ""

  [env id]

  (operation env
             id
             "flush"
             (fn [[stream _mode]]
               ($.io/flush stream)
               nil)))



(defn in

  ""

  [env id]

  (operation env
             id
             "read"
             (fn [[stream mode]]
               ((case mode
                  :bin $.read/stream-bin
                  :txt $.read/stream-txt)
                stream))))


(defn in+

  ""

  [env id]

  (operation env
             id
             "read"
             (fn [[stream mode]]
               ((case mode
                  :bin $.read/stream-bin+
                  :txt $.read/stream-txt+)
                stream))))



(defn in-line+

  ""

  [env id]

  (operation env
             id
             "read line"
             (fn [[stream _mode]]
               (-> stream
                   BufferedReader.
                   $.read/line))))



(defn out

  ""

  [env id cell]

  (operation env
             id
             "write"
             (fn [[stream mode]]
               ((case mode
                  :bin $.write/stream-bin
                  :txt $.write/stream-txt)
                stream
                cell)
               cell)))



(defn out!

  ""

  [env id cell]

  (operation env
             id
             "write flush"
             (fn [[stream mode]]
               (case mode
                 :bin ($.write/stream-bin stream
                                          cell)
                 :txt (do
                        ($.write/stream-txt stream
                                            cell)
                        ($.io/newline stream)))
               ($.io/flush stream)
               cell)))
