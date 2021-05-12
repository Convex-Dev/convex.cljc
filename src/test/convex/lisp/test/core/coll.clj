(ns convex.lisp.test.core.coll

  "Testing core functions operating on collections.
  
   Articulates essentially 2 kind of tests:

   - Regular function calls
   - Suites that some collections must pass, testing for consistency between collection functions.
  
   Main suites must be passed by all types of collection, whereas other suites like [[suite-assoc]] are specialized
   on collection type."

  {:author "Adam Helinski"}

  (:require [clojure.test            :as t]
            [convex.lisp.form        :as $.form]
            [convex.lisp.test.eval   :as $.test.eval]
            [convex.lisp.test.prop   :as $.test.prop]
            [convex.lisp.test.schema :as $.test.schema]))


(declare ctx-main
         suite-kv+)


;;;;;;;;;; Creating collections from functions


(t/deftest blob-map--

  (t/is (map? ($.test.eval/result '(blob-map)))))



($.test.prop/deftest ^:recur hash-map--

  ;; TODO. Also test failing with odd number of items.
  ;;
  ;; Cannot compare with Clojure: https://github.com/Convex-Dev/convex-web/issues/66

  ($.test.prop/check [:+ [:cat
                          :convex/data
                          :convex/data]]
                     (fn [x]
                       (map? ($.test.eval/result (list* 'hash-map
                                                      (map $.form/quoted
                                                           x)))))))



(t/deftest hash-map--no-arg

  (t/is (= {}
           ($.test.eval/result '(hash-map)))))



($.test.prop/deftest ^:recur hash-set--

  ;; Cannot compare with Clojure: https://github.com/Convex-Dev/convex-web/issues/66

  ($.test.prop/check [:vector
                      {:min 1}
                      :convex/data]
                     (fn [x]
                       (set? ($.test.eval/result (list* 'hash-set
                                                        (map $.form/quoted
                                                             x)))))))



(t/deftest hash-set--no-arg

  (t/is (= #{}
           ($.test.eval/result '(hash-set)))))



($.test.prop/deftest ^:recur list--

  ($.test.prop/create-data 'list
                           list))



($.test.prop/deftest ^:recur vector--

  ($.test.prop/create-data 'vector
                           vector))


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
             ($.form/templ {'?k k
                            '?v v
                            '?x x}
                           '(assoc ?x
                                   '?k
                                   '?v))
             k
             v)))



(defn ctx-main

  "Creates a context by interning the given values (using same symbols as this signature)."

  [x x-2 k v]

  (-> ($.form/templ {'?k   k
                     '?v   v
                     '?x   x
                     '?x-2 x-2}
                     '(do
                        (def k
                             '?k)
                        (def v
                             '?v)
                        (def x
                             ?x)
                        (def x-2
                             ?x-2)))
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
                                               (= (get x-2
                                                       (get k+
                                                            i))
                                                  (get v+
                                                       i)))
                                             k+))


        "`vec` correctly maps key-values"
        ($.test.eval/result ctx-2
                            '($/every? (fn [[k v]]
                                         (= v
                                            (get x-2
                                                 k)))
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
        ($.test.eval/error? ctx-2
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
                                               k)))
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


($.test.prop/deftest ^:recur main-blob-map

  ($.test.prop/check [:tuple
                      [:= '(blob-map)]
                      :convex/blob
                      :convex/data]
                     (comp suite-map
                           ctx-assoc)))



($.test.prop/deftest ^:recur main-map

  ($.test.prop/check [:tuple
                      :convex/map
                      :convex/data
                      :convex/data]
                     (fn [[x k v]]
                       (let [ctx (ctx-assoc ($.form/quoted x)
                                            k
                                            v)]
                         ($.test.prop/and* (suite-map ctx)
                                           (suite-hash-map ctx))))))



($.test.prop/deftest ^:recur main-nil

  ($.test.prop/check [:tuple
                      :convex/nil
                      :convex/data
                      :convex/data]
                     (comp suite-map
                           ctx-assoc)))



($.test.prop/deftest ^:recur main-sequential

  ;; TODO. Should `(assoc [] 0 :ok)` be legal? See https://github.com/Convex-Dev/convex/issues/94

  ($.test.prop/check [:tuple
                      [:and
                       [:or
                        :convex/list
                        :convex/vector]
                       [:fn
                        #(pos? (count %))]]
                      :convex/data]
                     (fn [[coll v]]
                       (let [ctx (ctx-assoc ($.form/quoted coll)
                                            (rand-int (count coll))
                                            v)]
                         ($.test.prop/and* (suite-assoc ctx)
                                           (suite-main ctx)
                                           (suite-sequential ctx))))))



($.test.prop/deftest ^:recur main-set

  ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/109
  ;;                         https://github.com/Convex-Dev/convex/issues/110

  ($.test.prop/check [:and
                      :convex/set
                      [:fn
                       #(pos? (count %))]]
                     (fn [x]
                       (suite-main (let [v (first x)]
                                     (ctx-main ($.form/quoted x)
                                               ($.form/quoted x)
                                               v
                                               v))))))


;;;;;;;;;; `assoc`


(defn- -assoc-fail

  ;; Helper for evaluating a failing call to `assoc`.


  ([tuple]

   (-assoc-fail $.form/quoted
                tuple))


  ([fmap-x [x k v]]

   ($.test.eval/error? ($.form/templ {'?k k
                                      '?v v
                                      '?x (fmap-x x)}
                                     '(assoc ?x
                                             '?k
                                             '?v)))))



($.test.prop/deftest ^:recur assoc--fail

  ($.test.prop/check [:tuple
                      ($.test.schema/data-without #{:convex/blob-map
                                                    :convex/list
                                                    :convex/map
                                                    :convex/nil
                                                    :convex/vector})
                      :convex/data
                      :convex/data]
                     -assoc-fail))



#_($.test.prop/deftest ^:recor assoc--blob-map-fail

  ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/101

  ($.test.prop/check [:tuple
                      [:= '(blob-map)]
                      ($.test.schema/data-without #{:convex/address  ;; Specialized blob
                                                    :convex/blob})
                      :convex/data]
                     (partial -assoc-fail
                              identity)))



#_($.test.prop/deftest ^:recur assoc--sequential-fail

  ;; TODO. Infinite loop because of a Malli bug, see https://github.com/metosin/malli/issues/442

  ($.test.prop/check [:and
                      [:tuple
                       [:or
                        :convex/list
                        :convex/vector]
                       :convex/data
                       :convex/data]
                      [:fn
                       (fn [[coll k _v]]
                         (not (or (number? k)
                                  (<= 0
                                      k
                                      (count coll)))))]]
                     -assoc-fail))


;;;;;;;;;; `assoc-in`


($.test.prop/deftest ^:recur assoc-in--fail-path

  ;; Trying to assoc using a path that is not a collection.
  ;;
  ;; TODO. Maybe update this: https://github.com/Convex-Dev/convex/issues/95

  ($.test.prop/check [:tuple
                      [:or
                       :convex/list
                       :convex/map
                       :convex/nil
                       :convex/vector]
                      :convex/scalar
                      :convex/data]
                     (fn [[x path v]]
                       ($.test.eval/error? ($.form/templ {'?path path
                                                          '?v    v
                                                          '?x    x}
                                                         '(assoc-in '?x
                                                                    '?path
                                                                    '?v))))))



#_($.test.prop/deftest ^:recur assoc-in--fail-type

  ;; Trying to assoc on an illegal type.
  ;;
  ;; TODO. Failing, must fix https://github.com/Convex-Dev/convex/issues/96
  ;;                         https://github.com/Convex-Dev/convex/issues/97

  ($.test.prop/check [:tuple
                      ($.test.schema/data-without #{:convex/list
                                                    :convex/map
                                                    :convex/nil
                                                    :convex/vector})
                      :convex.test/seqpath
                      :convex/data]
                     (fn [[x path v]]
                       ($.test.eval/error? ($.form/templ {'?path path
                                                          '?v    v
                                                          '?x    x}
                                                         '(assoc-in '?x
                                                                    '?path
                                                                    '?v))))))



(defn- -eval-assoc-in

  ;; Helper for writing and evaling the CVM code for passing `assoc-in` tests.

  [item path value]

  ($.test.eval/result ($.form/templ {'?item  item
                                   '?path  path
                                   '?value value}
                                  '(= '?value
                                      (let [item-2 (assoc-in '?item
                                                             '?path
                                                             '?value)]
                                        (if (empty? '?path)
                                          item-2
                                          (get-in item-2
                                                  '?path)))))))



($.test.prop/deftest ^:recur assoc-in--map

  ;; Tests `get-in` as well, and with nil besides maps.
  ;;
  ;; TODO. Currently, empty path returns the value. Keep an eye on: https://github.com/Convex-Dev/convex/issues/96

  ($.test.prop/check [:tuple
                      [:or
                       :convex/map
                       :convex/nil]
                      :convex.test/seqpath
                      :convex/data]
                     (fn [[x path v]]
                       (-eval-assoc-in (cond->
                                         x
                                         (seq path)
                                         (dissoc (first path)))
                                       path
                                       v))))



($.test.prop/deftest ^:recur assoc-in--vect

  ;; Tests `get-in` as well.

  ;; TODO. Adapt for lists as well.
  ;; TODO. Should `(assoc [] 0 :ok)` be legal? See https://github.com/Convex-Dev/convex/issues/94

  ($.test.prop/check [:tuple
                      [:vector
                       [:or
                        :convex/nil
                        :convex/map]]
                      :convex.test/seqpath
                      :convex/data]
                     (fn [[vect path v]]
                       (let [[path-2
                              vect-2] (if (seq path)
                                        [(into [(rand-int (count vect))]
                                               path)
                                         (let [k (first path)]
                                           (mapv #(dissoc %
                                                          k)
                                                 vect))]
                                        [path
                                         vect])]
                         (-eval-assoc-in vect-2
                                         path-2
                                         v)))))


;;;;;;;;;; Misc


($.test.prop/deftest ^:recur merge--

  ($.test.prop/check [:vector
                      [:or
                       :convex/map
                       :convex/nil]]
                     (fn [x]
                       (let [ctx ($.test.eval/ctx ($.form/templ {'?x x}
                                                                '(do
                                                                   (def arg+
                                                                        '?x)
                                                                   (def -merge
                                                                        (apply merge
                                                                               arg+)))))]
                         ($.test.prop/mult*

                           "Count of merge cannot be bigger than all involved key-values"
                           ($.test.eval/result ctx
                                               '(<= (count -merge)
                                                    (reduce (fn [acc arg]
                                                              (+ acc
                                                                 (count arg)))
                                                            0
                                                            arg+)))

                           "All key-values in merged result must be in at least one input"
                           ($.test.eval/result ctx
                                               '($/every? (fn [[k v]]
                                                            ($/some (fn [arg]
                                                                      (= v
                                                                         (get arg
                                                                              k)))
                                                                    arg+))
                                                          -merge))
                           )))))



($.test.prop/deftest ^:recur reduce--

  ($.test.prop/check [:and
                      :convex/collection
                      [:fn #(pos? (count %))]]
                     (fn [x]
                       ($.test.eval/result ($.form/templ {'?i (rand-int (count x))
                                                          '?x x}
                                                         '(let [x '?x
                                                                v (nth x
                                                                       ?i)]
                                                            (= v
                                                               (reduce (fn [acc item]
                                                                         (if (= item
                                                                                v)
                                                                           (reduced item)
                                                                           acc))
                                                                       :convex-sentinel
                                                                       x))))))))


;;;;;;;;;;


;; Creating collections

; blob-map
; hash-map
; hash-set
; list
; vector

;; Associative operations (with lists as well)

; assoc
; assoc-in
; contains-key?
; get
; get-in

;; Map operations


; dissoc
; keys
; merge
; values

;; Misc operations

; concat
; conj
; cons
; count
; empty
; empty?
; first
; into
; last
; map && mapv
; next
; nth
; reduce
; reduced
; second
