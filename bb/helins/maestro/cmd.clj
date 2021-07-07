(ns helins.maestro.cmd

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [test])
  (:require [clojure.string]
            [helins.maestro       :as $]
            [helins.maestro.alias :as $.alias]))


;;;;;;;;;;


(defn dev

  ""

  [ctx]

  (-> ctx
      (assoc :maestro/require
             [])
      ($/walk (concat [:task/test
                       :task/dev]
                      ($.alias/dev ctx)
                      (ctx :maestro/require)))
      $/require-test
      (assoc :maestro/exec-letter
             "M")))



(defn function

  ""

  [ctx]

  (-> ($/walk ctx)
      (assoc :maestro/exec-letter
             "X")))



(defn main-class

  ""

  [ctx]

  (-> ctx
      $/walk
      (as->
        ctx-2
        (update ctx-2
                :maestro/arg+
                (partial cons
                         (str "-m "
                              (or ($.alias/main-class ctx-2)
                                  (throw (ex-info "Alias needs `:maestro/main-class` pointing to the class containing the `-main` function"
                                                  {})))))))
      (assoc :maestro/exec-letter
             "M")))



(defn test

  ""

  [ctx f-test-alias+]

  (-> ctx
      (update :maestro/require
              conj
              :task/test)
      $/walk
      (as->
        ctx-2
        ($/require-test ctx-2
                        (f-test-alias+ ctx-2)))))



(defn test-broad

  ""

  [ctx]

  (test ctx
        :maestro/require))



(defn test-narrow

  ""

  [ctx]

  (test ctx
        :maestro/cli+))
