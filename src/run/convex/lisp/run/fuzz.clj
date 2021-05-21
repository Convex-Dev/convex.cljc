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
            [convex.lisp.schema            :as $.schema]
            [malli.generator               :as malli.gen])
  (:import java.io.File))


;;;;;;;;;;


(defn random

  "Tests robustness in an infinite loop by generating random Convex Lisp forms calling the core library.
  
   A map of options may be provided:
  
   | Key | Usage | Default |
   |---|---|---|
   | `:max-size` | Maximum size used by `test.check` | 5 |
   | `:root` | Path where error files will be stored as EDN | `\"report/fuzz\" |"

  [opt+]

  (let [max-size     (or (opt+ :max-size)
                         5)
        root         (or (opt+ :root)
                         "report/fuzz")
        d*ensure-dir (delay
                       (.mkdirs (File. root)))
        ctx          ($.ctx/create-fake)
        prop         (tc.prop/for-all* [(malli.gen/generator :convex.core/call
                                                             {:registry ($.schema/registry)})]
                                       (fn [x]
                                         ($.eval/value ctx
                                                       x)
                                         true))]
    (println \newline
             (format "Starting robustness fuzzy tester, saving errors to '%s'"
                     root))
    (while true
      (let [result (tc/quick-check 1e6
                                   prop
                                   :max-size max-size)]
        (when-not (result :pass)
          (let [path (format "%s/%s.edn"
                             root
                             (System/currentTimeMillis))]
            (println (format "Saving error to '%s'"
                             path))
            @d*ensure-dir
            (clojure.pprint/pprint result
                                   (clojure.java.io/writer path))))))))
