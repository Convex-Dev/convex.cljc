(ns convex.pfx

  "Creating and managing a key store for storing key pairs in a file.
  
   See [[convex.sign]] about key pairs."

  {:author "Adam Helinski"}

  (:import (convex.core.crypto AKeyPair
                               PFXTools)
           (convex.core.data AccountKey)
           (java.io File
                    FileNotFoundException
                    IOException)
           (java.security KeyStore))
  (:refer-clojure :exclude [load]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Key store lifeycle


(defn create

  "Creates a new key store in the file under `path`.
  
   An optional passphrase protecting the store may be provided.

   Returns nil if a file already exists at this path to prevent accidental overwriting
   since it could lead to a dramatic loss of key pairs."


  (^KeyStore [path]

   (create path
           nil))


  (^KeyStore [^String path passphrase]

   (when-not (.exists (File. path))
     (PFXTools/createStore (let [file (File. path)]
                             (-> file
                                 .getParentFile
                                 .mkdirs)
                             file)
                           passphrase))))



(defn load

  "Loads a key store from the file under `path`.
  
   Passphrase must be provided if the store is protected by one.

   Returns nil when the file is not found.
   Throws an exception (interpreting it as a store fails, wrong passphrase, etc).

   See [[create]]."


  (^KeyStore [path]

   (load path
         nil))


  (^KeyStore [^String path passphrase]

   (try
     (PFXTools/loadStore (File. path)
                         passphrase)
     (catch FileNotFoundException _ex
       nil))))



(defn open

  "Opens a key store at the file under `path`, creating it if needed.

   Convenient helper which successively tries `load` and `create` `if required.

   When a process should merely not create a store but merely use one, `load` is preferred."

  (^KeyStore [path]

   (open path
         nil))


  (^KeyStore [path passphrase]

   (let [store (or (load path
                         passphrase)
                   (create path
                           passphrase))]
     (when-not store
       (throw (IOException. "Unable to retrieve or create key store ; ensure path is accessible")))
     store)))



(defn save

  "Saves the given `key-store` to the file under `path`.
  
   An optional passphrase protecting the store may be provided."


  (^KeyStore [key-store path]

   (save key-store
         path
         nil))


  (^KeyStore [key-store ^String path ^String passphrase]

   (PFXTools/saveStore key-store
                       (File. path)
                       passphrase)
   key-store))


;;;;;;;;;; Adding and retrieving keys


(defn key-pair-get

  "Retrieves a key pair from the given `key-store`.

   See [[key-pair-set]]."

  ^AKeyPair

  [^KeyStore key-store alias-or-account-key passphrase]

  (PFXTools/getKeyPair key-store
                       (if (string? alias-or-account-key)
                         alias-or-account-key
                         (.toHexString ^AccountKey alias-or-account-key))
                       passphrase))



(defn key-pair-set

  "Adds the given `key-pair` to the `key-store`, protected by a mandatory `passphrase`.

   Public key is used as `alias` if none is provided.
  
   See [[key-pair-set]]."


  (^KeyStore [^KeyStore key-store ^AKeyPair key-pair passphrase]

   (PFXTools/setKeyPair key-store
                        key-pair
                        passphrase))


  (^KeyStore [^KeyStore key-store ^String alias ^AKeyPair key-pair passphrase]

   (PFXTools/setKeyPair key-store
                        alias
                        key-pair
                        passphrase)
   key-store))



(comment

  (def ks (open "/tmp/test_cvx.pfx" "password-key-store"))

  (key-pair-get ks "my-key-pair" "password-key-paidsr")

         )
