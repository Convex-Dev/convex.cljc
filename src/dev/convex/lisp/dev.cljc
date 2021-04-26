(ns convex.lisp.dev

  "CLJC playground during dev."

  {:author "Adam Helinski"}

  (:require [ajax.core                 :as http]
            [convex.lisp               :as $]
            [clojure.pprint]
            #?(:clj [clojure.reflect])
            [malli.core                :as malli])
  #?(:clj (:import (convex.core Init
                                State)
                   (convex.core.crypto AKeyPair
                                       Symmetric)
                   (convex.core.data Keyword
                                     Strings
                                     Symbol)
                   convex.core.data.prim.CVMDouble
                   (convex.core.lang Context
                                     RT))))


#?(:clj (set! *warn-on-reflection*
              true))

;;;;;;;;;;


#?(:clj (comment


  (-> State
      clojure.reflect/reflect
      clojure.pprint/pprint)

  State/EMPTY


  (-> Init/STATE
      $/to-clojure
      :accounts
      (->> (into []
                 (comp (map :environment)
                       (filter some?))))
      clojure.pprint/pprint)


  (do
    (-> Init/STATE
        $/convex->edn
        ;(->> (spit "/tmp/convex.edn"))
        $/read-edn
        ;clojure.pprint/pprint
        )
    nil)


  (-> "[:a
        #51
        0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff]"
      $/read
      $/to-edn
      $/read-edn
      )

  
  (AKeyPair/generate)



  ; lang.expanders.Expander

  

  (def ctx
       ($/context))


  (-> "[:a :b"
      ;[:a '(if true 42 0)]
      str
      $/read
      $/expand-compile
      $/result
      $/to-clojure
      )


  (-> (.run ctx
            (-> ctx
                (.expandCompile (-> `(~'transfer (~'address ~(.longValue Init/VILLAIN))
                                                 3)
                                    #_'(do
                                       (defn foo [x]
                                         (if (> x
                                                0)
                                           (recur (dec x))
                                           x))
                                       (foo 1e3))
                                    str
                                    $/read))
                .getResult))
      .getState
      (.getAccount Init/HERO)
      .getBalance
      ;.getResult
      $/to-clojure
      )


  


  ))
