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
  ;; Code is provided as a cell and executed by the peer but it does not require any consensus.
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
                      ($.read/string "(+ 2 2)"))
      deref)


  ;; A result is an object. The API provides functions for checking if an error occured,
  ;; getting the value, etc.
  ;;
  ;; This example query information about account #2.
  ;;
  (-> ($.client/query c
                      ($.cell/address 2)
                      ($.cell/* (account *address*)))
      deref
      $.client/value)


  ;; Error! Cannot increment a vector.
  ;;
  ;; See the [[result]] function in this namespace for an example of error handling.
  ;;
  (-> ($.client/query c
                      ($.cell/address 1)
                      ($.cell/* (inc [])))
      deref)


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
      deref)


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
                                        ($.read/string "(def foo 42)")))
      deref)


  ;; And a query to confirm that it worked, let us read `foo` in our account.
  ;;
  (-> ($.client/query c
                      addr
                      ($.cell/* foo))
      deref)


  ;;
  ;; EXAMPLE - Deploying and calling a smart contract
  ;;
  ;; An "actor" is an account that does not have a key pair. It means no one
  ;; is able to issue transactions for that account.
  ;;
  ;; Code is provided when the actor is created and this code determines everything
  ;; this actor can do.
  ;;
  ;; Like any account, actors can define some values. It can also provide functions,
  ;; an interface for changing those values. Hence, actors are the very basis of
  ;; smart contracts: they can host some arbitrary state and define in code how this
  ;; state is managed.
  ;;


  ;; First, let us deploy our actor in a transaction.
  ;;
  ;; This actor hosts `value` and 2 functions. Difference is explored below.
  ;;
  ;; See code and comments in CVX file 'project/recipe/src/cvx/main/simple_contract.cvx'
  ;;
  (def my-actor
       (-> ($.client/transact c
                              kp
                              ($.cell/invoke addr
                                             (seq-id)
                                             ($.read/file "project/recipe/src/cvx/main/simple_contract.cvx")))
           deref
           $.client/value))


  ;; First, let us apply its `set-value` function like any other function, with the only
  ;; difference it comes from another account.
  ;;
  ;; `lookup` is a way for retrieving a value by providing an address and a symbol.
  ;;
  (-> ($.client/transact c
                         kp
                         ($.cell/invoke addr
                                        (seq-id)
                                        ($.cell/* ((lookup ~my-actor
                                                           set-value) 42))))
      deref)


  ;; By querying `value` in our own account, we can notice that our previous transaction was
  ;; executed in the context of our account.
  ;;
  (-> ($.client/query c
                      addr
                      ($.cell/symbol "value"))
      deref)


  ;; Now, let us "call" the `set-value-in-actor` function. It has a special metadata `:callable?`
  ;; set to true and we will use `call` to apply it.
  ;;
  (-> ($.client/transact c
                         kp
                         ($.cell/invoke addr
                                        (seq-id)
                                        ($.cell/* (call ~my-actor
                                                        (set-value-in-actor 100)))))
      deref)


  ;; By quering `value` in the actor account, not our own, we can confirm that that our previous
  ;; transaction was executed in the context of the actor, not our account.
  ;;
  ;; Effectively, `set-value-in-actor` is a special function that allows us to pass execution on the
  ;; actor for the duration of the function.
  ;;
  ;; This is a very powerful construct: an actor can hold some state which can only be managed via
  ;; well-defined functions.
  ;;
  (-> ($.client/query c
                      my-actor
                      ($.cell/symbol "value"))
      deref)


  ;; Let's try to break it!
  ;;
  ;; `set-value-in-actor` enforces access control: only creator (our account) can change `value`.
  ;;
  ;; Using a query, let's see what happens if account #1 tried calling the contract. It fails!
  ;;
  (-> ($.client/query c
                      ($.cell/address 1)
                      ($.cell/* (call ~my-actor
                                      (set-value-in-actor :damn!))))
      deref)


  ;;
  ;; This is crucial to understand smart contracts: they are one or several actors, hosting some
  ;; arbitrary state and enforcing rules written as "callable" functions.
  ;;
  ;; Any smart contract, simple or complex, is built on this idea.
  ;;


  ;;
  ;; When done, we can close our client.
  ;;
  ($.client/close c)



  )
