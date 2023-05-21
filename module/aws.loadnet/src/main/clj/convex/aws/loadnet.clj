(ns convex.aws.loadnet

  (:require [cognitect.aws.client.api  :as aws]
            [convex.aws.loadnet.log]
            [convex.aws.loadnet.peer   :as $.aws.loadnet.peer]
            [convex.aws.loadnet.rpc    :as $.aws.loadnet.rpc]
            [convex.aws.loadnet.stack  :as $.aws.loadnet.stack]
            [convex.cell               :as $.cell]))


;;;;;;;;;;


(comment


  (def env ($.aws.loadnet.stack/client))


  (sort (keys (aws/ops (env :convex.aws.client/cloudformation))))

  (aws/doc (env :convex.aws.client/cloudformation) :DescribeStacks)


  (def env-2
       ($.aws.loadnet.stack/create (merge env
                                          {:convex.aws.key/file         "/Users/adam/Desktop/Test.pem"
                                           :convex.aws.loadnet/dir      "/tmp/loadnet"
                                           :convex.aws.loadnet/n.peer   3
                                           :convex.aws.stack/parameter+ {:KeyName          "Test"
                                                                         :PeerInstanceType "t2.micro"}
                                           :convex.aws.stack/tag+       {:Project "Ontochain"}})))

  ($.aws.loadnet.stack/delete env-2)

  ($.aws.loadnet.stack/describe env-2)
  ($.aws.loadnet.stack/resrc+ env-2)
  ($.aws.loadnet.stack/status env-2)

  ($.aws.loadnet.stack/cost (merge env
                                   {:convex.aws.loadnet/n.peer  10
                                    :convex.aws.stack/parameter {}}))


  (deref ($.aws.loadnet.rpc/worker env-2 2 (convex.cell/* (.sys.exit 0))))

  ($.aws.loadnet.peer/stop env-2)

  ($.aws.loadnet.rpc/rsync env-2
                           0
                           "/tmp/foo"
                           {:exclude ["store.etch"]})

  ($.aws.loadnet.peer/log+ env-2)
  ($.aws.loadnet.peer/etch env-2)



  )
