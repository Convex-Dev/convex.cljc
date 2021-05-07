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

  ($.test.prop/check [:vector
                      {:min 1}
		      	      :convex/data]
		      	     (fn [x]
		      	       (= (apply list
		      	       	   	    x)
		      	          ($.test.eval/form (list* 'list
 		      	       							  (map $.form/quoted
                                                       x)))))))



($.test.prop/deftest ^:recur vector--

  ($.test.prop/check [:vector
                      {:min 1}
		      		  :convex/data]
		      	     (fn [x]
		      	       (= (apply vector
                                 x)
		      	          ($.test.eval/form (list* 'vector
 		      	       							  (map $.form/quoted
                                                       x)))))))


;;;;;;;;;; Adding elements


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
                     (fn [[hmap k v]]
                       ($.test.eval/form ($.form/templ {'?map hmap
                                                        '?k   k
                                                        '?v   v}
                                                       '(= '?v
                                                           (get (assoc '?map
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

