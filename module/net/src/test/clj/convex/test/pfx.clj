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
         (.getCanonicalPath)))


;;;;;;;;;; Tests


(T/deftest alias+

  (T/is (= #{"bar"
             "foo"}
           (-> ($.pfx/create path)
               ($.pfx/key-pair-set "foo"
                                   ($.key-pair/ed25519)
                                   "passphrase")
               ($.pfx/key-pair-set "bar"
                                   ($.key-pair/ed25519)
                                   "passphrase")
               ($.pfx/alias+)
               (set)))))



(T/deftest key-pair-get

  (let [store ($.pfx/create path)]
    ($.pfx/key-pair-set store
                        "foo"
                        kp
                        "passphrase")
    (T/is (= kp
             ($.pfx/key-pair-get store
                                 "foo"
                                 "passphrase"))
          "Retrieve key")

    (T/is (nil? ($.pfx/key-pair-get store
                                    "bar"
                                    "passphrase"))
          "Inexistent alias returns nil")))



(T/deftest key-pair-rm

  (let [store (-> ($.pfx/create path)
                  ($.pfx/key-pair-set "foo"
                                      ($.key-pair/ed25519)
                                      "passphrase")
                  ($.pfx/key-pair-set "bar"
                                      ($.key-pair/ed25519)
                                      "passphrase"))]

    (T/is (= store
             ($.pfx/key-pair-rm store
                                "foo"))
          "Remove key pair")

    (T/is (nil? ($.pfx/key-pair-get store
                                    "foo"
                                    "passphrase"))
          "Key pair has been removed")))



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
