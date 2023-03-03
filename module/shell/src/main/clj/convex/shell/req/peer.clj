(ns convex.shell.req.peer

  (:import (convex.peer Server))
  (:require [convex.cell         :as $.cell]
            [convex.clj          :as $.clj]
            [convex.cvm          :as $.cvm]
            [convex.db           :as $.db]
            [convex.std          :as $.std]
            [convex.server       :as $.server]
            [convex.shell.req.kp :as $.shell.req.kp]
            [convex.shell.resrc  :as $.shell.resrc]))


;;;;;;;;;; Private


(defn -do-peer

  ;;

  [ctx peer f]

  (let [[ok?
         x]  ($.shell.resrc/unwrap ctx
                                   peer)]
    (if ok?
      (let [peer-2 x]
        (or (when-not (instance? Server
                                 peer-2)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Not a peer")))
            (f peer-2)))
      (let [ctx-2 x]
        ctx-2))))


;;;;;;;;;; Requests


(defn start

  [ctx [state key-pair host port]]

  (or (when-not ($.std/state? state)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Genesis state required")))
      (when-not ($.std/string? host)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Host must be a String")))
      (when-not ($.db/current)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "An Etch instance must be open")))
      (when-not ($.std/long? port)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Port to open must be a Long")))
      (let [port-2 ($.clj/long port)]
        (or (when-not (<= 1
                          port-2
                          65535)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Port to open must be >= 1 and <= 65535")))
            ($.shell.req.kp/do-kp ctx
                                  key-pair
                                  (fn [key-pair-2]
                                    (let [[ok?
                                           x]  (try
                                                 [true
                                                  ($.server/create key-pair-2
                                                                   {:convex.server/bind ($.clj/string host)
                                                                    :convex.server/db   ($.db/current)
                                                                    :convex.server/port port-2
                                                                    })]
                                                 (catch Throwable _ex
                                                   [false
                                                    ($.cvm/exception-set ($.cell/* :SHELL.PEER)
                                                                         ($.cell/* "Unable to create peer, check input parameters"))]))]
                                      (if ok?
                                        (let [server x]
                                          (try
                                            ;;
                                            ($.server/start server)
                                            ($.cvm/result-set ctx
                                                              ($.shell.resrc/create server))
                                            ;;
                                            (catch Throwable _ex
                                              ($.cvm/exception-set ctx
                                                                   ($.cell/* :SHELL.PEER)
                                                                   ($.cell/* "Unable to start peer server, check input parameters")))))
                                        (let [ctx-2 x]
                                          ctx-2)))))))))



(defn stop

  [ctx [peer]]

  (-do-peer ctx
            peer
            (fn [peer-2]
              (try
                ;;
                ($.server/stop peer-2)
                ($.cvm/result-set ctx
                                  nil)
                ;;
                (catch Exception _ex
                  ($.cvm/exception-set ctx
                                       ($.cell/* :SHELL.SERVER)
                                       ($.cell/* "Unable to stop peer")))))))
