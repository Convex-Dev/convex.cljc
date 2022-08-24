(ns convex.shell.stream

  "Handling files and STDIO streams.

   A stream is an id that represents an opened file or a STDIO streams. Those ids are kept in env.

   All operations, such as closing a stream or reading one, rely on [[operation]].

   Used for implementing IO requests."

  {:author "Adam Helinski"}

  (:import (java.lang AutoCloseable)
           (java.io BufferedReader))
  (:refer-clojure :exclude [flush])
  (:require [convex.cell       :as $.cell]
            [convex.read       :as $.read]
            [convex.shell.ctx  :as $.shell.ctx]
            [convex.shell.exec :as $.shell.exec]
            [convex.shell.io   :as $.shell.io]
            [convex.shell.kw   :as $.shell.kw]
            [convex.write      :as $.write]))


(set! *warn-on-reflection*
      true)


(declare out!)


;;;;;;;;;; Values


(def id-stderr

  "Id of STDERR."

  2)


;;;;;;;;;; Private


(defn- -fail

  ;; Used in case of failure.
  ;;
  ;; Reports error using [[convex.shell.exec/fail]], as expected, unless operation was involving writing to STDERR.
  ;; If writing to STDERR fails for some odd reason, then it is highly problematic as it is the ultimate place for
  ;; reporting errors. Env and error are passed to the function under `:convex.fun/fatal` expecting things to halt.

  [env id op+ err]

  (if (and (= id
              id-stderr)
           (or (op+ :flush)
               (op+ :write)))
    ((env :convex.shell/fatal)
     env
     err)
    ($.shell.exec/fail env
                       err)))



(defn- -dissoc

  ;; Dissociates the requested stream from env.

  [env id]

  (update env
          :convex.shell/stream+
          dissoc
          id))


;;;;;;;;;; Operations


(defn operation

  "Generic function for carrying out an operation.
  
   Retrieves the stream associated with `id` and executes `(f env stream`).
  
   Takes care of failure."

  [env id op+ f]

  (if-some [stream (get-in env
                           [:convex.shell/stream+
                            id])]

    (try
      
      ($.shell.ctx/def-result env
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
               ($.cell/error $.shell.kw/err-stream
                             ($.cell/string (format "Stream [%s] failed while performing: %s" 
                                                    id
                                                    op+))))))

    (-fail env
           id
           op+
           ($.cell/error $.shell.kw/err-stream
                         ($.cell/string (format "Stream [%s] closed or does not exist"
                                                id))))))


;;;


(defn close

  "Closes the requested stream."

  [env id]

  (-> env
      (operation id
                 #{:close}
                 (fn [^AutoCloseable stream]
                   (.close stream)
                   nil))
      (-dissoc id)))



(defn flush

  "Flushes the requested stream."

  [env id]

  (operation env
             id
             #{:flush}
             (fn [stream]
               ($.shell.io/flush stream)
               nil)))



(defn in

  "Reads a single cell from the requested stream."

  [env id]

  (-> env
      (operation id
                 #{:read}
                 $.read/stream)
      (-dissoc id)))



(defn in+

  "Reads all available cells from the requested stream and closes it."

  [env id]

  (-> env
      (operation id
                 #{:read}
                 $.read/stream+)
      (-dissoc id)))



(defn line+

  "Reads a line from the requested stream and parses it into a list of cells."

  [env id]

  (operation env
             id
             #{:read}
             (fn [stream]
               (-> stream
                   (BufferedReader.)
                   ($.read/line+)))))



(defn out

  "Writes `cell` to the requested stream."

  [env id cell]

  (operation env
             id
             #{:write}
             (fn [stream]
               ($.write/stream stream
                               cell)
               cell)))



(defn out!

  "Like [[out]] but appends a new line and flushes the stream."

  [env id cell]

  (operation env
             id
             #{:flush
               :write}
             (fn [stream]
               ($.write/stream stream
                               cell)
               ($.shell.io/newline stream)
               ($.shell.io/flush stream)
               cell)))


;;;;;;;;;; Opening file streams


(defn- -file

  ;; Used by [[file-in]] and [[file-out]].

  [env path open str-op]

  (try
    (let [file (open (str path))
          id   (inc (env :convex.shell.stream/id))]
      (-> env
          (assoc :convex.shell.stream/id
                 id)
          (assoc-in [:convex.shell/stream+
                     id]
                    file)
          ($.shell.ctx/def-result ($.cell/long id))))

    ;(catch FileNotFoundException _ex

    (catch Throwable _ex
      ($.shell.exec/fail env
                         ($.cell/error $.shell.kw/err-stream
                                       ($.cell/string (format "Unable to open file for '%s': %s"
                                                              path
                                                              str-op)))))))



(defn file-in

  "Opens an input stream for file under `path`."

  [env path]

  (-file env
         path
         $.shell.io/file-in
         #{:read}))



(defn file-out

  "Opens an output stream for file under `path`."

  [env path]

  (-file env
         path
         $.shell.io/file-out
         #{:write}))


;;;;;;;;;; Miscellaneous


(defn close-all

  "Closes all streams in env.
  
   Not needed if the shell is run standalone since the OS closes them when the process terminates."

  [{:as              env
    :convex.shell/keys [stream+]}]

  (doseq [^AutoCloseable stream (vals (dissoc stream+
                                              0
                                              1
                                              2))]
    (.close stream))
  (assoc env
         :convex.shell/stream+
         (select-keys stream+
                      [0
                       1
                       2])))
