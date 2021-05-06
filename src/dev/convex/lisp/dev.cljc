(ns convex.lisp.dev

  "CLJC playground during dev."

  {:author "Adam Helinski"}

  (:require [ajax.core                     :as http]
            [convex.lisp                   :as $]
            [convex.lisp.hex               :as $.hex]
            [convex.lisp.schema            :as $.schema]
            [convex.lisp.test.core]
            [convex.lisp.test.core.coerce]
            [convex.lisp.test.core.coll]
            [convex.lisp.test.core.fn]
            [convex.lisp.test.core.math]
            [convex.lisp.test.core.pred]
            [convex.lisp.test.data]
            [convex.lisp.test.eval]
            [convex.lisp.test.prop]
            [convex.lisp.test.schema]
            [convex.lisp.test.util         :as $.test.util]
			[clojure.data]
            [clojure.pprint]
            #?(:clj [clojure.reflect])
            [clojure.test.check.generators :as tc.gen]
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
     '(hash-map [] :vec '() :list)
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
      ;(malli/validate [:not [:enum 1 2]]
      ;                3
      (malli.gen/generate :convex/vector
                          {:registry (-> (malli/default-schemas)
                                         $.schema/registry
                                         )
                           :size     5
                           })
      nil))






  ))
