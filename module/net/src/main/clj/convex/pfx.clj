(ns convex.pfx

  "Creating and managing a key store for storing key pairs in a file.

   Key pairs are indexed by alias (string). A store may be protected by a passphrase (optional).
   Additionally, each key pair is protected by its own passphrase (mandatory).
  
   See [[convex.key-pair]] about key pairs."

  {:author "Adam Helinski"}

  (:import (convex.core.crypto AKeyPair
                               PFXTools)
           (convex.core.data AccountKey)
           (java.io File)
           (java.security KeyStore))
  (:refer-clojure :exclude [load]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Key store lifeycle


(defn create

  "Creates a new key store in the file under `path`.
  
   An optional passphrase protecting the store may be provided."


  (^KeyStore [path]

   (create path
           nil))


  (^KeyStore [^String path passphrase]

   (PFXTools/createStore (let [file (File. path)]
                           (-> file
                               .getParentFile
                               .mkdirs)
                           file)
                         passphrase)))



(defn load

  "Loads a key store from the file under `path`.
  
   Passphrase must be provided if the store is protected by one."


  (^KeyStore [path]

   (load path
         nil))


  (^KeyStore [^String path passphrase]

   (PFXTools/loadStore (File. path)
                       passphrase)))



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
                       passphrase)))


;;;;;;;;;; Adding and retrieving keys


(defn alias+

  "Returns a sequence of aliases available in the given `key-store`."

  [^KeyStore key-store]

  (enumeration-seq (.aliases key-store)))



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
  
   See [[key-pair-get]]."


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
