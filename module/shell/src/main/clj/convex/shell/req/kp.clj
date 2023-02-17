(ns convex.shell.req.kp

  "Requests relating to key pairs.
  
   Key pairs are represented as resources (see [[convex.shell.resrc]])."

  (:require [convex.cell        :as $.cell]
            [convex.cvm         :as $.cvm]
            [convex.key-pair    :as $.key-pair]
            [convex.shell.resrc :as $.shell.resrc]
            [convex.std         :as $.std]))


;;;;;;;;;; Private


(defn- -do-kp

  ;; Unwraps a key pair kept

  [ctx kp f]

  (let [[ok?
         x]  ($.shell.resrc/unwrap ctx
                                   kp)]
    (if ok?
      (let [kp-2 x]
        (if ($.key-pair/key-pair? kp-2)
          (f kp-2)
          ($.cvm/exception-set ($.cell/code-std* :ARGUMENT)
                               ($.cell/* "Not a key pair"))))
      (let [ctx-2 x]
        ctx-2))))


;;;;;;;;;; Public


(defn create

  "Request for creating a key pair from a random seed."

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.shell.resrc/create ($.key-pair/ed25519))))



(defn create-from-seed

  "Request for creating a key pair from a given seed."

  [ctx [seed]]

  (or (when-not (and ($.std/blob? seed)
                     (= ($.std/count seed)
                        32))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Seed must be a 32-byte Blob")))
      ($.cvm/result-set ctx
                        ($.shell.resrc/create ($.key-pair/ed25519 seed)))))



(defn public-key

  "Request for retrieving the public key of the given key pair."

  [ctx [kp]]

  (-do-kp ctx
          kp
          (fn [kp-2]
            ($.cvm/result-set ctx
                              ($.key-pair/account-key kp-2)))))



(defn seed

  "Request for retrieving the seed of the given key pair."

  [ctx [kp]]

  (-do-kp ctx
          kp
          (fn [kp-2]
            ($.cvm/result-set ctx
                              ($.key-pair/seed kp-2)))))
