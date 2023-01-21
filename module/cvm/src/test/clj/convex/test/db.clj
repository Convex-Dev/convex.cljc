(ns convex.test.db

  "Testing `convex.db`."

  {:author "Adam Helinski"}

  (:refer-clojure :exclude [flush])
  (:require [clojure.test :as T]
            [convex.cell  :as $.cell]
            [convex.db    :as $.db]))


;;;;;;;;;; Private


(defn -data

  "Produces a fresh cell without a cached ref."

  []
  
  ($.cell/* {:bar [:a :b #{c}]
             :foo 42}))



(defn- -open

  "Used by tests for assertions against a clean DB."

  []

  ($.db/current-set ($.db/open-tmp "convex-db-test")))


;;;;;;;;;; Tests


(T/deftest open-close-cycle

  (T/is (= (-data)
           (do
             (-open)
             (let [path ($.db/path)]
               ($.db/root-write (-data))
               ($.db/flush)
               ($.db/close)
               ($.db/current-set ($.db/open path))
               ($.db/root-read))))))



(T/deftest rw

  (T/is (= (-data)
           (do
             (-open)
             ($.db/read ($.db/write (-data)))))
        "Can read back written data"))



(T/deftest rw-root

  (T/is (= (-data)
           (do
             (-open)
             ($.db/root-write (-data))
             ($.db/root-read)))
        "Can read back written root data"))



(T/deftest size

  (T/is (< 0
           (do
             (-open)
             ($.db/size)))))



(T/deftest write-after-close

  (T/is (thrown? Exception
                 (do
                   (-open)
                   ($.db/close)
                   ($.db/write (-data))))
        "Cannot write after closing an instance"))


;;;


(T/deftest fake-cell-support

  (-open)

  (T/is (= ($.cell/hash ($.cell/* :DEREF-ME))
           ($.db/root-write ($.cell/fake (fn []))))
        "Writes the actual :DEREF-ME keyword")

  (T/is (= ($.cell/* :DEREF-ME)
           ($.db/root-read))
        "Reads the actual :DEREF-ME keyword"))


;;;;;;;;;; Gen tests


#_(defn suite-rw

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



#_(mprop/deftest rw

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
