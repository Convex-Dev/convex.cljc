(ns convex.lisp.dev

  "CLJC playground during dev."

  {:author "Adam Helinski"}

  (:require [ajax.core                     :as http]
            [convex.lisp                   :as $]
            [convex.lisp.schema            :as $.schema]
			[clojure.data]
            [clojure.pprint]
            #?(:clj [clojure.reflect])
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
        $/convex->edn
        ;(->> (spit "/tmp/convex.edn"))
        $/read-edn
        ;clojure.pprint/pprint
        )
    nil)


  


  ; lang.expanders.Expander

  

  (-> 
      ;'(concat [1 2] {:a :b})
      ;$/from-clojure
      "1e6"
      $/read
      $/eval
      $/result
      $/to-clojure
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

  

  (tc.prop/for-all [x (malli.gen/generator :int)]
    (double? x)
    (int? x))


  (time
    (do
      ;(malli/validate :convex/blob
      ;                (symbol "0xa7bb")
      (malli.gen/generate :convex/double
                          {:registry (-> (malli/default-schemas)
                                         $.schema/registry
                                         )
                           :size     5
                           })
      nil))




  ))
