(ns convex.shell.req.file

  "Requests relating to file utils.
  
   For the time being, only about opening streams. All other utilities are
   written in Convex Lisp."

  {:author "Adam Helinski"}

  (:require [convex.cell     :as $.cell]
            [convex.cvm      :as $.cvm]
            [convex.shell.io :as $.shell.io]
            [convex.std      :as $.std]))


;;;;;;;;;;


(defn- -stream

  ;; Used by [[stream-in]] and [[stream-out]].

  [ctx id path open str-op]

  (or (when-not ($.std/string? path)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Path to file must be a string")))
      (let [[stream
             ctx-err] (try
                        [(open (str path))
                         nil]
                        (catch Throwable _ex
                          [nil
                           ($.cvm/exception-set ctx
                                                ($.cell/* :STREAM)
                                                ($.cell/string (format "Unable to open file for '%s': %s"
                                                                       path
                                                                       str-op)))]))]
        (or ctx-err
            ($.cvm/result-set ctx
                              ($.cell/* [:stream
                                         ~($.cell/fake stream)
                                         ~id
                                         ~path]))))))



(defn stream-in

  "Request for opening an input stream for file under `path`."

  [ctx [id path]]

  (-stream ctx
           id
           path
           $.shell.io/file-in
           #{:read}))



(defn stream-out

  "Request for opening an output stream for file under `path`."

  [ctx [id path append?]]

  (-stream ctx
           id
           path
           (fn [path]
             ($.shell.io/file-out path
                                  append?))
           #{:write}))
