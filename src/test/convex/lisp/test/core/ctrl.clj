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

  ;; Used for nesting functions that ultimately should end preemptively.


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



(def gen-nest

  "Generating `n` value for [[-nested-fn]]."

  (TC.gen/choose 1
                 16))


;;;;;;;;;; Tests


($.test.prop/deftest assert--

  (TC.prop/for-all [n        gen-nest
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

  (TC.prop/for-all [n       gen-nest
                    code    $.gen/keyword
                    message $.gen/any
                    x-ploy  $.gen/any]
    (let [exec (fn [form]
                 ($.test.eval/error (-nested-fn n
                                                form
                                                x-ploy)))]
      ($.test.prop/and* ($.test.prop/checkpoint*
  
                          "Without code"

                          (let [ret (exec ($.form/templ* (fail ~message)))]
                            ($.test.prop/mult*

                              "No code"
                              (= :ASSERT
                                 (ret :convex.error/code))

                              "Message"
                              ($.test.util/eq ($.test.eval/result message)
                                              (ret :convex.error/message)))))

                        ($.test.prop/checkpoint*

                          "With code"

                          (let [ret (exec ($.form/templ* (fail ~code
                                                               ~message)))]
                            ($.test.prop/mult*

                              "Code"
                              (= code
                                 (ret :convex.error/code))

                              "Message"
                              ($.test.util/eq ($.test.eval/result message)
                                              (ret :convex.error/message)))))))))



($.test.prop/deftest halting

  (TC.prop/for-all [n        gen-nest
                    x-ploy   $.gen/any
                    x-return $.gen/any]
    ($.test.prop/mult*

      "`halt`"
      ($.test.util/eq ($.test.eval/result x-return)
                      ($.test.eval/result (-nested-fn n
                                                      'halt
                                                      x-ploy
                                                      x-return)))

      "`return`"
      ($.test.util/eq ($.test.eval/result* [~x-return
                                            ~x-ploy])
                      ($.test.eval/result* [~(-nested-fn n
                                                         'return
                                                         x-ploy
                                                         x-return)
                                            ~x-ploy])))))



($.test.prop/deftest if-like

  (TC.prop/for-all [sym    $.gen/symbol
                    falsy  $.gen/falsy
                    truthy $.gen/truthy]
    (let [ctx ($.test.eval/ctx* (do
                                  (def tag-false
                                       [:tag ~falsy])
                                  (def tag-true
                                       [:tag ~truthy])))]
     ($.test.prop/mult*

       "`if` false"
       ($.test.eval/result* ctx
                            (= tag-false
                               (if ~falsy
                                 tag-true
                                 tag-false)))

       "`if` true"
       ($.test.eval/result* ctx
                            (= tag-true
                               (if ~truthy
                                 tag-true
                                 tag-false)))

       "`if-let` false"
       ($.test.eval/result* ctx
                            (= tag-false
                               (if-let [~sym ~falsy]
                                 tag-true
                                 tag-false)))

       "`if-let` true"
       ($.test.eval/result* ctx
                            (= tag-true
                               (if-let [~sym ~truthy]
                                 tag-true
                                 tag-false)))

       "`when` false"
       ($.test.eval/result* ctx
                            (nil? (when ~falsy
                                    tag-true)))

       "`when` true"
       ($.test.eval/result* ctx
                            (= tag-true
                               (when ~truthy
                                 tag-true)))

       "`when-let` false"
       ($.test.eval/result* ctx
                            (nil? (when-let [~sym ~falsy]
                                    tag-true)))

       "`when-let` true"
       ($.test.eval/result* ctx
                            (= tag-true
                               (when-let [~sym ~truthy]
                                 tag-true)))

       "`when-not` false"
       ($.test.eval/result* ctx
                            (= tag-false
                               (when-not ~falsy
                                 tag-false)))
       "`when-not` true"
       ($.test.eval/result* ctx
                            (nil? (when-not ~truthy
                                    tag-true)))))))



($.test.prop/deftest rollback--

  (TC.prop/for-all [n        gen-nest
                    sym      $.gen/symbol
                    x-env    $.gen/any
                    x-return $.gen/any
                    x-ploy   $.gen/any]
    (let [ctx ($.test.eval/ctx* (do
                                  (def ~sym
                                       ~x-env)
                                  ~(-nested-fn n
                                               'rollback
                                               x-ploy
                                               x-return)
                                  ~x-ploy))]
      ($.test.prop/mult*

        "Returned value is the rollback value"
        ($.test.util/eq ($.test.eval/result x-return)
                        (-> ctx
                            $.ctx/result
                            $/datafy))

        "State has been rolled back"
        (let [form '(hash (encoding *state*))]
          ($.test.util/eq ($.test.eval/result form)
                          ($.test.eval/result ctx
                                              form)))))))
