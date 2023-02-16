(ns convex.shell.resrc

  "Disguising external resources as (fake) cells."

  (:require [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.std  :as $.std]))


;;;;;;;;;;


(def ^:private -*counter

  ;; 

  (atom 0))



(defn create

  "Returns a vector cell that wraps `x` (can be anything).

   See [[unwrap]]."

  ;; Returns a vector with an ever incrementing number so that those values
  ;; have some uniqueness (e.g. can be used as keys in a map).

  [x]

  ($.cell/* [~($.cell/fake x)
             ~($.cell/long (swap! -*counter
                                  inc))]))



(defn unwrap

  "Unwraps `resrc` which should have been created with [[create]].
  
   Returns a 2-tuple where the first item is a boolean indicating success and the second
   item is either the unwrapped value (in case of success) or a failed context (in case
   of failure)."

  [ctx resrc]

  (or (when (or (not ($.std/vector? resrc))
                (not (= ($.std/count resrc)
                        2)))
        [false
         ($.cvm/exception-set ctx
                              ($.cell/code-std* :ARGUMENT)
                              ($.cell/* "Not a resource"))])
      (let [resrc-2 ($.std/nth resrc
                               0)]
        (or (when-not ($.cell/fake? resrc-2)
              [false
               ($.cvm/exception-set ctx
                                    ($.cell/code-std* :ARGUMENT)
                                    ($.cell/* "Either not a resource or resource is stale"))])
            [true
             @resrc-2]))))
