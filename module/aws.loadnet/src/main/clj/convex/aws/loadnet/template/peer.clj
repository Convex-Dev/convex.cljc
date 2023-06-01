(ns convex.aws.loadnet.template.peer

  "Generating parts of AWS CloudFormation templates relating to Peer instances."

  (:require [clojure.string :as string]))


;;;;;;;;;;


(defn- -txt

  [line+]

  (string/join \newline
               line+))


;;;;;;;;;;


(def config-cloudwatch

  "Configuration for the AWS CloudWatch Agent.
   Used to monitor memory usage on Peer instances.

   Cf. https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch-Agent-Configuration-File-Details.html
       https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/metrics-collected-by-CloudWatch-agent.html#linux-metrics-enabled-by-CloudWatch-agent"

  {; :agent   {:metrics_collection_interval 60} ;; default
   :metrics {:append_dimensions {:InstanceId  {"Fn::Sub" "${!aws:InstanceId}"}}
             :metrics_collected {;; Not quite necessary, the most important thing to measure is Etch size.
                                 ;
                                 ; :disk {:measurement ["disk_used"]
                                 ;        :resources   ["/"]}
                                 ;
                                 :mem  {:measurement ["mem_used"]}}
             :namespace         "Convex/LoadNet"}})



(defn metadata

  "Template metadata for Peer instances.
   
   Provides the necessary configuration for running Cfn-init and installing the CloudWatch Agent.

   Cf. [[config-cloudwatch]]
       https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cfn-helper-scripts-reference.html"

  [name-peer]

  {"AWS::CloudFormation::Init"
   {:configSets
    {:default           ["01_setupCfnHup"
                         "02_config-amazon-cloudwatch-agent"
                         "03_restart_amazon-cloudwatch-agent"]
     :UpdateEnvironment ["02_config-amazon-cloudwatch-agent"
                         "03_restart_amazon-cloudwatch-agent"]}


    "01_setupCfnHup"
    {:files
     {"/etc/cfn/cfn-hup.conf"
      {:content {"Fn::Sub" (-txt ["[main]"
                                  "stack=${AWS::StackId}"
                                  "region=${AWS::Region}"
                                  "interval=1"])}
       :group   "root"
       :mode    "000400"
       :owner   "root"}

       "/etc/cfn/hooks.d/amazon-cloudwatch-agent-auto-reloader.conf'"
       {:content {"Fn::Sub"
                  (-txt ["[cfn-auto-reloader-hook]"
                         "triggers=post.update"
                         "path=Resources.EC2Instance.Metadata.AWS::CloudFormation::Init.02_config-amazon-cloudwatch-agent"
                         (format "action=/opt/aws/bin/cfn-init -v --stack ${AWS::StackId} --resource %s --region ${AWS::Region} --configsets UpdateEnvironment"
                                 name-peer)
                         "runas=root"])}
        :group   "root"
        :mode    "000400"
        :owner   "root"}

        "/lib/systemd/system/cfn-hup.service"
        {:content {"Fn::Sub"
                   (-txt ["[Unit]"
                          "Description=cfn-hup daemon"
                          "[Service]"
                          "Type=simple"
                          "ExecStart=/opt/aws/bin/cfn-hup"
                          "Restart=always"
                          "[Install]"
                          "WantedBy=multi-user.target"])}}}

     :commands
     {"01_enable_cfn_hup" {:command "systemctl enable cfn-hup.service"}
      "02_start_cfn_hup"  {:command "systemctl start cfn-hup.service"}}}


    "02_config-amazon-cloudwatch-agent"
    {:files
     {"/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json"
      {:content config-cloudwatch}}}


    "03_restart_amazon-cloudwatch-agent"
    {:commands {"01_stop_service"  {:command "/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a stop"}
                "02_start_service" {:command "/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json -s"}}}}})



(defn user-data

  "User Data used to run a scripts that installs on Peer instances everything that is necessary for
   the AWS CloudWatch Agent.

   Cf. [[config-cloudwatch]]
       [[metadata]]"

  [name-peer]

  {"Fn::Base64"
   {"Fn::Sub"
    (-txt ["#!/bin/bash -xe"
           ;; Install CloudWatch Agent.
           "wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb -O /tmp/amazon-cloudwatch-agent.deb"
           "dpkg -i /tmp/amazon-cloudwatch-agent.deb"
           ;; Install Cfn-init.
           "apt-get update"
           "apt-get -y install python3-pip"
           "pip install https://s3.amazonaws.com/cloudformation-examples/aws-cfn-bootstrap-py3-latest.tar.gz"
           "ln -s /root/aws-cfn-bootstrap-latest/init/ubuntu/cfn-hup /etc/init.d/cfn-hup"
           ;; Run Cfn-init.
           (format "cfn-init -v --stack ${AWS::StackName} --resource %s --region ${AWS::Region} --configsets default"
                   name-peer)
           "cfn-signal -e $? --stack ${AWS::StackName} --resource EC2Instance --region ${AWS::Region}"])}})


;;;


(defn resource

  "Returns a description of a Peer instance as a template resource."
  
  [i-peer name-peer ebs]

  {:Type       "AWS::EC2::Instance"
   :Metadata   (metadata name-peer)
   :Properties {:BlockDeviceMappings [{:DeviceName "/dev/sda1"
                                       :Ebs        ebs}]
                :IamInstanceProfile  {:Ref "RolePeer"}
                :ImageId             {"Fn::FindInMap" ["RegionalAMI"
                                                       {:Ref "AWS::Region"}
                                                       "AMI"]}
                :InstanceType        {:Ref "InstanceTypePeer"}
                :Monitoring          {:Ref "DetailedMonitoring"}
                :KeyName             {:Ref "KeyName"}
                :SecurityGroups      [{:Ref "SecurityGroup"}]
                :Tags                [{:Key   "Name"
                                       :Value {"Fn::Join" [" "
                                                           [{:Ref "AWS::StackName"}
                                                            (str "Peer " i-peer)]]}}]

   :UserData (user-data name-peer)}})



(defn resource+

  "Returns a description of Peer instances as template resources."

  [name-peer+ ebs]

  (into {}
        (map-indexed (fn [i-peer name-peer]
                       [name-peer
                        (resource i-peer
                                  name-peer
                                  ebs)]))
        name-peer+))
