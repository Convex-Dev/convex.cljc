(ns convex.lisp.dev

  "CLJC playground during dev."

  {:author "Adam Helinski"}

  (:require [ajax.core   :as http]
            [convex.lisp :as $]
            [clojure.pprint]
            #?(:clj [clojure.reflect]))
  #?(:clj (:import (convex.core Init
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
      $/convex->clojure
      :accounts
      (->> (into []
                 (comp (map :environment)
                       (filter some?))))
      clojure.pprint/pprint)



  (-> "[:a
        #51
        0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff]"
      $/read-string
      $/convex->edn
      $/read-edn
      )



  ; lang.expanders.Expander

  ))
