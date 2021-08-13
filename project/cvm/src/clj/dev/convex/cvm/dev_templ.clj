;; Meant to be copied as `dev.clj` and used privately for development.
;;
(ns convex.cvm.dev

  "Daydreaming at the REPL."

  {:clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [convex.cell  :as $.cell]
            [convex.read  :as $.read]
            [convex.cvm   :as $.cvm]
            [convex.write :as $.write]))


;;;;;;;;;;


(comment


  )
