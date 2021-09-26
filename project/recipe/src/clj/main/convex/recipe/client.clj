(ns convex.recipe.client

  ""

  {:author "Adam Helinski"}

  (:require [clojure.pprint]
            [convex.cell            :as $.cell]
            [convex.client          :as $.client]
            [convex.read            :as $.read]
            [convex.recipe.key-pair :as $.recipe.key-pair]
            [convex.recipe.rest     :as $.recipe.rest]))


;;;;;;;;;;


(defn result

  ""

  [future]

  (let [resp (deref future
                    4000
                    nil)]
    (when resp
      (let[error-code ($.client/error-code resp)
           value      ($.client/value resp)]
       (if error-code
         {:error?     true
          :error-code error-code
          :message    value
          :trace      ($.client/trace resp)}
         {:error? false
          :return value})))))



;;;;;;;;;;


(comment

  


  (def c
       ($.client/connect {:convex.server/host "convex.world"
                          :convex.server/port 18888}))



  (-> ($.client/query c
                      ($.cell/address 1)
                      ($.read/string "(+ 2 2)"))
      deref
      str)


  (-> ($.client/query c
                      ($.cell/address 1)
                      ($.read/string "(+ 2 2)"))
      deref
      $.client/value)


  (-> ($.client/query c
                      ($.cell/address 1)
                      ($.read/string "(inc [])"))
      result
      clojure.pprint/pprint)



  (def kp
       ($.recipe.key-pair/retrieve "private/recipe/client"))



  (def addr
       (let [addr ($.recipe.rest/create-account kp)]
         ($.recipe.rest/request-coin+ addr
                                      100000000)
         ($.cell/address addr)))


  (-> ($.client/query c
                      addr
                      ($.cell/symbol "*balance*"))
      deref
      str)


  (defn seq-id
    []
    (deref ($.client/sequence c
                              addr)))


  (-> ($.client/transact c
                         kp
                         ($.cell/invoke addr
                                        (seq-id)
                                        ($.read/string "(def foo 42)")))
      deref
      str)


  (-> ($.client/query c
                      addr
                      ($.cell/symbol "foo"))
      deref
      str)


  ($.client/close c)


  )
