(ns convex.gen.dev

  "Daydreaming at the REPL."

  {:clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [convex.cell                   :as $.cell]
            [convex.gen                    :as $.gen]
            [convex.test.gen]
            [clojure.test.check.generators :as TC.gen]))


;;;;;;;;;;


(comment



  )
