(ns convex.app.fuzz

  "Running fuzzy tests until user interrupts them.
  
   Meant to run in a terminal."

  {:author "Adam Helinski"}

  (:require [clojure.java.io]
            [clojure.pprint]
            [clojure.test.check            :as TC]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break.gen              :as $.break.gen]
            [convex.cvm                    :as $.cvm]
            [convex.clj.eval               :as $.clj.eval]
            [convex.clj.gen                :as $.clj.gen])
  (:import java.io.File))


;;;;;;;;;;


(defn random

  "Tests robustness in an infinite loop by generating random Convex Lisp forms calling the core library.
  
   A map of options may be provided:
  
   | Key | Usage | Default |
   |---|---|---|
   | `:max-size` | Maximum size used by `test.check` | 5 |
   | `:root` | Path where error files will be stored as EDN | `\"report/fuzz\" |"

  [option+]

  (let [max-size     (or (:max-size option+)
                         200)
        root         (or (:root option+)
                         "report/fuzz")
        d*ensure-dir (delay
                       (.mkdirs (File. ^String root)))
        ctx          ($.cvm/ctx)
        prop         (TC.prop/for-all [form ($.clj.gen/call $.break.gen/core-symbol
                                                            (TC.gen/vector $.clj.gen/any
                                                                           0
                                                                           8))]
                       ($.clj.eval/value ctx
                                         form)
                       true)
        a*print      (agent 0)
        n-core       (.availableProcessors (Runtime/getRuntime))]
    (println \newline
             (format "Starting robustness fuzzy tester on %d core(s), saving errors to '%s'"
                     n-core
                     root))
    (mapv (fn [_i-core]
            (future
              (while true
                (let [result (TC/quick-check 1e4
                                             prop
                                             :max-size max-size)]
                  (send a*print
                        (fn [n-test]
                          (let [n-test-2 (+ n-test
                                            (long (result :num-tests)))]
                            (println (format "Total number of tests: %d"
                                             n-test-2))
                            n-test-2)))
                  (when-not (result :pass?)
                    (let [path (format "%s/%s.edn"
                                       root
                                       (System/currentTimeMillis))]
                      (send a*print
                            (fn [n-test]
                              (println (format "Saving error to '%s'"
                                               path))
                              n-test))
                      @d*ensure-dir
                      (clojure.pprint/pprint result
                                             (clojure.java.io/writer path))))))))
          (range n-core))))
