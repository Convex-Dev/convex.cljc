(ns convex.aws

  "Common utilities related to Cognitect's AWS API."

  (:require [cognitect.aws.client.api  :as aws]
            [cognitect.aws.credentials :as aws.cred]))


;;;;;;;;;;


(defn client

  "Creates a client for a given region.

   For providing credentials explicitly (as opposed to the Cognitect API retrieving
   them from the environment), provide the following options:

   | Key                            | Value                                        |
   |--------------------------------|----------------------------------------------|
   | `:convex.aws.access-key/id`    | ID of the access key to use                  |
   | `:convex.aws.access-key/secret | Secret associated with the access key to use |"


  ([api region]

   (client api
           region
           nil))


  ([api region option+]

   (aws/client (merge {:api    api
                       :region region}
                      (when-some [key-id (:convex.aws.access-key/id option+)]
                        {:credentials-provider (aws.cred/basic-credentials-provider
                                                 {:access-key-id     key-id
                                                  :secret-access-key (option+ :convex.aws.access-key/secret)})})))))



(defn invoke

  "Invoke an operation using Cognitect's API.
  
   Throws in case of an anomaly."

  [client op request]

  (let [result (aws/invoke client
                           {:op      op
                            :request request})]
    (when (result :cognitect.anomalies/category)
      (throw (ex-info "Error while executing AWS operation"
                      result)))
    result))
