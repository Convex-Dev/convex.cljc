(ns convex.test.net

  "Testing `convex.client` and `convex.server`."

  {:author "Adam Helinski"}

  (:import (convex.core Peer))
  (:refer-clojure :exclude [sequence])
  (:require [clojure.test  :as T]
            [convex.cell   :as $.cell]
            [convex.client :as $.client]
            [convex.cvm    :as $.cvm]
            [convex.db     :as $.db]
            [convex.read   :as $.read]
            [convex.server :as $.server]
            [convex.sign   :as $.sign]))


;;;;;;;;;; Setup


(def db
     ($.db/open-temp))



(def kp
     ($.sign/ed25519))



(def account-key
     ($.sign/account-key kp))



(def addr
     ($.cell/address 12))



(def ctx
     (-> ($.cvm/ctx {:convex.peer/key account-key})
         ($.cvm/fork-to addr)
         ($.cvm/eval ($.cell/* (set-key ~account-key)))))



(def port
     20000)



(def server
     ($.server/create kp
                      {:convex.server/controller addr
                       :convex.server/db         db
                       :convex.server/host       "localhost"
                       :convex.server/port       port
                       :convex.server/state      [:use ($.cvm/state ctx)]}))


;;;


(def client
     nil)



(defn connect

  "Creates and connects a new client to test server."

  []

  ($.client/connect {:convex.server/host "localhost"
                     :convex.server/port port}))



(T/use-fixtures :once
                (fn [f]
                  ($.server/start server)
                  (def client
                       (connect))
                  (f)
                  ($.client/close client)
                  ($.server/stop server)))


;;;;;;;;;; Tests - Server


(T/deftest controller

  (T/is (= addr
           ($.server/controller server))))



(T/deftest db-

  (T/is (= db
           ($.server/db server))))



(T/deftest host

  (T/is (= "127.0.0.1"
           ($.server/host server))))



(T/deftest peer

  (T/is (instance? Peer
                   ($.server/peer server))))



(T/deftest persist

  (T/is (do
          ($.server/persist server)
          (some? ($.db/read-root db)))))



(T/deftest port-

  (T/is (= port
           ($.server/port server))))


;;;;;;;;;; Client


(T/deftest connected?

  (T/is ($.client/connected? client))

  (T/is (let [client-2 (connect)]
          ($.client/close client-2)
          (not ($.client/connected? client-2)))))



(T/deftest peer-status

  (T/is (-> ($.client/peer-status client)
            (deref 1000
                   :timeout)
            $.cell/map?)))



(T/deftest resolve-and-state

  (T/is (= (-> ($.client/state client)
               (deref 10000
                      :timeout))
           (-> ($.client/resolve client
                                 (-> ($.client/peer-status client)
                                     (deref 1000
                                            :timeout)
                                     $.client/value
                                     last
                                     $.cell/hash<-blob))
               (deref 10000
                      :timeout)))))



(T/deftest query

  (T/is (= ($.cell/long 4)
           (-> ($.client/query client
                               addr
                               ($.read/string "(def foo-query (+ 2 2))"))
               (deref 1000
                      :timeout)
               $.client/value))
        "Simple query")

  (T/is (= ($.cell/boolean false)
           (-> ($.client/query client
                               addr
                               ($.read/string "(defined? foo-query)"))
               (deref 1000
                      :timeout)
               $.client/value))
        "State change in previous query has been reversed"))



(T/deftest sequence

  (T/is (pos? (deref ($.client/sequence client
                                        addr)))))



(T/deftest transact

  (T/is (= ($.cell/long 4)
           (-> ($.client/transact client
                                  kp
                                  ($.cell/invoke addr
                                                 1
                                                 ($.read/string "(def foo-transact (+ 2 2))")))
               (deref 1000
                      :timeout)
               $.client/value))
        "Def within a transaction")

  (T/is (= ($.cell/long 4)
           (-> ($.client/transact client
                                  kp
                                  ($.cell/invoke addr
                                                 2
                                                 ($.cell/symbol "foo-transact")))
               (deref 1000
                      :timeout)
               $.client/value))
        "Def persisted across transactions"))
