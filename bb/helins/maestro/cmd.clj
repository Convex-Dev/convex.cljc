(ns helins.maestro.cmd

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [test])
  (:require [clojure.string]
            [helins.maestro       :as $]
            [helins.maestro.alias :as $.alias]))


;;;;;;;;;; Helpers


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
         ($/walk ($.alias/test+ ctx
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


;;;;;;;;;; Preparing for commands (calling a function, launching dev mode, ...)


(defn dev

  ""

  [ctx]

  (-> ctx
      ($/walk (concat [:task/test]
                      ($.alias/dev ctx)
                      (ctx :maestro/cli+)
                      [:task/dev]))
      require-test
      (update :maestro/exec-letter
              #(or %
                   \M))))



(defn function

  ""

  [ctx]

  (-> ($/walk ctx)
      (assoc :maestro/exec-letter
             "X")))



(defn main

  ""

  [ctx]

  (-> ($/walk ctx)
      (assoc :maestro/exec-letter
             "M")))



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
      ($/walk (conj (ctx :maestro/cli+)
                    :task/test))
      (as->
        ctx-2
        (require-test ctx-2
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
