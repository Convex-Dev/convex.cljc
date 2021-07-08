(ns helins.maestro.cmd

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [test])
  (:require [clojure.string]
            [helins.maestro       :as $]
            [helins.maestro.alias :as $.alias]))


;;;;;;;;;; Helpers


(defn require-alias+

  ""

  [ctx kw-track alias+]

  (-> ctx
      (assoc :maestro/require
             [])
      ($/walk alias+)
      (as->
        ctx-2
        (assoc ctx-2
               kw-track
               (ctx-2 :maestro/require)))
      (update :maestro/require
              concat
              (ctx :maestro/require))))



(defn require-dev+

  ""


  ([ctx]

   (require-dev+ ctx
                 (ctx :maestro/require)))


  ([ctx alias+]

   (require-alias+ ctx
                   :maestro/dev+
                   ($.alias/dev+ ctx
                                 alias+))))



(defn require-test+

  ""


  ([ctx]

   (require-test+ ctx
                  (ctx :maestro/require)))


  ([ctx alias+]

   (require-alias+ ctx
                   :maestro/test+
                   ($.alias/test+ ctx
                                  alias+))))


;;;;;;;;;; Preparing for commands (calling a function, launching dev mode, ...)


(defn dev

  ""

  [ctx]

  (-> ctx
      ($/walk (concat [:task/test
                       :task/dev]
                      (ctx :maestro/main+)))
      require-dev+
      require-test+
      (update :maestro/exec-char
              #(or %
                   \M))))



(defn function

  ""

  [ctx]

  (-> ($/walk ctx)
      (update :maestro/exec-char
              #(or %
                   \X))))



(defn main

  ""

  [ctx]

  (-> ($/walk ctx)
      (update :maestro/exec-char
              #(or %
                   \M))))



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
      (update :maestro/exec-char
              #(or %
                   \M))))



(defn test

  ""

  [ctx f-test-alias+]

  (-> ctx
      ($/walk (conj (ctx :maestro/main+)
                    :task/test))
      (as->
        ctx-2
        (require-test+ ctx-2
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
        :maestro/main+))
