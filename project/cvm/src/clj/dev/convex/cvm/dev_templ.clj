(ns convex.cvm.dev

  "Daydreaming at the REPL."

  {:clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [convex.cell     :as $.cell]
            [convex.cvm      :as $.cvm]
            [convex.form     :as $.form]
            [convex.read     :as $.read]
            [convex.test.cvm :as $.test.cvm]
            [convex.write    :as $.write]))


;;;;;;;;;;


(comment


  )
