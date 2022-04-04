(ns convex.run.nrepl

  ""

  {:author "Adam Helinski"}

  (:require [nrepl.core       :as nrepl]
            [nrepl.misc]
            [nrepl.server]
            [nrepl.transport]))


;;;;;;;;;;


(defn client

  ""

  []

  (nrepl/connect :port 47896))


(defn handler [message]
  (tap> [:message message])
  (if-some [response (case (message :op)
                       "clone"
                       {:new-session "0"
                        :status      :done}

                       "close"
                       {:status :done}

                       "describe"
                       {:ops    {"clone"    {}
                                 "describe" {}
                                 "eval"     {}}
                        :status :done}

                       "eval"
                       {:status :done
                        :value  (str 42)}

                       nil)]
    (do
      (tap> [:response response])
      (nrepl.transport/send (message :transport)
                            (nrepl.misc/response-for message
                                                     response)))
    (nrepl.server/unknown-op message)))



(defn server

  ""

  []

  (nrepl.server/start-server :port    47897
                             :handler (fn [message]
                                        (handler message))))

;;;;;;;;;;


(comment


  (def s (server))
  (.close s)


  (with-open [transport (nrepl/connect :port 14563)]
    (-> transport
        (nrepl/client 1000)
        (nrepl/message {:op "describe"})
        ; (nrepl/message {:op   "eval"
        ;                 :code "(+ 2 2)"})
        doall))

       




  )
