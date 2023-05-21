(ns convex.aws.loadnet

  (:require [cognitect.aws.client.api  :as aws]
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
                                           :convex.aws.loadnet/n.peer   10
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

  (def env-4
       ($.aws.loadnet.peer/syncer+ env-2))

  (-> env-3 :convex.aws.loadnet.cvx/peer+ (first))

  ($.aws.loadnet.rpc/worker env-3 0 (convex.cell/* (.sys.exit 0)))



  )
