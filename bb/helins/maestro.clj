(ns helins.maestro

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [test])
  (:require [babashka.tasks :as bb.task]
            [clojure.edn]
            [clojure.string]))


(declare data)


;;;;;;;;;;


(defn deps-edn

  ""

  ([]

   (deps-edn "deps.edn"))


  ([path]

    (-> path
        slurp
        clojure.edn/read-string)))




(defn ctx


  ([]

   (ctx *command-line-args*))


  ([[alias+ & arg+]]

   (if-some [alias-2+ (and alias+
                           (when (clojure.string/starts-with? alias+
                                                              ":")
                             (->> (clojure.string/split alias+
                                                        #":")
                                  rest
                                  (mapv keyword)
                                  not-empty)))]
    (-> (deps-edn)
        (merge (if (.ready *in*)
                 (clojure.edn/read *in*)
                 {}))
        (assoc :maestro/arg+     arg+
               :maestro/cli+     alias-2+
               :maestro/require  alias-2+))
    (throw (ex-info "At least one alias must be provided as first argument"
                    {})))))









(defn aggr-alias

  ""


  ([acc alias _config]

   (aggr-alias acc
               :maestro/require
               alias
               _config))

  ([acc kw alias _config]

   (update acc
           kw
           (fnil conj
                 [])
           alias)))



(defn aggr-env

  ""


  ([acc _alias config]

   (aggr-env acc
             :maestro/env
             _alias
             config))


  ([acc kw _alias config]

   (update acc
           kw
           merge
           (:maestro/env config))))



(defn aggr

  ""

  [acc alias config]

  (-> acc
      (aggr-alias alias
                  config)
      (aggr-env alias
                config)))




(defn walk

  ""


  ([ctx]

   (walk ctx
         aggr
         (ctx :maestro/require)))

  
  ([ctx alias+]

   (walk ctx
         aggr
         alias+))


  ([ctx f alias+]

   (reduce (fn [ctx-2 alias]
             (if (contains? (ctx-2 :maestro/seen+)
                            alias)
               ctx-2
               (let [data' (data ctx
                                 alias)]
                 (f (walk (update ctx-2
                                 :maestro/seen+
                                 (fnil conj
                                       #{})
                                 alias)
                          f
                          (:maestro/require data'))
                    alias
                    data'))))
           ctx
           alias+)))


;;;;;;;;;; Helpers


(defn related-alias+

  ""

  [kw default-ns deps-edn alias]

  (let [deps-alias (deps-edn :aliases)]
    (kw deps-alias)
    (let [alias-default (keyword default-ns
                                 (name alias))]
      (when (contains? deps-alias
                       alias-default)
        [alias-default]))))


;;;;;;;;;;


(defn data

  ""

  [ctx alias]

  (get-in ctx
          [:aliases
           alias]))



(defn dev

  ""


  ([ctx]

   (dev ctx
        (first (ctx :maestro/cli+))))


  ([ctx alias]

   (related-alias+ :maestro/dev
                   "dev"
                   ctx
                   alias)))



(defn main-class

  ""


  ([ctx]

   (main-class ctx
               (last (ctx :maestro/cli+))))


  ([ctx alias]

   (:maestro/main-class (data ctx
                              alias))))


(defn path+

  ""

  [ctx alias+]

  (into []
        (comp (map (partial get
                            (ctx :aliases)))
              (mapcat :extra-paths))
        alias+))



(defn root

  ""

  [ctx alias]

  (:maestro/root (data ctx
                       alias)))



(defn test

  ""

  [ctx alias]

  (related-alias+ :maestro/test
                  "test"
                  ctx
                  alias))



(defn test+

  ""


  ([ctx]

   (test+ ctx
          (ctx :maestro/require)))


  ([ctx alias+]

   (mapcat (partial test
                    ctx)
           alias+)))











(defn require-test

  ""


  ([ctx]

   (require-test ctx
                 (test+ ctx)))


  ([ctx alias+]

   (let [required (ctx :maestro/require)]
   (-> ctx
       (dissoc :maestro/require)
       (walk (test+ ctx
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



