(ns convex.test.db

  "Testing `convex.db`."

  {:author "Adam Helinski"}

  (:import (convex.core.data AVector))
  (:require [clojure.test                  :as T]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.clj.gen                :as $.clj.gen]
            [convex.cell                   :as $.cell]
            [convex.cvm.db                 :as $.cvm.db]
            [convex.db                     :as $.db]
            [convex.read                   :as $.read]
            [convex.ref                    :as $.ref]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Setup


(def etch
     ($.db/create-temp))



($.db/write-root etch
                 ($.cell/vector))


;;;;;;;;;; Unit tests


(T/deftest close

  (T/is (thrown? Exception
                 (let [db ($.db/create-temp)]
                   (.close db)
                   ($.db/write db
                               ($.read/string "[:a :b]"))))))

;;;;;;;;;; Gen tests


(defn suite-rw

  ""

  [db cell]

  (mprop/mult

    "Write"
    (def ref
         ($.db/write db
                     cell))

    "Write is persisted"
    ($.ref/persisted? ref)

    "Read"
    (= cell
       ($.db/read db
                  ($.ref/hash ref)))

    "Root read"
    (let [root ($.db/read-root db)]
      (def root
           root)
      ($.cell/vector? root))

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

  {:ratio-num  10
   :ratio-size 1}

  (let [etch ($.db/create-temp)]
    ($.cvm.db/local-set etch)
    ($.db/write-root etch
                     ($.cell/vector))
    (TC.prop/for-all [cell $.clj.gen/any]
      (let [cell-2 ($.read/string (pr-str cell))]
        (mprop/check

          "Etch"
          (suite-rw etch
                    cell-2))))))
