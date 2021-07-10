(ns convex.cvx.lib.trust.dev

  "Daydreaming at the REPL."

  {:clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [convex.cvm      :as $.cvm]
            [convex.clj.eval :as $.clj.eval]
            [convex.clj      :as $.clj]
            [convex.data     :as $.data]
            [convex.watch    :as $.watch]))


;;;;;;;;;;


(comment


  (def a*env
       (-> ($.watch/init {:convex.watch/on-change (fn [env]
                                                    (update env
                                                            :convex.sync/ctx
                                                            $.clj.eval/ctx
                                                            '(do
                                                               (set-key (blob "0000000000000000000000000000000000000000000000000000000000000000"))
                                                               (def trust
                                                                    (deploy (first trust))))))
                          :convex.watch/sym->dep  {'trust "project/cvx/lib/trust/src/cvx/main/convex/trust.cvx"}})
           $.watch/start))



  ($.cvm/exception ($.watch/ctx a*env))

  ($.watch/stop a*env)



  ($.clj.eval/result ($.watch/ctx a*env)
                     '(do
                        (let [addr (deploy (trust/build-whitelist {:whitelist [42]}))]
                          [(trust/trusted? addr
                                           42)
                           (trust/trusted? addr
                                           100)])))


  ($.clj.eval/result ($.watch/ctx a*env)
                     '(do
                        (let [addr (deploy (trust/add-trusted-upgrade nil))]
                          (call addr
                                (upgrade '(def foo 42)))
                          (lookup addr
                                  foo))))

  )
