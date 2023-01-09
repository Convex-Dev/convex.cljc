(ns convex.shell.dep.local

  (:require [babashka.fs      :as bb.fs]
            [clojure.string   :as string]
            [convex.cell      :as $.cell]
            [convex.shell.ctx :as $.shell.ctx]
            [convex.shell.kw  :as $.shell.kw]
            [convex.std       :as $.std]))


;;;;;;;;;;


(defn fetch

  [env project-child dep-parent actor-sym actor-path]

  
  (-> env
      (update-in [:convex.shell.dep/dep->project
                  dep-parent]
                 #(or %
                      ((env :convex.shell.dep/read-project) dep-parent
                                                            (let [dir (str ($.std/nth dep-parent
                                                                                      1))]
                                                              (if (bb.fs/absolute? dir)
                                                                dir
                                                                (let [dir-child (str ($.std/get project-child
                                                                                                $.shell.kw/dir))
                                                                      dir-2     (-> (format "%s/%s"
                                                                                            dir-child
                                                                                            dir)
                                                                                    (bb.fs/canonicalize)
                                                                                    (str))]

                                                                  (when (and (env :convex.shell.dep/foreign?)
                                                                             (not (string/starts-with? dir-child
                                                                                                       dir-2)))
                                                                    ($.shell.ctx/fail (env :convex.shell/ctx)
                                                                                      ($.cell/code-std* :ARGUMENT)
                                                                                      ($.cell/* {:ancestry ~(env :convex.shell.dep/ancestry)
                                                                                                 :message  "Foreign dependency cannot require a `:local` dependency outside of its project"})))
                                                                  dir-2))))))
      ((env :convex.shell.dep/jump) dep-parent
                                    actor-sym
                                    actor-path)))
