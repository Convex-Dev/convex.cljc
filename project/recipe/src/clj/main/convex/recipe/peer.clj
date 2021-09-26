(ns convex.recipe.peer

  ""

  {:author "Adam Helinski"}

  (:require [clj-http.client   :as http]
            [clojure.data.json :as json]
            [clojure.edn       :as edn]
            [convex.cell       :as $.cell]
            [convex.client     :as $.client]
            [convex.db         :as $.db]

            [convex.cvm.db     :as $.cvm.db]

            [convex.form       :as $.form]
            [convex.pfx        :as $.pfx]
            [convex.read       :as $.read]
            [convex.server     :as $.server]
            [convex.sign       :as $.sign]))


;;;;;;;;;;


(defn declare-peer

  ""

  [key-pair address]

  (let [client ($.client/connect {:convex.server/host "convex.world"
                                  :convex.server/port 18888})]
    (try

      (deref ($.client/transact client
                                key-pair
                                ($.cell/invoke address
                                               1
                                               ($.form/create-peer ($.sign/account-key key-pair)
                                                                   ($.cell/long 50000000))))
             4000
             nil)

      (finally
        ($.client/close client)))))



(defn request-coin+

  ""

  [address-long]

  (http/post "https://convex.world/api/v1/faucet"
             {:body               (json/write-str {"address" address-long
                                                   "amount"  100000000})
              :connection-timeout 4000}))


(defn create-account

  ""

  [dir key-pair]

  (let [address-long (-> (http/post "https://convex.world/api/v1/createAccount"
                                    {:body               (json/write-str {"accountKey" ($.sign/hex-string key-pair)})
                                     :connection-timeout 4000})
                         :body
                         json/read-str
                         (get "address"))
        address      ($.cell/address address-long)]
    (request-coin+ address-long)
    (when (nil? (declare-peer key-pair
                              address))
      (throw (ex-info "Timeout when declaring peer!"
                      {})))
    (spit (str dir
               "/peer.edn")
          (pr-str {:address address-long}))
    address))



(defn address

  ""

  [dir key-pair]

  (try

    (-> (slurp (str dir
                    "/peer.edn"))
        edn/read-string
        (get :address)
        $.cell/address)
    
    (catch Throwable _ex
      (create-account dir
                      key-pair))))



(defn key-pair

  ""

  [dir]

  (let [file-key-store (str dir
                            "/keystore.pfx")]
    (try

      ($.pfx/key-pair-get ($.pfx/load file-key-store)
                          "my-peer"
                          "my-password")

      (catch Throwable _ex
        (let [key-pair ($.sign/ed25519)]
          (-> ($.pfx/create file-key-store)
              ($.pfx/key-pair-set "my-peer"
                                  key-pair
                                  "my-password")
              ($.pfx/save file-key-store))
          key-pair)))))




(defn create

  ""

  [dir option+]

  (let [kp   (key-pair dir)
        addr (address dir
                      kp)]
    {:address addr
     :server  ($.server/create kp
                               (merge {:convex.server/db    ($.db/open (str dir
                                                                            "/db.etch"))
                                       :convex.server/state [:sync
                                                             {:convex.server/host "convex.world"}]}
                                      option+))}))





;;;;;;;;;;


(comment


  (def dir
       "private/recipe/peer/")


  (time
    (let [{:keys [address
                  server]} (create "private/recipe/peer"
                                   nil)]

      (def a
           address)

      (def s
           server)))



  ($.server/start s)


  (def c
       ($.client/connect))



  (-> ($.client/query c
                      ($.cell/address 1)
                      ($.read/string "#4651/foo"))
      deref
      str)



  ($.server/stop s)


  )
