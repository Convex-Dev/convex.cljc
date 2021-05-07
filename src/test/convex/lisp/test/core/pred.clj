(ns convex.lisp.test.core.pred

  "Tests Convex core type predicate. 
  
   Specialized predicates such as `contains-key?` or `fn?` are located in relevant namespace."

  {:author "Adam Helinski"}

  (:require [clojure.test            :as t]
            [convex.lisp.test.eval   :as $.test.eval]
            [convex.lisp.test.prop   :as $.test.prop]
            [convex.lisp.test.schema :as $.test.schema]))


;;;;;;;;;;


($.test.prop/deftest ^:recur address?--false

  ;; TODO. Also test `actor?`? See #74.

  ($.test.prop/pred-data-false 'address?
                               #{:convex/address
                                 :convex/boolean  ;; TODO. See #73
                                 :convex/char     ;; TODO. See #68
                                 :convex/double
                                 :convex/long}))



($.test.prop/deftest address?--true

  ($.test.prop/check :convex/address
                     (fn [x]
                       ($.test.eval/form (list 'address?
                                               x)))))



($.test.prop/deftest ^:recur blob?--false

  ($.test.prop/pred-data-false 'blob?
                               #{:convex/blob}))



($.test.prop/deftest blob?--true

  ($.test.prop/pred-data-true 'blob?
                              :convex/blob))



($.test.prop/deftest ^:recur boolean?--false

  ($.test.prop/pred-data-false 'boolean?
                               boolean?
                               #{:convex/boolean}))



(t/deftest boolean?--true

  (t/is (true? ($.test.eval/form true))
        "True")

  (t/is (false? ($.test.eval/form false))
        "False"))



($.test.prop/deftest ^:recur coll?--false

  ($.test.prop/pred-data-false 'coll?
                               coll?
                               #{:convex/list
                                 :convex/map
                                 :convex/set
                                 :convex/vector}))



($.test.prop/deftest ^:recur coll?--true

  ($.test.prop/pred-data-true 'coll?
                              coll?
                              :convex/collection))



($.test.prop/deftest ^:recur keyword?--false

  ($.test.prop/pred-data-false 'keyword?
                               keyword?
                               #{:convex/keyword}))



($.test.prop/deftest keyword?--true

  ($.test.prop/pred-data-true 'keyword?
                              keyword?
                              :convex/keyword))



($.test.prop/deftest ^:recur list?--false

  ($.test.prop/pred-data-false 'list?
                               list?
                               #{:convex/list}))



($.test.prop/deftest ^:recur list?--true

  ($.test.prop/pred-data-true 'list?
                              list?
                              :convex/list))



($.test.prop/deftest ^:recur long?--false

  ($.test.prop/pred-data-false 'long?
                               int?
                               #{:convex/long}))



($.test.prop/deftest long?--true

  ($.test.prop/pred-data-true 'long?
                              int?
                              :convex/long))



($.test.prop/deftest ^:recur map?--false

  ($.test.prop/pred-data-false 'map?
                               map?
                               #{:convex/map}))



($.test.prop/deftest ^:recur map?--true

  ($.test.prop/pred-data-true 'map?
                              map?
                              :convex/map))



($.test.prop/deftest ^:recur nil?--false

  ($.test.prop/pred-data-false 'nil?
                               nil?
                               #{:convex/nil}))



(t/deftest nil?--true

  (t/is (true? (nil? ($.test.eval/form nil))))

  (t/is (true? (nil? ($.test.eval/form '(do nil))))))



($.test.prop/deftest ^:recur number?--false

  ($.test.prop/pred-data-false 'number?
                               number?
                               #{:convex/boolean ;; TODO. See #73.
                                 :convex/char    ;; TODO. See #68.
                                 :convex/double
                                 :convex/long}))



($.test.prop/deftest number?--true

  ($.test.prop/pred-data-true 'number?
                              number?
                              :convex/number))



($.test.prop/deftest ^:recur set?--false

  ($.test.prop/pred-data-false 'set?
                               set?
                               #{:convex/set}))



($.test.prop/deftest ^:recur set?--true

  ($.test.prop/pred-data-true 'set?
                              set?
                              :convex/set))



($.test.prop/deftest ^:recur str?--false

  ($.test.prop/pred-data-false 'str?
                               string?
                               #{:convex/string}))



($.test.prop/deftest str?--true

  ($.test.prop/pred-data-true 'str?
                              string?
                              :convex/string))



($.test.prop/deftest symbol?--true

  ($.test.prop/pred-data-true 'symbol?
                              symbol?
                              :convex/symbol))



($.test.prop/deftest ^:recur symbol?--false

  ($.test.prop/pred-data-false 'symbol?
                               (partial $.test.schema/valid?
                                        :convex/symbol)
                               #{:convex/symbol}))



($.test.prop/deftest ^:recur vector?--false

  ($.test.prop/pred-data-false 'vector?
                               vector?
                               #{:convex/vector}))



($.test.prop/deftest ^:recur vector?--true

  ($.test.prop/pred-data-true 'vector?
                              vector?
                              :convex/vector))
