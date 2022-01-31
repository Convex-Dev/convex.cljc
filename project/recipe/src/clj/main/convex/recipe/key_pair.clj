(ns convex.recipe.key-pair

  "Key pairs are essential, they are used by clients to sign transactions and by peers to sign
   blocks of transactions.

   This example shows how to create a key pair and store it securely in a PFX file.

   Storing key pairs is always a sensitive topic. A PFX file allows storing one or several key pairs where
   each is given a alias and protected by a dedicated password. The file itself can be protected by a
   password as well.

   Creating and handling Ed25519 key pairs is done using namespace `convex.sign`.

   Creating and handling PFX iles is done using namespace `convex.pfx`.

   More information about public-key cryptography: https://en.wikipedia.org/wiki/Public-key_cryptography"

  {:author "Adam Helinski"}

  (:require [convex.pfx  :as $.pfx]
            [convex.sign :as $.sign]))


;;;;;;;;;; One plausible example


(defn retrieve

  "Tries to load the key pair from the key store under `path`.

   Store is created with a new key pair if necessary.

   Returns the key pair, either found or created."

  [path password-store alias-key-pair password-key-pair]

  (let [store ($.pfx/open path
                          password-store)]
    (or ($.pfx/key-pair-get store
                            alias-key-pair
                            password-key-pair)
        (let [key-pair ($.sign/ed25519)]
          (-> store
              ($.pfx/key-pair-set alias-key-pair
                                  key-pair
                                  password-key-pair)
              ($.pfx/save path
                          password-store))
          key-pair))))


;;;;;;;;;;


(comment

  
  ;; Example directory where key pair will be stored.
  ;;
  (def path
       "private/recipe/key-pair/keystore.pfx")

  
  ;; The first time, the key pair is generated and stored in a PFX file.
  ;;
  ;; Each subsequent time, the key pair is always retrieved from that file.
  ;;
  ;; Deleting that 'keystore.pfx' file in this directory will lose it forever.
  ;;
  (retrieve path
            "passwd-store"
            "my-key-pair"
            "passwd-key-pair")

  )
