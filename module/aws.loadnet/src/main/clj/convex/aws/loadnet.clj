(ns convex.aws.loadnet

  (:require [babashka.fs                       :as bb.fs]
            [clojure.edn                       :as edn]
            [cognitect.aws.client.api          :as aws]
            [convex.aws.loadnet.cloudformation :as $.aws.loadnet.cloudformation]
            [convex.aws.loadnet.cloudwatch     :as $.aws.loadnet.cloudwatch]
            [convex.aws.loadnet.default        :as $.aws.loadnet.default]
            [convex.aws.loadnet.log]
            [convex.aws.loadnet.peer           :as $.aws.loadnet.peer]
            [convex.aws.loadnet.peer.etch      :as $.aws.loadnet.peer.etch]
            [convex.aws.loadnet.rpc            :as $.aws.loadnet.rpc]
            [convex.aws.loadnet.stack          :as $.aws.loadnet.stack]
            [convex.aws.loadnet.stack-set      :as $.aws.loadnet.stack-set]
            [convex.cell                       :as $.cell]))


;;;;;;;;;;


(defn create

  [env]

  (assert (env :convex.aws/account))
  (assert (not-empty (env :convex.aws/region+)))
  (assert (get-in env
                  [:convex.aws.stack/parameter+
                   :KeyName]))
  (-> env
      (update :convex.aws.loadnet/dir
              #(-> (or %
                       $.aws.loadnet.default/dir)
                   (bb.fs/canonicalize)
                   (str)))
      (update :convex.aws.region/n.peer
              #(or %
                   $.aws.loadnet.default/n-peer))
      ($.aws.loadnet.cloudwatch/client+)
      ($.aws.loadnet.cloudformation/client+)
      ($.aws.loadnet.stack-set/create)))



(defn stop

  [env]

  (-> env
      ($.aws.loadnet.peer/stop)
      ($.aws.loadnet.peer/log+)
      ($.aws.loadnet.peer.etch/download)
      ($.aws.loadnet.peer.etch/stat+)
      ($.aws.loadnet.cloudwatch/download)
      ($.aws.loadnet.stack-set/delete)))



(defn stop-2

  [env]

  (-> env
      (merge (edn/read-string (slurp (format "%s/run.edn"
                                             (env :convex.aws.loadnet/dir)))))
      ($.aws.loadnet.cloudformation/client)
      ($.aws.loadnet.stack-set/delete)))


;;;;;;;;;;


(comment


  (def env
       (create {:convex.aws/account          (System/getenv "CONVEX_AWS_ACCOUNT")
                :convex.aws/region+          ["eu-central-1"
                                              "us-east-1"
                                              ;"us-west-1"
                                              ;"ap-southeast-1"
                                              ]
                :convex.aws.key/file         "/Users/adam/Code/convex/clj/private/Test"
                :convex.aws.loadnet/dir      "/tmp/loadnet"
                :convex.aws.region/n.peer    1
                :convex.aws.stack/parameter+ {:KeyName          "Test"
                                              :PeerInstanceType "t2.micro"
                                              }
                :convex.aws.stack/tag+       {:Project "Ontochain"}}))


  (future
    (do
      (stop env)
      nil))

  (future
    (do
      (stop-2 {:convex.aws/account     (System/getenv "CONVEX_AWS_ACCOUNT")
               :convex.aws.loadnet/dir "/tmp/loadnet"})
      nil))


  (future
    ($.aws.loadnet.stack-set/delete env))


  ($.aws.loadnet.stack-set/describe env)


  (deref ($.aws.loadnet.rpc/worker env 2 (convex.cell/* (.sys.exit 0))))


  ($.aws.loadnet.cloudwatch/download env)

  ($.aws.loadnet.peer/stop env)
  (time ($.aws.loadnet.peer/log+ env))
  (time ($.aws.loadnet.peer.etch/download env))
  ($.aws.loadnet.peer.etch/stat+ {:convex.aws.loadnet/dir "/tmp/loadnet"})


  )
