(ns convex.aws

  (:require [cognitect.aws.client.api  :as aws]
            [cognitect.aws.credentials :as aws.cred]))


;;;;;;;;;;


(defn client


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

  [client op request]

  (let [result (aws/invoke client
                           {:op      op
                            :request request})]
    (when (result :cognitect.anomalies/category)
      (throw (ex-info "Error while executing AWS operation"
                      result)))
    result))
