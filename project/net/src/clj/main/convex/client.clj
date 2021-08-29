(ns convex.client

  ""

  {:author "Adam Helinski"}

  (:import (convex.api Convex)
           (convex.core.crypto AKeyPair)
           (convex.core.data Address)
           (convex.peer Server)
           (java.net InetSocketAddress))
  (:refer-clojure :exclude [sequence]))


;;;;;;;;;; Lifecycle


(defn close

  ""

  [^Convex connection]

  (.close connection))



(defn connect

  ""


  ([]

   (connect "convex.world"
            43579))


  ([host]

   (connect host
            Server/DEFAULT_PORT))


  ([^String host ^long port]

   (Convex/connect (InetSocketAddress. host
                                       port))))


;;;;;;;;;; Getters / Setters


(defn address

  ""

  ;; Resets sequence.

  ^Address

  [^Convex client]

  (.getAddress client))



(defn address-set

  ""

  ^Convex

  [^Convex client ^Address address]

  (.setAddress client
               address)
  client)
  


(defn key-pair-set

  ""

  ^Convex

  [^Convex client ^AKeyPair key-pair]

  (.setKeyPair client
               key-pair)
  client)



(defn key-pub

  ""

  ^AKeyPair

  [^Convex client]

  (.getAccountKey client))



(defn sequence

  ""

  ;; Does a query if sequence is null.

  [^Convex client]

  (.getSequence client))



(defn sequence-set

  ""

  ^Convex

  [^Convex client sequence]

  (.setNextSequence client
                    (inc sequence))
  client)


;;;;;;;;;; Network



