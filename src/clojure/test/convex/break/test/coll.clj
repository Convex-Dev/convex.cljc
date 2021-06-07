(ns convex.break.test.coll

  "Testing core functions operating on collections.
  
   Articulates essentially 2 kind of tests:

   - Regular function calls
   - Suites that some collections must pass, testing for consistency between collection functions.
  
   Main suites must be passed by all types of collection, whereas other suites like [[suite-assoc]] are specialized
   on collection type."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.cvm.eval               :as $.cvm.eval]
            [convex.break.gen              :as $.break.gen]
            [convex.lisp                   :as $.lisp]
            [convex.lisp.gen               :as $.lisp.gen]
            [helins.mprop                  :as mprop]))


(declare ctx-main
         suite-kv+)


;;;;;;;;;; Reusing properties


(defn suite-new

  "Suite that all new collections created with constructor functions (eg. `list`). must pass."

  [ctx form-type?]

  (mprop/mult

    "Type is correct"

    ($.cvm.eval/result* ctx
                        (~form-type? x))


    "Same number of key-values"

    ($.cvm.eval/result ctx
                       '(= (count kv+)
                           (count x)))


    "All key-values can be retrieved"

    ($.cvm.eval/result ctx
                       '($/every? (fn [[k v]]
                                    (= v
                                       (get x
                                            k)
                                       (x k)))
                                  kv+))))


;;;;;;;;;; Creating collections from functions


(mprop/deftest blob-map--

  (TC.prop/for-all [kv+ ($.lisp.gen/kv+ $.lisp.gen/blob
                                        $.lisp.gen/any)]
    (suite-new ($.cvm.eval/ctx* (do
                                  (def kv+
                                       ~kv+)
                                  (def x
                                       (blob-map ~@(mapcat identity
                                                           kv+)))))
               '(fn [_] true))))



(mprop/deftest hash-map--

  ;; TODO. Also test failing with odd number of items.
  ;;
  ;; Cannot compare with Clojure: https://github.com/Convex-Dev/convex-web/issues/66


  {:ratio-size 2}
  (TC.prop/for-all [kv+ ($.lisp.gen/kv+ $.lisp.gen/any
                                        $.lisp.gen/any)]
    (suite-new ($.cvm.eval/ctx* (do
                                  (def kv+
                                       ~kv+)
                                  (def x
                                       (hash-map ~@(mapcat identity
                                                           kv+)))))
               'map?)))



(mprop/deftest hash-set--

  ;; Cannot compare with Clojure: https://github.com/Convex-Dev/convex-web/issues/66

  (TC.prop/for-all [x+ (TC.gen/vector-distinct $.lisp.gen/any)]
    (suite-new ($.cvm.eval/ctx* (do
                                  (def kv+
                                       ~(mapv #(vector %
                                                       true)
                                              x+))
                                  (def x
                                       (hash-set ~@x+))))
               'set?)))



(mprop/deftest list--

  (TC.prop/for-all [x+ (TC.gen/vector $.lisp.gen/any)]
    (suite-new ($.cvm.eval/ctx* (do
                                  (def kv+
                                       ~(mapv vector
                                              (range)
                                              x+))
                                  (def x
                                       (list ~@x+))))
               'list?)))



(mprop/deftest vector--

  (TC.prop/for-all [x+ (TC.gen/vector $.lisp.gen/any)]
    (suite-new ($.cvm.eval/ctx* (do
                                  (def kv+
                                       ~(mapv vector
                                              (range)
                                              x+))
                                  (def x
                                       (vector ~@x+))))
               'vector?)))


;;;;;;;;;; Main - Creating an initial context


(defn ctx-assoc

  "Creating a base context suitable for main suites and suites operating on types that support
   `assoc`.

   Relies on [[ctx-main]], where `x-2` becomes `x` with the added key-value."

  ([[x k v]]

   (ctx-assoc x
              k
              v))


  ([x k v]

   (ctx-main x
             ($.lisp/templ* (assoc ~x
                                   ~k
                                   ~v))
             k
             v)))



(defn ctx-main

  "Creates a context by interning the given values (using same symbols as this signature).

   `k` and `v` are quoted."

  [x x-2 k v]

  (-> ($.lisp/templ* (do
                       (def k
                            ~k)
                       (def v
                            ~v)
                       (def x
                            ~x)
                       (def x-2
                            ~x-2)))
       $.cvm.eval/ctx))


;;;;;;;;;; Main - Different suites targeting different collection capabilities


(defn suite-assoc

  "See checkpoint."

  [ctx]

  (mprop/check

    "`assoc`"

    (mprop/mult
  
      "Associating existing value does not change anything"

      ($.cvm.eval/result ctx
                         '(= x-2
                             (assoc x-2
                                    k
                                    v)))

  
      "Consistent with `assoc-in`"

      ($.cvm.eval/result ctx
                         '(= x-2
                             (assoc-in x
                                       [k]
                                       v))))))



(defn suite-dissoc

  "See checkpoint.
  
   Other `dissoc` tests based around working repeatedly with key-values are in [[suite-kv+]]."

  ;; TODO. Follow this issue, sets should pass this suite: https://github.com/Convex-Dev/convex/issues/178

  [ctx]

  (mprop/check

    "Suite revolving around `dissoc` and its consequences measurable via other functions."

    (let [ctx-2 ($.cvm.eval/ctx ctx
                                '(def x-3
                                      (dissoc x-2
                                              k)))]
      (mprop/mult

        "Does not contain key anymore"

        ($.cvm.eval/result ctx-2
                           '(not (contains-key? x-3
                                                k)))


        "`get` returns nil"

        ($.cvm.eval/result ctx-2
                           '(nil? (get x-3
                                       k)))


        "Using collection as function returns nil"

        ($.cvm.eval/result ctx-2
                           '(nil? (x-3 k)))


        "`get` returns 'not-found' value"

        ($.cvm.eval/result ctx-2
                           '(= :convex-sentinel
                               (get x-3
                                    k
                                    :convex-sentinel)))


        "`get-in` returns nil"

        ($.cvm.eval/result ctx-2
                           '(nil? (get-in x-3
                                          [k])))


        "`get-in` returns 'not-found' value"

        ($.cvm.eval/result ctx-2
                           '(= :convex-sentinel
                               (get-in x-3
                                       [k]
                                       :convex-sentinel)))


        "Keys do not contain key"

        ($.cvm.eval/result ctx-2
                           '(not (contains-key? (set (keys x-3))
                                                k)))


        "All other key-values are preserved"

        ($.cvm.eval/result ctx-2
                           '($/every? (fn [k]
                                        (= (get x-3
                                                k)
                                           (get x-2
                                                k)))
                                      (keys x-3)))


        "Equal to original or count updated as needed"

        ($.cvm.eval/result ctx-2
                           '(if (nil? x)
                              (= {}
                                 x-3)
                              (if (contains-key? x
                                                 k)
                                (= (count x-3)
                                   (dec (count x)))
                                (= x
                                   x-3))))))))



(defn suite-hash-map

  "Suite containing miscellaneous tests for hash-maps."

  [ctx]

  (mprop/check

    "Using `hash-map` to rebuild map"

    ($.cvm.eval/result ctx
                       '(= x-2
                           (apply hash-map
                                  (reduce (fn [acc [k v]]
                                            (conj acc
                                                  k
                                                  v))
                                          []
                                          x-2))))))



(defn suite-kv+

  "See checkpoint."

  [ctx]

  (mprop/check

    "Suite for collections that support `keys` and `values` (currently, only map-like types)."

    (let [ctx-2 ($.cvm.eval/ctx ctx
                                '(do
                                   (def k+
                                        (keys x-2))
                                   (def kv+
                                        (vec x-2))
                                   (def v+
                                        (values x-2))))]
      (mprop/mult

        "Keys contain new key"

        ($.cvm.eval/result ctx-2
                           '(contains-key? (set k+)
                                           k))


        "Order of `keys` is consistent with order of `values`"

        ($.cvm.eval/result ctx-2
                           '($/every-index? (fn [k+ i]
                                              (and (= (get x-2
                                                           (get k+
                                                                i))
                                                      (get v+
                                                           i))
                                                   (= (x-2 (k+ i))
                                                      (v+ i))))
                                            k+))


        "`vec` correctly maps key-values"

        ($.cvm.eval/result ctx-2
                           '($/every? (fn [[k v]]
                                        (= v
                                           (get x-2
                                                k)
                                           (x-2 k)))
                                      kv+))


        "`vec` is consitent with `into`"

        ($.cvm.eval/result ctx-2
                           '(= kv+
                               (into []
                                     x-2)))


        "Order of `keys` is consistent with `vec`"

        ($.cvm.eval/result ctx-2
                           '(= k+
                               (map first
                                    kv+)))


        "Order of `values` is consistent with `vec`"

        ($.cvm.eval/result ctx-2
                           '(= v+
                               (map second
                                    kv+)))


        "Order of `mapv` is consistent with `vec`"

        ($.cvm.eval/result ctx-2
                           '(= kv+
                               (mapv identity
                                     x-2)))


        "Contains all its keys"

        ($.cvm.eval/result ctx-2
                           '($/every? (fn [k]
                                        (contains-key? x-2
                                                       k))
                                      k+))


        "`assoc` is consistent with `count`"

        ($.cvm.eval/result ctx-2
                           '(= x-2
                               (reduce (fn [x-3 [k v]]
                                         (let [x-4 (assoc x-3
                                                          k
                                                          v)]
                                           (if (= (count x-4)
                                                  (inc (count x-3)))
                                             x-4
                                             (reduced false))))
                                       (empty x-2)
                                       kv+)))


       "Using `assoc` to rebuild map in a loop"

       ($.cvm.eval/result ctx-2
                          '(let [rebuild (fn [acc]
                                           (reduce (fn [acc-2 [k v]]
                                                     (assoc acc-2
                                                            k
                                                            v))
                                                   acc
                                                   x-2))]
                             (= x-2
                                (rebuild (empty x-2))
                                (rebuild x-2))))


       "Using `assoc` with `apply` to rebuild map"

       (let [ctx-3 ($.cvm.eval/ctx ctx-2
                                   '(def arg+
                                         (reduce (fn [acc [k v]]
                                                   (conj acc
                                                         k
                                                         v))
                                                 []
                                                 kv+)))]
         (mprop/mult

           "From an empty map"

           ($.cvm.eval/result ctx-3
                              '(= x-2
                                  (apply assoc
                                        (empty x-2)
                                        arg+)))


           "On the map itself"

           ($.cvm.eval/result ctx-3
                              '(= x-2
                                  (apply assoc
                                         x-2
                                         arg+)))))))))



(defn suite-main-mono

  "See checkpoint."

  [ctx]

  (mprop/check

    "Suite that all collections must pass (having exactly 1 item)."

    (let [ctx-2 ($.cvm.eval/ctx ctx
                                '(def x-3
                                      (conj (empty x-2)
                                            (first x-2))))]
      (mprop/mult

        "`cons`"

        ($.cvm.eval/result ctx-2
                           '(= (list 42
                                     (first x-3))
                               (cons 42
                                     x-3)))


        "`count` returns 1"

        ($.cvm.eval/result ctx-2
                             '(= 1
                                 (count x-3)))


        "Not empty"

        ($.cvm.eval/result ctx-2
                           '(not (empty? x-3)))


        "`first` and `last` are equivalent, consistent with `nth`"

        ($.cvm.eval/result ctx-2
                           '(= (first x-3)
                               (last x-3)
                               (nth x-3
                                    0)))


        "`next` returns nil"

        ($.cvm.eval/result ctx-2
                           '(nil? (next x-3)))


        "`second` is exceptional"

        ($.cvm.eval/exception? ctx-2
                               '(second x-3))))))



(defn suite-main-poly

  "See checkpoint."

  [ctx]

  (mprop/check

    "Suite that all collections must pass (having >= 1 item)."

    (mprop/mult

      "Contains key"

      ($.cvm.eval/result ctx
                         '(contains-key? x-2
                                         k))


      "`get` returns the value"

      ($.cvm.eval/result ctx
                         '(= v
                             (get x-2
                                  k)))


      "Using collection as function returns the value"

      ($.cvm.eval/result ctx
                         '(= v
                             (x-2 k)))


      "`get-in` returns the value"

      ($.cvm.eval/result ctx
                         '(= v
                             (get-in x-2
                                     [k])))


      "Cannot be empty"

      ($.cvm.eval/result ctx
                         '(not (empty? x-2)))


      "Count is at least 1"

      ($.cvm.eval/result ctx
                         '(>= (count x-2)
                              1))


      "`first` is not exceptional"

      ($.cvm.eval/result ctx
                         '(do
                            (first x-2)
                            true))


      "`(nth 0)` is not exceptional"

      ($.cvm.eval/result ctx
                         '(do
                            (nth x-2
                                 0)
                            true))


      "`last` is is not exceptional"

      ($.cvm.eval/result ctx
                         '(do
                            (last x-2)
                            true))


      "`nth` to last item is not exceptional"

      ($.cvm.eval/result ctx
                         '(do
                            (nth x-2
                                 (dec (count x-2)))
                            true))


      "`nth` is consistent with `first`"

      ($.cvm.eval/result ctx
                         '(= (first x-2)
                             (nth x-2
                                  0)))


      "`nth` is consistent with `last`"

      ($.cvm.eval/result ctx
                         '(= (last x-2)
                             (nth x-2
                                  (dec (count x-2)))))


      "`nth` is consistent with second"

      ($.cvm.eval/result ctx
                         '(if (>= (count x-2)
                                  2)
                            (= (second x-2)
                               (nth x-2
                                    1))
                            true))


      "Using `concat` to rebuild collection as a vector"

      ($.cvm.eval/result ctx
                         '(let [as-vec (vec x-2)]
                            (= as-vec
                               (apply concat
                                      (map vector
                                           x-2)))))


      "`cons`"

      (let [ctx-2 ($.cvm.eval/ctx ctx
                                  '(def -cons
                                        (cons (first x-2)
                                              x-2)))]
        (mprop/mult
          
          "Produces a list"

          ($.cvm.eval/result ctx-2
                             '(list? -cons))


          "Count is coherent compared to the consed collection"

          ($.cvm.eval/result ctx-2
                             '(= (count -cons)
                                 (inc (count x-2))))


          "First elements are consistent with setup"

          ($.cvm.eval/result ctx-2
                             '(= (first -cons)
                                 (second -cons)
                                 (first x-2)))


          "Consistent with `next`"

          ($.cvm.eval/result ctx-2
                             '(= (vec (next -cons))
                                 (vec x-2)))))


      "`cons` repeatedly reverse a collection"

      ($.cvm.eval/result ctx
                         '(= (into (list)
                                   x-2)
                             (reduce (fn [acc x]
                                       (cons x
                                             acc))
                                     (empty x-2)
                                     x-2)))


      "`next` preserves types of lists, returns vectors for other collections"

      ($.cvm.eval/result ctx
                         '(let [-next (next x-2)]
                            (if (nil? -next)
                              true
                              (if (list? x-2)
                                (list? -next)
                                (vector? -next)))))


      "`next` is consistent with `first`, `second`, and `count`"

      ($.cvm.eval/result ctx
                         '(loop [x-3 x-2]
                            (let [n-x-3 (count x-3)]
                              (if (zero? n-x-3)
                                true
                                (let [x-3-next (next x-3)]
                                  (if (> n-x-3
                                         1)
                                    (if (and (= (count x-3-next)
                                                (dec n-x-3))
                                             (= (second x-3)
                                                (first x-3-next)))
                                      (recur x-3-next)
                                      false)
                                    (if (nil? x-3-next)
                                      (recur x-3-next)
                                      false)))))))


      "`empty?` is consistent with `count?`"

      ($.cvm.eval/result ctx
                         '(let [-count-pos? (> (count x-2)
                                               0)
                                -empty?     (empty? x-2)]
                            (if -empty?
                              (not -count-pos?)
                              -count-pos?)))


      "`empty?` is consistent with `empty`"

      ($.cvm.eval/result ctx
                         '(empty? (empty x-2))))))



(defn suite-main

  "Gathering [[suite-main-mono]] and [[suite-main-poly]]."

  [ctx]

  (mprop/and (suite-main-poly ctx)
             (suite-main-mono ctx)))



(defn suite-map-like

  "See checkpoint."

  [ctx]

  (mprop/check

    "Suite for operations specific to map-like types (ie. blob-map, hash-map, and nil-."

    (mprop/mult

      "Count has been updated as needed"

      ($.cvm.eval/result ctx
                         '(= (count x-2)
                             (+ (count x)
                                (if (or (= x-2
                                           x)
                                        (contains-key? x
                                                       k))
                                  0
                                  1))))


      ;; TODO.Failing because of: https://github.com/Convex-Dev/convex/issues/103
      ;;
      ;; "Using `merge` to rebuild map"
      ;; ($.cvm.eval/result ctx
      ;;                    '(= x-2
      ;;                        (merge (empty x-2)
      ;;                               x-2)))
      ;;
      ;; "Merging original with new = new"
      ;; ($.cvm.eval/result ctx
      ;;                    '(= x-2
      ;;                        (merge x
      ;;                               x-2)))


      "`conj` is consistent with `assoc`"

      ($.cvm.eval/result ctx
                         '(if (map? x)
                            (= x-2
                               (conj x
                                     [k v]))
                            true))


      "`into` is consistent with `assoc`"

      ($.cvm.eval/result ctx
                         '(if (map? x)
                            (= x-2
                               (into x
                                     [[k v]]))
                            true))


      "All other key-values are preserved"

      ($.cvm.eval/result ctx
                         '($/every? (fn [k]
                                      (= (get x
                                              k)
                                         (get x-2
                                              k)
                                         (x-2 k)))
                                    (keys (dissoc x
                                                  k))))


      "Using `into` to rebuild map"

      (let [ctx-2 ($.cvm.eval/ctx ctx
                                  '(do
                                     (def -empty
                                          (empty x-2))
                                     (def as-list
                                          (into (list)
                                                x-2))))]
        (mprop/mult

          "On empty map"

          ($.cvm.eval/result ctx-2
                             '(= x-2
                                 (into -empty
                                       x-2)
                                 (into -empty
                                       as-list)))


          "Using `into` on map with this very same map does not change anything"

          ($.cvm.eval/result ctx-2
                             '(= x-2
                                 (into x-2
                                       x-2)
                                 (into x-2
                                       as-list))))))))



(defn suite-map

  "Combining all suites that a map-like type must pass, for ease of use."

  [ctx]

  (mprop/and (suite-assoc ctx)
             (suite-main ctx)
             (suite-dissoc ctx)
             (suite-kv+ ctx)
             (suite-map-like ctx)))



(defn suite-sequential

  "See checkpoint."

  [ctx]

  (mprop/check

    "Specific to sequential collections"

    (mprop/mult

      "`contains-key?` with indices"

      ($.cvm.eval/result ctx
                         '($/every-index? contains-key?
                                          x-2))


      "`get` is consistent with `nth`"

      ($.cvm.eval/result ctx
                         '($/every-index? (fn [x-2 i]
                                            (= (get x-2
                                                    i)
                                               (x-2 i)
                                               (nth x-2
                                                    i)))
                                          x-2))


      "Rebuilding sequential using `assoc` and `apply`"

      ($.cvm.eval/result ctx
                         '(= x-2
                             (apply assoc
                                    x-2
                                    (loop [acc []
                                           idx (dec (count x-2))]
                                      (if (< idx
                                             0)
                                        acc
                                        (recur (conj acc
                                                     idx
                                                     (get x-2
                                                          idx))
                                               (dec idx))))))))))


;;;;;;;;;; Generative tests for main suites


(mprop/deftest main-blob-map

  ;; TODO. Add proper blob-map generation.

  {:ratio-num 5}

  (TC.prop/for-all* [$.lisp.gen/blob
                     $.lisp.gen/any]
                    (comp suite-map
                          (partial ctx-assoc
                                   '(blob-map)))))



(mprop/deftest main-map

  {:ratio-num 5}

  (TC.prop/for-all [m $.lisp.gen/map
                    k $.lisp.gen/any
                    v $.lisp.gen/any]
    (let [ctx (ctx-assoc m
                         k
                         v)]
      (mprop/and (suite-map ctx)
                         (suite-hash-map ctx)))))



(mprop/deftest main-nil

  {:ratio-num 5}

  (TC.prop/for-all* [$.lisp.gen/nothing
                     $.lisp.gen/any
                     $.lisp.gen/any]
                    (comp suite-map
                          ctx-assoc)))



(mprop/deftest main-sequential

  {:ratio-num 8}

  (TC.prop/for-all [coll (TC.gen/such-that #(seq (cond->
                                                   %
                                                   (seq? %)
                                                   rest))
                                           $.lisp.gen/sequential)
                    v    $.lisp.gen/any]
    (let [ctx (ctx-assoc coll
                         (rand-int (count (cond->
                                            coll
                                            (seq? coll)
                                            rest)))

                         v)]
      (mprop/and (suite-assoc ctx)
                         (suite-main ctx)
                         (suite-sequential ctx)))))



(mprop/deftest main-set

  {:ratio-num 8}

  (TC.prop/for-all [s (TC.gen/not-empty $.lisp.gen/set)]
    (suite-main (let [v (first s)]
                  (ctx-main s
                            s
                            v
                            true)))))


;;;;;;;;;; `assoc`


(defn- -assoc-fail

  ;; Helper for evaluating a failing call to `assoc`.

  [x k v]

  ($.cvm.eval/exception?* (assoc ~x
                                 ~k
                                 ~v)))



(mprop/deftest assoc--fail

  {:ratio-num 10}

  (TC.prop/for-all* [($.lisp.gen/any-but #{$.lisp.gen/list
                                           $.lisp.gen/map
                                           $.lisp.gen/nothing
                                           $.lisp.gen/set
                                           $.lisp.gen/vector})
                     $.lisp.gen/any
                     $.lisp.gen/any]
                    -assoc-fail))



(mprop/deftest assoc--blob-map-fail

  {:ratio-num 10}

  (TC.prop/for-all [k ($.lisp.gen/any-but #{$.lisp.gen/address
                                       $.lisp.gen/blob})
                    v $.lisp.gen/any]
    (-assoc-fail '(blob-map)
                 k
                 v)))



(mprop/deftest assoc--sequential-fail

  {:ratio-num 10}

  (TC.prop/for-all [[x
                     k] (TC.gen/let [x $.lisp.gen/sequential
                                     k (TC.gen/such-that #(not (and (number? %)
                                                                    (<= 0
                                                                        %
                                                                        (dec (count (if (vector? x)
                                                                                      x
                                                                                      (rest x)))))))
                                                         $.lisp.gen/any)]
                          [x k])
                    v   $.lisp.gen/any]
    (-assoc-fail x
                 k
                 v)))


;;;;;;;;;; `assoc-in`


(mprop/deftest assoc-in--fail-path

  ;; Trying to assoc using a path that is not a collection.

  {:ratio-num 10}

  (TC.prop/for-all [x    (TC.gen/one-of [$.lisp.gen/list
                                         $.lisp.gen/map
                                         $.lisp.gen/nothing
                                         $.lisp.gen/vector])
                    path ($.lisp.gen/any-but #{$.lisp.gen/list
                                               $.lisp.gen/nothing
                                               $.lisp.gen/vector})
                    v    $.lisp.gen/any]
    ($.cvm.eval/exception?* (assoc-in ~x
                                      ~path
                                      ~v))))



(mprop/deftest assoc-in--fail-type

  {:ratio-num 10}

  (TC.prop/for-all [x    ($.lisp.gen/any-but #{$.lisp.gen/list
                                               $.lisp.gen/map
                                               $.lisp.gen/nothing
                                               $.lisp.gen/set
                                               $.lisp.gen/vector})
                    path (TC.gen/such-that #(seq (if (vector? %)
                                                   %
                                                   ($.lisp/meta-raw %)))
                                           $.lisp.gen/sequential)
                    v    $.lisp.gen/any]
    ($.cvm.eval/exception?* (assoc-in ~x
                                      ~path
                                      ~v))))



(defn- -eval-assoc-in

  ;; Helper for writing and evaling the CVM code for passing `assoc-in` tests.

  [x path v]

  ($.cvm.eval/result* (= ~v
                         (let [x-2 (assoc-in ~x
                                             ~path
                                             ~v)]
                           (if (empty? ~path)
                             x-2
                             (get-in x-2
                                     ~path))))))



(mprop/deftest assoc-in--map

  ;; TODO. Currently, empty path returns the value. Keep an eye on: https://github.com/Convex-Dev/convex/issues/96

  {:ratio-num 10}

  (TC.prop/for-all [x    $.break.gen/maybe-map
                    path $.lisp.gen/sequential
                    v    $.lisp.gen/any]
    (-eval-assoc-in (cond->
                      x
                      (seq path)
                      (dissoc ((if (seq? path)
                                 second
                                 first)
                               path)))
                    path
                    v)))


;;;;;;;;;; Misc


(mprop/deftest mapcat--

  {:ratio-num 10}
  
  (TC.prop/for-all [coll $.lisp.gen/collection]
    (mprop/mult

      "Duplicating items"

      ($.cvm.eval/result* (let [coll ~coll]
                            (= (vec (mapcat (fn [x]
                                              [x x])
                                            coll))
                               (reduce (fn [acc x]
                                         (conj acc
                                               x
                                               x))
                                       []
                                       coll))))


      "Keeping items at even positions"

      ($.cvm.eval/result* (do
                            (def n-mapcat
                                 -1)
                            (def n-reduce
                                 -1)
                            (defn even? [x]
                              (zero? (mod x
                                          2)))
                            (let [coll ~coll]
                              (= (vec (mapcat (fn [x]
                                                (def n-mapcat
                                                     (inc n-mapcat))
                                                (when (even? n-mapcat)
                                                  [x]))
                                              coll))
                                 (reduce (fn [acc x]
                                           (def n-reduce
                                                (inc n-reduce))
                                           (if (even? n-reduce)
                                             (conj acc
                                                   x)
                                             acc))
                                         []
                                         coll))))))))



(mprop/deftest mapping

  {:ratio-num 10}
  
  (TC.prop/for-all [coll $.lisp.gen/collection]
    (let [ctx ($.cvm.eval/ctx* (do
                                 (def coll
                                      ~coll)
                                 (def vect
                                      (vec coll))
                                 (def modified
                                      (mapv vector
                                            coll))))]
      (mprop/mult

        "`for` to recreate collection as vector"

        ($.cvm.eval/result ctx
                           '(= vect
                               (for [x coll]
                                 x)))


        "`for` to modify collection"

        ($.cvm.eval/result ctx
                           '(= modified
                               (for [x coll]
                                 [x])))


        "`mapv` with identity"

        ($.cvm.eval/result ctx
                           '(= vect
                               (mapv identity
                                     coll)))


        "`mapv` to modify collection"

        ($.cvm.eval/result ctx
                           '(= modified
                               (mapv vector
                                     coll)))


        "`mapcat`"

        (mprop/and (mprop/check
                     
                     "Modifies collection"

                     ($.cvm.eval/result ctx
                                        '(= modified
                                            (vec (mapcat (fn [x]
                                                           [[x]])
                                                         coll)))))

                   (let [ctx-2 ($.cvm.eval/ctx ctx
                                               '(def -mapcat
                                                     (mapcat vector
                                                             coll)))]
                     (if (seq? coll)
                       (mprop/mult

                         "Produces a list"

                         ($.cvm.eval/result ctx-2
                                            '(list? -mapcat))

                         "List is recreated"

                         ($.cvm.eval/result ctx-2
                                            '(= coll
                                                -mapcat)))
                       (mprop/mult

                         "Produces a vector"

                         ($.cvm.eval/result ctx-2
                                            '(vector? -mapcat))


                         "Recreates collection as a vector"
                         ($.cvm.eval/result ctx-2
                                            '(= vect
                                                -mapcat))))))))))



(mprop/deftest merge--

  {:ratio-num 4}

  (TC.prop/for-all [x+ (TC.gen/vector (TC.gen/one-of [$.lisp.gen/map
                                                      $.lisp.gen/nothing])
                                      0
                                      16)]
    (let [ctx ($.cvm.eval/ctx* (do
                                 (def arg+
                                      ~x+)
                                 (def merge-
                                      (merge ~@x+))))]
      (mprop/mult

        "Count of merge cannot be bigger than all involved key-values"

        ($.cvm.eval/result ctx
                           '(<= (count merge-)
                                (reduce (fn [acc arg]
                                          (+ acc
                                             (count arg)))
                                        0
                                        arg+)))


        "All key-values in merged result must be in at least one input"

        ($.cvm.eval/result ctx
                           '($/every? (fn [[k v]]
                                        ($/some (fn [arg]
                                                  (and (= v
                                                          (get arg
                                                               k))
                                                       (if (nil? arg)
                                                         true
                                                         (= v
                                                            (arg k)))))
                                                arg+))
                                      merge-))))))



(mprop/deftest reduce--

  {:ratio-num 10}

  (TC.prop/for-all [percent $.break.gen/percent
                    x       (TC.gen/such-that #(seq (if (seq? %)
                                                      ($.lisp/meta-raw %)
                                                      %))
                                              $.lisp.gen/collection)]
    ($.cvm.eval/result* (let [x ~x
                              v (nth x
                                     (long (floor (* ~percent
                                                     (dec (count x))))))]
                          (= v
                             (reduce (fn [acc item]
                                       (if (= item
                                              v)
                                         (reduced item)
                                         acc))
                                     :convex-sentinel
                                     x))))))


;;;;;;;;;; Negative tests


(mprop/deftest blob-map--err-cast

  (TC.prop/for-all [arg+ (let [set-gen-good #{$.lisp.gen/address
                                              $.lisp.gen/blob}]
                           ($.lisp.gen/mix-one-in (TC.gen/tuple ($.lisp.gen/any-but set-gen-good)
                                                                $.lisp.gen/any)
                                                  ($.lisp.gen/kv+ (TC.gen/one-of [(TC.gen/one-of (vec set-gen-good))
                                                                                  $.lisp.gen/any])
                                                                  $.lisp.gen/any)))]
    ($.cvm.eval/code?* :ARGUMENT
                       (blob-map ~@(mapcat identity
                                           arg+)))))



(mprop/deftest concat--err-cast

  (TC.prop/for-all [arg+ ($.lisp.gen/outlier #{$.lisp.gen/list
                                               $.lisp.gen/map
                                               $.lisp.gen/nothing
                                               $.lisp.gen/set
                                               $.lisp.gen/vector})]
    ($.cvm.eval/code?* :CAST
                       (concat ~@arg+))))



(mprop/deftest conj--err-cast

  ;; TODO. Blob-maps with non-blob keys.

  {:ratio-num 10}

  (TC.prop/for-all [x    $.break.gen/not-collection
                    arg+ (TC.gen/vector $.lisp.gen/any
                                        0
                                        6)]
    ($.cvm.eval/code?* :CAST
                       (conj ~x
                             ~@arg+))))



(mprop/deftest cons--err-cast

  {:ratio-num 20}

  (TC.prop/for-all [x        $.lisp.gen/any
                    not-coll $.break.gen/not-collection]
    ($.cvm.eval/code?* :CAST
                       (cons ~x
                             ~not-coll))))



(mprop/deftest contains-key?--err-cast

  {:ratio-num 15}

  (TC.prop/for-all [x $.break.gen/not-collection
                    k $.lisp.gen/any]
    ($.cvm.eval/code?* :CAST
                       (contains-key? ~x
                                      ~k))))


;;;;;;;;;;


; assoc
; assoc-in
; blob-map
; concat
; conj
; cons
; contains-key?
; count
; dissoc
; empty
; empty?
; first
; get
; get-in
; into
; keys
; last
; list
; map && mapv ???
; merge
; next
; nth
; second
; values
