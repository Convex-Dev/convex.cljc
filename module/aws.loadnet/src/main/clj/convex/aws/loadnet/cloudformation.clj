(ns convex.aws.loadnet.cloudformation

  "Generic utilities related to AWS Cloudformation."

  (:require [convex.aws :as $.aws]))


;;;;;;;;;;


(defn client

  "Creates a CloudFormation client for the given region (defaults to
   the first region in `:convex.aws/region+`)."


  ([env]

   (client env
           nil))


  ([env region]

   (let [region-2 (or region
                      (first (env :convex.aws/region+)))]
     (assoc-in env
               [:convex.aws.client/cloudformation+
                region-2]
               ($.aws/client :cloudformation
                             region-2
                             env)))))



(defn client+

  "Creates CloudFormation clients for all regions in `:convex.aws/region+`."

  [env]

  (reduce client
          env
          (env :convex.aws/region+)))



(defn invoke

  "Invokes a CloudFormation operation."

  ([env op request]

   (invoke env
           nil
           op
           request))


  ([env region op request]

   ($.aws/invoke (get-in env
                         [:convex.aws.client/cloudformation+
                          (or region
                              (first (env :convex.aws/region+)))])
                 op
                 request)))



(defn param+

  "Turns a Map of `parameter` -> `value` into the format that AWS understands."
  [env]

  (mapv (fn [[k v]]
          {:ParameterKey   (name k)
           :ParameterValue v})
        (env :convex.aws.stack/parameter+)))
