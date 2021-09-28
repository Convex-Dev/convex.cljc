(ns convex.recipe.client

  "This example show how to create and use the fast binary client by connecting to the current test net.

   2 types of requests:

   - Queries, how to read data from the network
   - Transactions, how to change data on the network
  
   Lastly, deployment of a simple smart contact is demonstrated."

  {:author "Adam Helinski"}

  (:require [clojure.pprint]
            [convex.cell            :as $.cell]
            [convex.client          :as $.client]
            [convex.cvm.db          :as $.cvm.db]
            [convex.db              :as $.db]
            [convex.read            :as $.read]
            [convex.recipe.key-pair :as $.recipe.key-pair]
            [convex.recipe.rest     :as $.recipe.rest]))


;;;;;;;;;;


(defn result

  "Example of error handling when receiving a result for a query or a transaction.

   In practice, error handling depends on application requirements.

   It it always best to provided a timeout when dereferencing the future."

  [future-result]

  (let [resp (deref future-result
                    4000
                    nil)]
    (when resp
      (let[error-code ($.client/error-code resp)
           value      ($.client/value resp)]
       (if error-code
         {:error?     true
          :error-code error-code
          :message    value
          :trace      ($.client/trace resp)}
         {:error? false
          :return value})))))



;;;;;;;;;;


(comment


  ;;
  ;; SPECIFYING A DATABASE
  ;;
  ;; Clients uses an instance of the Etch database because only incremental changes are transmitted
  ;; over the network. The database is used to remember data and only request missing pieces.
  ;;
  ;; That way, even large data structures can be shared efficiently.
  ;;
  ;; Especially when quering the same kind of data, it is a good idea using the same Etch file. Not mandatory
  ;; but will often improve performance.
  ;;
  ;; Also see the `convex.recipe.db`.
  ;;
  ($.cvm.db/local-set ($.db/open "private/recipe/client/db.etch"))


  ;; Let us create a client and connect to `convex.world` (current testnet).
  ;;
  (def c
       ($.client/connect {:convex.server/host "convex.world"
                          :convex.server/port 18888}))


  ;;
  ;; QUERIES
  ;;
  ;; Queries are akin to read operations.
  ;;
  ;; Code is provided and executed by the peer but it does not require any consensus.
  ;; Any change in the state is simply discarded.
  ;;

  ;; Query requesting a result for `(+ 2 2)`
  ;;
  ;; Any address can be provided, nothing needs to be signed. This is just a read operation.
  ;;
  ;; A future is returned which resolves to a result.
  ;;
  (-> ($.client/query c
                      ($.cell/address 1)
                      ($.cell/* (+ 2 2)))
      deref
      str)


  ;; A result is an object. The API provides functions for checking if an error occured,
  ;; getting the value, etc.
  ;;
  (-> ($.client/query c
                      ($.cell/address 1)
                      ($.cell/* (+ 2 2)))
      deref
      $.client/value)


  ;; See the [[result]] function in this namespace for an example of error handling.
  ;;
  (-> ($.client/query c
                      ($.cell/address 1)
                      ($.cell/* (inc [])))
      result
      clojure.pprint/pprint)


  ;;
  ;; TRANSACTIONS
  ;;
  ;; Transactions are akin to write operations.
  ;;
  ;; Code is provided in a transaction. Peers emit blocks of transactions and enter a consensus
  ;; phases where they:
  ;;
  ;; - Vote on the order of those blocks of transactions
  ;; - Execute transactions in that order
  ;;
  ;; Because every peers execute transactions in the same order, they produce the same result.
  ;;


  ;; Transactions must be signed by an account and are executed in the context of that account.
  ;;
  ;; It is mandatory: no one else can impersonate you, only you have the private key of your account.
  ;;
  ;; Hence, we borrom to 'key pair' recipe to get a key pair.
  ;;
  (def kp
       ($.recipe.key-pair/retrieve "private/recipe/client"))



  ;; We also need an account.
  ;;
  ;; Let us reuse the 'create-account' recipe, providing our key pair, and then request a bit of coins.
  ;;
  (def addr
       (let [addr ($.recipe.rest/create-account kp)]
         ($.recipe.rest/request-coin+ addr
                                      100000000)
         ($.cell/address addr)))


  ;; Let us confirme out balance using a query first. Should be 100 millions coins;
  ;;
  (-> ($.client/query c
                      addr
                      ($.cell/* *balance*))
      deref
      str)


  ;; When writing a transaction, a 'sequence id' must be provided to avoid replay attacks.
  ;;
  ;; Each account has a number stored on chain which represent the id of the next transaction. It must be
  ;; incremented on each transaction so that is someone tries to duplicate it, it will fail.
  ;;
  ;; We can query it from the network and increment it each time. To make the following examples simpler,
  ;; let us write a function.
  ;;
  (defn seq-id
    []
    (deref ($.client/sequence c
                              addr)))


  ;; Okay, first transaction!
  ;;
  ;; Let us provide:

  ;; - Code to execute: `(def foo 42)` as a cell
  ;; - Address of our account
  ;; - Maching key pair for signing the transaction
  ;; - Right sequence id
  ;;
  (-> ($.client/transact c
                         kp
                         ($.cell/invoke addr
                                        (seq-id)
                                        ($.cell/* (def foo 42))))
      deref
      str)


  ;; And a query to confirm that it worked, let us read `foo` in our account.
  ;;
  (-> ($.client/query c
                      addr
                      ($.cell/* foo))
      deref
      str)


  ;;
  ;; EXAMPLE - Deploying and calling a smart contract
  ;;


  ;; Let us transaction code that deploys an actor (automated account).
  ;;
  ;; Actors are automated accounts used for creating smart contracts.
  ;;
  ;; See comments in CVX file 'project/recipe/src/cvx/main/simple_contract.cvx'
  ;;
  (def my-actor
       (-> ($.client/transact c
                              kp
                              ($.cell/invoke addr
                                             (seq-id)
                                             ($.read/file "project/recipe/src/cvx/main/simple_contract.cvx")))
           deref
           $.client/value))


  ;; Now, let us call our actor.
  ;;
  ;; Transaction code uses `call` so that `(set-value 42)` is executed in the context of the actor.
  ;;
  (-> ($.client/transact c
                         kp
                         ($.cell/invoke addr
                                        (seq-id)
                                        ($.cell/* (call ~my-actor
                                                        (set-value 42)))))
      deref
      str)


  ;; We can query `value` is actor and confirms it is now set to 42.
  ;;
  (-> ($.client/query c
                      my-actor
                      ($.cell/* value))
      deref
      str)


  ;; Let's try to break it!
  ;;
  ;; Contract enforces access control: only creator (our account) can change `value`.
  ;;
  ;; Using a query, let's see what happens if account #1 tried calling the contract. It fails!
  ;;
  (-> ($.client/query c
                      ($.cell/address 1)
                      ($.cell/* (call ~my-actor
                                      (set-value :damn!))))
      deref
      str)


  ;;
  ;; When done, we can close our client.
  ;;
  ($.client/close c)



  )
