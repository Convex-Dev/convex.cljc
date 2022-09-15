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
      (let[error-code ($.client/result->error-code resp)
           value      ($.client/result->value resp)]
       (if error-code
         {:error?     true
          :error-code error-code
          :message    value
          :trace      ($.client/result->trace resp)}
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
  ;; Also see [[convex.recipe.db]] for explanations.
  ;;
  ($.db/current-set ($.db/open "private/recipe/client/db.etch"))


  ;; Let us create a client and connect to `convex.world` (current testnet).
  ;;
  (def client
       ($.client/connect {:convex.server/host "convex.world"
                          :convex.server/port 18888}))


  ;;
  ;; QUERIES
  ;;
  ;; Queries are akin to read operations.
  ;;
  ;; Code is provided as a cell and executed by the peer but it does not require any consensus.
  ;; Hence execution is super fase and any change in the state is simply discarded.
  ;;

  ;; Query requesting a result for `(+ 2 2)`
  ;;
  ;; Any address can be provided, nothing needs to be signed. This is just a read operation.
  ;;
  ;; A future is returned which resolves to a result.
  ;;
  (-> ($.client/query client
                      ($.cell/address 1)
                      ($.cell/* (+ 2 2)))
      (deref))


  ;; A result is an object. The API provides functions for checking if an error occured,
  ;; getting the value, etc.
  ;;
  ;; This example query information about account #2.
  ;;
  (-> ($.client/query client
                      ($.cell/address 2)
                      ($.cell/* (account *address*)))
      (deref)
      ($.client/result->value))


  ;; Error! Cannot increment a vector.
  ;;
  ;; See the [[result]] function in this namespace for an example of error handling.
  ;;
  (-> ($.client/query client
                      ($.cell/address 1)
                      ($.cell/* (inc [])))
      (deref))


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
  ;; Hence, we can borrow a little helper from [[convex.recipe.key-pair]] that will retrieve our key
  ;; pair from a PFX file or create it if necessary.
  ;;
  (def key-pair
       ($.recipe.key-pair/retrieve "private/recipe/client"))



  ;; We also need an account.
  ;;
  ;; Let us reuse helpers from [[convex.recipe.rest]] to create one by providing the key pair above
  ;; and requesting a bit of coins.
  ;;
  (def addr
       (let [addr ($.recipe.rest/create-account key-pair)]
         ($.recipe.rest/request-coin+ addr
                                      100000000)
         ($.cell/address addr)))


  ;; Let us confirm out balance using a query first. Should be 100 millions coins.
  ;;
  (-> ($.client/query client
                      addr
                      ($.cell/* *balance*))
      (deref))


  ;; When writing a transaction, a 'sequence id' must be provided to avoid replay attacks.
  ;;
  ;; Each account has a number stored on chain representing the id of the next transaction. It must be
  ;; incremented on each transaction so that duplicating a former transction fails.
  ;;
  ;; We can query it from the network and increment it each time. To make the following examples simpler,
  ;; let us write a function.
  ;;
  ;; (In practice it should have error handling)
  ;;
  (defn sequence-id
    []
    (deref ($.client/sequence-id client
                                 addr)))


  ;; Okay, first transaction!
  ;;
  ;; Let us provide:
  ;;
  ;; - Code to execute as a cell
  ;; - Address of our account
  ;; - Maching key pair for signing the transaction
  ;; - Right sequence id
  ;;
  (-> ($.client/transact client
                         key-pair
                         ($.cell/invoke addr
                                        (sequence-id)
                                        ($.cell/* (def foo (inc 41)))))
      (deref))


  ;; And a query to confirm that it worked, let us read freshly defined `foo` in our account.
  ;;
  (-> ($.client/query client
                      addr
                      ($.cell/* foo))
      (deref))


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
  ;; This actor hosts `value` and 2 functions.
  ;; The difference between those functions is explored below.
  ;;
  ;; See code and comments in CVX file 'project/recipe/src/cvx/main/simple_contract.cvx'.
  ;;
  (def my-actor
       (-> ($.client/transact client
                              key-pair
                              ($.cell/invoke addr
                                             (sequence-id)
                                             (first ($.read/file "module/recipe/src/main/cvx/simple_contract.cvx"))))
           (deref)
           ($.client/result->value)))


  ;; First, let us apply its `set-value` function like any other function, with the only
  ;; difference it comes from another account.
  ;;
  ;; `lookup` is a way for retrieving a value by providing an address and a symbol.
  ;;
  (-> ($.client/transact client
                         key-pair
                         ($.cell/invoke addr
                                        (sequence-id)
                                        ($.cell/* ((lookup ~my-actor
                                                           set-value) 42))))
      (deref))


  ;; By querying `value` in our own account, we can notice that our previous transaction was
  ;; executed in the context of our account.
  ;;
  ;; The `set-value` from our actor was used a library function.
  ;;
  (-> ($.client/query client
                      addr
                      ($.cell/* value))
      (deref))


  ;; Now, let us "call" the `set-value-in-actor` function. It has a special metadata `:callable?`
  ;; set to true allowing to execute if using `call`.
  ;;
  ;; When using `call`, the function is not executed in the context of our own account but rather
  ;; in the context of the function owner.
  ;;
  (-> ($.client/transact client
                         key-pair
                         ($.cell/invoke addr
                                        (sequence-id)
                                        ($.cell/* (call ~my-actor
                                                        (set-value-in-actor 100)))))
      (deref))


  ;; By quering `value` in the actor account, not our own, we can confirm that that our previous
  ;; transaction was indeed executed in the context of the actor.
  ;;
  ;; Effectively, `set-value-in-actor` is a special function that allows us to pass execution on the
  ;; actor for the duration of the function.
  ;;
  ;; This is a very powerful construct: an actor can hold some state which can only be managed via
  ;; well-defined functions. This form the basis of smart contracts: managing arbitrary state following
  ;; rules described in code.
  ;;
  (-> ($.client/query client
                      my-actor
                      ($.cell/* value))
      (deref))


  ;; Let's try to break it!
  ;;
  ;; `set-value-in-actor` enforces access control: only the creator of that actor (our account) can
  ;; change `value`.
  ;;
  ;; Using a query, let's see what happens if account #1 tries calling the contract.
  ;; Spoiler: we get an error back!
  ;;
  (-> ($.client/query client
                      ($.cell/address 1)
                      ($.cell/* (call ~my-actor
                                      (set-value-in-actor :damn!))))
      (deref))


  ;;
  ;; This is crucial to understand smart contracts: they are formed by one or several actors hosting
  ;; some arbitrary state and allowing altering this state via "callable" functions.
  ;;
  ;; Any smart contract, simple or complex, is built on this idea.
  ;;


  ;;
  ;; When done, we can close our client.
  ;;
  ($.client/close client)


  )
