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
           ""))


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
         ""))


  (^KeyStore [^String path passphrase]

   (PFXTools/loadStore (File. path)
                       passphrase)))



(defn save

  ""


  (^KeyStore [key-store path]

   (save key-store
         path
         ""))


  (^KeyStore [key-store ^String path ^String passphrase]

   (PFXTools/saveStore key-store
                       (File. path)
                       passphrase)))


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
