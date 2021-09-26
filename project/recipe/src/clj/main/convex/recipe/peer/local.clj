(ns convex.recipe.peer.local

  ""

  {:author "Adam Helinski"}

  (:require [convex.cell            :as $.cell]
            [convex.client          :as $.client]
            [convex.db              :as $.db]
            [convex.read            :as $.read]
            [convex.recipe.key-pair :as $.recipe.key-pair]
            [convex.server          :as $.server]))


;;;;;;;;;; Creating a local server


(defn server

  ""

  [dir option+]

  ($.server/create ($.recipe.key-pair/retrieve dir)
                   (merge {:convex.server/db ($.db/open (str dir
                                                             "/db.etch"))}
                          option+)))


;;;;;;;;;;


(comment


  (def dir
       "private/recipe/peer/local")


  (def s
       (server dir
               {:convex.server/host "localhost"
                :convex.server/port 18888}))


  ($.server/start s)


  (def c
       ($.client/connect))


  (def addr
       ($.cell/address 12))


  (def kp
       ($.recipe.key-pair/retrieve dir))


  (-> ($.client/transact c
                         kp
                         ($.cell/invoke addr
                                        (deref ($.client/sequence c
                                                                  addr))
                                        ($.read/string "(def foo 42)")))
      deref
      str)


  (-> ($.client/query c
                      addr
                      ($.cell/symbol "foo"))
      deref
      str)
                         

  (do
    ($.client/close c)
    ($.server/stop s))


  )
