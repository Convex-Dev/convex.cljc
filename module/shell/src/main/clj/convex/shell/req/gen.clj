(ns convex.shell.req.gen

  (:require [clojure.test.check.generators :as TC.gen]
            [convex.cell                   :as $.cell]
            [convex.clj                    :as $.clj]
            [convex.cvm                    :as $.cvm]
            [convex.gen                    :as $.gen]
            [convex.std                    :as $.std]))


;;;;;;;;;;


(defn do-gen

  [ctx gen f]

  (or (when-not (and ($.std/vector? gen)
                     (= ($.std/count gen)
                        2))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Does not seem to be a generator")))
      (let [f*gen ($.std/nth gen
                             0)]
        (or (when-not ($.cell/fake? f*gen)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Either not a generator or a stale generator")))
            (let [gen-2 @f*gen]
              (or (when-not (TC.gen/generator? gen-2)
                    ($.cvm/exception-set ctx
                                         ($.cell/code-std* :ARGUMENT)
                                         ($.cell/* "Not a generator")))
                  (f ctx
                     gen-2)))))))



(defn create

  [id gen]

  ($.cell/* [~($.cell/fake gen)
             ~id]))


;;;;;;;;;;


(defn make

  ;; size must be pos?

  [ctx [gen size seed]]

  (or (when-not ($.std/long? size)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Size must be a Long")))
      (when-not (or (nil? seed)
                    ($.std/long? seed))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Seed must be nil or a Long")))
      (do-gen ctx
              gen
              (fn [ctx-2 gen-2]
                ($.cvm/result-set ctx-2
                                  (if seed
                                    (TC.gen/generate gen-2
                                                     ($.clj/long size)
                                                     ($.clj/long seed))
                                    (TC.gen/generate gen-2
                                                     ($.clj/long size))))))))
