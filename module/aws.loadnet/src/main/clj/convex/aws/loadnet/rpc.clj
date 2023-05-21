(ns convex.aws.loadnet.rpc

  (:require [convex.cell       :as $.cell]
            [convex.read       :as $.read]
            [convex.std        :as $.std]
            [convex.write      :as $.write]
            [protosens.process :as P.process]))


;;;;;;;;;;


(defn ssh

  [env i-peer command]

  (P.process/run (concat ["ssh"
                          "-i" (or (env :convex.aws.key/file)
                                   (throw (IllegalArgumentException. "Path to key missing")))
                          "-o" "StrictHostKeyChecking=no"
                          (str "ubuntu@"
                               (if (string? i-peer)
                                 i-peer
                                 (get-in env
                                         [:convex.aws.ip/peer+
                                          i-peer])))]
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



(defn worker

  [env i-peer cell]

  (let [process (cvx env
                     i-peer
                     ($.cell/*
                       (let [[in
                              out] (.worker.pipe+ "peer")]
                         (.worker.exec {:in  (.file.stream.out in)
                                        :out (.file.stream.in out)}
                                       ~cell))))]
    (delay
      (when-not (zero? (:exit @process))
        (throw (ex-info "SSH error while executing remote CVX command"
                        {:convex.aws/i.peer      i-peer
                         :convex.aws.process/err (slurp (:out process))})))
      (let [[ok?
             _
             x]  (seq (first ($.read/string (slurp (:out process)))))]
        (when-not ($.std/true? ok?)
          (throw (ex-info "CVM exception while executing remote CVX command"
                          {:convex.aws/i.peer    i-peer
                           :convex.cvm/exception x})))
        x))))
