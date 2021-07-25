(ns convex.run.init

  ""

  {:author "Adam Helinski"}

  (:import (java.io File))
  (:require [convex.data       :as $.data]
            [convex.io         :as $.io]
            [convex.run.ctx    :as $.run.ctx]
            [convex.run.stream :as $.run.stream]
            [convex.run.sym    :as $.run.sym]))


;;;;;;;;;;


(defn hook-error

  ""

  [env]

  ($.run.stream/out! env
                     (env :convex.run/err)
                     (env :convex.run/error)))



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
      (assoc :convex.run/err        stream-err
             :convex.run/in         stream-in
             :convex.run/out        stream-out
             :convex.run.hook/error hook-error
             :convex.run.stream/id  5)
      (update :convex.sync/ctx-base
              #(or %
                   $.run.ctx/base))
      (update :convex.run/path
              (fn [^String path]
                (when path
                  (.getCanonicalPath (File. path)))))
      (update :convex.run/stream+
              merge
              stream+)
      (as->
        env-2
        ($.run.ctx/def-env env-2
                           :convex.sync/ctx-base
                           {$.run.sym/in          ($.data/long (env-2 :convex.run/in))
                            $.run.sym/out         ($.data/long (env-2 :convex.run/out))
                            $.run.sym/out-err     ($.data/long (env-2 :convex.run/err))
                            $.run.sym/single-run? ($.data/boolean (env-2 :convex.run/single-run?))}))
      $.run.ctx/init))
