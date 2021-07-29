(ns convex.run.stream

  ""

  {:author "Adam Helinski"}

  (:import (java.lang AutoCloseable)
           (java.io BufferedReader))
  (:refer-clojure :exclude [flush])
  (:require [convex.data    :as $.data]
            [convex.io      :as $.io]
            [convex.read    :as $.read]
            [convex.run.ctx :as $.run.ctx]
            [convex.run.err :as $.run.err]
            [convex.run.kw  :as $.run.kw]
            [convex.write   :as $.write]))


(set! *warn-on-reflection*
      true)


(declare out!)


;;;;;;;;;;


(def id-stderr

  ""

  2)


;;;;;;;;;;


(defn- -fail

  ;;

  [env id op+ err]

  (if (and (= id
              id-stderr)
           (or (op+ :flush)
               (op+ :write)))
    ((env :convex.run/fatal)
     env
     err)
    ($.run.err/fail env
                    err)))



(defn operation

  ""

  [env id op+ f]

  (if-some [stream (get-in env
                           [:convex.run/stream+
                            id])]

    (try
      
      ($.run.ctx/def-result env
                            (f stream))
      
      (catch ClassCastException _ex
        (-fail env
               id
               op+
               ($.data/error ($.data/code-std* :ARGUMENT)
                             ($.data/string (format "Stream [%s] is missing capability: %s"
                                                    id
                                                    op+)))))

      (catch Throwable _ex
        (-fail env
               id
               op+
               ($.data/error $.run.kw/err-stream
                             ($.data/string (format "Stream [%s] failed while performing: %s" 
                                                    id
                                                    op+))))))

    (-fail env
           id
           op+
           ($.data/error $.run.kw/err-stream
                         ($.data/string (format "Stream [%s] closed or does not exist"
                                                id))))))




(defn- -dissoc

  ;;

  [env id]

  (update env
          :convex.run/stream+
          dissoc
          id))




(defn close

  ""

  [env id]

  (-> env
      (operation id
                 #{:close}
                 (fn [^AutoCloseable stream]
                   (.close stream)
                   nil))
      (-dissoc id)))



(defn flush

  ""

  [env id]

  (operation env
             id
             #{:flush}
             (fn [stream]
               ($.io/flush stream)
               nil)))



(defn in

  ""

  [env id]

  (-> env
      (operation id
                 #{:read}
                 $.read/stream)
      (-dissoc id)))



(defn in+

  ""

  [env id]

  (-> env
      (operation id
                 #{:read}
                 $.read/stream+)
      (-dissoc id)))



(defn in-line+

  ""

  [env id]

  (operation env
             id
             #{:read}
             (fn [stream]
               (-> stream
                   BufferedReader.
                   $.read/line+))))



(defn out

  ""

  [env id cell]

  (operation env
             id
             #{:write}
             (fn [stream]
               ($.write/stream stream
                               cell)
               cell)))



(defn out!

  ""

  [env id cell]

  (operation env
             id
             #{:flush
               :write}
             (fn [stream]
               ($.write/stream stream
                               cell)
               ($.io/newline stream)
               ($.io/flush stream)
               cell)))


;;;;;;;;;;


(defn- -file

  ""

  [env path file str-op]

  (try
    (let [id (inc (env :convex.run.stream/id))]
      (-> env
          (assoc :convex.run.stream/id
                 id)
          (assoc-in [:convex.run/stream+
                     id]
                    file)
          ($.run.ctx/def-result ($.data/long id))))
    (catch Throwable _ex
      ($.run.err/fail env
                      ($.data/error $.run.kw/err-stream
                                    ($.data/string (format "Unable to open file for %s: %s"
                                                           path
                                                           str-op)))))))



(defn file-in

  ""

  [env path]

  (-file env
         path
         ($.io/file-in path)
         #{:read}))



(defn file-out

  ""

  [env path]

  (-file env
         path
         ($.io/file-out path)
         #{:write}))


;;;;;;;;;;


(defn close-all

  ""

  [{:as              env
    :convex.run/keys [stream+]}]

  (doseq [^AutoCloseable stream (vals (dissoc stream+
                                              0
                                              1
                                              2))]
    (.close stream))
  (assoc env
         :convex.run/stream+
         (select-keys stream+
                      [0
                       1
                       2])))
