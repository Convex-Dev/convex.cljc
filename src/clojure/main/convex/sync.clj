(ns convex.sync

  "About syncing running Convex Lisp source code with CVM contextes.

   Series of abstract utilities for handling code reading, updating, errors, ...

   A prime example is [[disk]] which reads and loads unevaluated source files into a CVM context.

   Another one is [[convex.watch/start]] which builds on [[disk]] as well as other utilities from this namespace
   for providing a live-reloading experience."

  {:author "Adam Helinski"}

  (:import (java.io File))
  (:refer-clojure :exclude [eval
                            load])
  (:require [convex.code :as $.code]
            [convex.cvm  :as $.cvm]))


(declare load
         unload)


;;;;;;;;;; Altering the "env" map (executing environment)


(defn assoc-code

  "Associates the given `code` to `input` in `env`."

  [env input code]

  (assoc-in env
            [:input->code
             input]
            code))



(defn assoc-err-read

  "Adds `err` as a read error that occured when reading `input`."

  [env input err]

  (update env
          :error
          (fn [error]
            (if (identical? (first error)
                            :load)
              (update error
                      1
                      assoc
                      input
                      err)
              [:load
               {input err}]))))


;;;;;;;;;; Dealing with inputs


(defn load

  "Reads the given `input` (or all `:inputs+` in `env`).
  
   Essentially does `((env :read) env input)`, which means that the `:read` function in `env`
   must appropriately handle read errors using [[assoc-err-read]].

   Even in case of errors, all inputs are processed."

  ([env]

   (reduce (env :read)
           env
           (env :input+)))


  ([env input]

   ((env :read)
    env
    input)))



(defn reload

  "Like [[load]] but meant to be used whenever an input is updated.

   Updates read errors."

  [env input]

  (-> (let [error        (env :error)
            input->error (when (and error
                                    (identical? (first error)
                                                :load))
                           (not-empty (dissoc (second error)
                                              input)))]
        (if input->error
          (assoc env
                 :error
                 [:load
                  input->error])
          (dissoc env
                  :error)))
      (update :input->code
              dissoc
              input)
      (load input)))



(defn unload

  "Opposite of [[load]], removes code for the given `input`.

   This adds an error with [[assoc-err-read]] since it means the input is now missing."

  [env input]

  (-> env
      (update :input->code
              dissoc
              input)
      (assoc-err-read input
                      :unload)))


;;;;;;;;;;


(defn patch

  "Looks for changes in `:input->change` (map of `input` -> `One of #{:create :delete :modify}`) and
   applies them by using [[reload]] and [[unload]] as needed."

  ([env]

   (reduce-kv patch
              (dissoc env
                      :input->change)
              (env :input->change)))


  ([env path change]

   ((if (identical? change
                    :delete)
      unload
      reload)
    env
    path)))


;;;;;;;;;; Executing steps


(let [-eval (fn [env ctx]
              (reduce (let [{:keys [input->code]} env]
                        (fn [env-2 input]
                          (try
                            (update env-2
                                    :ctx
                                    (fn [ctx]
                                      ($.cvm/eval ctx
                                                  (input->code input))))
                            (catch Throwable err
                              (reduced (-> env-2
                                           (assoc :error
                                                  [:eval
                                                   {:exception err
                                                    :input     input}])
                                           (dissoc :ctx)))))))
                      (assoc env
                             :ctx
                             ctx)
                      (env :input+)))]

  (defn eval

    "Evaluates the code for all `:input+` in `env` on `:ctx`, unless there is an `:error` attached.
    
      If `ctx` is not explicitly provided, it is fetched and forked from `:ctx-base` in the given `env`."


    ([env]

     (if (env :error)
       env
       (-eval env
              ($.cvm/fork (env :ctx-base)))))


    ([env ctx]

     (if (env :error)
       env
       (-eval env
              ctx)))))


;;;;;;;;;;


(defn disk

  "Into the given CVM `ctx` (or a newly created one if not provided), reads the given files and internis then as unevaluted code
   under their respective symbols.
  
   Only IO utility from this namespaces.

   Returns a map which shall be called an \"environment\" map. For simply loading files, only the prepared `:ctx` or the possible
   `:error` are really needed. However, it contains other key-values which can be used with utilities from this namespace for further
   processing, like [[convex.watch/start]] does.

   An environment map contains:

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
   (-> (load {'my-lib \"./path/to/lib.cvx\"})
       :ctx
       (convex.clj.eval/ctx '(def my-lib
                                  (deploy my-lib))))
   ```

   An `:error` is a 2-tuple vector where the first item is a keyword indicating an error type and second item is information:

   | Position 0 | Position 1 |
   |---|---|
   | `:load`` | Map of `file path` -> `Java exception occured during reading` |
   | `:eval` | Map with `:exception` (either Java or CVM exception) and `:input` (which input caused this evaluation error) |"


  ([sym->path]

   (disk ($.cvm/ctx)
         sym->path))


  ([ctx sym->path]

   (let [input+        (reduce (fn [input+ [sym ^String path]]
                                 (conj input+
                                       [(.getCanonicalPath (File. path))
                                        ($.code/symbol (str sym))]))
                               []
                               sym->path)
         path->cvm-sym (into {}
                             input+)
         read-input    (fn [env path]
                         (try
                           (assoc-code env
                                       path
                                       ($.code/def (path->cvm-sym path)
                                                   ($.code/quote (-> path
                                                                     slurp
                                                                     $.cvm/read))))
                           (catch Throwable ex
                             (assoc-err-read env
                                             path
                                             ex))))]
     (-> {:input+        (mapv first
                               input+)
          :path->cvm-sym path->cvm-sym
          :read          read-input}
         load
         (eval ctx)))))
