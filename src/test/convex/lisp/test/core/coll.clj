(ns convex.lisp.test.core.coll

  ""

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


($.test.prop/deftest ^:recur assoc--fail

  ($.test.prop/check [:tuple
                      ($.test.schema/data-without #{:convex/list
                                                    :convex/map
                                                    :convex/nil
                                                    :convex/vector})
                      :convex/data
                      :convex/data]
                     (fn [[x k v]]
                       ($.test.eval/exceptional ($.form/templ {'?k k
                                                               '?v v
                                                               '?x x}
                                                              '(assoc '?x
                                                                      '?k
                                                                      '?v))))))



($.test.prop/deftest ^:recur assoc--map

  ;; Tests `get` as well, and with nil besides maps.

  ($.test.prop/check [:tuple
                      [:or
                       :convex/map
                       :convex/nil]
                      :convex/data
                      :convex/data]
                     (fn [[x k v]]
                       ($.test.eval/form ($.form/templ {'?k k
                                                        '?v v
                                                        '?x x}
                                                       '(= '?v
                                                           (get (assoc '?x
                                                                       '?k
                                                                       '?v)
                                                                '?k)))))))




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
                       ($.test.eval/form ($.form/templ {'?coll coll
                                                        '?k    (rand-int (count coll))
                                                        '?v    v}
                                                       '(= '?v
                                                           (get (assoc '?coll
                                                                       ?k
                                                                       '?v)
                                                                ?k)))))))



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
                     (fn [[coll k v]]
                       ($.test.eval/exceptional ($.form/templ {'?coll coll
                                                               '?k    k
                                                               '?v    v}
                                                              '(assoc '?coll
                                                                      '?k
                                                                      '?v))))))


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
                       ($.test.eval/form ($.form/templ {'?path (into [(rand-int (count vect))]
                                                                     path)
                                                        '?v    v
                                                        '?vect (if (seq path)
                                                                 (let [k (first path)]
                                                                   (mapv #(dissoc %
                                                                                  k)
                                                                         vect))
                                                                 vect)}
                                                       '(= '?v
                                                           (get-in (assoc-in '?vect
                                                                             '?path
                                                                             '?v)
                                                                   '?path)))))))


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
; keys

; dissoc



;; Map operations

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

