(ns convex.lisp.test.core.coll

  "Testing core functions operating on collections.
  
   Articulates essentially 2 kind of tests:

   - Regular function calls
   - Suites that some collections must pass, testing for consistency between collection functions.
  
   Main suites must be passed by all types of collection, whereas other suites like [[suite-assoc]] are specialized
   on collection type."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.lisp                   :as $.lisp]
            [convex.lisp.gen               :as $.gen]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.gen          :as $.test.gen]
            [convex.lisp.test.prop         :as $.test.prop]))


(declare ctx-main
         suite-kv+)


;;;;;;;;;; Reusing properties


(defn suite-new

  "Suite that all new collections created with constructor functions (eg. `list`). must pass."

  [ctx form-type?]

  ($.test.prop/mult*

    "Type is correct"
    ($.test.eval/result* ctx
                         (~form-type? x))

    "Same number of key-values"
    ($.test.eval/result ctx
                        '(= (count kv+)
                            (count x)))

    "All key-values can be retrieved"
    ($.test.eval/result ctx
                        '($/every? (fn [[k v]]
                                     (= v
                                        (get x
                                             k)
                                        ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/145
                                        ; (x k)
                                        ))
                                   kv+))))


;;;;;;;;;; Creating collections from functions


($.test.prop/deftest blob-map--

  (TC.prop/for-all [kv+ ($.test.gen/kv+ $.gen/blob
                                        $.gen/any)]
    (suite-new ($.test.eval/ctx* (do
                                   (def kv+
                                        ~kv+)
                                   (def x
                                        (blob-map ~@(mapcat identity
                                                            kv+)))))
               '(fn [_] true))))



($.test.prop/deftest hash-map--

  ;; TODO. Also test failing with odd number of items.
  ;;
  ;; Cannot compare with Clojure: https://github.com/Convex-Dev/convex-web/issues/66

  (TC.prop/for-all [kv+ ($.test.gen/kv+ $.gen/any
                                        $.gen/any)]
    (suite-new ($.test.eval/ctx* (do
                                   (def kv+
                                        ~kv+)
                                   (def x
                                        (hash-map ~@(mapcat identity
                                                            kv+)))))
               'map?)))



($.test.prop/deftest hash-set--

  ;; Cannot compare with Clojure: https://github.com/Convex-Dev/convex-web/issues/66

  (TC.prop/for-all [x+ (TC.gen/vector-distinct $.gen/any)]
    (suite-new ($.test.eval/ctx* (do
                                   (def kv+
                                        ~(mapv #(vector %
                                                        %)
                                               x+))
                                   (def x
                                        (hash-set ~@x+))))
               'set?)))



($.test.prop/deftest list--

  (TC.prop/for-all [x+ (TC.gen/vector $.gen/any)]
    (suite-new ($.test.eval/ctx* (do
                                   (def kv+
                                        ~(mapv vector
                                               (range)
                                               x+))
                                   (def x
                                        (list ~@x+))))
               'list?)))



($.test.prop/deftest vector--

  (TC.prop/for-all [x+ (TC.gen/vector $.gen/any)]
    (suite-new ($.test.eval/ctx* (do
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
       $.test.eval/ctx))


;;;;;;;;;; Main - Different suites targeting different collection capabilities


(defn suite-assoc

  "See checkpoint."

  [ctx]

  ($.test.prop/checkpoint*

    "`assoc`"

    ($.test.prop/mult*
  
      "Associating existing value does not change anything"
      ($.test.eval/result ctx
                          '(= x-2
                              (assoc x-2
                                     k
                                     v)))
  
      "Consistent with `assoc-in`"
      ($.test.eval/result ctx
                          '(= x-2
                              (assoc-in x
                                        [k]
                                        v))))))



(defn suite-dissoc

  "See checkpoint.
  
   Other `dissoc` tests based around working repeatedly with key-values are in [[suite-kv+]]."

  [ctx]

  ($.test.prop/checkpoint*

    "Suite revolving around `dissoc` and its consequences measurable via other functions."

    (let [ctx-2 ($.test.eval/ctx ctx
                                 '(def x-3
                                       (dissoc x-2
                                               k)))]
      ($.test.prop/mult*

        "Does not contain key anymore"
        ($.test.eval/result ctx-2
                            '(not (contains-key? x-3
                                                 k)))

        "`get` returns nil"
        ($.test.eval/result ctx-2
                            '(nil? (get x-3
                                        k)))

        "Using collection as function returns nil"
        ($.test.eval/result ctx-2
                            '(nil? (x-3 k)))

        "`get` returns 'not-found' value"
        ($.test.eval/result ctx-2
                            '(= :convex-sentinel
                                (get x-3
                                     k
                                     :convex-sentinel)))

        "`get-in` returns nil"
        ($.test.eval/result ctx-2
                            '(nil? (get-in x-3
                                           [k])))

        ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/102
        ;; "`get-in` returns 'not-found' value"
        ;; ($.test.eval/result ctx-2
        ;;                   '(= :convex-sentinel
        ;;                       (get-in x-3
        ;;                               [k]
        ;;                               :convex-sentinel)))

        "Keys do not contain key"
        ($.test.eval/result ctx-2
                            '(not (contains-key? (set (keys x-3))
                                                 k)))

        "All other key-values are preserved"
        ($.test.eval/result ctx-2
                            '($/every? (fn [k]
                                         (= (get x-3
                                                 k)
                                            (get x-2
                                                 k)))
                                       (keys x-3)))

        "Equal to original or count updated as needed"
        ($.test.eval/result ctx-2
                            '(if (nil? x)
                               (= {}
                                  x-3)
                               (if (contains-key? x
                                                  k)
                                 (= (count x-3)
                                    (dec (count x)))
                                 (= x
                                    x-3))))
        ))))



(defn suite-hash-map

  "Suite containing miscellaneous tests for hash-maps."

  [ctx]

  ($.test.prop/checkpoint*

    "Using `hash-map` to rebuild map"
    ($.test.eval/result ctx
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

  ($.test.prop/checkpoint*

    "Suite for collections that support `keys` and `values` (currently, only map-like types)."

    (let [ctx-2 ($.test.eval/ctx ctx
                                 '(do
                                    (def k+
                                         (keys x-2))
                                    (def kv+
                                         (vec x-2))
                                    (def v+
                                         (values x-2))))]
      ($.test.prop/mult*

        "Keys contain new key"
        ($.test.eval/result ctx-2
                            '(contains-key? (set k+)
                                            k))

        "Order of `keys` is consistent with order of `values`"
        ($.test.eval/result ctx-2
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
        ($.test.eval/result ctx-2
                            '($/every? (fn [[k v]]
                                         (= v
                                            (get x-2
                                                 k)
                                            (x-2 k)))
                                       kv+))

        "`vec` is consitent with `into`"
        ($.test.eval/result ctx-2
                            '(= kv+
                                (into []
                                      x-2)))

        "Order of `keys` is consistent with `vec`"
        ($.test.eval/result ctx-2
                            '(= k+
                                (map first
                                     kv+)))

        "Order of `values` is consistent with `vec`"
        ($.test.eval/result ctx-2
                            '(= v+
                                (map second
                                     kv+)))

        "Order of `mapv` is consistent with `vec`"
        ($.test.eval/result ctx-2
                            '(= kv+
                                (mapv identity
                                      x-2)))


        "Contains all its keys"
        ($.test.eval/result ctx-2
                            '($/every? (fn [k]
                                         (contains-key? x-2
                                                        k))
                                       k+))

        "`assoc` is consistent with `count`"
        ($.test.eval/result ctx-2
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
       ($.test.eval/result ctx-2
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
       (let [ctx-3 ($.test.eval/ctx ctx-2
                                    '(def arg+
                                          (reduce (fn [acc [k v]]
                                                    (conj acc
                                                          k
                                                          v))
                                                  []
                                                  kv+)))]
         ($.test.prop/mult*

           "From an empty map"
           ($.test.eval/result ctx-3
                               '(= x-2
                                   (apply assoc
                                         (empty x-2)
                                         arg+)))

           "On the map itself"
           ($.test.eval/result ctx-3
                               '(= x-2
                                   (apply assoc
                                          x-2
                                          arg+)))))
       ))))



(defn suite-main-mono

  "See checkpoint."

  [ctx]

  ($.test.prop/checkpoint*

    "Suite that all collections must pass (having exactly 1 item)."

    (let [ctx-2 ($.test.eval/ctx ctx
                                 '(def x-3
                                       (conj (empty x-2)
                                             (first x-2))))]
      ($.test.prop/mult*

        "`cons`"
        ($.test.eval/result ctx-2
                            '(= (list 42
                                      (first x-3))
                                (cons 42
                                      x-3)))

        "`count` returns 1"
        ($.test.eval/result ctx-2
                            '(= 1
                                (count x-3)))

        "Not empty"
        ($.test.eval/result ctx-2
                            '(not (empty? x-3)))

        "`first` and `last` are equivalent, consistent with `nth`"
        ($.test.eval/result ctx-2
                            '(= (first x-3)
                                (last x-3)
                                (nth x-3
                                     0)))

        "`next` returns nil"
        ($.test.eval/result ctx-2
                            '(nil? (next x-3)))

        "`second` is exceptional"
        ($.test.eval/exception? ctx-2
                                '(second x-3))

        ))))



(defn suite-main-poly

  "See checkpoint."

  [ctx]

  ($.test.prop/checkpoint*

    "Suite that all collections must pass (having >= 1 item)."

    ($.test.prop/mult*

      ;; <!> TODO. Fails on sets because of: https://github.com/Convex-Dev/convex/issues/109
      ;;                                     https://github.com/Convex-Dev/convex/issues/110
      
      ;; "Contains key"
      ;; ($.test.eval/result ctx
      ;;                     '(contains-key? x-2
      ;;                                     k))

      ;; "`get` returns the value"
      ;; ($.test.eval/result ctx
      ;;                     '(= v
      ;;                         (get x-2
      ;;                              k)))

      ;; "Using collection as function returns the value"
      ;; ($.test.eval/result ctx
      ;;                     '(= v
      ;;                         (x-2 k)))

      ;; "`get-in` returns the value"
      ;; ($.test.eval/result ctx
      ;;                     '(= v
      ;;                         (get-in x-2
      ;;                                 [k])))

      "Cannot be empty"
      ($.test.eval/result ctx
                          '(not (empty? x-2)))

      "Count is at least 1"
      ($.test.eval/result ctx
                          '(>= (count x-2)
                               1))

      "`first` is not exceptional"
      ($.test.eval/result ctx
                          '(do
                             (first x-2)
                             true))

      "`(nth 0)` is not exceptional"
      ($.test.eval/result ctx
                          '(do
                             (nth x-2
                                  0)
                             true))

      "`last` is is not exceptional"
      ($.test.eval/result ctx
                          '(do
                             (last x-2)
                             true))

      "`nth` to last item is not exceptional"
      ($.test.eval/result ctx
                          '(do
                             (nth x-2
                                  (dec (count x-2)))
                             true))

      "`nth` is consistent with `first`"
      ($.test.eval/result ctx
                          '(= (first x-2)
                              (nth x-2
                                   0)))

      "`nth` is consistent with `last`"
      ($.test.eval/result ctx
                          '(= (last x-2)
                              (nth x-2
                                   (dec (count x-2)))))

      "`nth` is consistent with second"
      ($.test.eval/result ctx
                          '(if (>= (count x-2)
                                   2)
                             (= (second x-2)
                                (nth x-2
                                     1))
                             true))

      "Using `concat` to rebuild collection as a vector"
      ($.test.eval/result ctx
                          '(let [as-vec (vec x-2)]
                             (= as-vec
                                (apply concat
                                       (map vector
                                            x-2)))))

      "`cons`"
      (let [ctx-2 ($.test.eval/ctx ctx
                                   '(def -cons
                                         (cons (first x-2)
                                               x-2)))]
        ($.test.prop/mult*
          
          "Produces a list"
          ($.test.eval/result ctx-2
                              '(list? -cons))

          "Count is coherent compared to the consed collection"
          ($.test.eval/result ctx-2
                              '(= (count -cons)
                                  (inc (count x-2))))

          "First elements are consistent with setup"
          ($.test.eval/result ctx-2
                              '(= (first -cons)
                                  (second -cons)
                                  (first x-2)))

          "Consistent with `next`"
          ($.test.eval/result ctx-2
                              '(= (vec (next -cons))
                                  (vec x-2)))))

      "`cons` repeatedly reverse a collection"
      ($.test.eval/result ctx
                          '(= (into (list)
                                    x-2)
                              (reduce (fn [acc x]
                                        (cons x
                                              acc))
                                      (empty x-2)
                                      x-2)))

      "`next` preserves types of lists, returns vectors for other collections"
      ($.test.eval/result ctx
                          '(let [-next (next x-2)]
                             (if (nil? -next)
                               true
                               (if (list? x-2)
                                 (list? -next)
                                 (vector? -next)))))

      "`next` is consistent with `first`, `second`, and `count`"
      ($.test.eval/result ctx
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
      ($.test.eval/result ctx
                          '(let [-count-pos? (> (count x-2)
                                                0)
                                 -empty?     (empty? x-2)]
                             (if -empty?
                               (not -count-pos?)
                               -count-pos?)))

      "`empty?` is consistent with `empty`"
      ($.test.eval/result ctx
                          '(empty? (empty x-2)))
      )))



(defn suite-main

  "Gathering [[suite-main-mono]] and [[suite-main-poly]]."

  [ctx]

  ($.test.prop/and* (suite-main-poly ctx)
                    (suite-main-mono ctx)))



(defn suite-map-like

  "See checkpoint."

  [ctx]

  ($.test.prop/checkpoint*

    "Suite for operations specific to map-like types (ie. blob-map, hash-map, and nil-."

    ($.test.prop/mult*

      "Count has been updated as needed"
      ($.test.eval/result ctx
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
      ;; ($.test.eval/result ctx
      ;;                     '(= x-2
      ;;                         (merge (empty x-2)
      ;;                                x-2)))
      ;;
      ;; "Merging original with new = new"
      ;; ($.test.eval/result ctx
      ;;                     '(= x-2
      ;;                         (merge x
      ;;                                x-2)))

      "`conj` is consistent with `assoc`"
      ($.test.eval/result ctx
                          '(if (map? x)
                             (= x-2
                                (conj x
                                      [k v]))
                             true))

      "`into` is consistent with `assoc`"
      ($.test.eval/result ctx
                          '(if (map? x)
                             (= x-2
                                (into x
                                      [[k v]]))
                             true))

      "All other key-values are preserved"
      ($.test.eval/result ctx
                          '($/every? (fn [k]
                                       (= (get x
                                               k)
                                          (get x-2
                                               k)
                                          (x-2 k)))
                                     (keys (dissoc x
                                                   k))))

      "Using `into` to rebuild map"
      (let [ctx-2 ($.test.eval/ctx ctx
                                   '(do
                                      (def -empty
                                           (empty x-2))
                                      (def as-list
                                           (into (list)
                                                 x-2))))]
        ($.test.prop/mult*

          "On empty map"
          ($.test.eval/result ctx-2
                              '(= x-2
                                  (into -empty
                                        x-2)
                                  (into -empty
                                        as-list)))


          "Using `into` on map with this very same map does not change anything"
          ($.test.eval/result ctx-2
                              '(= x-2
                                  (into x-2
                                        x-2)
                                  (into x-2
                                        as-list)))))
      )))



(defn suite-map

  "Combining all suites that a map-like type must pass, for ease of use."

  [ctx]

  ($.test.prop/and* (suite-assoc ctx)
                    (suite-main ctx)
                    (suite-dissoc ctx)
                    (suite-kv+ ctx)
                    (suite-map-like ctx)))



(defn suite-sequential

  "See checkpoint."

  [ctx]

  ($.test.prop/checkpoint*

    "Specific to sequential collections"

    ($.test.prop/mult*

      "`contains-key?` with indices"
      ($.test.eval/result ctx
                          '($/every-index? contains-key?
                                           x-2))

      "`get` is consistent with `nth`"
      ($.test.eval/result ctx
                          '($/every-index? (fn [x-2 i]
                                             (= (get x-2
                                                     i)
                                                (x-2 i)
                                                (nth x-2
                                                     i)))
                                           x-2))

      "Rebuilding sequential using `assoc` and `apply`"
      ($.test.eval/result ctx
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
                                                (dec idx)))))))
      )))


;;;;;;;;;; Generative tests for main suites


($.test.prop/deftest main-blob-map

  ;; TODO. Add proper blob-map generation.

  (TC.prop/for-all* [$.gen/blob
                     $.gen/any]
                    (comp suite-map
                          (partial ctx-assoc
                                   '(blob-map)))))



($.test.prop/deftest main-map

  (TC.prop/for-all [m $.gen/map
                    k $.gen/any
                    v $.gen/any]
    (let [ctx (ctx-assoc m
                         k
                         v)]
      ($.test.prop/and* (suite-map ctx)
                        (suite-hash-map ctx)))))



($.test.prop/deftest main-nil

  (TC.prop/for-all* [$.gen/nothing
                     $.gen/any
                     $.gen/any]
                    (comp suite-map
                          ctx-assoc)))



($.test.prop/deftest main-sequential

  (TC.prop/for-all [coll (TC.gen/such-that #(seq (cond->
                                                   %
                                                   (seq? %)
                                                   rest))
                                           $.gen/sequential)
                    v    $.gen/any]
    (let [ctx (ctx-assoc coll
                         (rand-int (count (cond->
                                            coll
                                            (seq? coll)
                                            rest)))

                         v)]
      ($.test.prop/and* (suite-assoc ctx)
                        (suite-main ctx)
                        (suite-sequential ctx)))))



($.test.prop/deftest main-set

  (TC.prop/for-all [s (TC.gen/not-empty $.gen/set)]
    (suite-main (let [v (first s)]
                  (ctx-main s
                            s
                            v
                            v)))))


;;;;;;;;;; `assoc`


(defn- -assoc-fail

  ;; Helper for evaluating a failing call to `assoc`.

  [x k v]

  ($.test.eval/exception?* (assoc ~x
                                  ~k
                                  ~v)))



($.test.prop/deftest assoc--fail

  (TC.prop/for-all* [($.gen/any-but #{$.gen/list
                                      $.gen/map
                                      $.gen/nothing
                                      $.gen/vector})
                     $.gen/any
                     $.gen/any]
                    -assoc-fail))



($.test.prop/deftest assoc--blob-map-fail

  (TC.prop/for-all [k ($.gen/any-but #{$.gen/address
                                       $.gen/blob})
                    v $.gen/any]
    (-assoc-fail '(blob-map)
                 k
                 v)))



($.test.prop/deftest assoc--sequential-fail

  (TC.prop/for-all [[x
                     k] (TC.gen/let [x $.gen/sequential
                                     k (TC.gen/such-that #(not (and (number? %)
                                                                    (<= 0
                                                                        %
                                                                        (dec (count (if (vector? x)
                                                                                      x
                                                                                      (rest x)))))))
                                                         $.gen/any)]
                          [x k])
                    v   $.gen/any]
    (-assoc-fail x
                 k
                 v)))


;;;;;;;;;; `assoc-in`


($.test.prop/deftest assoc-in--fail-path

  ;; Trying to assoc using a path that is not a collection.

  (TC.prop/for-all [x    (TC.gen/one-of [$.gen/list
                                         $.gen/map
                                         $.gen/nothing
                                         $.gen/vector])
                    path ($.gen/any-but #{$.gen/list
                                          $.gen/nothing
                                          $.gen/vector})
                    v    $.gen/any]
    ($.test.eval/exception?* (assoc-in ~x
                                       ~path
                                       ~v))))



($.test.prop/deftest assoc-in--fail-type

  (TC.prop/for-all [x    ($.gen/any-but #{$.gen/list
                                          $.gen/map
                                          $.gen/nothing
                                          $.gen/vector})
                    path (TC.gen/such-that #(seq (if (vector? %)
                                                   %
                                                   ($.lisp/meta-raw %)))
                                           $.gen/sequential)
                    v    $.gen/any]
    ($.test.eval/exception?* (assoc-in ~x
                                       ~path
                                       ~v))))



(defn- -eval-assoc-in

  ;; Helper for writing and evaling the CVM code for passing `assoc-in` tests.

  [x path v]

  ($.test.eval/result* (= ~v
                          (let [x-2 (assoc-in ~x
                                              ~path
                                              ~v)]
                            (if (empty? ~path)
                              x-2
                              (get-in x-2
                                      ~path))))))



($.test.prop/deftest assoc-in--map

  ;; TODO. Currently, empty path returns the value. Keep an eye on: https://github.com/Convex-Dev/convex/issues/96

  (TC.prop/for-all [x    $.test.gen/maybe-map
                    path $.gen/sequential
                    v    $.gen/any]
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


($.test.prop/deftest mapcat--

  (TC.prop/for-all [coll $.gen/collection]
    ($.test.prop/mult*

      "Duplicating items"
      ($.test.eval/result* (let [coll ~coll]
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
      ($.test.eval/result* (do
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



($.test.prop/deftest mapping

  (TC.prop/for-all [coll $.gen/collection]
    (let [ctx ($.test.eval/ctx* (do
                                  (def coll
                                       ~coll)
                                  (def vect
                                       (vec coll))
                                  (def modified
                                       (mapv vector
                                             coll))))]
      ($.test.prop/mult*

        "`for` to recreate collection as vector"
        ($.test.eval/result ctx
                            '(= vect
                                (for [x coll]
                                  x)))

        "`for` to modify collection"
        ($.test.eval/result ctx
                            '(= modified
                                (for [x coll]
                                  [x])))

        "`mapv` with identity"
        ($.test.eval/result ctx
                            '(= vect
                                (mapv identity
                                      coll)))

        "`mapv` to modify collection"
        ($.test.eval/result ctx
                            '(= modified
                                (mapv vector
                                      coll)))

        "`mapcat`"
        ($.test.prop/and* ($.test.prop/checkpoint* "Modifies collection"
                                                   ($.test.eval/result ctx
                                                                       '(= modified
                                                                           (vec (mapcat (fn [x]
                                                                                          [[x]])
                                                                                        coll)))))
                          (let [ctx-2 ($.test.eval/ctx ctx
                                                       '(def -mapcat
                                                             (mapcat vector
                                                                     coll)))]

                            (if (seq? coll)
                              ($.test.prop/mult*

                                "Produces a list"
                                ($.test.eval/result ctx-2
                                                    '(list? -mapcat))
                                "List is recreated"
                                ($.test.eval/result ctx-2
                                                    '(= coll
                                                        -mapcat)))
                              ($.test.prop/mult*

                                "Produces a vector"
                                ($.test.eval/result ctx-2
                                                    '(vector? -mapcat))

                                "Recreates collection as a vector"
                                ($.test.eval/result ctx-2
                                                    '(= vect
                                                        -mapcat))))))))))



($.test.prop/deftest merge--

  (TC.prop/for-all [x+ (TC.gen/vector (TC.gen/one-of [$.gen/map
                                                      $.gen/nothing])
                                      0
                                      16)]
    (let [ctx ($.test.eval/ctx* (do
                                  (def arg+
                                       ~x+)
                                  (def merge-
                                       (merge ~@x+))))]
      ($.test.prop/mult*

        "Count of merge cannot be bigger than all involved key-values"
        ($.test.eval/result ctx
                            '(<= (count merge-)
                                 (reduce (fn [acc arg]
                                           (+ acc
                                              (count arg)))
                                         0
                                         arg+)))

        "All key-values in merged result must be in at least one input"
        ($.test.eval/result ctx
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



($.test.prop/deftest reduce--

  (TC.prop/for-all [percent $.test.gen/percent
                    x       (TC.gen/such-that #(seq (if (seq? %)
                                                      ($.lisp/meta-raw %)
                                                      %))
                                              $.gen/collection)]
    ($.test.eval/result* (let [x ~x
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


($.test.prop/deftest blob-map--err-cast

  (TC.prop/for-all [arg+ (let [set-gen-good #{$.gen/address
                                              $.gen/blob}]
                           ($.test.gen/mix-one-in (TC.gen/tuple ($.gen/any-but set-gen-good)
                                                                $.gen/any)
                                                  ($.test.gen/kv+ (TC.gen/one-of [(TC.gen/one-of (vec set-gen-good))
                                                                                  $.gen/any])
                                                                  $.gen/any)))]
    ($.test.eval/error-cast?* (blob-map ~@(mapcat identity
                                                  arg+)))))



($.test.prop/deftest concat--err-cast

  (TC.prop/for-all [arg+ ($.test.gen/outlier #{$.gen/list
                                               $.gen/map
                                               $.gen/set
                                               $.gen/vector})]
    ($.test.eval/error-cast?* (concat ~@arg+))))



($.test.prop/deftest conj--err-cast

  ;; TODO. Blob-maps with non-blob keys.

  (TC.prop/for-all [x    $.test.gen/not-collection
                    arg+ (TC.gen/vector $.gen/any
                                        0
                                        6)]
    ($.test.eval/error-cast?* (conj ~x
                                    ~@arg+))))



($.test.prop/deftest cons--err-cast

  (TC.prop/for-all [x        $.gen/any
                    not-coll $.test.gen/not-collection]
    ($.test.eval/error-cast?* (cons ~x
                                    ~not-coll))))



($.test.prop/deftest contains-key?--err-cast

  (TC.prop/for-all [x $.test.gen/not-collection
                    k $.gen/any]
    ($.test.eval/error-cast?* (contains-key? ~x
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
