# Table of contents
-  [`convex.aws`](#convex.aws)  - Common utilities related to Cognitect's AWS API.
    -  [`client`](#convex.aws/client) - Creates a client for a given region.
    -  [`invoke`](#convex.aws/invoke) - Invoke an operation using Cognitect's API.
-  [`convex.aws.loadnet`](#convex.aws.loadnet)  - Running loadnets.
    -  [`create`](#convex.aws.loadnet/create) - Creates and start a loadnet on AWS.
    -  [`start`](#convex.aws.loadnet/start) - If [[create]] has trouble awaiting SSH connections to EC2 instances (as logged), run this with the returned <code>env</code> to try starting the simulation again on currently deployed machines.
    -  [`stop`](#convex.aws.loadnet/stop) - Stops a simulation and deletes the whole CloudFormation stack set.
-  [`convex.aws.loadnet.cloudformation`](#convex.aws.loadnet.cloudformation)  - Generic utilities related to AWS Cloudformation.
    -  [`client`](#convex.aws.loadnet.cloudformation/client) - Creates a CloudFormation client for the given region (defaults to the first region in <code>:convex.aws/region+</code>).
    -  [`client+`](#convex.aws.loadnet.cloudformation/client+) - Creates CloudFormation clients for all regions in <code>:convex.aws/region+</code>.
    -  [`invoke`](#convex.aws.loadnet.cloudformation/invoke) - Invokes a CloudFormation operation.
    -  [`param+`](#convex.aws.loadnet.cloudformation/param+) - Turns a Map of <code>parameter</code> -> <code>value</code> into the format that AWS understands.
-  [`convex.aws.loadnet.cloudwatch`](#convex.aws.loadnet.cloudwatch)  - Collecting EC2 instance metrics from AWS CloudWatch for Peers: - CPU utilization - Memory usage - Network volume (inbound and outbound).
    -  [`client+`](#convex.aws.loadnet.cloudwatch/client+) - Creates CloudWatch clients for all regions in the stack set.
    -  [`download`](#convex.aws.loadnet.cloudwatch/download) - Retrieves all metrics and computes statistics.
    -  [`fetch`](#convex.aws.loadnet.cloudwatch/fetch) - Retrieves metrics from all regions.
    -  [`fetch-region`](#convex.aws.loadnet.cloudwatch/fetch-region) - Retrieves metrics from the given <code>region</code>.
    -  [`stat+`](#convex.aws.loadnet.cloudwatch/stat+) - Computes and logs statistics about metrics.
-  [`convex.aws.loadnet.default`](#convex.aws.loadnet.default)  - Default values used throughout this module.
    -  [`detailed-monitoring`](#convex.aws.loadnet.default/detailed-monitoring) - Detailed Monitoring of EC2 instances enabled by default.
    -  [`dir`](#convex.aws.loadnet.default/dir) - Working directory for a simulation is the current working directory by default.
    -  [`instance-type-load`](#convex.aws.loadnet.default/instance-type-load) - Default EC2 instance type for load generators.
    -  [`instance-type-peer`](#convex.aws.loadnet.default/instance-type-peer) - Default EC2 instance type for peers.
    -  [`n-load`](#convex.aws.loadnet.default/n-load) - Default number of load generators per region.
    -  [`n-peer`](#convex.aws.loadnet.default/n-peer) - Default number of peers per region.
    -  [`peer-native?`](#convex.aws.loadnet.default/peer-native?) - Peers will run on the JVM by default, as opposed to running natively.
-  [`convex.aws.loadnet.load`](#convex.aws.loadnet.load)  - Managing Load Generators.
    -  [`start`](#convex.aws.loadnet.load/start) - Starts all Load Generators on EC2 instances (if needed).
    -  [`stop`](#convex.aws.loadnet.load/stop) - Kills all Load Generators (if any).
-  [`convex.aws.loadnet.load.log`](#convex.aws.loadnet.load.log)  - Log analysis of load generators for computing finality.
    -  [`dir`](#convex.aws.loadnet.load.log/dir) - Returns the directory where all logs will be downloaded from load generator instances.
    -  [`download`](#convex.aws.loadnet.load.log/download) - Downloads all logs from load generator instances.
    -  [`stat+`](#convex.aws.loadnet.load.log/stat+) - Statistical analysis of load generator logs.
-  [`convex.aws.loadnet.log`](#convex.aws.loadnet.log)  - Logging setup redirecting entries directly to the STDOUT file descriptor.
    -  [`newline`](#convex.aws.loadnet.log/newline)
    -  [`out`](#convex.aws.loadnet.log/out)
-  [`convex.aws.loadnet.peer`](#convex.aws.loadnet.peer)  - Managing peer instances.
    -  [`log+`](#convex.aws.loadnet.peer/log+) - Downloads logs from all Peers.
    -  [`start`](#convex.aws.loadnet.peer/start) - Starts all Peer processes on EC2 instances.
    -  [`start-genesis`](#convex.aws.loadnet.peer/start-genesis) - Starts a Genesis Peer on a remote EC2 instance.
    -  [`start-syncer+`](#convex.aws.loadnet.peer/start-syncer+) - Starts Syncing Peers on EC2 instance.
    -  [`stop`](#convex.aws.loadnet.peer/stop) - Stops all Peer processes.
-  [`convex.aws.loadnet.peer.etch`](#convex.aws.loadnet.peer.etch)  - Downlading Etch instances from peer instances and conducting statistical analysis.
    -  [`download`](#convex.aws.loadnet.peer.etch/download) - Downloads the Etch instance of a peer (peer 0 by default, aka Genesis Peer).
    -  [`stat+`](#convex.aws.loadnet.peer.etch/stat+) - Computes and outputs statistics on an Etch instance that has been [[download]]ed.
-  [`convex.aws.loadnet.rpc`](#convex.aws.loadnet.rpc)  - Remote operations on EC2 instances through SSH.
    -  [`await-ssh`](#convex.aws.loadnet.rpc/await-ssh) - Tries to await all SSH servers from load generator and peer instances.
    -  [`cvx`](#convex.aws.loadnet.rpc/cvx) - Executes CVX code (<code>cell</code>) on a remote instance using the Convex Shell (native).
    -  [`jcvx`](#convex.aws.loadnet.rpc/jcvx) - Executes CVX code (<code>cell</code>) on a remote instance using the Convex Shell (JVM).
    -  [`kill-process`](#convex.aws.loadnet.rpc/kill-process) - Kills a remote process through [[ssh]].
    -  [`rsync`](#convex.aws.loadnet.rpc/rsync) - Performs RSYNC on a remote instance.
    -  [`ssh`](#convex.aws.loadnet.rpc/ssh) - Executes a remote command through SSH given the key in <code>env</code> where IPs are stored (<code>:convex.aws.ip/load+</code> or <code>:convex.aws.ip/peer+</code>) and the index of the IP to use.
    -  [`worker`](#convex.aws.loadnet.rpc/worker) - Executes CVX code (<code>cell</code>) on a remote instance using the Convex Shell (native) to connect to the peer worker.
-  [`convex.aws.loadnet.stack`](#convex.aws.loadnet.stack)  - AWS operations relating to CloudFormation stacks.
    -  [`describe`](#convex.aws.loadnet.stack/describe) - Queries a description the stack of the given <code>region</code>.
    -  [`ip+`](#convex.aws.loadnet.stack/ip+) - Queries a Vector of instance IPs for the given <code>region</code>.
    -  [`output+`](#convex.aws.loadnet.stack/output+) - Queries the stack outputs of the given <code>region</code>.
    -  [`peer-instance+`](#convex.aws.loadnet.stack/peer-instance+) - Queries a Vector of information about peer instances in the given <code>region</code>.
    -  [`peer-instance-id+`](#convex.aws.loadnet.stack/peer-instance-id+) - Queries a Vector of peer instance IDs for the given <code>region</code>.
    -  [`resrc+`](#convex.aws.loadnet.stack/resrc+) - Queries a sequence of resources for the stack in the <code>given</code> region.
    -  [`status`](#convex.aws.loadnet.stack/status) - Queries the current status of the stack in the given <code>region</code>.
-  [`convex.aws.loadnet.stack-set`](#convex.aws.loadnet.stack-set)  - Managing an AWS CloudFormation stack set.
    -  [`create`](#convex.aws.loadnet.stack-set/create) - Creates a stack set and retrieve all necessary information for starting a simulation, such as IP addresses of EC2 instances.
    -  [`delete`](#convex.aws.loadnet.stack-set/delete) - Deletes a stack set.
    -  [`describe`](#convex.aws.loadnet.stack-set/describe) - Queries a description of the current stack set.
-  [`convex.aws.loadnet.stack-set.op`](#convex.aws.loadnet.stack-set.op)  - Operations on AWS Cloudformation stack sets.
    -  [`create`](#convex.aws.loadnet.stack-set.op/create) - Creates stacks for required regions in the stack set, deploying all peers and load generators.
    -  [`delete`](#convex.aws.loadnet.stack-set.op/delete) - Deletes stacks from all regions of the stack set, stopping all peers and load generators.
    -  [`fetch`](#convex.aws.loadnet.stack-set.op/fetch) - Queries a Vector of stack instances.
    -  [`ip+`](#convex.aws.loadnet.stack-set.op/ip+) - Queries IPs of all peers and all load generators across all regions of the stack set.
    -  [`region->id`](#convex.aws.loadnet.stack-set.op/region->id) - Queries stack IDs for all regions.
-  [`convex.aws.loadnet.template`](#convex.aws.loadnet.template)  - Creating an AWS CloudFormation template for a stack containing all Load Generator and Peer instances, as well as any required configuration.
    -  [`load-generator+`](#convex.aws.loadnet.template/load-generator+) - Returns descriptions of Load Generators as template resources.
    -  [`mapping+`](#convex.aws.loadnet.template/mapping+) - Template Mappings for selecting the right AMI depending on in the region.
    -  [`net`](#convex.aws.loadnet.template/net) - Entry point for generating a template for a whole loadnet.
    -  [`output+`](#convex.aws.loadnet.template/output+) - Returns template outputs.
    -  [`parameter+`](#convex.aws.loadnet.template/parameter+) - Template parameters.
    -  [`region->ami`](#convex.aws.loadnet.template/region->ami) - Map of <code>AWS Region</code> -> <code>AMI ID</code>.
    -  [`resource+`](#convex.aws.loadnet.template/resource+) - Returns a description of all template resources.
    -  [`security-group`](#convex.aws.loadnet.template/security-group) - Security group allowing SSH and Convex Peers.
-  [`convex.aws.loadnet.template.peer`](#convex.aws.loadnet.template.peer)  - Generating parts of AWS CloudFormation templates relating to Peer instances.
    -  [`config-cloudwatch`](#convex.aws.loadnet.template.peer/config-cloudwatch) - Configuration for the AWS CloudWatch Agent.
    -  [`metadata`](#convex.aws.loadnet.template.peer/metadata) - Template metadata for Peer instances.
    -  [`resource`](#convex.aws.loadnet.template.peer/resource) - Returns a description of a Peer instance as a template resource.
    -  [`resource+`](#convex.aws.loadnet.template.peer/resource+) - Returns a description of Peer instances as template resources.
    -  [`user-data`](#convex.aws.loadnet.template.peer/user-data) - User Data used to run a scripts that installs on Peer instances everything that is necessary for the AWS CloudWatch Agent.

-----
# <a name="convex.aws">convex.aws</a>


Common utilities related to Cognitect's AWS API.




## <a name="convex.aws/client">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws.clj#L12-L39) `client`</a>
``` clojure

(client api region)
(client api region option+)
```


Creates a client for a given region.

   For providing credentials explicitly (as opposed to the Cognitect API retrieving
   them from the environment), provide the following options:

   | Key                            | Value                                        |
   |--------------------------------|----------------------------------------------|
   | `:convex.aws.access-key/id`    | ID of the access key to use                  |
   | `:convex.aws.access-key/secret | Secret associated with the access key to use |

## <a name="convex.aws/invoke">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws.clj#L43-L57) `invoke`</a>
``` clojure

(invoke client op request)
```


Invoke an operation using Cognitect's API.
  
   Throws in case of an anomaly.

-----
# <a name="convex.aws.loadnet">convex.aws.loadnet</a>


Running loadnets.
   
   See [`create`](#convex.aws.loadnet/create).




## <a name="convex.aws.loadnet/create">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet.clj#L38-L208) `create`</a>
``` clojure

(create env)
```


Creates and start a loadnet on AWS.

   Provisions the required number of Peers as well as Load Generators that
   will simulate User transactions. Logs the whole process and collects a
   whole series of performance metrics.


   Env is a Map providing options:

     `:convex.aws/account`
        AWS account number.
        Loadnet will be deployed as an AWS CloudFormation stack set in this account.

        It must have self-managed permissions by granting it the `AWSCloudFormationStackSetAdministrationRole`
        role and the `AWSCloudFormationStackSetExecutionRole` role.

        See [this guide](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/stacksets-prereqs-self-managed.html).

        Mandatory.

     `:convex.aws/region+`
       Vector of supported AWS regions (as Strings).
       Currently: ap-southeast-1, eu-central-1, us-east-1, us-west-1
       Mandatory.

     `:convex.aws.key/file`
       Path to private key used for connecting to all EC2 instances.
       The same key pair must be available in all regions under the same name.
       Mandatory.

     `:convex.aws.loadnet/dir`
       Path to directory where all data, metrics, and statistics will be persisted.
       Defaults to `"./"`.

     `:convex.aws.loadnet/timer`
       Number of minutes the simulation load will run before shutting down the loadnet.
       Defaults to `nil`, meaning the simulation must be stopped manually.
       For meaningful results, run for at least 5 minutes.
       See [`stop`](#convex.aws.loadnet/stop).

     `:convex.aws.loadnet.peer/native?`
       If `true` (default), Peers will run on the JVM (advised for much better performance).
       If `false`, they will run natively, compiled with GraalVM Native-Image.

     `:convex.aws.loadnet.scenario/path`
       Actor path to a simulation scenario from the following repository (as a List).

     `:convex.aws.loadnet.scenario/param+`
       Configuration for the simulation scenario chosen in `:convex.aws.loadnet.scenario/path`
       (as a Map).
    
     `:convex.aws.region/n.peer`
       Number of Load Generators to deploy per region.
       Defaults to `3`.

     `:convex.aws.region/n.peer`
       Number of Peers to deploy per region.
       Defaults to `8`.
    
     `:convex.aws.stack/parameter+`
       Map of parameters for the CloudFormation template:

          `:DetailedMonitoring`
            Enables 1-minute resolution on CloudWatch metrics (but incurs extra cost).
            Defaults to `"true"` (Boolean as a String).

          `:KeyName`
            Name of the key pair as registered on AWS.
            Related to the `:convex.aws.key/file` option.

          `:InstanceTypeLoad`
            EC2 instance type to use for Load Generators.
            Defaults to `"t2.micro"`.

          `:InstanceTypePeer`
            EC2 instance type to use for Peers.
            Defaults to `"m4.2xlarge"`.
    
     `:convex.aws.stack/tag+`
       Map of AWS tags to attach to the CloudFormation stack set, thus on all created
       resources.


   Uses the [Cognitect AWS API](https://github.com/cognitect-labs/aws-api).
   Follow instructions there for credentials.

   You must also create a `CloudWatchAgentServerRole` that Peer instances will use.
   See [this guide](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/create-iam-roles-for-cloudwatch-agent-commandline.html).

   
   Returns `env` augmented with extra information.


   E.g. Simulation of Automated Market Maker operations over 10 Peers in
        a single region, with 20 Load Generators deployed alongside to
        emulate 2000 Users trading between 5 fungible tokens:

   ```clojure
   (def env
        (create {:convex.aws/account                 "513128561298"
                 :convex.aws/region+                 ["eu-central-1"]
                 :convex.aws.key/file                "some/dir/Test"
                 :convex.aws.loadnet/dir             "/tmp/loadnet"
                 :convex.aws.loadnet/timer           10
                 :convex.aws.loadnet.scenario/path   '(lib sim scenario torus)
                 :convex.aws.loadnet.scenario/param+ {:n.token 5
                                                      :n.user  2000}
                 :convex.aws.region/n.load           20
                 :convex.aws.region/n.peer           10
                 :convex.aws.stack/parameter+        {:KeyName "Test"}}
                 :convex.aws.stack/tag+              {:Project "Foo"}}))
   ```

## <a name="convex.aws.loadnet/start">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet.clj#L226-L257) `start`</a>
``` clojure

(start env)
```


If [`create`](#convex.aws.loadnet/create) has trouble awaiting SSH connections to EC2 instances (as logged),
   run this with the returned `env` to try starting the simulation again on currently
   deployed machines.

## <a name="convex.aws.loadnet/stop">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet.clj#L261-L286) `stop`</a>
``` clojure

(stop env)
```


Stops a simulation and deletes the whole CloudFormation stack set.

   Key data and metrics will be logged to STDOUT.
  
   Returns `env` augmented with that kind of results.

-----

-----
# <a name="convex.aws.loadnet.cloudformation">convex.aws.loadnet.cloudformation</a>


Generic utilities related to AWS Cloudformation.




## <a name="convex.aws.loadnet.cloudformation/client">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/cloudformation.clj#L11-L32) `client`</a>
``` clojure

(client env)
(client env region)
```


Creates a CloudFormation client for the given region (defaults to
   the first region in `:convex.aws/region+`).

## <a name="convex.aws.loadnet.cloudformation/client+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/cloudformation.clj#L36-L44) `client+`</a>
``` clojure

(client+ env)
```


Creates CloudFormation clients for all regions in `:convex.aws/region+`.

## <a name="convex.aws.loadnet.cloudformation/invoke">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/cloudformation.clj#L48-L67) `invoke`</a>
``` clojure

(invoke env op request)
(invoke env region op request)
```


Invokes a CloudFormation operation.

## <a name="convex.aws.loadnet.cloudformation/param+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/cloudformation.clj#L71-L79) `param+`</a>
``` clojure

(param+ env)
```


Turns a Map of `parameter` -> `value` into the format that AWS understands.

-----
# <a name="convex.aws.loadnet.cloudwatch">convex.aws.loadnet.cloudwatch</a>


Collecting EC2 instance metrics from AWS CloudWatch for Peers:
  
   - CPU utilization
   - Memory usage
   - Network volume (inbound and outbound)




## <a name="convex.aws.loadnet.cloudwatch/client+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/cloudwatch.clj#L86-L100) `client+`</a>
``` clojure

(client+ env)
```


Creates CloudWatch clients for all regions in the stack set.

## <a name="convex.aws.loadnet.cloudwatch/download">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/cloudwatch.clj#L302-L310) `download`</a>
``` clojure

(download env)
```


Retrieves all metrics and computes statistics.

## <a name="convex.aws.loadnet.cloudwatch/fetch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/cloudwatch.clj#L119-L141) `fetch`</a>
``` clojure

(fetch env)
```


Retrieves metrics from all regions.

## <a name="convex.aws.loadnet.cloudwatch/fetch-region">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/cloudwatch.clj#L145-L226) `fetch-region`</a>
``` clojure

(fetch-region env region)
```


Retrieves metrics from the given `region`.

## <a name="convex.aws.loadnet.cloudwatch/stat+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/cloudwatch.clj#L230-L296) `stat+`</a>
``` clojure

(stat+ env)
```


Computes and logs statistics about metrics.

-----
# <a name="convex.aws.loadnet.default">convex.aws.loadnet.default</a>


Default values used throughout this module




## <a name="convex.aws.loadnet.default/detailed-monitoring">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/default.clj#L9-L13) `detailed-monitoring`</a>

Detailed Monitoring of EC2 instances enabled by default.

## <a name="convex.aws.loadnet.default/dir">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/default.clj#L17-L21) `dir`</a>

Working directory for a simulation is the current working directory by default.

## <a name="convex.aws.loadnet.default/instance-type-load">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/default.clj#L25-L29) `instance-type-load`</a>

Default EC2 instance type for load generators.

## <a name="convex.aws.loadnet.default/instance-type-peer">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/default.clj#L33-L37) `instance-type-peer`</a>

Default EC2 instance type for peers.

## <a name="convex.aws.loadnet.default/n-load">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/default.clj#L41-L45) `n-load`</a>

Default number of load generators per region.

## <a name="convex.aws.loadnet.default/n-peer">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/default.clj#L49-L53) `n-peer`</a>

Default number of peers per region.

## <a name="convex.aws.loadnet.default/peer-native?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/default.clj#L57-L61) `peer-native?`</a>

Peers will run on the JVM by default, as opposed to running natively.

-----
# <a name="convex.aws.loadnet.load">convex.aws.loadnet.load</a>


Managing Load Generators.




## <a name="convex.aws.loadnet.load/start">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/load.clj#L13-L80) `start`</a>
``` clojure

(start env)
```


Starts all Load Generators on EC2 instances (if needed).

   Monitors and logs any Load Generator that seemingly terminates before the simulated
   ends, meaning something went wrong and simulated users were not able to transact
   anymore.

## <a name="convex.aws.loadnet.load/stop">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/load.clj#L84-L108) `stop`</a>
``` clojure

(stop env)
```


Kills all Load Generators (if any).

-----
# <a name="convex.aws.loadnet.load.log">convex.aws.loadnet.load.log</a>


Log analysis of load generators for computing finality.




## <a name="convex.aws.loadnet.load.log/dir">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/load/log.clj#L18-L26) `dir`</a>
``` clojure

(dir env)
```


Returns the directory where all logs will be downloaded from load generator
   instances.

## <a name="convex.aws.loadnet.load.log/download">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/load/log.clj#L30-L54) `download`</a>
``` clojure

(download env)
```


Downloads all logs from load generator instances.
  
   Also see [`dir`](#convex.aws.loadnet.load.log/dir).

## <a name="convex.aws.loadnet.load.log/stat+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/load/log.clj#L58-L113) `stat+`</a>
``` clojure

(stat+ env)
```


Statistical analysis of load generator logs.
  
   For the time being, computes the finality only.

-----
# <a name="convex.aws.loadnet.log">convex.aws.loadnet.log</a>


Logging setup redirecting entries directly to the STDOUT file descriptor.
   
   Plays nicely at the REPL.




## <a name="convex.aws.loadnet.log/newline">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/log.clj#L16-L18) `newline`</a>

## <a name="convex.aws.loadnet.log/out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/log.clj#L22-L24) `out`</a>

-----
# <a name="convex.aws.loadnet.peer">convex.aws.loadnet.peer</a>


Managing peer instances.




## <a name="convex.aws.loadnet.peer/log+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/peer.clj#L209-L232) `log+`</a>
``` clojure

(log+ env)
```


Downloads logs from all Peers.

## <a name="convex.aws.loadnet.peer/start">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/peer.clj#L164-L174) `start`</a>
``` clojure

(start env)
```


Starts all Peer processes on EC2 instances.

## <a name="convex.aws.loadnet.peer/start-genesis">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/peer.clj#L67-L103) `start-genesis`</a>
``` clojure

(start-genesis env)
```


Starts a Genesis Peer on a remote EC2 instance.

## <a name="convex.aws.loadnet.peer/start-syncer+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/peer.clj#L107-L158) `start-syncer+`</a>
``` clojure

(start-syncer+ env)
```


Starts Syncing Peers on EC2 instance.
   They are gradually bootstrapped by syncing against the Genesis Peer, and then between themselves.

## <a name="convex.aws.loadnet.peer/stop">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/peer.clj#L178-L203) `stop`</a>
``` clojure

(stop env)
```


Stops all Peer processes.

-----
# <a name="convex.aws.loadnet.peer.etch">convex.aws.loadnet.peer.etch</a>


Downlading Etch instances from peer instances and conducting statistical analysis.




## <a name="convex.aws.loadnet.peer.etch/download">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/peer/etch.clj#L21-L51) `download`</a>
``` clojure

(download env)
(download env i-peer)
```


Downloads the Etch instance of a peer (peer 0 by default, aka Genesis Peer).

## <a name="convex.aws.loadnet.peer.etch/stat+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/peer/etch.clj#L57-L186) `stat+`</a>
``` clojure

(stat+ env)
(stat+ env i-peer)
```


Computes and outputs statistics on an Etch instance that has been [`download`](#convex.aws.loadnet.peer.etch/download)ed.

   Offers insights on consensus and key values like Transactions Per Second.

-----
# <a name="convex.aws.loadnet.rpc">convex.aws.loadnet.rpc</a>


Remote operations on EC2 instances through SSH.




## <a name="convex.aws.loadnet.rpc/await-ssh">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/rpc.clj#L43-L90) `await-ssh`</a>
``` clojure

(await-ssh env)
```


Tries to await all SSH servers from load generator and peer instances.

   Returns a Boolean in `env` under `:convex.aws.loadnet/ssh-ready?` indicating success.

## <a name="convex.aws.loadnet.rpc/cvx">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/rpc.clj#L144-L167) `cvx`</a>
``` clojure

(cvx env k-ip+ i-ip cell)
(cvx env k-ip+ i-ip cell option+)
```


Executes CVX code (`cell`) on a remote instance using the Convex Shell (native).
  
   See [`ssh`](#convex.aws.loadnet.rpc/ssh) for arguments.

## <a name="convex.aws.loadnet.rpc/jcvx">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/rpc.clj#L171-L206) `jcvx`</a>
``` clojure

(jcvx env k-ip+ i-ip cell)
(jcvx env k-ip+ i-ip cell option+)
```


Executes CVX code (`cell`) on a remote instance using the Convex Shell (JVM).
  
   See [`ssh`](#convex.aws.loadnet.rpc/ssh) for arguments.

## <a name="convex.aws.loadnet.rpc/kill-process">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/rpc.clj#L210-L222) `kill-process`</a>
``` clojure

(kill-process env k-ip+ i-ip file-pid)
```


Kills a remote process through [`ssh`](#convex.aws.loadnet.rpc/ssh).

## <a name="convex.aws.loadnet.rpc/rsync">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/rpc.clj#L226-L266) `rsync`</a>
``` clojure

(rsync env k-ip+ i-ip src dest)
(rsync env k-ip+ i-ip src dest option+)
```


Performs RSYNC on a remote instance.
  
   Uses [`ssh`](#convex.aws.loadnet.rpc/ssh) as a remote shell.

## <a name="convex.aws.loadnet.rpc/ssh">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/rpc.clj#L94-L121) `ssh`</a>
``` clojure

(ssh env k-ip+ i-ip command)
(ssh env k-ip+ i-ip command option+)
```


Executes a remote command through SSH given the key in `env` where IPs
   are stored (`:convex.aws.ip/load+` or `:convex.aws.ip/peer+`) and the
   index of the IP to use.

   Options are forwarded to `protosens.process/run` (used to run the local
   SSH process).

## <a name="convex.aws.loadnet.rpc/worker">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/rpc.clj#L270-L302) `worker`</a>
``` clojure

(worker env k-ip+ i-ip cell)
```


Executes CVX code (`cell`) on a remote instance using the Convex Shell (native)
   to connect to the peer worker.
  
   See [`ssh`](#convex.aws.loadnet.rpc/ssh) for arguments.

-----
# <a name="convex.aws.loadnet.stack">convex.aws.loadnet.stack</a>


AWS operations relating to CloudFormation stacks.




## <a name="convex.aws.loadnet.stack/describe">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack.clj#L30-L44) `describe`</a>
``` clojure

(describe env region)
```


Queries a description the stack of the given `region`.

## <a name="convex.aws.loadnet.stack/ip+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack.clj#L48-L71) `ip+`</a>
``` clojure

(ip+ env region)
```


Queries a Vector of instance IPs for the given `region`.
  
   First the IPs of load generators, then the IPs of peers.

## <a name="convex.aws.loadnet.stack/output+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack.clj#L75-L83) `output+`</a>
``` clojure

(output+ env region)
```


Queries the stack outputs of the given `region`.

## <a name="convex.aws.loadnet.stack/peer-instance+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack.clj#L87-L97) `peer-instance+`</a>
``` clojure

(peer-instance+ env region)
```


Queries a Vector of information about peer instances in the given `region`.

## <a name="convex.aws.loadnet.stack/peer-instance-id+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack.clj#L101-L109) `peer-instance-id+`</a>
``` clojure

(peer-instance-id+ env region)
```


Queries a Vector of peer instance IDs for the given `region`.

## <a name="convex.aws.loadnet.stack/resrc+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack.clj#L113-L126) `resrc+`</a>
``` clojure

(resrc+ env region)
```


Queries a sequence of resources for the stack in the `given` region.

## <a name="convex.aws.loadnet.stack/status">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack.clj#L130-L138) `status`</a>
``` clojure

(status env region)
```


Queries the current status of the stack in the given `region`.

-----
# <a name="convex.aws.loadnet.stack-set">convex.aws.loadnet.stack-set</a>


Managing an AWS CloudFormation stack set.




## <a name="convex.aws.loadnet.stack-set/create">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack_set.clj#L16-L92) `create`</a>
``` clojure

(create env)
```


Creates a stack set and retrieve all necessary information for starting a simulation,
   such as IP addresses of EC2 instances.

## <a name="convex.aws.loadnet.stack-set/delete">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack_set.clj#L96-L124) `delete`</a>
``` clojure

(delete env)
```


Deletes a stack set.
  
   If required, also deletes existing stacks first (a necessary condition).

## <a name="convex.aws.loadnet.stack-set/describe">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack_set.clj#L128-L138) `describe`</a>
``` clojure

(describe env)
```


Queries a description of the current stack set.

-----
# <a name="convex.aws.loadnet.stack-set.op">convex.aws.loadnet.stack-set.op</a>


Operations on AWS Cloudformation stack sets.




## <a name="convex.aws.loadnet.stack-set.op/create">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack_set/op.clj#L55-L88) `create`</a>
``` clojure

(create env)
```


Creates stacks for required regions in the stack set, deploying all peers and load generators.

## <a name="convex.aws.loadnet.stack-set.op/delete">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack_set/op.clj#L92-L106) `delete`</a>
``` clojure

(delete env)
```


Deletes stacks from all regions of the stack set, stopping all peers and load generators.

## <a name="convex.aws.loadnet.stack-set.op/fetch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack_set/op.clj#L110-L128) `fetch`</a>
``` clojure

(fetch env)
```


Queries a Vector of stack instances.

## <a name="convex.aws.loadnet.stack-set.op/ip+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack_set/op.clj#L132-L191) `ip+`</a>
``` clojure

(ip+ env)
```


Queries IPs of all peers and all load generators across all regions of the stack set.

   Returned in `env` respectively under `:convex.aws.ip/peer+` and `convex.aws.ip/load+`.

## <a name="convex.aws.loadnet.stack-set.op/region->id">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/stack_set/op.clj#L195-L212) `region->id`</a>
``` clojure

(region->id env)
```


Queries stack IDs for all regions.
  
   Returned in `env` under `:convex.aws.stack/region->id`.

-----
# <a name="convex.aws.loadnet.template">convex.aws.loadnet.template</a>


Creating an AWS CloudFormation template for a stack containing
   all Load Generator and Peer instances, as well as any required
   configuration.




## <a name="convex.aws.loadnet.template/load-generator+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/template.clj#L84-L107) `load-generator+`</a>
``` clojure

(load-generator+ name-load+ ebs)
```


Returns descriptions of Load Generators as template resources.

## <a name="convex.aws.loadnet.template/mapping+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/template.clj#L27-L36) `mapping+`</a>

Template Mappings for selecting the right AMI depending on in the region.

## <a name="convex.aws.loadnet.template/net">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/template.clj#L151-L184) `net`</a>
``` clojure

(net)
(net env)
```


Entry point for generating a template for a whole loadnet.

## <a name="convex.aws.loadnet.template/output+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/template.clj#L111-L131) `output+`</a>
``` clojure

(output+ name-load+ name-peer+)
```


Returns template outputs.
   Used for collecting IP addresses of Load Generator and Peer instances.

## <a name="convex.aws.loadnet.template/parameter+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/template.clj#L60-L78) `parameter+`</a>

Template parameters.

## <a name="convex.aws.loadnet.template/region->ami">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/template.clj#L14-L21) `region->ami`</a>

Map of `AWS Region` -> `AMI ID`.

## <a name="convex.aws.loadnet.template/resource+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/template.clj#L135-L145) `resource+`</a>
``` clojure

(resource+ name-load+ name-peer+ ebs)
```


Returns a description of all template resources.

## <a name="convex.aws.loadnet.template/security-group">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/template.clj#L40-L56) `security-group`</a>

Security group allowing SSH and Convex Peers.

-----
# <a name="convex.aws.loadnet.template.peer">convex.aws.loadnet.template.peer</a>


Generating parts of AWS CloudFormation templates relating to Peer instances.




## <a name="convex.aws.loadnet.template.peer/config-cloudwatch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/template/peer.clj#L22-L38) `config-cloudwatch`</a>

Configuration for the AWS CloudWatch Agent.
   Used to monitor memory usage on Peer instances.

   Cf. https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch-Agent-Configuration-File-Details.html
       https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/metrics-collected-by-CloudWatch-agent.html#linux-metrics-enabled-by-CloudWatch-agent

## <a name="convex.aws.loadnet.template.peer/metadata">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/template/peer.clj#L42-L109) `metadata`</a>
``` clojure

(metadata name-peer)
```


Template metadata for Peer instances.
   
   Provides the necessary configuration for running Cfn-init and installing the CloudWatch Agent.

   Cf. [`config-cloudwatch`](#convex.aws.loadnet.template.peer/config-cloudwatch)
       https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cfn-helper-scripts-reference.html

## <a name="convex.aws.loadnet.template.peer/resource">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/template/peer.clj#L143-L166) `resource`</a>
``` clojure

(resource i-peer name-peer ebs)
```


Returns a description of a Peer instance as a template resource.

## <a name="convex.aws.loadnet.template.peer/resource+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/template/peer.clj#L170-L182) `resource+`</a>
``` clojure

(resource+ name-peer+ ebs)
```


Returns a description of Peer instances as template resources.

## <a name="convex.aws.loadnet.template.peer/user-data">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/aws.loadnet/src/main/clj/convex/aws/loadnet/template/peer.clj#L113-L137) `user-data`</a>
``` clojure

(user-data name-peer)
```


User Data used to run a scripts that installs on Peer instances everything that is necessary for
   the AWS CloudWatch Agent.

   Cf. [`config-cloudwatch`](#convex.aws.loadnet.template.peer/config-cloudwatch)
       [`metadata`](#convex.aws.loadnet.template.peer/metadata)
