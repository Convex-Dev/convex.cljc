(ns convex.test.net

  "Testing `convex.client` and `convex.server`."

  {:author "Adam Helinski"}

  (:import (convex.core Belief
                        Peer))
  (:require [clojure.test    :as T]
            [convex.cell     :as $.cell]
            [convex.client   :as $.client]
            [convex.cvm      :as $.cvm]
            [convex.db       :as $.db]
            [convex.key-pair :as $.key-pair]
            [convex.server   :as $.server]
            [convex.std      :as $.std]))


;;;;;;;;;; Setup


(def d*db
     (delay
       ($.db/current-set ($.db/open-tmp))))



(def kp
     ($.key-pair/ed25519))



(def account-key
     ($.key-pair/account-key kp))



(def user
     $.cvm/genesis-user)



(def port
     20000)



(def d*server
     (delay
       ($.server/create kp
                        {:convex.server/controller user
                         :convex.server/db         @d*db
                         :convex.server/host       "localhost"
                         :convex.server/port       port
                         :convex.server/state      [:use (-> ($.cvm/ctx {:convex.cvm/genesis-key+ [account-key]})
                                                             ($.cvm/state))]})))


;;;


(def client
     nil)



(def client-local
     nil)



(defn -connect

  "Creates and connects a new client to test server."

  []

  ($.client/connect {:convex.server/host "localhost"
                     :convex.server/port port}))



(defn -deref

  "In case a future never resolves."

  [future]

  (deref future
         10000
         ::timeout))



(defn -sequence-id

  [client]

  (-deref ($.client/sequence-id client
                                user)))



(defn test-client+

  [f]

  (T/testing
    "Remote client"
    (f client))
  (T/testing
    "Local client"
    (f client-local)))


(T/use-fixtures :once
                (fn [f]
                  ($.server/start @d*server)
                  (alter-var-root #'client
                                  (constantly (-connect)))
                  (alter-var-root #'client-local
                                  (constantly ($.client/connect-local @d*server)))
                  (f)
                  ($.client/close client)
                  ($.client/close client-local)
                  ($.server/stop @d*server)))


;;;;;;;;;; Tests - Server


(T/deftest belief

  (T/is (instance? Belief
                   ($.server/belief @d*server))))



(T/deftest broadcast-count

  (T/is (int? ($.server/broadcast-count @d*server))))



(T/deftest controller

  (T/is (= user
           ($.server/controller @d*server))))



(T/deftest db-

  (T/is (= @d*db
           ($.server/db @d*server))))



(T/deftest host

  (T/is (= "127.0.0.1"
           ($.server/host @d*server))))



(T/deftest peer

  (T/is (instance? Peer
                   ($.server/peer @d*server))))



(T/deftest persist

  (T/is (do
          ($.server/persist @d*server)
          (some? ($.db/root-read)))))



(T/deftest port-

  (T/is (= port
           ($.server/port @d*server))))



(T/deftest state

  (T/is ($.std/state? ($.server/state @d*server))))


;;;;;;;;;; Client


(T/deftest connected?

  (T/is ($.client/connected? client))

  (T/is (let [client-2 (-connect)]
          ($.client/close client-2)
          (not ($.client/connected? client-2)))))



(T/deftest peer-status

  (test-client+
    (fn [client]
      (T/is (-> ($.client/peer-status client)
                -deref
                $.std/map?)))))



(T/deftest resolve-and-state

  (test-client+
    (fn [client]
      (T/is (= (-> ($.client/state client)
                   -deref)
               (-> ($.client/resolve client
                                     (-> ($.client/peer-status client)
                                         -deref
                                         $.client/result->value
                                         last
                                         $.cell/hash<-blob))
                   -deref))))))



(T/deftest query

  (test-client+
    (fn [client]

      (T/is (= ($.cell/long 4)
               (-> ($.client/query client
                                   user
                                   ($.cell/* (def foo-query (+ 2 2))))
                   -deref
                   $.client/result->value))
            "Simple query")

      (T/is (= ($.cell/boolean false)
               (-> ($.client/query client
                                   user
                                   ($.cell/* (defined? foo-query)))
                   -deref
                   $.client/result->value))
            "State change in previous query has been reversed"))))



(T/deftest sequence-id

  (test-client+
    (fn [client]
      (T/is (pos? (-sequence-id client))))))



(T/deftest transact

  (test-client+
    (fn [client]

      (T/is (= ($.cell/long 4)
               (-> ($.client/transact client
                                      kp
                                      ($.cell/invoke user
                                                     (-sequence-id client)
                                                     ($.cell/* (def foo-transact (+ 2 2)))))
                   -deref
                   $.client/result->value))
            "Def within a transaction")

      (T/is (= ($.cell/long 4)
               (-> ($.client/transact client
                                      kp
                                      ($.cell/invoke user
                                                     (-sequence-id client)
                                                     ($.cell/symbol "foo-transact")))
                   -deref
                   $.client/result->value))
            "Def persisted across transactions"))))
