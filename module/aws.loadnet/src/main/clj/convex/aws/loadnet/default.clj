(ns convex.aws.loadnet.default
  
  "Default values used throughout this module")


;;;;;;;;;;


(def detailed-monitoring
  
  "Detailed Monitoring of EC2 instances enabled by default."

  "true")



(def dir

  "Working directory for a simulation is the current working directory by default."

  "./")



(def instance-type-load

  "Default EC2 instance type for load generators."

  "t2.micro")



(def instance-type-peer

  "Default EC2 instance type for peers."

  "m4.2xlarge")



(def n-iter-trx

  "Some scenarios support looping the transaction code N times."

  1)



(def n-load

  "Default number of load generators per region."

  3)



(def n-peer

  "Default number of peers per region."

  8)



(def peer-native?

  "Peers will run on the JVM by default, as opposed to running natively."

  false)



(def volume-load

  "In GB, volume for load generator instances."

  8)



(def volume-peer

  "In GB, volume for peer instances."

  16)
