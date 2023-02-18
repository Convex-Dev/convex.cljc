(ns convex.shell.req.pfx

  "Requests relating to PFX stores for key pairs."

  (:import (java.security KeyStore))
  (:refer-clojure :exclude [load])
  (:require [convex.cell         :as $.cell]
            [convex.clj          :as $.clj]
            [convex.cvm          :as $.cvm]
            [convex.pfx          :as $.pfx]
            [convex.shell.req.kp :as $.shell.req.kp]
            [convex.shell.resrc  :as $.shell.resrc]
            [convex.std          :as $.std]))


;;;;;;;;;; Private


(defn -ensure-alias

  [ctx alias]

  (when-not ($.std/string? alias)
    ($.cvm/exception-set ctx
                         ($.cell/code-std* :ARGUMENT)
                         ($.cell/* "Alias must be a String"))))



(defn -ensure-passphrase

  [ctx passphrase]

  (when-not ($.std/string? passphrase)
    ($.cvm/exception-set ctx
                         ($.cell/code-std* :ARGUMENT)
                         ($.cell/* "Passphrase must be a String"))))



(defn -ensure-path

  [ctx path]

  (when-not ($.std/string? path)
    ($.cvm/exception-set ctx
                         ($.cell/code-std* :ARGUMENT)
                         ($.cell/* "Alias must be a String"))))


;;;;;;;;;; Helpers


(defn do-store

  "Unwraps a PFX store from a resource."

  [ctx store f]
  
  (let [[ok?
         x]  ($.shell.resrc/unwrap ctx
                                   store)]
    (if ok?
      (let [store-2 x]
        (if (instance? KeyStore
                       store-2)
          (f store-2)
          ($.cvm/exception-set ($.cell/code-std* :ARGUMENT)
                               ($.cell/* "Not a PFX store"))))
      (let [ctx-2 x]
        ctx-2))))


;;;;;;;;;;


(defn create

  "Request for creating a new store."

  [ctx [path passphrase]]

  (or (when-not ($.std/string? path)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Path to PFX store must be a String")))
      (-ensure-passphrase ctx
                          passphrase)
      (try
        ($.cvm/result-set ctx
                          ($.shell.resrc/create ($.pfx/create ($.clj/string path)
                                                              (when passphrase
                                                                ($.clj/string passphrase)))))
        (catch Exception _ex
          ($.cvm/exception-set ctx
                               ($.cell/* :SHELL.PFX)
                               ($.cell/* "Unable to create PFX store (is the path correct?)"))))))



(defn kp-get

  "Request for retrieving a key pair from a store."

  [ctx [store alias passphrase]]

  (or (-ensure-alias ctx
                     alias)
      (-ensure-passphrase ctx
                          passphrase)
      (do-store ctx
                store
                (fn [store-2]
                  (try
                    ($.cvm/result-set ctx
                                      ($.shell.resrc/create
                                        ($.pfx/key-pair-get store-2
                                                            ($.clj/string alias)
                                                            ($.clj/string passphrase))))
                    (catch Exception _ex
                      ($.cvm/exception-set ctx
                                           ($.cell/* :SHELL.PFX)
                                           ($.cell/* "Unable to retrieve key pair with given alias and passphrase"))))))))



(defn kp-set

  "Request for adding a key pair to a store."

  [ctx [store alias kp passphrase]]

  (or (-ensure-alias ctx
                     alias)
      (-ensure-passphrase ctx
                          passphrase)
      (do-store ctx
                store
                (fn [store-2]
                  ($.shell.req.kp/do-kp
                    ctx
                    kp
                    (fn [kp-2]
                      (try
                        ;;
                        ($.pfx/key-pair-set store-2
                                            ($.clj/string alias)
                                            kp-2
                                            ($.clj/string passphrase))
                        ($.cvm/result-set ctx
                                          store)
                        ;;
                        (catch Exception _ex
                          ($.cvm/exception-set ctx
                                               ($.cell/* :SHELL.PFX)
                                               ($.cell/* "Unable to set key pair"))))))))))



(defn load

  "Request for loading an existing store from a file."

  [ctx [path passphrase]]

  (or (-ensure-path ctx
                    path)
      (-ensure-passphrase ctx
                          passphrase)
      (try
        ($.cvm/result-set ctx
                          ($.shell.resrc/create ($.pfx/load ($.clj/string path)
                                                            ($.clj/string passphrase))))
        (catch Exception _ex
          ($.cvm/exception-set ctx
                               ($.cell/* :SHELL.PFX)
                               ($.cell/* "Unable to open PFX store at the given path"))))))



(defn save

  "Request for saving a store to a file."

  [ctx [store path passphrase]]

  (or (-ensure-path ctx
                    path)
      (-ensure-passphrase ctx
                          passphrase)
      (do-store ctx
                store
                (fn [store-2]
                  (try
                    ;;
                    ($.pfx/save store-2
                                ($.clj/string path)
                                ($.clj/string passphrase))
                    ($.cvm/result-set ctx
                                      store)
                    ;;
                    (catch Exception _ex
                      ($.cvm/exception-set ctx
                                           ($.cell/* :SHELL.PFX)
                                           ($.cell/* "Unable to save PFX store (is the path correct?)"))))))))
