(ns convex.example.disk

  "Loading or watching Convex Lisp files from disk."

  {:author "Adam Helinski"}

  (:require [convex.code     :as $.code]
            [convex.clj.eval :as $.clj.eval]
            [convex.disk     :as $.disk]))



;;;;;;;;;;


(comment


  ;; Loading a vector of files (one in this example).
  ;;
  ;; Code is wraped in `$.code/deploy` which deploys it under the given '$ symbol.
  ;; If load was successful, `:ctx` contains the prepared context.

  (def ctx
       (:ctx ($.disk/load [["src/convex/break/util.cvx"
                            {:wrap (partial $.code/deploy
                                            '$)}]])))



  ($.clj.eval/result* ctx
                      $/foo)


  ;; Watching a vector of files (still one in this example).
  ;;
  ;; Everything a source file changes, it is being processed again.
  ;; Derefencing the result forks and returns an up-to-date context.

  (def w*ctx
       ($.disk/watch [["src/convex/break/util.cvx"
                       {:wrap (partial $.code/deploy
                                       '$)}]]
                     {:on-error println}))


  ($.clj.eval/result* @w*ctx
                      $/foo)

  (.close w*ctx)


  )
