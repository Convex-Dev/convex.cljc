(ns convex.lib.trust

  "Dev environment for the Trust library."

  {:author           "Adam Helinski"
   :clj-kondo/config '{:linters {:unused-namespace {:level :off}}}}

  (:require [convex.code     :as $.code]
            [convex.cvm      :as $.cvm]
            [convex.clj.eval :as $.clj.eval]
            [convex.clj      :as $.clj]
            [convex.watch    :as $.watch]))


;;;;;;;;;;


(comment


  (def a*env
       (-> ($.watch/init {:convex.watch/on-change (fn [env]
                                                    (update env
                                                            :convex.sync/ctx
                                                            $.clj.eval/ctx
                                                            '(def trust
                                                                  (deploy trust))))
                          :convex.watch/sym->dep  {'trust "src/convex/lib/trust.cvx"}})
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
