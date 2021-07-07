(ns dev

  "Daydreaming at the REPL."

  {:clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [convex.code :as $.code]
            [convex.read :as $.read]
            [convex.cvm  :as $.cvm]))


;;;;;;;;;;



