(ns convex.shell.req.account

  (:import (convex.core State)
           (convex.core.data AccountStatus))
  (:require [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.std  :as $.std]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(defn control

  [ctx [address-controller address-target]]

  (or (when-not (or (nil? address-controller)
                    (and ($.std/address address-controller)
                         ($.cvm/account ctx
                                        address-controller)))
        ($.cvm/exception-set ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Controller address must be nil or point to an existing account")))
      (if-let [^AccountStatus account-target (and ($.std/address? address-target)
                                                  ($.cvm/account ctx
                                                                 address-target))]
          (-> ctx
              ($.cvm/state-set (-> ^State ($.cvm/state ctx)
                                   (.putAccount address-target
                                                (.withController account-target
                                                                 address-controller))))

              ($.cvm/result-set (.getController account-target)))
          ($.cvm/exception-set ctx
                               ($.cell/code-std* :ARGUMENT)
                               ($.cell/* "Address to control must point to an existing account")))))



(defn switch

  [ctx [address]]

  (or (when-not ($.std/address? address)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Must provide an address for switching to another account")))
      (when-not ($.cvm/account ctx
                               address)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :NOBODY)
                             ($.cell/* "Cannot switch to an inexistent account")))
      (-> ctx
          ($.cvm/fork-to address)
          ($.cvm/result-set address))))
