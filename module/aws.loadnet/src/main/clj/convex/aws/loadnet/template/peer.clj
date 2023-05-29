(ns convex.aws.loadnet.template.peer

  (:require [clojure.string :as string]))


;;;;;;;;;;


(defn- -txt

  [line+]

  (string/join \newline
               line+))


;;;;;;;;;;


(defn metadata

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
      {:content {:metrics {:append_dimensions {:InstanceId  {"Fn::Sub" "${!aws:InstanceId}"}}
                           :metrics_collected {:mem {:measurement  ["mem_used_percent"]}
                                               :swap {:measurement ["swap_used_percent"]}}}}}}}


    "03_restart_amazon-cloudwatch-agent"
    {:commands {"01_stop_service"  {:command "/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a stop"}
                "02_start_service" {:command "/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json -s"}}}}})



(defn user-data

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

  [name-peer+ ebs]

  (into {}
        (map-indexed (fn [i-peer name-peer]
                       [name-peer
                        (resource i-peer
                                  name-peer
                                  ebs)]))
        name-peer+))
