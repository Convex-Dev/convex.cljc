(ns convex.recipe.key-pair

  "Key pairs are essential, they are used by clients to sign transactions and by peers to sign
   blocks of transactions.

   This example shows how to create a key pair and store it securely in a PFX file.

   Storing key pairs is always a sensitive topic. A PFX file allows storing one or several key pairs where
   each is given a alias and protected by a dedicated password. The file itself can be protected by a
   password as well.

   Creating and handling Ed25519 key pairs is done using namespace [[convex.sign]].

   Creating and handling PFX iles is done using namespace [[convex.pfx]].

   More information about public-key cryptography: https://en.wikipedia.org/wiki/Public-key_cryptography"

  {:author "Adam Helinski"}

  (:require [convex.key-pair :as $.key-pair]
            [convex.pfx      :as $.pfx]))


;;;;;;;;;; One plausible example


(defn retrieve

  "Tries to load the key pair from file 'keystore.pfx' in given `dir`.

   When not found, generates a new key pair and saves it in a new file. A real application might require more sophisticated error handling.

   Returns the key pair."

  [dir]

  (let [file-key-store (str dir
                            "/keystore.pfx")]
    (try

      (-> ($.pfx/load file-key-store)
          ($.pfx/key-pair-get "my-key-pair"
                              "my-password"))

      (catch Throwable _ex
        (let [key-pair ($.key-pair/ed25519)]
          (-> ($.pfx/create file-key-store)
              ($.pfx/key-pair-set "my-key-pair"
                                  key-pair
                                  "my-password")
              ($.pfx/save file-key-store))
          key-pair)))))


;;;;;;;;;;


(comment

  
  ;; Example directory where key pair will be stored.
  ;;
  (def dir
       "private/recipe/key-pair/")

  
  ;; The first time, the key pair is generated and stored in a PFX file.
  ;;
  ;; Each subsequent time, the key pair is always retrieved from that file.
  ;;
  ;; Deleting that 'keystore.pfx' file in this directory will lose it forever.
  ;;
  (retrieve dir)


  ;;
  ;; Let us inspect the core ideas in `retrieve`.
  ;;


  ;; Generating a key pair.
  ;;
  (def key-pair
       ($.key-pair/ed25519))


  ;; File for storing our "key store" capable of securely hosting one or several key pairs.
  ;;
  (def file
       (str dir
            "/keystore.pfx"))


  ;; We create a file for our "key store" capable of securely hosting one or several key pairs,
  ;; our key pair under an alias and protected by a password. Lastly, the updated key store is
  ;; saved to the file.
  ;;
  (-> ($.pfx/create file)
      ($.pfx/key-pair-set "my-key-pair"
                          key-pair
                          "my-password")
      ($.pfx/save file))


  ;; At any later time, we can load that file and retrieve our key pair by providing its alias and
  ;; its password.
  ;;
  (= key-pair
     (-> ($.pfx/load file)
         ($.pfx/key-pair-get "my-key-pair"
                             "my-password")))


  ;;
  ;; Although not mandatory, it is a good idea also specifying a password for the key store itself
  ;; when using [[$.pfx/create]], [[$.pfx/save]], and [[$.pfx/load]].
  ;;  


  )
