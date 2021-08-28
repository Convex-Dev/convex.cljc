(ns convex.client

  ""

  {:author "Adam Helinski"}

  (:import (convex.api Convex)
           (java.net InetSocketAddress)))


;;;;;;;;;;


(defn close

  ""

  [^Convex connection]

  (.close connection))



(defn connect

  ""


  ([]

   (connect "convex.world"
            43579))


  ([^String host ^long port]

   (Convex/connect (InetSocketAddress. host
                                       port))))


