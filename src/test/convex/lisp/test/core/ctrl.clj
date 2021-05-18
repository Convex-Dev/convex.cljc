(ns convex.lisp.test.core.ctrl

  "Testing flow control constructs."

  {:author "Adam Helinski"}

  (:require [clojure.string]
            [convex.lisp           :as $]
            [convex.lisp.ctx       :as $.ctx]
            [convex.lisp.form      :as $.form]
            [convex.lisp.test.eval :as $.test.eval]
            [convex.lisp.test.prop :as $.test.prop]
            [convex.lisp.test.util :as $.test.util]))


;;;;;;;;;; Helpers


(defn- -nested-fn

  ;; Used by [[halting--]] and [[rollback--]] for nesting functions that ultimately should end preemptively.


  ([n form]

   (if (<= n
           0)
     form
     (list (list 'fn
                 []
                 (-nested-fn (dec n)
                             form)))))

  ([n form x-ploy]

   (-nested-fn n
               (list 'do
                     form
                     (list 'quote
                           x-ploy))))


  ([n sym x-ploy x-return]

   (-nested-fn n
               (list sym
                     (list 'quote
                           x-return))
               x-ploy)))


;;;;;;;;;; Tests


($.test.prop/deftest ^:recur assert--

  ($.test.prop/check [:tuple
                      [:int
                       {:max 16
                        :min 1}]
                      :convex/data
                      :convex/truthy]
                     (fn [[n x-ploy x-return]]
                       (identical? :ASSERT
                                   (-> ($.test.eval/error (-nested-fn n
                                                                      ($.form/templ* (assert (not (quote ~x-return))))
                                                                      x-ploy))
                                       :convex.error/code)))))



($.test.prop/deftest ^:recur and-or

  ($.test.prop/check [:vector
                      :convex/data]
                     (fn [x]
                       (let [x-quoted  (map $.form/quoted
                                            x)
                             assertion (fn [sym]
                                         ($.test.eval/like-clojure? (list* sym
                                                                           x-quoted)))]
                         ($.test.prop/mult*

                           "`and` consistent with Clojure"
                           (assertion 'and)

                           "`or` consistent with Clojure"
                           (assertion 'or))))))



($.test.prop/deftest ^:recur cond--

  ($.test.prop/check [:vector
                      :convex/data]
                     (fn [x]
                       (let [x-quoted (map $.form/quoted
                                           x)]
                         ($.test.util/eq (eval (list* 'cond
                                                      (if (even? (count x-quoted))
                                                        x-quoted
                                                        (concat (butlast x-quoted)
                                                                [:else
                                                                 (last x-quoted)]))))
                                         ($.test.eval/result (list* 'cond
                                                                    x-quoted)))))))



($.test.prop/deftest ^:recur fail--

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



($.test.prop/deftest ^:recur halting

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



($.test.prop/deftest ^:recur if-like

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



($.test.prop/deftest ^:recur rollback--

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
