(ns convex.test.cvm

  {:author "Adam Helinski"}

  (:require [clojure.test :as T]
            [convex.cell  :as $.cell]
            [convex.cvm   :as $.cvm]))


;;;;;;;;;;


(T/deftest actor?

  (T/is (true? ($.cvm/actor? ($.cvm/ctx)
                             ($.cell/address 0))))

  (T/is (false? ($.cvm/actor? ($.cvm/ctx)
                              $.cvm/genesis-user))))


;;;;;;;;;; From expansion to execution


(T/deftest execution

  (let [form ($.cell/* (if true 42 0))]
    (T/is (= ($.cell/* 42)
             (->> form
                  ($.cvm/eval ($.cvm/ctx))
                  ($.cvm/result))
             (->> form
                  ($.cvm/eval ($.cvm/ctx))
                  ($.cvm/result))
             (->> form
                  ($.cvm/expand ($.cvm/ctx))
                  ($.cvm/compile)
                  ($.cvm/exec)
                  ($.cvm/result))
             (->> form
                  ($.cvm/expand-compile ($.cvm/ctx))
                  ($.cvm/exec)
                  ($.cvm/result))))))


;;;;;;;;;;


(T/deftest exception

  (T/testing
    "Without exception"

    (T/is (nil? ($.cvm/exception ($.cvm/ctx))))

    (T/is (false? ($.cvm/exception? ($.cvm/ctx)))))

  (T/testing
    "With exception"

    (let [code    ($.cell/* :code)
          message ($.cell/* :message)
          ctx     ($.cvm/exception-set ($.cvm/ctx)
                                       code
                                       message)
          ex      ($.cvm/exception ctx)]

      (T/is (= code
               ($.cvm/exception-code ex)))

      (T/is (= message
               ($.cvm/exception-message ex)))

      (T/is ($.cvm/exception? ctx))

      (T/is (-> ctx
                ($.cvm/exception-clear)
                ($.cvm/exception)
                (nil?))))))


;;;;;;;;;;


(T/deftest transact

  (let [ctx (-> ($.cvm/ctx)
                ($.cvm/fork-to ($.cell/address 0))
                ($.cvm/transact ($.cell/invoke $.cvm/genesis-user
                                               1
                                               ($.cell/* (def x
                                                              42)))))]

    (T/is (= ($.cell/* 42)
             ($.cvm/result ctx))
          "Success")

    (T/is (= ($.cell/address 0)
             ($.cvm/address ctx))
          "Restored input context after applying the transaction")

    (T/is (= ($.cell/* {x 42})
             ($.cvm/env ctx
                        $.cvm/genesis-user))
          "Transaction applied to target account")))
