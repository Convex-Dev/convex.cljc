(ns convex.test.sign
  
  "Testing `convex.sign`."

  {:author "Adam Helinski"}

  (:import (convex.core.data AccountKey
                             SignedData))
  (:refer-clojure :exclude [keys])
  (:require [clojure.test :as T]
            [convex.read  :as $.read]
            [convex.sign  :as $.sign]))


;;;;;;;;;; Setup


(def kp
     ($.sign/ed25519-gen))


;;;;;;;;;; Tests


(T/deftest account-key

  (T/is (instance? AccountKey
                   ($.sign/account-key kp))))



(T/deftest keys


  (T/is (= kp
           ($.sign/ed25519-from ($.sign/key-public kp)
                                ($.sign/key-private kp)))
        "Extract pub and priv keys and recreate key pair"))



(T/deftest seed

  (T/is (= kp
           ($.sign/ed25519-from ($.sign/seed kp)))))



(T/deftest signed

  (T/is (instance? SignedData
                   ($.sign/signed kp
                                  ($.read/string "{:a :b}")))))


