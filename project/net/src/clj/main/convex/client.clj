(ns convex.client

  ""

  {:author "Adam Helinski"}

  (:import (convex.api Convex)
           (convex.peer Server)
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


  ([host]

   (connect host
            Server/DEFAULT_PORT))


  ([^String host ^long port]

   (Convex/connect (InetSocketAddress. host
                                       port))))

