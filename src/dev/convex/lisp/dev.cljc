(ns convex.lisp.dev

  "CLJC playground during dev."

  {:author "Adam Helinski"}

  (:require [ajax.core                     :as http]
            [convex.lisp                   :as $]
            [convex.lisp.hex               :as $.hex]
            [convex.lisp.schema            :as $.schema]
			[clojure.data]
            [clojure.pprint]
            #?(:clj [clojure.reflect])
            [clojure.walk]
            [clojure.test.check.properties :as tc.prop]
            [clojure.test.check.results    :as tc.result]
            [malli.core                    :as malli]
            [malli.generator               :as malli.gen])
  #?(:clj (:import clojure.lang.RT
                   (convex.core Init
                                State))))


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
        $/to-edn
        ;(->> (spit "/tmp/convex.edn"))
        $/read-edn
        ;clojure.pprint/pprint
        )
    nil)


  


  ; lang.expanders.Expander

  

  (-> 
      '(address 123456789)
      $/from-clojure
      $/eval
      $/result
      $/to-clojure
      )


  (-> 
      "'[(unquote)]"
      str
      $/read
      $/expand-compile
      $/query
      $/result
      ;$/to-clojure
      )

  
  (time
    (do
      ;(malli/validate :convex/hash
      ;                (symbol "0x0000000000000000000000000000000000000000000000000000000000000001")
      (malli.gen/generate :convex/blob-8
                          {:registry (-> (malli/default-schemas)
                                         $.schema/registry
                                         )
                           :size     5
                           })
      nil))






  ))
