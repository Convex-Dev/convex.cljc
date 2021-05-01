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
      ;'*initial-expander*
      '*juice*
      $/from-clojure
      $/eval
      $/result
      ;$/to-clojure
      )


  (-> 
      "1e200"
      str
      $/read
      $/expand-compile
      $/query
      $/result
      $/to-clojure
      )

  
  (time
    (do
      ;(malli/validate :convex/blob
      ;                (symbol "0xa7bb")
      (malli.gen/generate :convex/hexstring-32
                          {:registry (-> (malli/default-schemas)
                                         $.schema/registry
                                         )
                           :size     50
                           })
      nil))






  ))
