(ns convex.run.init

  ""

  {:author "Adam Helinski"}

  (:import (java.io File))
  (:require [convex.data     :as $.data]
            [convex.io       :as $.io]
            [convex.run.ctx  :as $.run.ctx]
            [convex.run.exec :as $.run.exec]
            [convex.run.kw   :as $.run.kw]
            [convex.run.sym  :as $.run.sym]))


;;;;;;;;;;


(def hook-end

  ""

  identity)


(defn hook-error

  ""

  [env]

  (as-> env
        env-2

    ((env-2 :convex.run.hook/out)
     env-2
     (env-2 :convex.run/error))

    ((env-2 :convex.run/flush)
     env-2)))


(defn hook-out

  ""

  [env x]

  (when x
    (print (str x)))
  env)



(def stream+

  ""

  {0 [$.io/stdin-txt
      :txt]
   1 [$.io/stdin-bin
      :bin]
   2 [$.io/stdout-txt
      :txt]
   3 [$.io/stdout-bin
      :bin]
   4 [$.io/stderr-txt
      :txt]
   5 [$.io/stderr-bin
      :bin]})



(def stream-err
     4)



(def stream-in
     0)



(def stream-out
     2)



(defn env

  "Initializes important functions and values in the given `env`, using defaults when relevant.

   Must be used prior to other preparatory functions such as [[main-file]].

   Operates over:

   | Key | Action | Mandatory |
   |---|---|---|
   | `:convex.run/path` | If present, ensures the path to the main file is canonical |
   | `:convex.run.hook/end` | Ensures a default end hook, see namespace description |
   | `:convex.run.hook/error` | Ensures a default error hook, see namespace description |
   | `:convex.run.hook/out` | Ensures a default output hook, see namespace description |
   | `:convex.run/single-run? | Whether code is run once or more (watch mode), defaults to false |
   | `:convex.sync/cx-base | Ensures a default base context, see [[convex.run.ctx/base]] |"

  [env]

  (-> env
      (->> (merge {:convex.run/err        stream-err
                   :convex.run/in         stream-in
                   :convex.run/out        stream-out
                   :convex.run.hook/end   hook-end
                   :convex.run.hook/error hook-error
                   :convex.run.hook/out   hook-out
                   :convex.run.stream/id  5
                   :convex.sync/ctx-base  $.run.ctx/base}))
      (update :convex.run/path
              (fn [^String path]
                (when path
                  (.getCanonicalPath (File. path)))))
      (update :convex.run/stream+
              (partial merge
                       stream+))
      ($.run.ctx/def-help :convex.sync/ctx-base
                          {$.run.sym/single-run? ($.data/boolean (env :convex.run/single-run?))})
      ($.run.ctx/def-mode :convex.sync/ctx-base
                          $.run.exec/mode-eval
                          $.run.kw/mode-eval)
      $.run.ctx/init))
