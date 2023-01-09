(ns convex.shell.req.stream

  (:import (convex.core.exceptions ParseException)
           (java.io BufferedReader)
           (java.lang AutoCloseable)
           (java.util.stream Collectors))
  (:refer-clojure :exclude [flush])
  (:require [convex.cell      :as $.cell]
            [convex.cvm       :as $.cvm]
            [convex.read      :as $.read]
            [convex.shell.env :as $.shell.env]
            [convex.shell.io  :as $.shell.io]
            [convex.std       :as $.std]
            [convex.write     :as $.write]))


(declare close)


;;;;;;;;;


(defn- -close-when-no-result

  ;; Sometimes it is best closing the stream if an operation returns no result.

  [ctx handle]

  (cond->
    ctx
    (not ($.cvm/result ctx))
    (close handle)))



(defn- -dissoc

  ;; Dissociates the requested stream from env.

  [ctx handle]

  ($.shell.env/update ctx
                      (fn [env]
                        (update env
                                :convex.shell/handle->stream
                                dissoc
                                handle))))




(defn- -fail

  ;; Used in case of failure.
  ;;
  ;; Reports error using [[convex.shell.exec.fail/err]], as expected, unless operation involved STDERR.
  ;; If using STDERR, there is no way to print errors, hence the process should terminate with a special exit code.

  [ctx handle op+ message]

  (if (and (= handle
              ($.cell/* :stderr))
           (or (op+ :flush)
               (op+ :write)))
    ($.cvm/exception-set ctx
                         ($.cell/* :EXIT)
                         ($.cell/* 3))
    ($.cvm/exception-set ctx
                         ($.cell/* :STREAM)
                         message)))



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



;;;;;;;;;;


(defn operation

  "Generic function for carrying out an operation.
  
   Retrieves the stream associated with `handle` and executes `(f env stream`).
  
   Takes care of failure."

  [ctx handle op+ f]

  (if-some [stream (-> ctx
                       ($.shell.env/get)
                       (get-in [:convex.shell/handle->stream
                                handle]))]
    ;; Stream exists.
    (try
      ;;
      (f ctx
         stream)
      ;;
      (catch ClassCastException _ex
        (-fail ctx
               handle
               op+
               ($.cell/string (format "Stream is missing capability: %s"
                                      op+))))
      ;;
      (catch ParseException ex
        ($.cvm/exception-set ctx
                             ($.cell/* :STREAM)
                             ($.cell/* ($.cell/string (.getMessage ex)))))
      ;;
      (catch Throwable _ex
        (-fail ctx
               handle
               op+
               ($.cell/string (format "Stream failed while performing: %s"
                                      op+)))))
    ;; Stream does not exist
    (-fail ctx
           handle
           op+
           ($.cell/string "Stream closed or does not exist"))))


;;;;;;;;;;


(defn close

  "Closes the requested stream.
   A result to propagate may be provided."


  ([ctx arg+]

   (close ctx
          arg+
          nil))


  ([ctx [handle] result]

   (if (= handle
          ($.cell/* :stderr))
     ($.cvm/exception-set ctx
                          ($.cell/code-std* :ARGUMENT)
                          ($.cell/* "Cannot close STDERR"))
     (-> ctx
         (operation handle
                    #{:close}
                    (fn [ctx-2 ^AutoCloseable stream]
                      (.close stream)
                      ($.cvm/result-set ctx-2
                                        result)))
         (-dissoc handle)))))



(defn flush

  "Flushes the requested stream."

  [ctx [handle]]

  (operation ctx
             handle
             #{:flush}
             (fn [ctx-2 stream]
               ($.shell.io/flush stream)
               ($.cvm/result-set ctx-2
                                 handle))))



(defn in+

  "Reads all available cells from the requested stream and closes it."

  [ctx [handle]]

  (-> ctx
      (operation handle
                 #{:read}
                 (fn [ctx-2 stream]
                   ($.cvm/result-set ctx-2
                                     ($.read/stream stream))))
      (-dissoc handle)))



(defn line

  "Reads a line from the requested stream and parses it into a list of cells."

  [ctx [handle]]

  (-> ctx
      (operation handle
                 #{:read}
                 (fn [ctx-2 stream]
                   ($.cvm/result-set ctx-2
                                     ($.read/line stream))))
      (-close-when-no-result handle)))



(defn- -out

  ;; See [[out]].

  [ctx handle cell stringify]

  (operation ctx
             handle
             #{:write}
             (fn [ctx-2 stream]
               ($.write/stream stream
                               stringify
                               cell)
               ($.cvm/result-set ctx-2
                                 cell))))



(defn out

  "Writes `cell` to the requested stream."

  [ctx [handle cell]]

  (-out ctx
        handle
        cell
        -str-cvx))



(defn- -outln

  ;; See [[outln]].

  [env handle cell stringify]

  (operation env
             handle
             #{:flush
               :write}
             (fn [ctx stream]
               ($.write/stream stream
                               stringify
                               cell)
               ($.shell.io/newline stream)
               ($.shell.io/flush stream)
               ($.cvm/result-set ctx
                                 cell))))



(defn outln

  "Like [[out]] but appends a new line and flushes the stream."

  [env [handle cell]]

  (-outln env
          handle
          cell
          -str-cvx))



(defn txt-in

  "Reads everything from the requested stream as text."

  [ctx [handle]]

  (let [ctx-2 (operation ctx
                         handle
                         #{:read}
                         (fn [ctx-2 ^BufferedReader stream]
                           ($.cvm/result-set ctx-2
                                             (-> stream
                                                 (.lines)
                                                 (.collect (Collectors/joining (System/lineSeparator)))
                                                 ($.cell/string)))))]
    (close ctx-2
           handle
           ($.cvm/result ctx-2))))



(defn txt-line

  "Reads a line from the requested stream as text."

  [ctx [handle]]

  (-> ctx
      (operation handle
                 #{:read}
                 (fn [ctx-2 ^BufferedReader stream]
                   ($.cvm/result-set ctx-2
                                     (some-> (.readLine stream)
                                             ($.cell/string)))))
      (-close-when-no-result handle)))



(defn txt-out

  "Like [[out]] but if `cell` is a string, then it is not quoted."

  [ctx [handle cell]]

  (-out ctx
        handle
        cell
        -str-txt))



(defn txt-outln

  "Is to [[outln]] what [[out-txt]] is to [[out]]."

  [ctx [handle cell]]

  (-outln ctx
          handle
          cell
          -str-txt))
