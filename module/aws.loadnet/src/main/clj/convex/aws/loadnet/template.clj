(ns convex.aws.loadnet.template

  "Creating an AWS CloudFormation template for a stack containing
   all Load Generator and Peer instances, as well as any required
   configuration."

  (:require [convex.aws.loadnet.default       :as $.aws.loadnet.default]
            [convex.aws.loadnet.template.peer :as $.aws.loadnet.template.peer]))


;;;;;;;;;; Data


(def region->ami

  "Map of `AWS Region` -> `AMI ID`."

  {"ap-southeast-1" "ami-049586d94c45f8964"
   "eu-central-1"   "ami-035806966bfb82c24"
   "us-east-1"      "ami-0106204bf4a268cad"
   "us-west-1"      "ami-0e16b4adf2f1591de"})


;;;


(def mapping+

  "Template Mappings for selecting the right AMI depending on in the region."

  {"RegionalAMI" (into {}
                       (map (juxt first
                                  (comp (fn [ami]
                                          {"AMI" ami})
                                        second)))
                       region->ami)})



(def security-group

  "Security group allowing SSH and Convex Peers."

  {:Type       "AWS::EC2::SecurityGroup"
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
                                        :ToPort      "18888"}]}})



(def parameter+

  "Template parameters."

  {"DetailedMonitoring" {:Type        "String"
                         :Description "Enables EC2 detailed monitoring on peer instances"
                         :Default     $.aws.loadnet.default/detailed-monitoring}
   "KeyName"            {:Type                  "AWS::EC2::KeyPair::KeyName"
                         :Description           "Name of an existing EC2 KeyPair to enable SSH access to peers"
                         :ConstraintDescription "Must be the name of an existing EC2 KeyPair"}
   "InstanceTypeLoad"   {:Type        "String"
                         :Default     $.aws.loadnet.default/instance-type-load
                         :Description "Instance type to be used for load generators"}
   "InstanceTypePeer"   {:Type        "String"
                         :Default     $.aws.loadnet.default/instance-type-peer
                         :Description "Instance type to be used for peers"}
   "RolePeer"           {:Type        "String"
                         :Default     "CloudWatchAgentServerRole"
                         :Description "IAM role for Peers, see https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/create-iam-roles-for-cloudwatch-agent-commandline.html"}})


;;;;;;;;;; Functions


(defn load-generator+

  "Returns descriptions of Load Generators as template resources."

  [name-load+ ebs]

  (into {}
        (map (fn [i-load name-load]
               [name-load
                {:Type       "AWS::EC2::Instance"
                 :Properties {:BlockDeviceMappings [{:DeviceName "/dev/sda1"
                                                     :Ebs        ebs}]
                              :ImageId             {"Fn::FindInMap" ["RegionalAMI"
                                                                     {:Ref "AWS::Region"}
                                                                     "AMI"]}
                              :InstanceType        {:Ref "InstanceTypeLoad"}
                              :KeyName             {:Ref "KeyName"}
                              :SecurityGroups      [{:Ref "SecurityGroup"}]
                              :Tags                [{:Key   "Name"
                                                     :Value {"Fn::Join" [" "
                                                                         [{:Ref "AWS::StackName"}
                                                                          (str "Load " i-load)]]}}]}}])
             (range)
             name-load+)))



(defn output+

  "Returns template outputs.
   Used for collecting IP addresses of Load Generator and Peer instances."

  [name-load+ name-peer+]

  (let [out (fn [fstr-name-out name-resrc+]
              (into {}
                    (map-indexed (fn [i-resrc name-resrc]
                                   [(format fstr-name-out
                                            i-resrc)
                                    {:Description (format "%s public IP"
                                                          name-resrc)
                                     :Value       {"Fn::GetAtt" [name-resrc
                                                                 "PublicIp"]}}]))
                    name-resrc+))]
    (merge (out "IpLoad%d"
                name-load+)
           (out "IpPeer%d"
                name-peer+))))



(defn resource+

  "Returns a description of all template resources."

  [env name-load+ name-peer+]

  (let [ebs {:DeleteOnTermination true}]
    (merge {"SecurityGroup" security-group}
           (load-generator+ name-load+
                            (assoc ebs
                                   :VolumeSize
                                   (env :convex.aws.loadnet.load/volume)))
           ($.aws.loadnet.template.peer/resource+ name-peer+
                                                  (assoc ebs
                                                         :VolumeSize
                                                         (env :convex.aws.loadnet.peer/volume))))))


;;;


(defn net

  "Entry point for generating a template for a whole loadnet."


  ([]

   (net nil))


  ([env]

   (let [name-load+ (mapv #(str "Load"
                                %)
                          (range (or (:convex.aws.region/n.load env)
                                     $.aws.loadnet.default/n-load)))
         name-peer+ (mapv #(str "Peer"
                                %)
                          (range (or (:convex.aws.region/n.peer env)
                                     $.aws.loadnet.default/n-peer)))]
     {:Description "Convex network for load testing"
      :Mappings    mapping+
      :Outputs     (output+ name-load+
                            name-peer+)
      :Parameters  parameter+
      :Resources   (resource+ env
                              name-load+
                              name-peer+)})))
