(ns convex.shell.req.gen.static

  (:refer-clojure :exclude [boolean
                            char
                            double
                            keyword
                            long
                            symbol])
  (:require [convex.cvm         :as $.cvm]
            [convex.gen         :as $.gen]
            [convex.shell.resrc :as $.shell.resrc]))


;;;;;;;;;;


(let [gen ($.shell.resrc/create $.gen/address)]

  (defn address
  
    [ctx _arg+]
    
    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/any)]

  (defn any

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))


(let [gen ($.shell.resrc/create $.gen/any-coll)]

  (defn any-coll

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/any-list)]

  (defn any-list

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/any-map)]

  (defn any-map

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/any-set)]

  (defn any-set

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/any-vector)]

  (defn any-vector

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/blob-32)]

  (defn blob-32

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/boolean)]

  (defn boolean

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/char)]

  (defn char

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/char-alphanum)]

  (defn char-alphanum

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/double)]

  (defn double

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/falsy)]

  (defn falsy

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/keyword)]

  (defn keyword

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))


(let [gen ($.shell.resrc/create $.gen/long)]

  (defn long

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))


(let [gen ($.shell.resrc/create $.gen/number)]

  (defn number

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/nothing)]

  (defn nothing

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))


(let [gen ($.shell.resrc/create $.gen/scalar)]

  (defn scalar

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.resrc/create $.gen/symbol)]

  (defn symbol

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))


(let [gen ($.shell.resrc/create $.gen/truthy)]

  (defn truthy

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))
