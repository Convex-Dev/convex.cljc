(ns convex.lisp.run.fuzz

  "Running fuzzy tests until user interrupts them.
  
   Meant to run in a terminal."

  {:author "Adam Helinski"}

  (:require [clojure.java.io]
            [clojure.pprint]
            [clojure.test.check            :as tc]
            [clojure.test.check.properties :as tc.prop]
            [convex.lisp.ctx               :as $.ctx]
            [convex.lisp.eval              :as $.eval]
            [convex.lisp.gen               :as $.gen])
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
                         5)
        root         (or (:root option+)
                         "report/fuzz")
        d*ensure-dir (delay
                       (.mkdirs (File. root)))
        ctx          ($.ctx/create-fake)
        prop         (tc.prop/for-all [form ($.gen/random-call)]
                       ($.eval/value ctx
                                     form)
                       true)
        a*print      (agent 0)
        n-core       (.availableProcessors (Runtime/getRuntime))]
    (println \newline
             (format "Starting robustness fuzzy tester on %d core(s), saving errors to '%s'"
                     n-core
                     root))
    (dotimes [_ n-core]
      (future
        (while true
          (let [result (tc/quick-check 1e4
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
                                       (clojure.java.io/writer path))))))))))
