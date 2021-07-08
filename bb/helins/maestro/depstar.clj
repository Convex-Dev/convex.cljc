(ns helins.maestro.depstar

  ""

  {:author "Adam Helinski"}

  (:require [helins.maestro       :as $]
            [helins.maestro.alias :as $.alias]))


;;;;;;;;;;


(defn -jar

  ""

  [ctx dir alias f]

  (let [main+      (ctx :maestro/main+)
        ctx-2      ($/walk ctx
                           main+)
        alias-main (last main+)]
    (-> ctx-2
        ($/walk [alias])
        (assoc :maestro/main
               alias-main)
        (as->
          ctx-3
          (update ctx-3
                  :maestro/arg+
                  (partial cons
                           (let [root (or ($.alias/root ctx-3
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
        (assoc :maestro/exec-char
               "X")
        f)))


;;;;;;;;;;


(defn jar

  ""


  ([]

   (jar ($/ctx)))


  ([ctx]

   (-jar ctx
         "jar"
         :task/jar
         identity)))



(defn uberjar

  ""


  ([]

   (uberjar ($/ctx)))


  ([ctx]

   (-jar ctx
         "uberjar"
         :task/uberjar
         (fn [ctx]
           (if-some [main-class ($.alias/main-class ctx
                                                    (ctx :maestro/main))]
             (update ctx
                     :maestro/arg+
                     (partial cons
                              (str ":main-class "
                                   main-class)))
             ctx)))))
