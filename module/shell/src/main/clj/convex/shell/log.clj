(ns convex.shell.log

  "Handles logging done via Timbre.
  
   Note: SLF4J from the core Java libraries is being redirected to Timbre."

  (:import (java.util Date))
  (:require [convex.cell       :as $.cell]
            [convex.shell.io   :as $.shell.io]
            [convex.shell.time :as $.shell.time]
            [convex.std        :as $.std]
            [convex.write      :as $.write]
            [taoensso.timbre   :as log]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Private


(def ^:private -*out

  ;; Default output for logging.

  (atom $.shell.io/stdout-txt))


;;;;;;;;;; Public


(defn out

  "Returns the stream currently used for logging."

  []

  @-*out)



(defn out-set

  "Sets [[out]] to the given `stream`."

  [stream]

  (reset! -*out
          stream))


;;;;;;;;;;


;; For the time being, only handle above `:error`. 
;;
(log/swap-config!
  (fn [config]
    (assoc config
           ;;
           :min-level
           :warn
           ;;
           :appenders
           {:cvx {:enabled? true
                  :fn       (fn [entry]
                              (try
                                ;;
                                (let [stream (out)]
                                  ($.write/stream stream
                                                  (fn [cell]
                                                    (str ($.write/string cell)))
                                                  ($.cell/* [~($.cell/string ($.shell.time/instant->iso (.toInstant ^Date (entry :instant))))
                                                             ~($.cell/keyword (name (entry :level)))
                                                             ~(let [location ($.cell/symbol (entry :?ns-str))
                                                                    line     (entry :?line)]
                                                                (if line
                                                                  ($.cell/* [~location
                                                                             ~($.cell/long (entry :?line))])
                                                                  location))
                                                             ~(let [arg+ (entry :vargs)
                                                                    x    (first arg+)]
                                                                (if ($.std/cell? x)
                                                                  x
                                                                  ($.cell/vector (keep (fn [x]
                                                                                         (let [x-2 ($.cell/any x)]
                                                                                           (when ($.std/cell? x-2)
                                                                                             x-2)))
                                                                                       arg+))))]))
                                  ($.shell.io/newline stream)
                                  ($.shell.io/flush stream))
                                ;;
                                (catch Throwable _ex
                                  ;; Nothing special can be done if logging fails.
                                  nil)))}})))
