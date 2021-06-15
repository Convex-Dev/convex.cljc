(ns convex.example.disk

  "Loading or watching Convex Lisp files from disk."

  {:author "Adam Helinski"}

  (:require [convex.clj.eval :as $.clj.eval]
            [convex.sync     :as $.sync]
            [convex.watch    :as $.watch]))



;;;;;;;;;;


(comment


  ;; Loading a file, binding the code to the given symbol and then deploying it as a library..
  ;;
  (def ctx
       (-> ($.sync/disk {'$ "src/convex/break/util.cvx"})
           :ctx
           ($.clj.eval/ctx '(def $
                                 (deploy $)))))


  ($.clj.eval/result* ctx
                      $/foo)


  ;; Similar  example but live-reloads the file on change.
  ;;
  (def a*env
       (-> ($.watch/init {:on-change (fn [env]
                                       (update env
                                               :ctx
                                               $.clj.eval/ctx
                                               '(def $
                                                     (deploy $))))
                          :sym->dep  {'$ "src/convex/break/util.cvx"}})
           $.watch/start))


  ($.clj.eval/result* ($.watch/ctx a*env)
                      $/foo)


  ($.watch/stop a*env)



  )
