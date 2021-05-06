(ns convex.lisp.test.mult

  ""

  {:author "Adam Helinski"}

  (:require [convex.lisp.form        :as $.form]
            [convex.lisp.test.eval   :as $.test.eval]
            [convex.lisp.test.prop   :as $.test.prop]
            [convex.lisp.test.schema :as $.test.schema]))


;;;;;;;;;;


(defn new-account

  ""

  [subprop+ ctx actor?]

  (conj subprop+
        ["Address is interned"
         #($.test.schema/valid? :convex/address
                                ($.test.eval/form ctx
                                                  'addr))]
        ["(account?)"
         #($.test.eval/form ctx
                            '(account? addr))]
        ["(actor?)"
         #(actor? ($.test.eval/form ctx
                                    '(actor? addr)))]
        ["(address?)"
         #($.test.eval/form ctx
                            '(address? addr))]
        ["(balance)"
         #(zero? ($.test.eval/form ctx
                                   '(balance addr)))]
        ["(get-holding)"
         #(nil? ($.test.eval/form ctx
                                  '(get-holding addr)))]
        ["(account) and comparing with *state*"
         #(let [[addr-long
                 account]  ($.test.eval/form ctx
                                             '[(long addr)
                                               (account addr)])]
            (= account
               ($.test.eval/form ctx
                                 ($.form/templ {'?addr addr-long}
                                               '(get-in *state*
                                                        [:accounts
                                                         ?addr])))))]))

(defn fn?-

  ""

  [subprop+ form]

  (conj subprop+
        ["Function?"
         #($.test.eval/form (list 'fn?
                                  form))]))



(defn fn-call

  ""

  [subprop+ form arg+ ret]

  (conj subprop+
        ["Direct call"
         #($.test.eval/form (list '=
                                  ret
                                  (list* form
                                         arg+)))]
        ["Calling after interning"
         #($.test.prop/mult-result ($.test.eval/form ($.form/templ {'?call (list* 'f
                                                                                  arg+)
                                                                    '?fn   form
                                                                    '?ret  ret}
                                                                   '(do
                                                                      (def f
                                                                           ?fn)
                                                                      [(fn? f)
                                                                       (= ?ret
                                                                          ?call)])))
                                   ["Fn?"
                                    "Equal"])]
        ["Calling as local binding"
         #($.test.prop/mult-result ($.test.eval/form ($.form/templ {'?call (list* 'f
                                                                                  arg+)
                                                                    '?fn   form
                                                                    '?ret  ret}
                                                                   '(let [f ?fn]
                                                                      [(fn? f)
                                                                       (= ?ret
                                                                          ?call)])))
                                   ["Fn?"
                                    "Equal"])]))
