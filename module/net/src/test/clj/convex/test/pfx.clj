(ns convex.test.pfx

  "Testing `convex.pfx`."

  {:author "Adam Helinski"}

  (:import (java.io File))
  (:refer-clojure :exclude [alias])
  (:require [clojure.test    :as T]
            [convex.key-pair :as $.key-pair]
            [convex.pfx      :as $.pfx]))


;;;;;;;;;; Setup


(def alias
     "alias")


(def kp
     ($.key-pair/ed25519))



(def passphrase-kp
     "pass-kp")



(def passphrase-ks
     "pass-kp")



(def path
     (-> (File/createTempFile "foo"
                              nil)
         .getCanonicalPath))


;;;;;;;;;; Tests


(T/deftest main

  (T/is (= kp
           (-> ($.pfx/create path
                             passphrase-ks)
               ($.pfx/key-pair-set alias
                                   kp
                                   passphrase-kp)
               ($.pfx/save path
                           passphrase-ks)
               ($.pfx/key-pair-get alias
                                   passphrase-kp)))
        "Adding a key to a new store and saving it")

  (T/is (= kp
           (-> ($.pfx/load path
                           passphrase-ks)
               ($.pfx/key-pair-get alias
                                   passphrase-kp)))
        "Loading store and retrieving key")

  (T/is (thrown? Throwable
                 ($.pfx/load path
                             "bad"))
        "Cannot load store with wrong passphrase")

  (T/is (thrown? Throwable
                 (-> ($.pfx/load path
                                 passphrase-ks)
                     ($.pfx/key-pair-get alias
                                         "bad")))
        "Cannot retrieve key with wrong passphrase"))
