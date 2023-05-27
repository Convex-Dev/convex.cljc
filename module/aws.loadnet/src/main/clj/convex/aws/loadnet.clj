(ns convex.aws.loadnet

  (:require [babashka.fs                       :as bb.fs]
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
      ($.aws.loadnet.cloudformation/client+)
      ($.aws.loadnet.stack-set/create)))


;;;;;;;;;;


(comment


  (def env
       (create {:convex.aws/account          (System/getenv "CONVEX_AWS_ACCOUNT")
                :convex.aws/region+          ["eu-central-1"
                                              ;"us-east-1"
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
    ($.aws.loadnet.stack-set/delete env))

  (future
    ($.aws.loadnet.stack-set/stop env))

  ($.aws.loadnet.stack-set/describe env)


  (deref ($.aws.loadnet.rpc/worker env 2 (convex.cell/* (.sys.exit 0))))


  (def env-2 ($.aws.loadnet.cloudwatch/client+ env))
  (def x ($.aws.loadnet.cloudwatch/fetch env-2))
  ($.aws.loadnet.cloudwatch/save env-2 x)

  ($.aws.loadnet.peer/stop env)
  (time ($.aws.loadnet.peer/log+ env))
  (time ($.aws.loadnet.peer.etch/download env))
  ($.aws.loadnet.peer.etch/stat+ {:convex.aws.loadnet/dir "/tmp/loadnet"})


  )
