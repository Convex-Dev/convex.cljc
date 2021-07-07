(ns helins.maestro.depstar

  ""

  {:author "Adam Helinski"}

  (:require [helins.maestro :as $]))


;;;;;;;;;;


(defn -jar

  ""

  [ctx dir alias f]

  (let [ctx-2      ($/walk ctx)
        alias-main (last (ctx-2 :maestro/cli+))]
    (-> ctx-2
        (dissoc :maestro/require)
        ($/walk [alias])
        (assoc :maestro/main
               alias-main)
        (as->
          ctx-3
          (update ctx-3
                  :maestro/arg+
                  (partial cons
                           (let [root (or ($/root ctx-3
                                                  alias-main)
                                          (throw (ex-info "Alias needs `:maestro/root` pointing to project root directory"
                                                          {})))]
                             (format ":jar %s/%s/%s.jar :aliases '%s' :pom-file '\"%s/pom.xml\"'"
                                     (or (ctx :maestro.depstar/dir)
                                         "build")
                                     dir
                                     root
                                     (ctx-2 :maestro/require)
                                     root)))))
        (assoc :maestro/exec-letter
               "X")
        f)))


;;;;;;;;;;


(defn jar

  ""

  [ctx]

  (-jar ctx
        "jar"
        :task/jar
        identity))



(defn uberjar

  ""

  [ctx]

  (-jar ctx
        "uberjar"
        :task/uberjar
        (fn [ctx]
          (if-some [main-class ($/main-class ctx
                                             (ctx :maestro/main))]
            (update ctx
                    :maestro/arg+
                    (partial cons
                             (str ":main-class "
                                  main-class)))
            ctx))))
