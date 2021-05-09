(ns convex.lisp.test.core.coll

  "Testing core functions operating on collections."

  {:author "Adam Helinski"}

  (:require [clojure.test            :as t]
            [convex.lisp.form        :as $.form]
            [convex.lisp.test.eval   :as $.test.eval]
            [convex.lisp.test.prop   :as $.test.prop]
            [convex.lisp.test.schema :as $.test.schema]))


;;;;;;;;;;


(t/deftest blob-map--

  (t/is (map? ($.test.eval/form '(blob-map)))))



($.test.prop/deftest ^:recur hash-map--

  ;; TODO. Also test failing with odd number of items.
  ;;
  ;; Cannot compare with Clojure: https://github.com/Convex-Dev/convex-web/issues/66

  ($.test.prop/check [:+ [:cat
                          :convex/data
                          :convex/data]]
                     (fn [x]
                       (map? ($.test.eval/form (list* 'hash-map
                                                      (map $.form/quoted
                                                           x)))))))



(t/deftest hash-map--no-arg

  (t/is (= {}
           ($.test.eval/form '(hash-map)))))



($.test.prop/deftest ^:recur hash-set--

  ;; Cannot compare with Clojure: https://github.com/Convex-Dev/convex-web/issues/66

  ($.test.prop/check [:vector
                      {:min 1}
                      :convex/data]
                     (fn [x]
                       (set? ($.test.eval/form (list* 'hash-set
                                                      (map $.form/quoted
                                                           x)))))))



(t/deftest hash-set--no-arg

  (t/is (= #{}
           ($.test.eval/form '(hash-set)))))



($.test.prop/deftest ^:recur list--

  ($.test.prop/create-data 'list
                           list))



($.test.prop/deftest ^:recur vector--

  ($.test.prop/create-data 'vector
                           vector))


;;;;;;;;;; `assoc`


(defn- -assoc-fail

  ;; Helper for evaling a failing call to `assoc`.


  ([tuple]

   (-assoc-fail $.form/quoted
                tuple))


  ([fmap-x [x k v]]

   ($.test.eval/exceptional ($.form/templ {'?k k
                                           '?v v
                                           '?x (fmap-x x)}
                                          '(assoc ?x
                                                  '?k
                                                  '?v)))))




(defn mult-assoc

  ""

  [ctx]

  ($.test.prop/checkpoint*

    "Basic `assoc` properties common to all suported types"

    ($.test.prop/mult*

      "Contains key"
      ($.test.eval/form ctx
                        '(contains-key? x-2
                                        k))

      "Associating existing value does not change anything"
      ($.test.eval/form ctx
                        '(= x-2
                            (assoc x-2
                                   k
                                   v)))

      "Consistent with `assoc-in`"
      ($.test.eval/form ctx
                        '(= x-2
                            (assoc-in x
                                      [k]
                                      v)))

      "`get` returns the value"
      ($.test.eval/form ctx
                        '(= v
                            (get x-2
                                 k)))

      "`get-in` returns the value"
      ($.test.eval/form ctx
                        '(= v
                            (get-in x-2
                                    [k])))
      "Cannot be empty"
      ($.test.eval/form ctx
                        '(not (empty? x-2)))

      "Count is at least 1"
      ($.test.eval/form ctx
                        '(>= (count x-2)
                             1))

      "`first` is not exceptional"
      ($.test.eval/form ctx
                        '(do
                           (first x-2)
                           true))

      "`(nth 0)` is not exceptional"
      ($.test.eval/form ctx
                        '(do
                           (nth x-2
                                0)
                           true))

      "`last` is is not exceptional"
      ($.test.eval/form ctx
                        '(do
                           (last x-2)
                           true))

      "`nth` to last item is not exceptional"
      ($.test.eval/form ctx
                        '(do
                           (nth x-2
                                (dec (count x-2)))
                           true))

      "`nth` is consistent with `first`"
      ($.test.eval/form ctx
                        '(= (first x-2)
                            (nth x-2
                                 0)))

      "`nth` is consistent with `last`"
      ($.test.eval/form ctx
                        '(= (last x-2)
                            (nth x-2
                                 (dec (count x-2)))))

      "`nth` is consistent with second"
      ($.test.eval/form ctx
                        '(if (>= (count x-2)
                                 2)
                           (= (second x-2)
                              (nth x-2
                                   1))
                           true))
      )))







(defn mult-assoc-hashmap

  ""

  [ctx]

  ($.test.prop/checkpoint*

    "Using `hash-map` to rebuild map"
    ($.test.eval/form ctx
                      '(= x-2
                          (apply hash-map
                                 (reduce (fn [acc [k v]]
                                           (conj acc
                                                 k
                                                 v))
                                         []
                                         x-2))))))



(defn mult-assoc-kv+

  ""

  [ctx]

  ($.test.prop/checkpoint*

    "Using `keys` and various functions ensuring consistent order in maps (blob-map and hash-map)"
    (let [ctx-2 ($.test.eval/form->ctx ctx
                                      '(do
                                         (def k+
                                              (keys x-2))
                                         (def kv+
                                              (vec x-2))))]
      ($.test.prop/mult*

        "Keys contain new key"
        ($.test.eval/form ctx-2
                          '(contains-key? (set k+)
                                          k))

        "`vec` is consitent with `into`"
        ($.test.eval/form ctx-2
                          '(= kv+
                              (into []
                                    x-2)))

        "Order of `keys` is consistent with `vec`"
        ($.test.eval/form ctx-2
                          '(= k+
                              (map first
                                   kv+)))

        "Order of `values` is consistent with `vec`"
        ($.test.eval/form ctx-2
                          '(= (values x-2)
                              (map second
                                   kv+)))

        "Order of `mapv` is consistent with `vec`"
        ($.test.eval/form ctx-2
                          '(= kv+
                              (mapv identity
                                    x-2)))
        ))))



(defn mult-assoc-dissoc

  ""

  [ctx]

  ($.test.prop/checkpoint*

    "Working with `dissoc` on a key that has been `assoc`iated (hence must exist)"
    (let [ctx-2 ($.test.eval/form->ctx ctx
                                       '(def x-3
                                             (dissoc x-2
                                                     k)))]
      ($.test.prop/mult*

        "Does not contain key anymore"
        ($.test.eval/form ctx-2
                          '(not (contains-key? x-3
                                               k)))

        "`get` returns nil"
        ($.test.eval/form ctx-2
                          '(nil? (get x-3
                                      k)))

        "`get` returns 'not-found' value"
        ($.test.eval/form ctx-2
                          '(= :convex-sentinel
                              (get x-3
                                   k
                                   :convex-sentinel)))

        "`get-in` returns nil"
        ($.test.eval/form ctx-2
                          '(nil? (get-in x-3
                                         [k])))

        ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/102
        ;; "`get-in` returns 'not-found' value"
        ;; ($.test.eval/form ctx-2
        ;;                   '(= :convex-sentinel
        ;;                       (get-in x-3
        ;;                               [k]
        ;;                               :convex-sentinel)))

        "Keys do not removed key"
        ($.test.eval/form ctx
                          '(not (contains-key? (set (keys x-3))
                                               k)))

        "All other key-values are preserved"
        ($.test.eval/form ctx
                          '(reduce (fn [_acc k]
                                     (or (= (get x-3
                                                 k)
                                            (get x-2
                                                 k))
                                         (reduced false)))
                                   true
                                   (keys x-3)))

        "Equal to original or count updated as needed"
        ($.test.eval/form ctx-2
                          '(if (nil? x)
                             (= {}
                                x-3)
                             (if (contains-key? x
                                                k)
                               (= (count x-3)
                                  (dec (count x)))
                               (= x
                                  x-3))))

        "Working with keys and key-values"
        (mult-assoc-kv+ ctx)
        ))))



(defn mult-assoc-map-specific

  ""

  [ctx]

  ($.test.prop/checkpoint*

    "`assoc` properties for map-like types (blob-map, hash-map, and nil)"

    ($.test.prop/mult*

      "Count has been updated as needed"
      ($.test.eval/form ctx
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
      ;; ($.test.eval/form ctx
      ;;                   '(= x-2
      ;;                       (merge (empty x-2)
      ;;                              x-2)))
      ;;
      ;; "Merging original with new = new"
      ;; ($.test.eval/form ctx
      ;;                   '(= x-2
      ;;                       (merge x
      ;;                              x-2)))

      "`conj` is consistent with `assoc`"
      ($.test.eval/form ctx
                        '(if (map? x)
                           (= x-2
                              (conj x
                                    [k v]))
                           true))

      "`into` is consistent with `assoc`"
      ($.test.eval/form ctx
                        '(if (map? x)
                           (= x-2
                              (into x
                                    [[k v]]))
                           true))

      "All other key-values are preserved"
      ($.test.eval/form ctx
                        '(reduce (fn [_acc k]
                                   (or (= (get x
                                               k)
                                          (get x-2
                                               k))
                                       (reduced false)))
                                 true
                                 (keys (dissoc x
                                               k))))

      "Using `into` to rebuild map"
      ($.test.eval/form ctx
                        '(= x-2
                            (into (empty x-2)
                                  x-2)))
      )))




(defn mult-assoc-map

  ""

  [ctx]

  ($.test.prop/and* (mult-assoc ctx)
                    (mult-assoc-dissoc ctx)
                    (mult-assoc-kv+ ctx)
                    (mult-assoc-map-specific ctx)))





(defn ctx-assoc

  ;; Helper for evaling valid call to `assoc` followed by `get` with the same key.
  ;;
  ;; By default, `fmap-x` quotes `x`.


  ([[x k v]]

   (ctx-assoc x
              k
              v))


  ([x k v]

   (-> ($.form/templ {'?k k
                      '?v v
                      '?x x}
                     '(do
                        (def k
                             '?k)
                        (def v
                             '?v)
                        (def x
                             ?x)
                        (def x-2
                             (assoc x
                                    k
                                    v))))
       $.test.eval/form->ctx)))




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



($.test.prop/deftest ^:recur assoc--blob-map

  ($.test.prop/check [:tuple
                      [:= '(blob-map)]
                      :convex/blob
                      :convex/data]
                     (comp mult-assoc-map
                           ctx-assoc)))



#_($.test.prop/deftest ^:recor assoc--blob-map-fail

  ;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/101

  ($.test.prop/check [:tuple
                      [:= '(blob-map)]
                      ($.test.schema/data-without #{:convex/address  ;; Specialized blob
                                                    :convex/blob})
                      :convex/data]
                     (partial -assoc-fail
                              identity)))



($.test.prop/deftest ^:recur assoc--map

  ;; Tests `get` as well, and with nil besides maps.

  ($.test.prop/check [:tuple
                      :convex/map
                      :convex/data
                      :convex/data]
                     (fn [[x k v]]
                       (let [ctx (ctx-assoc ($.form/quoted x)
                                            k
                                            v)]
                         ($.test.prop/and* (mult-assoc-map ctx)
                                           (mult-assoc-hashmap ctx))))))



($.test.prop/deftest ^:recur assoc--nil

  ;; Tests `get` as well, and with nil besides maps.

  ($.test.prop/check [:tuple
                      :convex/nil
                      :convex/data
                      :convex/data]
                     (comp mult-assoc-map
                           ctx-assoc)))



($.test.prop/deftest ^:recur assoc--sequential

  ;; Tests `get` as well.

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
                       (mult-assoc (ctx-assoc ($.form/quoted coll)
                                              (rand-int (count coll))
                                              v)))))





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
                       ($.test.eval/exceptional ($.form/templ {'?path path
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
                       ($.test.eval/exceptional ($.form/templ {'?path path
                                                               '?v    v
                                                               '?x    x}
                                                              '(assoc-in '?x
                                                                         '?path
                                                                         '?v))))))



(defn- -eval-assoc-in

  ;; Helper for writing and evaling the CVM code for passing `assoc-in` tests.

  [item path value]

  ($.test.eval/form ($.form/templ {'?item  item
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

; conj
; count
; empty
; empty?
; first
; into
; last
; next
; nth
; reduce
; reduced
; second


;; Producing vectors

; concat
; map && mapv ???



;; Producing lists

; cons



;; Set operations

; difference
; disj
; intersection
; subset
; union
