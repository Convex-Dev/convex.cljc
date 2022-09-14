(ns convex.shell.stream

  "Handling files and STDIO streams.

   A stream is an id that represents an opened file or a STDIO streams. Those ids are kept in env.

   All operations, such as closing a stream or reading one, rely on [[operation]].

   Used for implementing IO requests."

  {:author "Adam Helinski"}

  (:import (convex.core.exceptions ParseException)
           (java.io BufferedReader)
           (java.lang AutoCloseable)
           (java.util.stream Collectors))
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


(declare close
         outln)


;;;;;;;;;; Values


(def id-stderr

  "Id of STDERR."

  2)


;;;;;;;;;; Private


(defn- -close-when-no-result

  ;; Sometimes it is best closing the stream if an operation returns no result.

  [env id]

  (cond->
    env
    (not ($.shell.ctx/result env))
    (close id)))


(defn- -dissoc

  ;; Dissociates the requested stream from env.

  [env id]

  (update env
          :convex.shell/stream+
          dissoc
          id))



(defn- -fail

  ;; Used in case of failure.
  ;;
  ;; Reports error using [[convex.shell.exec.fail/err]], as expected, unless operation involved STDERR.
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



(defn- -str-cvx

  ;; Stringifies the given `cell` to be readable.

  [cell]

  (str ($.write/string cell)))



(defn- -str-txt

  ;; Stringifies the given `cell` but do not double quote if it is a string.

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
               ($.shell.err/stream ($.cell/long id)
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
               ($.shell.err/stream ($.cell/long id)
                                   ($.cell/string (str "Stream failed while performing: " 
                                                       op+))))))
    ;; Stream does not exist
    (-fail env
           id
           op+
           ($.shell.err/stream ($.cell/long id)
                               ($.cell/string "Stream closed or does not exist")))))


;;;


(defn close

  "Closes the requested stream.
   A result to propagate may be provided."


  ([env id]

   (close env
          id
          nil))


  ([env id result]

   (-> env
       (operation id
                  #{:close}
                  (fn [^AutoCloseable stream]
                    (.close stream)
                    result))
       (-dissoc id))))



(defn flush

  "Flushes the requested stream."

  [env id]

  (operation env
             id
             #{:flush}
             (fn [stream]
               ($.shell.io/flush stream)
               nil)))



(defn in+

  "Reads all available cells from the requested stream and closes it."

  [env id]

  (-> env
      (operation id
                 #{:read}
                 $.read/stream)
      (-dissoc id)))



(defn line

  "Reads a line from the requested stream and parses it into a list of cells."

  [env id]

  (-> env
      (operation id
                 #{:read}
                 $.read/line)
      (-close-when-no-result id)))



(defn- -out

  ;; See [[out]].

  [env id cell stringify]

  (operation env
             id
             #{:write}
             (fn [stream]
               ($.write/stream stream
                               stringify
                               cell)
               nil)))



(defn out

  "Writes `cell` to the requested stream."

  [env id cell]

  (-out env
        id
        cell
        -str-cvx))



(defn- -outln

  ;; See [[outln]].

  [env id cell stringify]

  (operation env
             id
             #{:flush
               :write}
             (fn [stream]
               ($.write/stream stream
                               stringify
                               cell)
               ($.shell.io/newline stream)
               ($.shell.io/flush stream)
               nil)))



(defn outln

  "Like [[out]] but appends a new line and flushes the stream."

  [env id cell]

  (-outln env
          id
          cell
          -str-cvx))



(defn txt-in

  "Reads everything from the requested stream as text."

  [env id]

  (let [env-2 (operation env
                         id
                         #{:read}
                         (fn [^BufferedReader stream]
                           (-> stream
                               (.lines)
                               (.collect (Collectors/joining (System/lineSeparator)))
                               ($.cell/string))))]
    (close env-2
           id
           ($.shell.ctx/result env-2))))



(defn txt-line

  "Reads a line from the requested stream as text."

  [env id]

  (-> env
      (operation id
                 #{:read}
                 (fn [^BufferedReader stream]
                   (some-> (.readLine stream)
                           ($.cell/string))))
      (-close-when-no-result id)))



(defn txt-out

  "Like [[out]] but if `cell` is a string, then it is not quoted."

  [env id cell]

  (-out env
        id
        cell
        -str-txt))



(defn txt-outln

  "Is to [[outln]] what [[out-txt]] is to [[out]]."

  [env id cell]

  (-outln env
          id
          cell
          -str-txt))


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

  [env path append?]

  (-file env
         path
         (fn [path]
           ($.shell.io/file-out path
                                append?))
         #{:write}))
