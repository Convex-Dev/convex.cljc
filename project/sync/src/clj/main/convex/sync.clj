(ns convex.sync

  "About syncing running Convex Lisp source code with CVM contextes.

   Series of abstract utilities for handling code reading, updating, errors, ...

   A prime example is [[disk]] which reads and loads unevaluated source files into a CVM context.

   Another one is [[convex.watch/start]] which builds on [[disk]] as well as other utilities from this namespace
   for providing a live-reloading experience."

  {:author "Adam Helinski"}

  (:import (java.io File)
           (java.nio.file NoSuchFileException))
  (:refer-clojure :exclude [eval
                            load])
  (:require [convex.data :as $.data]
            [convex.read :as $.read]
            [convex.cvm  :as $.cvm]))


(declare load
         unload)


;;;;;;;;;; Altering the "env" map (executing environment)


(defn assoc-code

  "Associates the given `code` to `input` in `env` under `:convex.sync/input->code`."

  [env input code]

  (assoc-in env
            [:convex.sync/input->code
             input]
            code))



(defn assoc-err-read

  "In `env` under `:convex.sync/error`, adds `err` as a read error that occured when reading `input`."

  [env input err]

  (-> env
      (dissoc :convex.sync/ctx)
      (update :convex.sync/error
              (fn [error]
                (if (identical? (first error)
                                :load)
                  (update error
                          1
                          assoc
                          input
                          err)
                  [:load
                   {input err}])))))


;;;;;;;;;; Dealing with inputs


(defn load

  "Reads the given `input` (or all `:convex.sync/inputs+` in `env`).
  
   Essentially does `((env :convex.sync/read) env input)`, which means that the `:convex.sync/read` function in `env`
   must appropriately handle read errors using [[assoc-err-read]].

   Even in case of errors, all inputs are processed."

  ([env]

   (reduce (env :convex.sync/read)
           env
           (env :convex.sync/input+)))


  ([env input]

   ((env :convex.sync/read)
    env
    input)))



(defn reload

  "Like [[load]] but meant to be used whenever an input is updated.

   Updates read errors."

  [env input]

  (-> (let [error        (env :convex.sync/error)
            input->error (when (and error
                                    (identical? (first error)
                                                :load))
                           (not-empty (dissoc (second error)
                                              input)))]
        (if input->error
          (assoc env
                 :convex.sync/error
                 [:load
                  input->error])
          (dissoc env
                  :convex.sync/error)))
      (update :convex.sync/input->code
              dissoc
              input)
      (load input)))



(defn unload

  "Opposite of [[load]], removes code for the given `input`.

   This adds an error with [[assoc-err-read]] since it means the input is now missing."

  [env input]

  (-> env
      (update :convex.sync/input->code
              dissoc
              input)
      (assoc-err-read input
                      :unload)))


;;;;;;;;;;


(defn patch

  "Looks for changes in `:convex.sync/input->change` (map of `input` -> `One of #{:create :delete :modify}`) and
   applies them by using [[reload]] and [[unload]] as needed."

  ([env]

   (reduce-kv patch
              (dissoc env
                      :convex.sync/input->change)
              (env :convex.sync/input->change)))


  ([env input change]

   ((if (identical? change
                    :delete)
      unload
      reload)
    env
    input)))


;;;;;;;;;; Executing steps


(let [-eval (fn [env ctx]
              (reduce (let [{:convex.sync/keys [input->code]} env]
                        (fn [env-2 input]
                          (let [ctx-2     ($.cvm/eval (env-2 :convex.sync/ctx)
                                                      (input->code input))
                                exception ($.cvm/exception ctx-2)]
                            (if exception
                              (reduced (-> env-2
                                           (assoc :convex.sync/error
                                                  [:eval
                                                   input
                                                   exception])
                                           (dissoc :convex.sync/ctx)))
                              (assoc env-2
                                     :convex.sync/ctx
                                     ctx-2)))))
                      (assoc env
                             :convex.sync/ctx
                             ctx)
                      (env :convex.sync/input+)))]

  (defn eval

    "Evaluates the code for all `:convex.sync/input+` in `env` on `:convex.sync/ctx`.
    
     If `ctx` is not explicitly provided, it is fetched and forked from `:convex.sync/ctx-base` in the given `env`.

     In case of of CVM exception, removes the `:convex.sync/ctx`` and attach an error under `:convex.sync/error` such as:
     
       `[:eval input cvm-exception]`"


    ([env]

     (if (env :convex.sync/error)
       env
       (-eval env
              ($.cvm/fork (env :convex.sync/ctx-base)))))


    ([env ctx]

     (if (env :convex.sync/error)
       env
       (-eval env
              ctx)))))


;;;;;;;;;;


(defn disk

  "Into the given CVM `ctx` (or a newly created one if not provided), reads the given files and interns them as unevaluted code
   under their respective symbols. Conceptually, they can be considered as dependency files.

   A dependency is either a filename or a `java.io.Reader`. Each filename is turned into its canonical form.
  
   Unevaluated code is a list of raw quoted forms. Often, Convex Lisp files have only one top-level `do` form bundling several forms
   meant to be executed as a single transaction. However, it could be useful for a dependency file to have several top-level forms.
  
   Only IO utility from this namespaces.

   Returns a map which shall be called an \"environment\" map. For simply loading files, only the prepared `:convex.sync/ctx` or the possible
   `:convex.sync/error` are really needed. However, it contains other key-values which can be used with utilities from this namespace for further
   processing, like [[convex.watch/start]] does.

   An environment map contains:

   | Key | Value |
   |---|---|
   | `:convex.sync/ctx` | If there is no `:error`, the prepared context |
   | `:convex.sync/error` | Signals an error and the absence of `:ctx` (see below) |
   | `:convex.sync/input+` | Vector of requested input file paths |
   | `:convex.sync/input->code` | Map of `input file path` -> `CVM code`, uses `:read` |
   | `:convex.sync/input->cvm-symbol` | Map of `file path` -> `CVM symbol used for interning` |
   | `:convex.sync/read` | Read function used by the [[convex.sync]] namespace, not a user concern |

   This interned code can they be used as needed through the power of Lisp. Typically, either `deploy` or `eval` is used.

   Eg. Reading a file and deploying as a library (without error checking):
   
   ```clojure
   (-> (load {'my-lib \"./path/to/lib.cvx\"})
       :ctx
       (convex.clj.eval/ctx '(def my-lib
                                  (deploy my-lib))))
   ```

   Any error is attached under `:convex.sync/error`. Possibly:

   - Failure during reading:

   | Reason | Meaning |
   |---|---|
   | `[:not-found]` | File does not exist or is not accessible |
   | `[:unknown Exception]]` | Unknown exception while loading file |

   - Failure during evaluation: see [[eval]]."

  ([sym->input]

   (disk ($.cvm/ctx)
         sym->input))


  ([ctx sym->input]

   (let [input+         (reduce (fn [input+ [sym input]]
                                  (conj input+
                                        [(if (string? input)
                                           (.getCanonicalPath (File. ^String input))
                                           input)
                                         ($.data/symbol (str sym))]))
                                []
                                sym->input)
         input->cvm-sym (into {}
                              input+)
         read-dep       (fn X [env input]
                          (let [[err
                                 form+] (try
                                          
                                          [nil
                                           ((if (string? input)
                                              $.read/file+
                                              $.read/stream+)
                                            input)]

                                          (catch NoSuchFileException _ex
                                            [[:not-found]
                                             nil])

                                          (catch Throwable ex
                                            [[:unknown
                                              ex]
                                             nil]))]
                            (if err
                              (assoc-err-read env
                                              input
                                              err)
                              (assoc-code env
                                          input
                                          ($.data/def (input->cvm-sym input)
                                                      ($.data/quote form+))))))]
     (-> {:convex.sync/input+         (mapv first
                                            input+)
          :convex.sync/input->cvm-sym input->cvm-sym
          :convex.sync/read           read-dep}
         load
         (eval ctx)))))
