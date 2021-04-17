(ns convex.lisp.dev

  "CLJC playground during dev."

  {:author "Adam Helinski"}

  (:require [ajax.core   :as http]
            [convex.lisp :as $]
            [clojure.pprint]
            #?(:clj [clojure.reflect]))
  #?(:clj (:import convex.core.State)))


#?(:clj (set! *warn-on-reflection*
              true))

;;;;;;;;;;


#?(:clj (comment


  (-> State
      clojure.reflect/reflect
      clojure.pprint/pprint)

  State/EMPTY



  ))
