(ns convex.aws.loadnet

  (:require [babashka.fs                       :as bb.fs]
            [cognitect.aws.client.api          :as aws]
            [convex.aws.loadnet.cloudformation :as $.aws.loadnet.cloudformation]
            [convex.aws.loadnet.log]
            [convex.aws.loadnet.metric         :as $.aws.loadnet.metric]
            [convex.aws.loadnet.peer           :as $.aws.loadnet.peer]
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
                       "./")
                   (bb.fs/canonicalize)
                   (str)))
      (update :convex.aws.region/n.peer
              #(or %
                   3))
      ($.aws.loadnet.cloudformation/client+)
      ($.aws.loadnet.stack-set/create)
      ))


;;;;;;;;;;


(comment


  (def env
       (create {:convex.aws/account          (System/getenv "CONVEX_AWS_ACCOUNT")
                :convex.aws/region+          ["eu-central-1"]
                :convex.aws.key/file         "/Users/adam/Desktop/Test.pem"
                :convex.aws.loadnet/dir      "/tmp/loadnet"
                :convex.aws.region/n.peer    2
                :convex.aws.stack/parameter+ {:KeyName          "Test"
                                              :PeerInstanceType "t2.micro"}
                :convex.aws.stack/tag+       {:Project "Ontochain"}}))


  ($.aws.loadnet.stack-set/delete env)

  ($.aws.loadnet.stack-set/describe env)


  (deref ($.aws.loadnet.rpc/worker env 2 (convex.cell/* (.sys.exit 0))))
  ($.aws.loadnet.peer/stop env)



  (sort (keys (aws/ops (env :convex.aws.client/cloudformation))))
  (aws/doc (env :convex.aws.client/cloudformation) :DeleteStackSet)


  (def env-2 ($.aws.loadnet.metric/client+ env))
  (def x ($.aws.loadnet.metric/fetch env-2))
  ($.aws.loadnet.metric/save env-2 x)

  ($.aws.loadnet.peer/log+ env-2)
  ($.aws.loadnet.peer/etch env-2)
  ($.aws.loadnet.peer/etch-stat {:convex.aws.loadnet/dir "/tmp/loadnet"})


  )
