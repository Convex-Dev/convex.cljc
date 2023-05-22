(ns convex.aws.loadnet.rpc

  (:import (java.net InetSocketAddress
                     Socket
                     SocketTimeoutException))
  (:require [convex.cell       :as $.cell]
            [convex.read       :as $.read]
            [convex.std        :as $.std]
            [convex.write      :as $.write]
            [protosens.process :as P.process]
            [taoensso.timbre   :as log]))


;;;;;;;;;;


(defn- -key

  [env]

  (or (env :convex.aws.key/file)
      (throw (IllegalArgumentException. "Path to key missing"))))



(defn- -peer-ip

  [env i-peer]

  (let [ip (get-in env
                   [:convex.aws.ip/peer+
                    i-peer])]
    (try
      (.connect (Socket.)
                (InetSocketAddress. ip
                                    22)
                30000)
      (catch SocketTimeoutException _ex
        (log/error (format "SSH port seems closed on %s"
                           ip))
        (throw (Exception. "SSH port closed"))))
    (str "ubuntu@"
         ip)))


;;;;;;;;;;


(defn ssh

  [env i-peer command]

  (P.process/run (concat ["ssh"
                          "-i" (-key env)
                          "-o" "StrictHostKeyChecking=no"
                          (-peer-ip env
                                    i-peer)]
                         command)))

;;;


(defn- -cvx

  [cvx-cmd env i-peer cell]

  (ssh env
       i-peer
       [cvx-cmd
        (format "'%s'"
                ($.write/string Long/MAX_VALUE
                                cell))]))



(defn cvx

  [env i-peer cell]

  (-cvx "cvx"
        env
        i-peer
        cell))



(defn jcvx

  [env i-peer cell]

  (-cvx "jcvx"
        env
        i-peer
        cell))



(defn rsync


  ([env i-peer dest]

   (rsync env
          i-peer
          dest
          nil))


  ([env i-peer dest option+]

   (let [process (P.process/run (reduce (fn [cmd path]
                                          (conj cmd
                                                "--exclude"
                                                path))
                                        ["rsync"
                                         "-azrv"
                                         "-e"    (format "ssh -i %s -o StrictHostKeyChecking=no"
                                                         (-key env))
                                         (str (-peer-ip env
                                                        i-peer)
                                              ":/tmp/peer/"
                                              (:src option+))
                                         dest]
                                        (:exclude option+)))]
     (or (= 0
            (:exit @process))
         (do
           (log/error (format "Rsync over peer %d failed: %s"
                              i-peer
                              (slurp (:err process))))
           false)))))



(defn worker

  [env i-peer cell]

  (let [process (cvx env
                     i-peer
                     ($.cell/*
                       (let [[in
                              out] (.worker.pipe+ "peer")]
                         (.worker.exec {:in  (.file.stream.out in)
                                        :out (.file.stream.in out)}
                                       (quote ~cell)))))]
    (delay
      (when-not (zero? (:exit @process))
        (throw (ex-info "SSH error while executing remote CVX command"
                        {:convex.aws/i.peer      i-peer
                         :convex.aws.process/err (slurp (:err process))})))
      (let [[ok?
             _
             x]  (seq (first ($.read/string (slurp (:out process)))))]
        (when-not ($.std/true? ok?)
          (throw (ex-info "CVM exception while executing remote CVX command"
                          {:convex.aws/i.peer    i-peer
                           :convex.cvm/exception x})))
        x))))
