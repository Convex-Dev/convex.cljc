(ns convex.disk

  "Loading Convex Lisp files in contextes + live-reloading.
  
   See [[convex.example.disk]] namespace for examples."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [intern
                            load
                            read])
  (:import (java.io File))
  (:require [convex.code :as $.code]
            [convex.cvm  :as $.cvm]
            [convex.sync :as $.sync]
            [hawk.core   :as watcher]))


;;;;;;;;;; Miscellaneous


(defn path-canonical

  "Returns the canonical, unique representation of the given `path`."

  [^String path]

  (.getCanonicalPath (File. path)))



(defn read

  "Reads the file located at `path` and returns Convex code."

  [path]

  (-> path
      slurp
      $.cvm/read))


;;;;;;;;;; Loading files once


(defn load

  "Into the given `ctx` (or a newly created one if not provided), reads the given files and intern then as unevaluted code
   under their respective symbols.

   Returns a \"environment\" map which contains:

   | Key | Value |
   |---|---|
   | `:ctx` | If there is no `:error`, the prepared context |
   | `:error` | Signals an error and the absence of `:ctx` (see below) |
   | `:input+` | Vector of requested input file paths |
   | `:input->code` | Map of `input file path` -> `CVM code`, uses `:read` |
   | `:path->cvm-symbol` | Map of `file path` -> `CVM symbol used for interning` |
   | `:read` | Read function used by the [[convex.sync]] namespace, not a user concern |

   This interned code can they be used as needed through the power of Lisp. Typically, either `deploy` or `eval` is used.

   Eg. Reading a file and deploying as a library (without error checking):
   
   ```clojure
   (-> (convex.disk/load {'my-lib \"./path/to/lib.cvx\"})
       :ctx
       (convex.clj.eval/ctx '(def my-lib
                                  (deploy my-lib))))
   ```

   An `:error` is a 2-tuple vector where the first item is a keyword indicating an error type and second item is information:

   | Position 0 | Position 1 |
   |---|---|
   | `:input->error` | Map of `file path` -> `Java exception occured during reading` |
   | `:eval` | Map with `:exception` (either Java or CVM exception) and `:input` (which input caused this evaluation error) |"


  ([sym->path]

   (load ($.cvm/ctx)
         sym->path))


  ([ctx sym->path]

   (let [input+        (reduce (fn [input+ [sym ^String path]]
                                 (conj input+
                                       [(path-canonical path)
                                        ($.code/symbol (str sym))]))
                               []
                               sym->path)
         path->cvm-sym (into {}
                             input+)
         read-input    (fn [env path]
                         (try
                           ($.sync/assoc-code env
                                              path
                                              ($.code/def (path->cvm-sym path)
                                                          ($.code/quote (read path))))
                           (catch Throwable ex
                             ($.sync/assoc-err-read env
                                                    path
                                                    ex))))]
     (-> {:input+        (mapv first
                               input+)
          :path->cvm-sym path->cvm-sym
          :read          read-input}
         $.sync/load
         ($.sync/eval ctx)))))
