(ns convex.test.key-pair
  
  "Testing `convex.key-pair`."

  {:author "Adam Helinski"}

  (:import (convex.core.data AccountKey))
  (:refer-clojure :exclude [keys])
  (:require [clojure.test    :as T]
            [convex.cell     :as $.cell]
            [convex.key-pair :as $.key-pair]))


;;;;;;;;;; Setup


(def kp
     ($.key-pair/ed25519))


;;;;;;;;;; Tests


(T/deftest account-key

  (T/is (instance? AccountKey
                   ($.key-pair/account-key kp))))



(T/deftest hex-string

  (T/is (= (.substring (str ($.key-pair/account-key kp))
                       2)
           ($.key-pair/hex-string kp))))



(T/deftest keys

  (T/is (= kp
           ($.key-pair/ed25519 ($.key-pair/key-public kp)
                               ($.key-pair/key-private kp)))
        "Extract pub and priv keys and recreate key pair"))



(T/deftest seed

  (T/is (= kp
           ($.key-pair/ed25519 ($.key-pair/seed kp)))))



(T/deftest sign+verify

  (let [cell   ($.cell/* [1 2 3])
        signed ($.key-pair/sign kp
                                cell)]
    (T/is (= ($.key-pair/signed->account-key signed)
             ($.key-pair/account-key kp)))
    (T/is (= ($.key-pair/signed->cell signed)
             cell))
    (T/is ($.key-pair/verify ($.key-pair/account-key kp)
                             ($.key-pair/signed->signature signed)
                             cell))))



(T/deftest verify-hash

  (let [hash   ($.cell/hash ($.cell/* [1 2 3]))
        signed ($.key-pair/sign kp
                                hash)]
    (T/is ($.key-pair/verify ($.key-pair/account-key kp)
                             ($.key-pair/signed->signature signed)
                             hash))))
