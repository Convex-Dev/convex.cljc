(ns convex.sync.dev

  "Daydreaming at the REPL."

  {:clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [convex.clj.eval :as $.clj.eval]
            [convex.sync     :as $.sync]))


;;;;;;;;;;


(comment


  (def ctx
       (-> ($.sync/disk {'$ "project/break/src/cvx/main/convex/break.cvx"})
           :convex.sync/ctx
           ($.clj.eval/ctx '(def $
                                 (deploy (first $))))))


  ($.clj.eval/result* ctx
                      $/foo)


  )
