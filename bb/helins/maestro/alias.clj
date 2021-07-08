(ns helins.maestro.alias

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [test]))


(declare related+)


;;;;;;;;;;


(defn data

  ""

  [ctx alias]

  (get-in ctx
          [:aliases
           alias]))



(defn dev

  ""

  [ctx alias]

  (related+ ctx
            :maestro/dev
            "dev"
            alias))



(defn dev+

  ""


  ([ctx]

   (dev+ ctx
         (ctx :maestro/require)))


  ([ctx alias+]

   (mapcat (partial dev
                    ctx)
           alias+)))



(defn main-class

  ""


  ([ctx]

   (main-class ctx
               (last (ctx :maestro/main+))))


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



(defn related+

  ""

  [ctx kw default-ns alias]

  (let [deps-alias (ctx :aliases)]
    (kw deps-alias)
    (let [alias-default (keyword default-ns
                                 (name alias))]
      (when (contains? deps-alias
                       alias-default)
        [alias-default]))))



(defn root

  ""

  [ctx alias]

  (:maestro/root (data ctx
                       alias)))



(defn test

  ""

  [ctx alias]

  (related+ ctx
            :maestro/test
            "test"
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
