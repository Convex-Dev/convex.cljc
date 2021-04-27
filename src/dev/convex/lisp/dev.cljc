(ns convex.lisp.dev

  "CLJC playground during dev."

  {:author "Adam Helinski"}

  (:require [ajax.core                 :as http]
            [convex.lisp               :as $]
            [convex.lisp.schema        :as $.schema]
            [clojure.pprint]
            #?(:clj [clojure.reflect])
            [malli.core                :as malli]
            [malli.generator           :as malli.gen])
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


  (-> 'Infinity
      $/from-clojure
      $/eval
      $/result
      $/to-clojure
      )


  (-> 
      [:a '(if true 42 0)]
      str
      $/read
      $/expand-compile
      $/query
      $/result
      $/to-clojure
      )


  (def ctx
       (-> '(blob "f")
           str
           $/read
           $/eval
           ;$/result
           ;$/to-clojure
           ))

  (->> 42
       str
       $/read
       ($/eval ctx)
       $/result
       $/to-clojure)



  (def reg
       (-> (malli/default-schemas)
           $.schema/registry
           (assoc :list
                  (malli/-collection-schema {:empty '()
                                             :pred  list?
                                             :type  :list}))
           ))



  (time
    (do
      (malli.gen/sample :convex/list
                          {:registry (-> (malli/default-schemas)
                                         $.schema/registry
                                         )
                           :size     5
                           })
      nil))




  (-> x
      str
      $/read
      $/eval
      ;$/result
      .getExceptional
      )

  ))
