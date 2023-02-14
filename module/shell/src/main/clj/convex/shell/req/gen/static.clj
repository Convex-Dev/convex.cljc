(ns convex.shell.req.gen.static

  (:refer-clojure :exclude [boolean
                            char
                            double
                            keyword
                            long
                            symbol])
  (:require [convex.cell          :as $.cell]
            [convex.cvm           :as $.cvm]
            [convex.gen           :as $.gen]
            [convex.shell.req.gen :as $.shell.req.gen]))


;;;;;;;;;;


(let [gen ($.shell.req.gen/create ($.cell/long -1)
                                  $.gen/address)]

  (defn address
  
    [ctx _arg+]
    
    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -2)
                                  $.gen/any)]

  (defn any

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))


(let [gen ($.shell.req.gen/create ($.cell/long -3)
                                  $.gen/any-coll)]

  (defn any-coll

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -4)
                                  $.gen/any-list)]

  (defn any-list

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -5)
                                  $.gen/any-map)]

  (defn any-map

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -3)
                                  $.gen/any-set)]

  (defn any-set

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -7)
                                  $.gen/any-vector)]

  (defn any-vector

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -8)
                                  $.gen/blob-32)]

  (defn blob-32

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -9)
                                  $.gen/boolean)]

  (defn boolean

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -10)
                                  $.gen/char)]

  (defn char

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -11)
                                  $.gen/char-alphanum)]

  (defn char-alphanum

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -12)
                                  $.gen/double)]

  (defn double

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -13)
                                  $.gen/falsy)]

  (defn falsy

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -14)
                                  $.gen/keyword)]

  (defn keyword

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))


(let [gen ($.shell.req.gen/create ($.cell/long -15)
                                  $.gen/long)]

  (defn long

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))


(let [gen ($.shell.req.gen/create ($.cell/long -16)
                                  $.gen/number)]

  (defn number

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -17)
                                  $.gen/nothing)]

  (defn nothing

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))


(let [gen ($.shell.req.gen/create ($.cell/long -18)
                                  $.gen/scalar)]

  (defn scalar

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))



(let [gen ($.shell.req.gen/create ($.cell/long -19)
                                  $.gen/symbol)]

  (defn symbol

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))


(let [gen ($.shell.req.gen/create ($.cell/long -20)
                                  $.gen/truthy)]

  (defn truthy

    [ctx _arg+]

    ($.cvm/result-set ctx
                      gen)))
