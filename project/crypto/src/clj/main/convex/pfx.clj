(ns convex.pfx

  ""

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

  ""


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

  ""


  (^KeyStore [path]

   (load path
         nil))


  (^KeyStore [^String path passphrase]

   (PFXTools/loadStore (File. path)
                       passphrase)))



(defn save

  ""

  ^KeyStore

  [^KeyStore key-store]

  key-store)


;;;;;;;;;; Adding and retrieving keys


(defn key-pair-get

  ""

  ^AKeyPair

  [^KeyStore key-store alias-or-account-key passphrase]

  (PFXTools/getKeyPair key-store
                       (if (string? alias-or-account-key)
                         alias-or-account-key
                         (.toHexString ^AccountKey alias-or-account-key))
                       passphrase))



(defn key-pair-set

  ""

  ^KeyStore

  [^KeyStore key-store ^AKeyPair key-pair passphrase]

  (PFXTools/saveKey key-store
                    key-pair
                    passphrase))
