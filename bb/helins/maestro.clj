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
         (ctx :maestro/main+)))


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


  ([deps-edn cli-arg+]

   (let [arg-first    (or (first cli-arg+)
                          (throw (ex-info "No argument provided, requires at least one alias"
                                          {})))
         [arg-first-2
          exec-char]  (if (clojure.string/starts-with? arg-first
                                                       "-")
                        (try
                          [(.substring ^String arg-first
                                       2)
                           (nth arg-first
                                1)]
                          (catch Throwable _ex
                            (throw (ex-info "First argument starting with '-' needs a Clojure execution letter (eg. '-M')"
                                            {:maestro/arg arg-first}))))
                        [arg-first
                         nil])]
      (-> deps-edn
          (merge (if (.ready *in*)
                   (clojure.edn/read *in*)
                   {}))
          (assoc :maestro/aggr    aggr
                 :maestro/arg+    (rest cli-arg+)
                 :maestro/main+   (or (and (clojure.string/starts-with? arg-first-2
                                                                        ":")
                                           (->> (clojure.string/split arg-first-2
                                                                      #":")
                                                rest
                                                (mapv keyword)
                                                not-empty))
                                      (throw (ex-info "At least one alias must be specified"
                                                      {:maestro/arg arg-first-2})))
                 :maestro/require [])
          (cond->
            exec-char
            (assoc :maestro/exec-char exec-char))))))


;;;;;;;;;; Miscellaneous insigts


(defn cmd

  ""

  [ctx]

  (format "-%s%s %s"
          (ctx :maestro/exec-char)
          (clojure.string/join ""
                               (ctx :maestro/require))
          (clojure.string/join " "
                               (ctx :maestro/arg+))))
