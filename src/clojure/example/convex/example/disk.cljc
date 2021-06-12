(ns convex.example.disk

  "Loading or watching Convex Lisp files from disk."

  {:author "Adam Helinski"}

  (:require [convex.code     :as $.code]
            [convex.clj.eval :as $.clj.eval]
            [convex.disk     :as $.disk]))



;;;;;;;;;;


(comment


  ;; Loading a file, binding the code to the given symbol and then deploying it as a library..
  ;;
  (def ctx
       (-> ($.disk/load {'$ "src/convex/break/util.cvx"})
           :ctx
           ($.clj.eval/ctx '(def $
                                 (deploy $)))))


  ($.clj.eval/result* ctx
                      $/foo)


  ;; Like previously example but live-reloads the file on change.
  ;;
  (def w*ctx
       ($.disk/watch {'$ "src/convex/break/util.cvx"}
                     (fn on-run [env]
                       (update env
                               :ctx
                               $.clj.eval/ctx
                               '(def $
                                     (deploy $))))))


  ($.clj.eval/result* @w*ctx
                      $/foo)

  (.close w*ctx)


  )
