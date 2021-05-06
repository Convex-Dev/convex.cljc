(ns convex.lisp.test.core.pred

  "Tests Convex core type predicate. 
  
   Specialized predicates such as `contains-key?` or `fn?` are located in relevant namespace."

  {:author "Adam Helinski"}

  (:require [clojure.test                    :as t]
            [clojure.test.check.properties   :as tc.prop]
            [clojure.test.check.clojure-test :as tc.ct]
            [convex.lisp                     :as $]
            [convex.lisp.test.eval           :as $.test.eval]
            [convex.lisp.test.prop           :as $.test.prop]
            [convex.lisp.test.schema         :as $.test.schema]
            [convex.lisp.test.util           :as $.test.util]))


(def max-size-coll 5)


;;;;;;;;;;


(tc.ct/defspec address?--false

  ;; TODO. Also test `actor?`? See #74.

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'address?
                               #{:convex/address
                                 :convex/boolean  ;; TODO. See #73
                                 :convex/char     ;; TODO. See #68
                                 :convex/double
                                 :convex/long}))



(tc.ct/defspec address?--true

  ($.test.prop/check :convex/address
                     (fn [x]
                       ($.test.eval/form (list 'address?
                                               x)))))



(tc.ct/defspec blob?--false

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'blob?
                               #{:convex/blob}))



(tc.ct/defspec blob?--true

  ($.test.prop/pred-data-true 'blob?
                              :convex/blob))



(tc.ct/defspec boolean?--false

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'boolean?
                               boolean?
                               #{:convex/boolean}))



(t/deftest boolean?--true

  (t/is (true? ($.test.eval/form true))
        "True")

  (t/is (false? ($.test.eval/form false))
        "False"))



(tc.ct/defspec coll?--false

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'coll?
                               coll?
                               #{:convex/list
                                 :convex/map
                                 :convex/set
                                 :convex/vector}))



(tc.ct/defspec coll?--true

  {:max-size max-size-coll}

  ($.test.prop/pred-data-true 'coll?
                              coll?
                              :convex/collection))



(tc.ct/defspec keyword?--false

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'keyword?
                               keyword?
                               #{:convex/keyword}))



(tc.ct/defspec keyword?--true

  ($.test.prop/pred-data-true 'keyword?
                              keyword?
                              :convex/keyword))



(tc.ct/defspec list?--false

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'list?
                               list?
                               #{:convex/list}))



(tc.ct/defspec list?--true

  {:max-size max-size-coll}

  ($.test.prop/pred-data-true 'list?
                              list?
                              :convex/list))



(tc.ct/defspec long?--false

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'long?
                               int?
                               #{:convex/long}))



(tc.ct/defspec long?--true

  ($.test.prop/pred-data-true 'long?
                              int?
                              :convex/long))



(tc.ct/defspec map?--false

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'map?
                               map?
                               #{:convex/map}))



(tc.ct/defspec map?--true

  {:max-size max-size-coll}

  ($.test.prop/pred-data-true 'map?
                              map?
                              :convex/map))



(tc.ct/defspec nil?--false

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'nil?
                               nil?
                               #{:convex/nil}))



(t/deftest nil?--true

  (t/is (true? (nil? ($.test.eval/form nil))))

  (t/is (true? (nil? ($.test.eval/form '(do nil))))))



(tc.ct/defspec number?--false

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'number?
                               number?
                               #{:convex/boolean ;; TODO. See #73.
                                 :convex/char    ;; TODO. See #68.
                                 :convex/double
                                 :convex/long}))



(tc.ct/defspec number?--true

  ($.test.prop/pred-data-true 'number?
                              number?
                              :convex/number))



(tc.ct/defspec set?--false

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'set?
                               set?
                               #{:convex/set}))



(tc.ct/defspec set?--true

  {:max-size max-size-coll}

  ($.test.prop/pred-data-true 'set?
                              set?
                              :convex/set))



(tc.ct/defspec str?--false

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'str?
                               string?
                               #{:convex/string}))



(tc.ct/defspec str?--true

  ($.test.prop/pred-data-true 'str?
                              string?
                              :convex/string))



(tc.ct/defspec symbol?--true

  ($.test.prop/pred-data-true 'symbol?
                              symbol?
                              :convex/symbol))



(tc.ct/defspec symbol?--false

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'symbol?
                               (partial $.test.schema/valid?
                                        :convex/symbol)
                               #{:convex/symbol}))



(tc.ct/defspec vector?--false

  {:max-size max-size-coll}

  ($.test.prop/pred-data-false 'vector?
                               vector?
                               #{:convex/vector}))



(tc.ct/defspec vector?--true

  {:max-size max-size-coll}

  ($.test.prop/pred-data-true 'vector?
                              vector?
                              :convex/vector))
