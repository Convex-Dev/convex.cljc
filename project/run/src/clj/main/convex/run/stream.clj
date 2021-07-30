(ns convex.run.stream

  ""

  {:author "Adam Helinski"}

  (:import (java.lang AutoCloseable)
           (java.io BufferedReader
                    FileNotFoundException))
  (:refer-clojure :exclude [flush])
  (:require [convex.cell    :as $.cell]
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
               ($.cell/error ($.cell/code-std* :ARGUMENT)
                             ($.cell/string (format "Stream [%s] is missing capability: %s"
                                                    id
                                                    op+)))))

      (catch Throwable _ex
        (-fail env
               id
               op+
               ($.cell/error $.run.kw/err-stream
                             ($.cell/string (format "Stream [%s] failed while performing: %s" 
                                                    id
                                                    op+))))))

    (-fail env
           id
           op+
           ($.cell/error $.run.kw/err-stream
                         ($.cell/string (format "Stream [%s] closed or does not exist"
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

  [env path open str-op]

  (try
    (let [file (open path)
          id   (inc (env :convex.run.stream/id))]
      (-> env
          (assoc :convex.run.stream/id
                 id)
          (assoc-in [:convex.run/stream+
                     id]
                    file)
          ($.run.ctx/def-result ($.cell/long id))))

    ;(catch FileNotFoundException _ex

    (catch Throwable _ex
      ($.run.err/fail env
                      ($.cell/error $.run.kw/err-stream
                                    ($.cell/string (format "Unable to open file for %s: %s"
                                                           path
                                                           str-op)))))))



(defn file-in

  ""

  [env path]

  (-file env
         path
         $.io/file-in
         #{:read}))



(defn file-out

  ""

  [env path]

  (-file env
         path
         $.io/file-out
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
