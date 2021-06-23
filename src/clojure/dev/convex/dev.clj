(ns convex.dev

  "Dev playground."

  {:author           "Adam Helinski"
   :clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [clojure.data]
            [clojure.pprint]
            [convex.app.fuzz]
            [convex.app.run]
            [convex.break]
            [convex.break.gen]
            [convex.break.test.account]
            [convex.break.test.actor]
            [convex.break.test.code]
            [convex.break.test.coerce]
            [convex.break.test.coll]
            [convex.break.test.ctrl]
            [convex.break.test.def]
            [convex.break.test.fn]
            [convex.break.test.fuzz]
            [convex.break.test.literal]
            [convex.break.test.loop]
            [convex.break.test.math]
            [convex.break.test.pred]
            [convex.break.test.set]
            [convex.break.test.symbolic]
            [convex.break.test.syntax]
            [convex.clj                    :as $.clj]
            [convex.clj.gen                :as $.clj.gen]
            [convex.clj.test]
            [convex.code                   :as $.code]
            [convex.cvm                    :as $.cvm]
            [convex.clj.eval               :as $.clj.eval]
            [convex.cvm.test]
            [convex.example.disk]
            [convex.example.exec]
            [convex.example.templ]
			[convex.lib.lab.xform]
            [convex.lib.trust]
            [convex.lib.test.trust]
            [convex.run                    :as $.run]
            [convex.run.err                :as $.run.err]
            [convex.run.exec               :as $.run.exec]
            [convex.run.kw                 :as $.run.kw]
            [convex.run.strx               :as $.run.strx]
            [convex.run.sym                :as $.run.sym]
            [convex.sync                   :as $.sync]
            [convex.watch                  :as $.watch]
            [clojure.test.check.generators :as TC.gen]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;;


(def ppr
     clojure.pprint/pprint)


(defn tap

  ""

  [x]

  (println x)
  (flush))


(add-tap tap)


;;;;;;;;;;


(comment


  (def ctx
       (-> ($.sync/disk {'store "src/convex/lib/lab/xform/store.cvx2"
                         'xform "src/convex/lib/lab/xform.cvx"})
           :convex.sync/ctx
           ($.clj.eval/ctx '(do
                              (eval (first store))
                              (def xform
                                   (deploy (first xform)))))))

  ($.cvm/exception ctx)



  ($.clj.eval/result* ctx
                      inventory)




  (def a*env
       (-> ($.watch/init {:convex.watch/ms-debounce 1000
                          :convex.watch/on-change   (fn [env]
                                                      (tap> [:on-change
                                                             (with-out-str (ppr (dissoc env
                                                                                        :convex.sync/input->code)))])
                                                      (update env
                                                              :convex.sync/ctx
                                                              $.clj.eval/ctx
                                                              '(def $
                                                                    (deploy (first $)))))
                          :convex.watch/sym->dep    {'$ "src/convex/break/util.cvx2"}})
           $.watch/start))

  (ppr @a*env)
  (agent-error a*env)


  ($.watch/stop a*env)


  ($.cvm/exception ($.watch/ctx a*env))

  ($.clj.eval/result* ($.watch/ctx a*env)
                      $/foo)




  (def a* (agent [:a :b]
                 :error-mode :fail))

  (set-error-handler! a*
                 (fn [a e]
                   (tap> [:e e])))
  (send a*
        (fn [_]
          (tap> :ok)
          (throw (ex-info "foo" {}))))

  (deref a*)
  (agent-error a*)

  )
