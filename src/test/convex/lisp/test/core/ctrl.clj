(ns convex.lisp.test.core.ctrl

  "Testing flow control constructs."

  {:author "Adam Helinski"}

  (:require [clojure.string]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.lisp                   :as $]
            [convex.lisp.ctx               :as $.ctx]
            [convex.lisp.form              :as $.form]
            [convex.lisp.gen               :as $.gen]
            [convex.lisp.test.eval         :as $.test.eval]
            [convex.lisp.test.prop         :as $.test.prop]
            [convex.lisp.test.util         :as $.test.util]))


;;;;;;;;;; Helpers


(defn- -nested-fn

  ;; Used by [[halting--]] and [[rollback--]] for nesting functions that ultimately should end preemptively.


  ([n form]

   (if (<= n
           0)
     form
     ($.form/templ* ((fn []
                       ~(-nested-fn (dec n)
                                    form))))))

  ([n form x-ploy]

   (-nested-fn n
               ($.form/templ* (do
                                ~form
                                ~x-ploy))))


  ([n sym x-ploy x-return]

   (-nested-fn n
               ($.form/templ* (~sym ~x-return))
               x-ploy)))


;;;;;;;;;; Tests


($.test.prop/deftest assert--

  (TC.prop/for-all [n        (TC.gen/choose 1
                                            16)
                    x-ploy   $.gen/any
                    x-return $.gen/truthy]
    (identical? :ASSERT
                (-> ($.test.eval/error (-nested-fn n
                                                   ($.form/templ* (assert (not ~x-return)))
                                                   x-ploy))
                    :convex.error/code))))



($.test.prop/deftest logic

  (TC.prop/for-all [[falsy+
                     mix+
                     truthy+] (TC.gen/bind (TC.gen/tuple (TC.gen/vector $.gen/falsy
                                                                        1
                                                                        16)
                                                         (TC.gen/vector $.gen/truthy
                                                                        1
                                                                        16))
                                           (fn [[falsy+ truthy+]]
                                             (TC.gen/tuple (TC.gen/return falsy+)
                                                           (TC.gen/shuffle (concat falsy+
                                                                                   truthy+))
                                                           (TC.gen/return truthy+))))]
    ($.test.prop/mult*

      "`and` on falsy"
      ($.test.eval/result* (= ~(first falsy+)
                              (and ~@falsy+)))

      "`and` on mixed"
      ($.test.eval/result* (= ~(first (filter (comp not
                                                    boolean)
                                              mix+))
                              (and ~@mix+)))

      "`and` on truthy"
      ($.test.eval/result* (= ~(last truthy+)
                              (and ~@truthy+)))


      "`or` on falsy"
      ($.test.eval/result* (= ~(last falsy+)
                              (or ~@falsy+)))
      
      "`or` on mixed"
      ($.test.eval/result* (= ~(first (filter boolean
                                              mix+))
                              (or ~@mix+)))

      "`or` on truthy"
      ($.test.eval/result* (= ~(first truthy+)
                              (or ~@truthy+))))))




($.test.prop/deftest cond--

  (TC.prop/for-all [else? $.gen/boolean
                    x+    (TC.gen/vector (TC.gen/tuple $.gen/boolean
                                                       (TC.gen/one-of [$.gen/falsy
                                                                       $.gen/truthy]))
                                         1
                                         16)]
    ($.test.eval/result* (= ~(or (first (into []
                                              (comp (map second)
                                                    (filter boolean)
                                                    (take 1))
                                              x+))
                                 (when else?
                                   (second (peek x+))))
                            (cond
                              ~@(cond->
                                  (mapcat (fn [[identity? x]]
                                            [(if identity?
                                               (list 'identity
                                                     x)
                                               x)
                                             x])
                                          x+)
                                  else?
                                  butlast))))))



($.test.prop/deftest fail--

  ($.test.prop/check [:tuple
                      [:int
                       {:max 16
                        :min 1}]
                      [:and
                       :convex/data
                       [:not :convex/nil]]
                      :convex/data
                      :convex/data]
                     (fn [[n code message x-ploy]]
                       ($.test.util/eq {:convex.error/code    code
                                        :convex.error/message message}
                                       (select-keys ($.test.eval/error (-nested-fn n
                                                                                   ($.form/templ* (fail (quote ~code)
                                                                                                        (quote ~message)))
                                                                                   x-ploy))
                                                    [:convex.error/code
                                                     :convex.error/message])))))



($.test.prop/deftest halting

  ($.test.prop/check [:tuple
                      [:int
                       {:max 16
                        :min 1}]
                      :convex/data
                      :convex/data]
                     (fn [[n x-ploy x-return]]
                       ($.test.prop/mult*

                         "`halt`"
                         ($.test.util/eq x-return
                                         ($.test.eval/result (-nested-fn n
                                                                         'halt
                                                                         x-ploy
                                                                         x-return)))

                         "`return`"
                         ($.test.util/eq [x-return
                                          x-ploy]
                                         ($.test.eval/result* [~(-nested-fn n
                                                                            'return
                                                                            x-ploy
                                                                            x-return)
                                                               (quote ~x-ploy)]))))))



($.test.prop/deftest if-like

  ($.test.prop/check [:tuple
                      [:and
                       :convex/symbol
                       [:fn #(not (clojure.string/includes? (str %)
                                                            "."))]]
                      :convex/data]
                     (fn [[sym x]]
                       ($.test.prop/mult*

                         "`if` is consistent with Clojure"
                         ($.test.eval/like-clojure?* (if (quote ~x)
                                                       :true
                                                       :false))

                         "`if-let` is consistent with Clojure"
                         ($.test.eval/like-clojure?* (if-let [~sym (quote ~x)]
                                                       :true
                                                       :false))

                         "`when` is consistent with Clojure"
                         ($.test.eval/like-clojure?* (when (quote ~x)
                                                       :true))

                         "`when-let` is consistent with Clojure"
                         ($.test.eval/like-clojure?* (when-let [~sym (quote ~x)]
                                                       :true))

                         "`when-not` is consistent with Clojure"
                         ($.test.eval/like-clojure?* (when-not (quote ~x)
                                                       :true))))))



($.test.prop/deftest rollback--

  ($.test.prop/check [:tuple
                      [:int
                       {:max 16
                        :min 1}]
                      :convex/symbol
                      :convex/data
                      :convex/data
                      :convex/data]
                     (fn [[n sym x-env x-return x-ploy]]
                       (let [ctx ($.test.eval/ctx* (do
                                                     (def ~sym
                                                          (quote ~x-env))
                                                     ~(-nested-fn n
                                                                  'rollback
                                                                  x-ploy
                                                                  x-return)
                                                     (quote ~x-ploy)))]
                         ($.test.prop/mult*

                           "Returned value is the rollback value"
                           ($.test.util/eq x-return
                                           (-> ctx
                                               $.ctx/result
                                               $/datafy))

                           "State has been rolled back"
                           (let [form '(hash (encoding *state*))]
                             ($.test.util/eq ($.test.eval/result form)
                                             ($.test.eval/result ctx
                                                                 form))))))))
