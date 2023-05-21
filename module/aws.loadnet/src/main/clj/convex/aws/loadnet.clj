(ns convex.aws.loadnet

  (:require [cognitect.aws.client.api  :as aws]
            [convex.cell               :as $.cell]
            [convex.read               :as $.read]
            [convex.std                :as $.std]
            [convex.write              :as $.write]

            [convex.aws.loadnet.stack  :as $.aws.loadnet.stack]
            [convex.aws.loadnet.peer   :as $.aws.loadnet.peer]
            [convex.aws.loadnet.rpc    :as $.aws.loadnet.rpc]

            [protosens.process         :as P.process]))


;;;;;;;;;; Remote commands













(defn peer-ready

  [key i-peer stack]

  (let [process (P.process/run ["ssh"
                                "-i" key
                                "-o" "StrictHostKeyChecking=no"
                                (str "ubuntu@"
                                     (get-in stack
                                             [:convex.aws.ip/peer+
                                              i-peer]))
                                "cvx"
                                (->> ($.cell/*
                                       (let [[in
                                              out] (.worker.pipe+ "peer")]
                                         (.worker.exec {:in  (.file.stream.out in)
                                                        :out (.file.stream.in out)}
                                                       '(inc 42))))
                                     ($.write/string Long/MAX_VALUE)
                                     (format "'%s'"))])]
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
      x)))
  

;;;;;;;;;;


(comment


  (def env ($.aws.loadnet.stack/client))


  (sort (keys (aws/ops (env :convex.aws.client/cloudformation))))

  (aws/doc (env :convex.aws.client/cloudformation) :DescribeStacks)


  (def env-2
       ($.aws.loadnet.stack/create (merge env
                                          {:convex.aws.key/file         "/Users/adam/Desktop/Test.pem"
                                           :convex.aws.loadnet/n.peer   1
                                           :convex.aws.stack/parameter+ {:KeyName          "Test"
                                                                         :PeerInstanceType "t2.micro"}
                                           :convex.aws.stack/tag+       {:Project "Ontochain"}})))

  ($.aws.loadnet.stack/delete env-2)

  ($.aws.loadnet.stack/describe env-2)
  ($.aws.loadnet.stack/resrc+ env-2)
  ($.aws.loadnet.stack/status env-2)

  ($.aws.loadnet.stack/cost env-2)



  (def env-3
       ($.aws.loadnet.peer/genesis env-2))

  (-> env-3 :convex.aws.loadnet.cvx/peer+ (first))

  ($.aws.loadnet.rpc/worker env-3 0 (convex.cell/* (.sys.exit 0)))



  )
