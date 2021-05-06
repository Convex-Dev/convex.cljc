(ns convex.lisp.edn

  "Translating Convex objects into EDN and reading them back."

  {:author "Adam Helinski"}

  (:require [clojure.tools.reader.edn])
  #?(:clj (:import convex.core.data.ACell))
  #?(:clj (:refer-clojure :exclude [read])))


;;;;;;;;;;


(defn read

  "Reads a string of Convex form expressed as EDN.
  
   Opposite of [[to-edn]]."

  [string]

  (clojure.tools.reader.edn/read-string {:readers {'account    (fn [account]
                                                                 [:convex/account
                                                                  account])
                                                   'addr       (fn [address]
                                                                 (symbol (str "#"
                                                                              address)))
                                                   'blob       (fn [blob]
                                                                 ;;
                                                                 ;; TODO. Cannot easily convert to hexstring, see #63.
                                                                 ;;
                                                                 (list 'blob
                                                                       blob))
                                                   'context    (fn [ctx]
                                                                 [:convex/ctx
                                                                  ctx])
                                                   'expander   (fn [expander]
                                                                 [:convex/expander
                                                                  expander])
                                                   'signeddata (fn [hash]
                                                                 [:convex/signed-data
                                                                  hash])
                                                   'syntax     (fn [{:keys [datum]
                                                                     mta   :meta}]
                                                                 (if (and (seq mta)
                                                                          (not (second mta))
                                                                          (nil? (get mta
                                                                                     :start)))
                                                                   (list 'syntax
                                                                         datum
                                                                         mta)
                                                                   datum))}}
                                        string))



#?(:clj (defn write

  "Translates a Convex form into an EDN string.
  
   Opposite of [[read-edn]]."
  
  [^ACell form]

  (.ednString form)))
