(ns helins.maestro

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [test])
  (:require [babashka.tasks       :as bb.task]
            [clojure.edn]
            [clojure.string]
            [helins.maestro.aggr  :as $.aggr]
            [helins.maestro.alias :as $.alias]))


;;;;;;;;;; Operation over a context


(defn aggr

  ""

  [acc alias config]

  (-> acc
      ($.aggr/alias alias
                    config)
      ($.aggr/env alias
                  config)))



(defn walk

  ""


  ([ctx]

   (walk ctx
         (ctx :maestro/require)))


  ([ctx alias+]

   (let [user-aggr (ctx :maestro/aggr)]
     (reduce (fn [ctx-2 alias]
               (if (contains? (ctx-2 :maestro/seen+)
                              alias)
                 ctx-2
                 (let [data ($.alias/data ctx
                                          alias)]
                   (user-aggr (walk (update ctx-2
                                            :maestro/seen+
                                            (fnil conj
                                                  #{})
                                            alias)
                                     (:maestro/require data))
                               alias
                               data))))
             (update ctx
                     :maestro/seen+
                     (fn [seen+]
                       (or seen+
                           #{})))
             alias+))))


;;;;;;;;;; I/O


(defn deps-edn

  ""

  ([]

   (deps-edn "deps.edn"))


  ([path]

    (-> path
        slurp
        clojure.edn/read-string)))


;;;;;;;;;; Initialization


(defn ctx


  ([]

   (ctx (deps-edn)
        *command-line-args*))


  ([deps-edn [alias+ & arg+]]

   (if-some [alias-2+ (and alias+
                           (when (clojure.string/starts-with? alias+
                                                              ":")
                             (->> (clojure.string/split alias+
                                                        #":")
                                  rest
                                  (mapv keyword)
                                  not-empty)))]
    (-> deps-edn
        (merge (if (.ready *in*)
                 (clojure.edn/read *in*)
                 {}))
        (assoc :maestro/aggr     aggr
               :maestro/arg+     arg+
               :maestro/cli+     alias-2+
               :maestro/require  alias-2+))
    (throw (ex-info "At least one alias must be provided as first argument"
                    {})))))


;;;;;;;;;; Miscellaneous helpers


(defn require-test

  ""


  ([ctx]

   (require-test ctx
                 ($.alias/test+ ctx)))


  ([ctx alias+]

   (let [required (ctx :maestro/require)]
     (-> ctx
         (assoc :maestro/require
                [])
         (walk ($.alias/test+ ctx
                              alias+))
         (as->
           ctx-2
           (assoc ctx-2
                  :maestro/test+
                  (ctx-2 :maestro/require)))
         (assoc :maestro/main+
                required)
         (update :maestro/require
                 concat
                 required)))))


;;;;;;;;;; Running Clojure commands


(defn cmd

  ""

  [ctx]

  (format "-%s%s %s"
          (ctx :maestro/exec-letter)
          (clojure.string/join ""
                               (ctx :maestro/require))
          (clojure.string/join " "
                               (ctx :maestro/arg+))))



(defn clojure

  ""

  [ctx]

  (let [cmd-2 (cmd ctx)]
    (when (ctx :maestro/debug?)
      (println cmd-2))
    (bb.task/clojure {:extra-env (ctx :maestro/env)}
                     cmd-2)))
