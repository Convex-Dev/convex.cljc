(ns convex.dev.lib.asset

  "Dev environment for the Asset library."

  {:author "Adam Helinski"}

  (:require [convex.cvm      :as $.cvm]
            [convex.cvm.eval :as $.cvm.eval]
            [convex.lisp     :as $.lisp]))


;;;;;;;;;;


(comment


  (def w*ctx
       ($.cvm/watch {"src/convex/lib/asset.cvx" '$}))

  (.close w*ctx)

  
  ($.cvm.eval/result @w*ctx
                     ($.lisp/templ* (do
                                      (def asset
                                           (deploy (quote
                                                     (do

                                                       (def offer+
                                                            {})

                                                       (def owner->quantity
                                                            {})

                                                       (defn accept [sender quantity]
                                                         (let [offer (get-in offer*
                                                                             [sender
                                                                              *caller*])]
                                                           (cond
                                                             (nil? offer)
                                                             (fail :STATE
                                                                   "No offer from that sender")

                                                             (< offer
                                                                quantity)
                                                             (fail :FUNDS
                                                                   "Requested quantity is bigger than offer")

                                                             :else
                                                             (do
                                                               (def owner->quantity
                                                                    (assoc owner->quantity
                                                                           *caller*
                                                                           (+ (owner->quantity *caller*)
                                                                              quantity)))
                                                               (def offer+
                                                                    (let [offer-sender (dissoc (offer+ *caller*))]
                                                                      (if (empty? offer-sender)
                                                                        (dissoc offer+
                                                                                *caller*)
                                                                        (assoc offer+
                                                                               *caller*
                                                                               offer-sender))))))))

                                                       (defn get-offer [sender receiver]
                                                         (*offer+ sender))

                                                       (defn offer [receiver quantity]
                                                         (def offer+
                                                              (assoc offer+
                                                                     (assoc (offer+ *caller*)
                                                                            receiver
                                                                            (+ quantity
                                                                               (or (get-in offer+
                                                                                           [*caller*
                                                                                            receiver])
                                                                                   ))))))

                                                         
                                                         
                                                       ))))
                                      ;($/transfer addr
                                      ;            42
                                      ;            {:some :data})
                                      )))
                     

  )
