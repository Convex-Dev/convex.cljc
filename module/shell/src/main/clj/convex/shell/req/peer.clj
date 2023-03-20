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

  ;; Executes `f` only if the user has provided a peer.

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


(defn ^:no-doc -endpoint->map

  [endpoint]

  ($.cell/* {:host ~($.cell/string (endpoint :convex.server/host))
             :port ~($.cell/long (endpoint :convex.server/port))}))



(defn- -init

  ;; Used by the [[init-*]] functions.

  [ctx [key-pair host n-peer poll-delay port root-key url] map-option+]

  (or (when-not ($.std/string? host)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Host must be a String")))
      (when-not ($.std/long? n-peer)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Number of peers must be a Long")))
      (when-not ($.std/long? poll-delay)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Poll delay must be a Long")))
      (when-not ($.db/current)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "An Etch instance must be open")))
      (when-not (or (nil? url)
                    ($.std/string? url))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "URL must be Nil or a String")))
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
                                    (try
                                      ($.cvm/result-set ctx
                                                        ($.shell.resrc/create
                                                          ($.server/create key-pair-2
                                                                           (map-option+ {:convex.server/bind       ($.clj/string host)
                                                                                         :convex.server/db         ($.db/current)
                                                                                         :convex.server/n-peer     ($.clj/long n-peer)
                                                                                         :convex.server/poll-delay (max 0
                                                                                                                        ($.clj/long poll-delay))
                                                                                         :convex.server/port       port-2
                                                                                         :convex.server/root-key   root-key
                                                                                         :convex.server/url        (some-> url 
                                                                                                                           ($.clj/string))}))))
                                      (catch IllegalStateException ex
                                        ($.cvm/exception-set ctx
                                                             ($.cell/* :SHELL.PEER)
                                                             ($.cell/string (.getMessage ex))))
                                      (catch Throwable _ex
                                        ($.cvm/exception-set ctx
                                                             ($.cell/* :SHELL.PEER)
                                                             ($.cell/* "Unable to initialize peer, check input parameters"))))))))))


;;;;;;;;;; Requests


(defn n-belief-received

  [ctx [peer]]

  (-do-peer ctx
            peer
            (fn [peer-2]
              ($.cvm/result-set ctx
                                ($.cell/long ($.server/n-belief-received peer-2))))))



(defn n-belief-sent

  [ctx [peer]]

  (-do-peer ctx
            peer
            (fn [peer-2]
              ($.cvm/result-set ctx
                                ($.cell/long ($.server/n-belief-sent peer-2))))))



(defn connection+

  [ctx [peer]]

  (-do-peer ctx
            peer
            (fn [^Server peer-2]
              ($.cvm/result-set ctx
                                ($.cell/set (or (-> peer-2
                                                    (.getConnectionManager)
                                                    (.getConnections)
                                                    (keys))
                                                []))))))



(defn controller

  [ctx [peer]]

  (-do-peer ctx
            peer
            (fn [peer-2]
              ($.cvm/result-set ctx
                                ($.server/controller peer-2)))))



(defn data

  [ctx [peer]]

  (-do-peer ctx
            peer
            (fn [peer-2]
              ($.cvm/result-set ctx
                                ($.server/data peer-2)))))



(defn endpoint

  [ctx [peer]]

  (-do-peer ctx
            peer
            (fn [peer-2]
              ($.cvm/result-set ctx
                                (-endpoint->map ($.server/endpoint peer-2))))))



(defn init-db

  [ctx arg+]

  (-init ctx
         arg+
         (fn [option+]
           (assoc option+
                  :convex.server/state
                  [:db]))))



(defn init-state

  [ctx [state & arg+]]

  (or (when-not ($.std/state? state)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Genesis state required")))
      (-init ctx
             arg+
             (fn [option+]
               (assoc option+
                      :convex.server/state
                      [:use state])))))



(defn init-sync

  [ctx [remote-host remote-port & arg+]]

  (or (when-not ($.std/string? remote-host)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Remote host must be a String")))
      (when-not ($.std/long? remote-port)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Remote port must be a Long")))
      (let [remote-port-2 ($.clj/long remote-port)]
        (or (when-not (<= 1
                          remote-port-2
                          65535)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Remote port must be >= 1 and <= 65535")))
            (-init ctx
                   arg+
                   (fn [option+]
                     (assoc option+
                            :convex.server/state
                            [:sync {:convex.server/host ($.clj/string remote-host)
                                    :convex.server/port remote-port-2}])))))))



(defn persist

  [ctx [peer]]

  (-do-peer ctx
            peer
            (fn [peer-2]
              (if ($.server/persist peer-2)
                ($.cvm/result-set ctx
                                  peer)
                ($.cvm/exception-set ctx
                                     ($.cell/* :SHELL.PEER)
                                     ($.cell/* "Unable to persist peer data"))))))



(defn pubkey

  [ctx [peer]]

  (-do-peer ctx
            peer
            (fn [peer-2]
              ($.cvm/result-set ctx
                                ($.server/pubkey peer-2)))))



(defn start

  [ctx [peer]]

  (-do-peer ctx
            peer
            (fn [peer-2]
              (try
                ;;
                ($.server/start peer-2)
                ($.cvm/result-set ctx
                                  peer)
                ;;
                (catch Throwable _ex
                  ($.cvm/exception-set ctx
                                       ($.cell/* :SHELL.PEER)
                                       ($.cell/* "Unable to start peer server, check initialization parameters")))))))



(defn state

  [ctx [peer]]

  (-do-peer ctx
            peer
            (fn [peer-2]
              ($.cvm/result-set ctx
                                ($.server/state peer-2)))))



(defn status

  [ctx [peer]]

  (-do-peer ctx
            peer
            (fn [peer-2]
              ($.cvm/result-set ctx
                                ($.server/status peer-2)))))



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
