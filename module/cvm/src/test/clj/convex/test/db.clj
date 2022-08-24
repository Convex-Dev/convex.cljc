(ns convex.test.db

  "Testing `convex.db`."

  {:author "Adam Helinski"}

  (:import (convex.core.data AVector))
  (:refer-clojure :exclude [ref])
  (:require [clojure.test                  :as T]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.cell                   :as $.cell]
            [convex.cvm.db                 :as $.cvm.db]
            [convex.db                     :as $.db]
            [convex.gen                    :as $.gen]
            [convex.ref                    :as $.ref]
            [convex.std                    :as $.std]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Setup


(def etch
     ($.db/open-temp))



($.db/write-root etch
                 ($.cell/vector))


;;;;;;;;;; Unit tests


(T/deftest close

  (T/is (thrown? Exception
                 (let [db ($.db/open-temp)]
                   (.close db)
                   ($.db/write db
                               ($.cell/* [:a :b]))))))

;;;;;;;;;; Gen tests


(defn suite-rw

  "Writes and read back `cell`, flushing in between if required by `flush?`.

   Also maintains a vector of all `[hash cell]` in root. Each time this suite is called, ensures all those cells
   are still accessible from their hash. Past cannot be disturbed by new entries."

  [db cell flush?]

  (mprop/mult

    "Write"
    #_:clj-kondo/ignore
    (def ref
         ($.db/write db
                     cell))

    "Write is persisted"
    ($.ref/persisted? ref)

    "Flush"
    (if flush?
      ($.db/flush db)
      true)

    "Read"
    (= cell
       ($.db/read db
                  ($.ref/hash ref)))

    "Root read"
    (let [root ($.db/read-root db)]
      #_:clj-kondo/ignore
      (def root
           root)
      ($.std/vector? root))

    "Root write"
    ($.db/write-root db
                     (.conj ^AVector root
                            ($.cell/vector [($.ref/hash ref)
                                            cell])))

    "Past is intact"
    (every? (fn [tuple]
              (= (second tuple)
                 ($.db/read db
                            ($.cell/hash<-hex (.toHexString (first tuple))))))
            root)))



(mprop/deftest rw

  {:ratio-num  30
   :ratio-size 5}

  (let [etch ($.db/open-temp)]
    ($.cvm.db/local-set etch)
    ($.db/write-root etch
                     ($.cell/vector))
    (TC.prop/for-all [cell   $.gen/any
                      flush? TC.gen/boolean]
      (mprop/check

        "Etch"
        (suite-rw etch
                  cell
                  flush?)))))
