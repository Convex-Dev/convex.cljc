(ns convex.recipe.key-pair

  "Key pairs are essential, they are used by clients to sign transactions and by peers to sign
   blocks of transactions.

   This example shows how to create a key pair and store it securely in a PFX file.

   Storing key pairs is always a sensitive topic. A PFX file allows storing one or several key pairs where
   each is given a alias and protected by a dedicated password. The file itself can be protected by a
   password as well.

   Creating and handling Ed25519 key pairs is done using namespace `convex.sign`.

   Creating and handling PFX iles is done using namespace `convex.pfx`.

   API: https://cljdoc.org/d/world.convex/crypto.clj/0.0.0-alpha0/api/convex"

  {:author "Adam Helinski"}

  (:require [convex.pfx  :as $.pfx]
            [convex.sign :as $.sign]))


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
          ($.pfx/key-pair-get "my-peer"
                              "my-password"))

      (catch Throwable _ex
        (let [key-pair ($.sign/ed25519)]
          (-> ($.pfx/create file-key-store)
              ($.pfx/key-pair-set "my-peer"
                                  key-pair
                                  "my-password")
              ($.pfx/save file-key-store))
          key-pair)))))


;;;;;;;;;;


(comment

  
  ;; The first time, the key pair is generated and stored in a PFX file.
  ;;
  ;; Each subsequent time, the key pair is always retrieved from that file.
  ;;
  ;; Deleting that 'keystore.pfx' file in this directory will lose it forever.
  ;;
  (retrieve "private/recipe/key-pair")


  )
