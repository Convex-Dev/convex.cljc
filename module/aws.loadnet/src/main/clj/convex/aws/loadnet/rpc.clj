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


(defn- -ip

  [env k-ip+ i-ip]

  (format "ubuntu@%s"
          (get-in env
                  [k-ip+
                   i-ip])))



(defn- -key

  [env]

  (or (env :convex.aws.key/file)
      (throw (IllegalArgumentException. "Path to key missing"))))


;;;;;;;;;;


(defn await-ssh

  [env]

  (log/info "Awaiting for all SSH servers to be ready")
  (let [env-2
        (reduce (fn [env-2 ip]
                  (loop [i 10]
                    (or (try
                          (with-open [^Socket socket (Socket.)]
                            (.connect socket
                                      (InetSocketAddress. ip
                                                          22)
                                      60000))
                          env-2
                          ;;
                          (catch SocketTimeoutException _ex
                            (log/error (format "Timeout when awaiting SSH server seems closed on %s"
                                               ip))
                            (reduced (assoc env-2
                                            :convex.aws.loadnet/ssh-ready?
                                            false)))
                          ;;
                          (catch Exception _ex
                            (when (zero? i)
                              (log/error (format "Unable to establish SSH connection to %s"
                                                 ip))
                              (reduced (assoc env-2
                                              :convex.aws.loadnet/ssh-ready?
                                              false)))))
                        (recur (dec i)))))
                (assoc env
                       :convex.aws.loadnet/ssh-ready?
                       true)
                (concat (env :convex.aws.ip/load+)
                        (env :convex.aws.ip/peer+)))]
    (when (env-2 :convex.aws.loadnet/ssh-ready?)
      ;; Extra delay just in case.
      (Thread/sleep 5000))
    env-2))



(defn ssh

  [env k-ip+ i-ip command]

  (P.process/run (concat ["ssh"
                          "-i" (-key env)
                          "-o" "StrictHostKeyChecking=no"
                          (-ip env
                               k-ip+
                               i-ip)]
                         command)))


;;;


(defn- -cvx

  [cvx-cmd env k-ip+ i-ip cell]

  (ssh env
       k-ip+
       i-ip
       [cvx-cmd
        (format "'%s'"
                ($.write/string Long/MAX_VALUE
                                cell))]))



(defn cvx

  [env k-ip+ i-ip cell]

  (-cvx "cvx"
        env
        k-ip+
        i-ip
        cell))



(defn jcvx

  [env k-ip+ i-ip cell]

  (-cvx "jcvx"
        env
        k-ip+
        i-ip
        cell))



(defn kill-process

  [env k-ip+ i-ip file-pid]

  (ssh env
       k-ip+
       i-ip
       ["kill"
        "-9"
        (format "`cat %s`"
                file-pid)]))



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
                                         (str (-ip env
                                                   :convex.aws.ip/peer+
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

  [env k-ip+ i-ip cell]

  (let [process (cvx env
                     k-ip+
                     i-ip
                     ($.cell/*
                       (let [[in
                              out] (.worker.pipe+ "peer")]
                         (.worker.exec {:in  (.file.stream.out in)
                                        :out (.file.stream.in out)}
                                       (quote ~cell)))))]
    (delay
      (when-not (zero? (:exit @process))
        (throw (ex-info "SSH error while executing remote CVX command"
                        {:convex.aws.loadnet/worker [k-ip+
                                                     i-ip]
                         :convex.aws.process/err    (slurp (:err process))})))
      (let [[ok?
             _
             x]  (seq (first ($.read/string (slurp (:out process)))))]
        (when-not ($.std/true? ok?)
          (throw (ex-info "CVM exception while executing remote CVX command"
                          {:convex.aws.loadnet/worker [k-ip+
                                                       i-ip]
                           :convex.cvm/exception      x})))
        x))))
