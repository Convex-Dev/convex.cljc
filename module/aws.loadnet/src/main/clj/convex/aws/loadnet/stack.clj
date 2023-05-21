(ns convex.aws.loadnet.stack

  (:require [clojure.data.json           :as json]
            [clojure.string              :as string]
            [cognitect.aws.client.api    :as aws]
            [cognitect.aws.credentials   :as aws.cred]
            [convex.aws.loadnet.peer     :as $.aws.loadnet.peer]
            [convex.aws.loadnet.template :as $.aws.loadnet.template]
            [taoensso.timbre             :as log]))


(declare describe
         status)


;;;;;;;;;; Init


(defn client


  ([]

   (client nil))


  ([env]

   (assoc env
          :convex.aws.client/cloudformation
          (aws/client (merge {:api    :cloudformation
                              :region "eu-central-1"}
                      (when (contains? env
                                       :access-key-id)



                        {:credentials-provider (aws.cred/basic-credentials-provider env)}))))))


;;;;;;;;;; Operations


(defn- -await

  [env]

  (log/info "Awaiting stack creation")
  (loop []
    (let [status- (status env)]
      (case status-
        ;;
        "CREATE_IN_PROGRESS"
        (do
          (Thread/sleep 2000)
          (recur))
        ;;
        "CREATE_COMPLETE"
        (do
          ;; Sometimes instances need a little bit of time for their SSH server to start.
          (Thread/sleep 2000)
          env)
        ;;
        (throw (ex-info "Something failed while creating the stack"
                        {:convex.aws.stack/status status-}))))))



(defn- -invoke

  [env op request]

  (let [result (aws/invoke (env :convex.aws.client/cloudformation)
                           {:op      op
                            :request request})]
    (when (result :cognitect.anomalies/category)
      (throw (ex-info "Error while executing AWS operation"
                      result)))
    result))



(defn -ip-peer+

  [env]

  (log/info "Fetching IP addresses of all peer instances")
  (let [output+ (:Outputs (describe env))]
    (assoc env
           :convex.aws.ip/peer+
           (->> output+
                (keep (fn [output]
                       (let [^String k (output :OutputKey)]
                         (when (string/starts-with? k
                                                    "IpPeer")
                           [(Integer/parseInt (.substring k
                                                          6))
                            (output :OutputValue)]))))
                (sort-by first)
                (mapv second)))))


;;;


(defn cost

  [env]

  (-> (-invoke env
               :EstimateTemplateCost
               {:Parameters   [{:ParameterKey   "KeyName"
                                :ParameterValue "Test"}]
                :TemplateBody (json/write-str ($.aws.loadnet.template/net env))})
       (:Url)))



(defn create

  [env]

  (let [stack-name (or (:convex.aws.stack/name env)
                       (str "LoadNet-"
                            (System/currentTimeMillis)))
        _          (log/info (format "Creating stack for LoadNet called '%s'"
                                     stack-name))
        result     (-invoke env
                            :CreateStack
                            {;:OnFailure   "DELETE"
                             :Parameters   (mapv (fn [[k v]]
                                                   {:ParameterKey   (name k)
                                                    :ParameterValue v})
                                                 (env :convex.aws.stack/parameter+))
                             :StackName    stack-name
                             :Tags         (mapv (fn [[k v]]
                                                   {:Key   (name k)
                                                    :Value v})
                                                 (env :convex.aws.stack/tag+))
                             :TemplateBody (json/write-str ($.aws.loadnet.template/net env))})]
    (-> env
        (assoc :convex.aws.stack/id   (result :StackId)
               :convex.aws.stack/name stack-name)
        (-await)
        (-ip-peer+)
        ($.aws.loadnet.peer/start))))



(defn delete

  [env]

  (-invoke env
           :DeleteStack
           {:StackName (env :convex.aws.stack/name)})
  nil)



(defn describe

  [env]

  (-> (-invoke env
               :DescribeStacks
               {:StackName (env :convex.aws.stack/name)})
      (:Stacks)
      (first)))



(defn resrc+

  [env]

  (-> (-invoke env
               :DescribeStackResources
               {:StackName (env :convex.aws.stack/name)})
      (:StackResources)))



(defn status

  [env]

  (:StackStatus (describe env)))
