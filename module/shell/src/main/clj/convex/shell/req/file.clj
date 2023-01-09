(ns convex.shell.req.file

  (:require [convex.cell      :as $.cell]
            [convex.cvm       :as $.cvm]
            [convex.shell.env :as $.shell.env]
            [convex.shell.io  :as $.shell.io]
            [convex.std       :as $.std]))


;;;;;;;;;;


(defn- -stream

  ;; Used by [[stream-in]] and [[stream-out]].

  [ctx handle path open str-op]

  (or (when (-> ctx
                ($.shell.env/get)
                (get-in [:convex.shell/handle->stream
                         handle]))
        ($.cvm/exception-set ctx
                             ($.cell/* :STREAM)
                             ($.cell/* "Handle already exists")))
      (when-not ($.std/string? path)
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
            (-> ctx
                ($.shell.env/update (fn [env]
                                      (assoc-in env
                                                [:convex.shell/handle->stream
                                                 handle]
                                                stream)))
                ($.cvm/result-set handle))))))



(defn stream-in

  "Opens an input stream for file under `path`."

  [ctx [handle path]]

  (-stream ctx
           handle
           path
           $.shell.io/file-in
           #{:read}))



(defn stream-out

  "Opens an output stream for file under `path`."

  [ctx [handle path append?]]

  (-stream ctx
           handle
           path
           (fn [path]
             ($.shell.io/file-out path
                                  append?))
           #{:write}))
