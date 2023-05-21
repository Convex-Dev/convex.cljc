(ns convex.aws.loadnet.template)


;;;;;;;;;;


(defn net


  ([]

   (net nil))


  ([env]

   (let [volume-size   (:convex.aws.instance/volume env)
         ebs           (cond->
                         {:DeleteOnTermination true}
                         volume-size
                         (assoc :VolumeSize
                                volume-size))
         name-peer+    (mapv (fn [i-peer]
                               (str "Peer"
                                    i-peer))
                             (range (or (:convex.aws.loadnet/n.peer env)
                                        3)))]
     {:Description "Convex network for load testing"
      :Outputs     (into {}
                         (map (fn [i-peer name-peer]
                                [(format "IpPeer%d"
                                         i-peer)
                                 {:Description (format "%s public IP"
                                                       name-peer)
                                  :Value       {"Fn::GetAtt" [name-peer
                                                              "PublicIp"]}}])
                              (range)
                              name-peer+))
      :Parameters  {"KeyName"          {:Type                  "AWS::EC2::KeyPair::KeyName"
                                        :Description           "Name of an existing EC2 KeyPair to enable SSH access to peers"
                                        :ConstraintDescription "Must be the name of an existing EC2 KeyPair"}
                    "PeerInstanceType" {:Type        "String"
                                        :Default     "m4.2xlarge"
                                        :Description "Instance type to be used for peers"}}
      :Resources   (into {"SecurityGroup" {:Type       "AWS::EC2::SecurityGroup"
                                           :Properties {:GroupDescription     "Network access for peers"
                                                        :GroupName            {:Ref "AWS::StackName"}
                                                        :SecurityGroupIngress [{:CidrIp      "0.0.0.0/0"
                                                                                :Description "SSH"
                                                                                :FromPort    "22"
                                                                                :IpProtocol  "tcp"
                                                                                :ToPort      "22"}
                                                                               {:CidrIp      "0.0.0.0/0"
                                                                                :Description "Standard port for Convex peers"
                                                                                :FromPort    "18888"
                                                                                :IpProtocol  "tcp"
                                                                                :ToPort      "18888"}]}}}
                         (map (fn [i-peer name-peer]
                                [name-peer
                                 {:Type       "AWS::EC2::Instance"
                                  :Properties {:BlockDeviceMappings [{:DeviceName "/dev/sda1"
                                                                      :Ebs        ebs}]
                                               :ImageId             "ami-057b1d40595cd9308"
                                               :InstanceType        {:Ref "PeerInstanceType"}
                                               :KeyName             {:Ref "KeyName"}
                                               :SecurityGroups      [{:Ref "SecurityGroup"}]
                                               :Tags                [{:Key   "Name"
                                                                      :Value {"Fn::Join" [" "
                                                                                          [{:Ref "AWS::StackName"}
                                                                                           (str "Peer " i-peer)]]}}]}}])
                              (range)
                              name-peer+))})))
