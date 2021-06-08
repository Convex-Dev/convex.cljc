(ns convex.cvm.file

  ""

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [intern])
  (:require [convex.cvm      :as $.cvm]
            [convex.cvm.type :as $.cvm.type]))


;;;;;;;;;;


(defn run

  ""


  ([ctx path]

   (run ctx
        path
        identity))


  ([ctx path wrap-read]

   (let [juice- ($.cvm/juice ctx)]
     (.withJuice ($.cvm/eval ($.cvm/fork ctx)
                             (-> path
                                 slurp
                                 $.cvm/read
                                 wrap-read))
                 juice-))))


;;;;;;;;;;



(defn deploy

  ""


  ([ctx sym path]

   (deploy ctx
           sym
           path
           identity))


  ([ctx sym path wrap-read]

  (let [sym-2 ($.cvm.type/symbol sym)]
    (run ctx
         path
         (fn [parsed]
           ($.cvm.type/list (list ($.cvm.type/symbol 'do)
                                  ($.cvm.type/list (list ($.cvm.type/symbol 'def)
                                                         sym-2
                                                         ($.cvm.type/list (list ($.cvm.type/symbol 'deploy)
                                                                                ($.cvm.type/list (list ($.cvm.type/symbol 'quote)
                                                                                                       (wrap-read parsed)))))))
                                  ($.cvm.type/list (list ($.cvm.type/symbol 'import)
                                                         ($.cvm.type/list (list ($.cvm.type/symbol 'address)
                                                                                sym-2))
                                                         ($.cvm.type/keyword :as)
                                                         sym-2)))))))))



(defn intern

  ""


  ([ctx sym path]

   (intern ctx
           sym
           path
           identity))
          

  ([ctx sym path wrap-read]

   (run ctx
        path
        (fn [parsed]
          ($.cvm.type/list (list ($.cvm.type/symbol 'def)
                                 ($.cvm.type/symbol sym)
                                 (wrap-read parsed)))))))
