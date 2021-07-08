(ns convex.app.run.dev

  "Daydreaming at the REPL."

  {:clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [convex.app.run      :as $.app.run]
            [convex.app.run.help :as $.app.run.help]
            [convex.watch        :as $.watch]))


;;;;;;;;;;


(comment


  ($.app.run/-main)

  ($.app.run/-main "command"
                   "eval")

  ($.app.run/-main "describe"
                   "help"
                   "about")

  ($.app.run/-main "eval"
                   "(help/about sreq 'dep)")

  ($.app.run/-main "load"
                   "project/run/src/cvx/dev/convex/run/dev.cvx")


  (def a*
       ($.app.run/-main "watch"
                        "project/run/src/cvx/dev/convex/run/dev.cvx"))

  (convex.watch/stop a*)


  )
