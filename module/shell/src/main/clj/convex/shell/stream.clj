(ns convex.shell.stream

  "Handling files and STDIO streams.

   A stream is an id that represents an opened file or a STDIO streams. Those ids are kept in env.

   All operations, such as closing a stream or reading one, rely on [[operation]].

   Used for implementing IO requests."

  {:author "Adam Helinski"}

  (:import (convex.core.exceptions ParseException)
           (java.lang AutoCloseable)
           (java.io BufferedReader))
  (:refer-clojure :exclude [flush])
  (:require [convex.cell            :as $.cell]
            [convex.read            :as $.read]
            [convex.shell.ctx       :as $.shell.ctx]
            [convex.shell.err       :as $.shell.err]
            [convex.shell.exec.fail :as $.shell.exec.fail]
            [convex.shell.io        :as $.shell.io]
            [convex.shell.kw        :as $.shell.kw]
            [convex.std             :as $.std]
            [convex.write           :as $.write]))


(set! *warn-on-reflection*
      true)


(declare outln)


;;;;;;;;;; Values


(def id-stderr

  "Id of STDERR."

  2)


;;;;;;;;;; Private


(defn- -fail

  ;; Used in case of failure.
  ;;
  ;; Reports error using [[convex.shell.exec.fail/err]], as expected, unless operation was involved STDERR.
  ;; If using STDERR, there is no way to print errors, hence the process should terminate with a special exit code.

  [env id op+ err]

  (if (and (= id
              id-stderr)
           (or (op+ :flush)
               (op+ :write)))
    ($.shell.ctx/exit env
                      3)
    ($.shell.exec.fail/err env
                           err)))



(defn- -dissoc

  ;; Dissociates the requested stream from env.

  [env id]

  (update env
          :convex.shell/stream+
          dissoc
          id))



(defn- -str

  ;; Properly stringifying cells.

  [cell]

  (let [s (str cell)]
    (if ($.std/char? cell)
      (str \\
           s)
      s)))


;;;;;;;;;; Operations


(defn operation

  "Generic function for carrying out an operation.
  
   Retrieves the stream associated with `id` and executes `(f env stream`).
  
   Takes care of failure."

  [env id op+ f]

  (if-some [stream (get-in env
                           [:convex.shell/stream+
                            id])]
    ;; Stream exists.
    (try
      ;;
      ($.shell.ctx/def-result env
                              (f stream))
      ;;
      (catch ClassCastException _ex
        (-fail env
               id
               op+
               ($.cell/error ($.cell/code-std* :ARGUMENT)
                             ($.cell/string (str "Stream is missing capability: %s"
                                                 op+)))))
      ;;
      (catch ParseException ex
        ($.shell.exec.fail/err env
                               ($.shell.err/reader-stream ($.cell/long id)
                                                          ($.cell/string (.getMessage ex)))))
      ;;
      (catch Throwable _ex
        (-fail env
               id
               op+
               ($.cell/error $.shell.kw/err-stream
                             ($.cell/string (str "Stream failed while performing: " 
                                                 op+))))))
    ;; Stream does not exist
    (-fail env
           id
           op+
           ($.cell/error $.shell.kw/err-stream
                         ($.cell/string "Stream closed or does not exist")))))


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
                               -str
                               cell)
               cell)))



(defn outln

  "Like [[out]] but appends a new line and flushes the stream."

  [env id cell]

  (operation env
             id
             #{:flush
               :write}
             (fn [stream]
               ($.write/stream stream
                               -str
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
      ($.shell.exec.fail/err env
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
