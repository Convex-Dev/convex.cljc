(ns convex.watch.dev

  "Daydreaming at the REPL."

  {:clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [convex.clj.eval :as $.clj.eval]
            [convex.watch    :as $.watch]))


;;;;;;;;;;


(comment


  (def a*env
       (-> ($.watch/init {:convex.watch/on-change (fn [env]
                                                    (update env
                                                            :convex.sync/ctx
                                                            $.clj.eval/ctx
                                                            '(def $
                                                                  (deploy (first $)))))
                          :convex.watch/sym->dep  {'$ "project/break/src/cvx/main/convex/break.cvx"}})
           $.watch/start))
  
  
  ($.clj.eval/result* ($.watch/ctx a*env)
                      $/foo)
  
  
  ($.watch/stop a*env)


  )
