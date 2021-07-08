(ns convex.clj.dev

  "Daydreaming at the REPL."

  {:clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [convex.clj      :as $.clj]
            [convex.clj.eval :as $.clj.eval]
            [convex.clj.gen  :as $.clj.gen]))


;;;;;;;;;;



