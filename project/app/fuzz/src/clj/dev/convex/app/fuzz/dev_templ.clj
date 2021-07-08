(ns convex.app.fuzz.dev

  "Daydreaming at the REPL."

  {:clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [convex.app.fuzz :as $.app.fuzz]))


;;;;;;;;;;


(comment


  (def future+
       ($.app.fuzz/random {:out tap>}))


  (run! future-cancel
        future+)


  )
