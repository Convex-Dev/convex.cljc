(ns task.aliases

  "Alias related tasks."

  (:require [babashka.tasks          :as bb.task]
            [protosens.maestro       :as maestro]
            [protosens.maestro.alias :as maestro.alias]))


;;;;;;;;;; Core implementation


(defn- -task

  ;;

  [behavior basis]

  (maestro/task (assoc basis
                       :maestro.task/finalize
                       (case behavior
                         :-M    (fn [basis-2]
                                  (bb.task/clojure (str "-M"
                                                        (maestro.alias/stringify+ (basis-2 :maestro/require)))))
                         :print nil))))


;;;;;;;;;; Exposed tasks


(defn dev

  [behavior]

  (-task behavior
         {:maestro/alias+   [:task/dev]
          :maestro/profile+ ['dev
                             'test]}))



(defn -test-basis

  ;;

  [direct?]

  {:maestro/alias+   [:task/test]
   :maestro/profile+ [(with-meta 'test
                                 {:direct? direct?})]})



(defn test

  [behavior]

  (-task behavior
         (-test-basis true)))



(defn test-upstream

  [behavior]

  (-task behavior
         (-test-basis false)))
