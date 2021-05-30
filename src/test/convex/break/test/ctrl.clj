(ns convex.break.test.ctrl

  "Testing flow control constructs."

  {:author "Adam Helinski"}

  (:require [clojure.string]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break.eval             :as $.break.eval]
            [convex.break.prop             :as $.break.prop]
            [convex.break.util             :as $.break.util]
            [convex.cvm                    :as $.cvm]
            [convex.lisp                   :as $.lisp]
            [convex.lisp.gen               :as $.lisp.gen]))


;;;;;;;;;; Helpers


(defn- -nested-fn

  ;; Used for nesting functions that ultimately should end preemptively.


  ([n form]

   (if (<= n
           0)
     form
     ($.lisp/templ* ((fn []
                       ~(-nested-fn (dec n)
                                    form))))))

  ([n form x-ploy]

   (-nested-fn n
               ($.lisp/templ* (do
                                ~form
                                ~x-ploy))))


  ([n sym x-ploy x-return]

   (-nested-fn n
               ($.lisp/templ* (~sym ~x-return))
               x-ploy)))



(def gen-nest

  "Generating `n` value for [[-nested-fn]]."

  (TC.gen/choose 1
                 16))


;;;;;;;;;; Tests


($.break.prop/deftest assert--

  (TC.prop/for-all [n        gen-nest
                    x-ploy   $.lisp.gen/any
                    x-return $.lisp.gen/truthy]
    (identical? :ASSERT
                (-> ($.break.eval/exception (-nested-fn n
                                                        ($.lisp/templ* (assert (not ~x-return)))
                                                        x-ploy))
                    :convex.error/code))))



($.break.prop/deftest logic

  (TC.prop/for-all [[falsy+
                     mix+
                     truthy+] (TC.gen/bind (TC.gen/tuple (TC.gen/vector $.lisp.gen/falsy
                                                                        1
                                                                        16)
                                                         (TC.gen/vector $.lisp.gen/truthy
                                                                        1
                                                                        16))
                                           (fn [[falsy+ truthy+]]
                                             (TC.gen/tuple (TC.gen/return falsy+)
                                                           (TC.gen/shuffle (concat falsy+
                                                                                   truthy+))
                                                           (TC.gen/return truthy+))))]
    ($.break.prop/mult*

      "`and` on falsy"
      ($.break.eval/result* (= ~(first falsy+)
                               (and ~@falsy+)))

      "`and` on mixed"
      ($.break.eval/result* (= ~(first (filter (comp not
                                                     boolean)
                                               mix+))
                               (and ~@mix+)))

      "`and` on truthy"
      ($.break.eval/result* (= ~(last truthy+)
                               (and ~@truthy+)))


      "`or` on falsy"
      ($.break.eval/result* (= ~(last falsy+)
                               (or ~@falsy+)))
      
      "`or` on mixed"
      ($.break.eval/result* (= ~(first (filter boolean
                                               mix+))
                               (or ~@mix+)))

      "`or` on truthy"
      ($.break.eval/result* (= ~(first truthy+)
                               (or ~@truthy+))))))




($.break.prop/deftest cond--

  (TC.prop/for-all [else? $.lisp.gen/boolean
                    x+    (TC.gen/vector (TC.gen/tuple $.lisp.gen/boolean
                                                       (TC.gen/one-of [$.lisp.gen/falsy
                                                                       $.lisp.gen/truthy]))
                                         1
                                         16)]
    ($.break.eval/result* (= ~(or (first (into []
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



($.break.prop/deftest fail--

  (TC.prop/for-all [n       gen-nest
                    code    (TC.gen/such-that some?
                                              $.lisp.gen/any)
                    message $.lisp.gen/any
                    x-ploy  $.lisp.gen/any]
    (let [exec      (fn [form]
                      ($.break.eval/exception (-nested-fn n
                                                          form
                                                          x-ploy)))
          message-2 ($.break.eval/result message)]
      ($.break.prop/and* ($.break.prop/checkpoint*
   
                           "Without code"

                           (let [ret (exec ($.lisp/templ* (fail ~message)))]
                             ($.break.prop/mult*

                               "No code"
                               (= :ASSERT
                                  (ret :convex.error/code))

                               "Message"
                               ($.break.util/eq message-2
                                                (ret :convex.error/message)))))

                         ($.break.prop/checkpoint*

                           "With code"

                           (let [ret (exec ($.lisp/templ* (fail ~code
                                                                ~message)))]
                             ($.break.prop/mult*

                               "Code"
                               ($.break.util/eq ($.break.eval/result code)
                                                (ret :convex.error/code))

                               "Message"
                               ($.break.util/eq message-2
                                                (ret :convex.error/message)))))))))



($.break.prop/deftest halting

  (TC.prop/for-all [n        gen-nest
                    x-ploy   $.lisp.gen/any
                    x-return $.lisp.gen/any]
    ($.break.prop/mult*

      "`halt`"
      ($.break.util/eq ($.break.eval/result x-return)
                       ($.break.eval/result (-nested-fn n
                                                        'halt
                                                        x-ploy
                                                        x-return)))

      "`return`"
      ($.break.util/eq ($.break.eval/result* [~x-return
                                              ~x-ploy])
                       ($.break.eval/result* [~(-nested-fn n
                                                           'return
                                                           x-ploy
                                                           x-return)
                                              ~x-ploy])))))



($.break.prop/deftest if-like

  (TC.prop/for-all [sym    $.lisp.gen/symbol
                    falsy  $.lisp.gen/falsy
                    truthy $.lisp.gen/truthy]
    (let [ctx ($.break.eval/ctx* (do
                                   (def tag-false
                                        [:tag ~falsy])
                                   (def tag-true
                                        [:tag ~truthy])))]
     ($.break.prop/mult*

       "`if` false"
       ($.break.eval/result* ctx
                             (= tag-false
                                (if ~falsy
                                  tag-true
                                  tag-false)))

       "`if` true"
       ($.break.eval/result* ctx
                             (= tag-true
                                (if ~truthy
                                  tag-true
                                  tag-false)))

       "`if-let` false"
       ($.break.eval/result* ctx
                             (= tag-false
                                (if-let [~sym ~falsy]
                                  tag-true
                                  tag-false)))

       "`if-let` true"
       ($.break.eval/result* ctx
                             (= tag-true
                                (if-let [~sym ~truthy]
                                  tag-true
                                  tag-false)))

       "`when` false"
       ($.break.eval/result* ctx
                             (nil? (when ~falsy
                                     tag-true)))

       "`when` true"
       ($.break.eval/result* ctx
                             (= tag-true
                                (when ~truthy
                                  tag-true)))

       "`when-let` false"
       ($.break.eval/result* ctx
                             (nil? (when-let [~sym ~falsy]
                                     tag-true)))

       "`when-let` true"
       ($.break.eval/result* ctx
                             (= tag-true
                                (when-let [~sym ~truthy]
                                  tag-true)))

       "`when-not` false"
       ($.break.eval/result* ctx
                             (= tag-false
                                (when-not ~falsy
                                  tag-false)))
       "`when-not` true"
       ($.break.eval/result* ctx
                             (nil? (when-not ~truthy
                                     tag-true)))))))



($.break.prop/deftest rollback--

  (TC.prop/for-all [n        gen-nest
                    sym      $.lisp.gen/symbol
                    x-env    $.lisp.gen/any
                    x-return $.lisp.gen/any
                    x-ploy   $.lisp.gen/any]
    (let [ctx ($.break.eval/ctx* (do
                                   (def ~sym
                                        ~x-env)
                                   ~(-nested-fn n
                                                'rollback
                                                x-ploy
                                                x-return)
                                   ~x-ploy))]
      ($.break.prop/mult*

        "Returned value is the rollback value"
        ($.break.util/eq ($.break.eval/result x-return)
                         (-> ctx
                             $.cvm/result
                             $.cvm/as-clojure))

        "State has been rolled back"
        (let [form '(hash (encoding *state*))]
          ($.break.util/eq ($.break.eval/result form)
                           ($.break.eval/result ctx
                                                form)))))))


;;;;;;;;;; Negative tests


;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/163
;;
;; ($.break.prop/deftest x-let--error-cast
;; 
;;   ;; Any binding form that is not a vector should be rejected.
;; 
;;   (TC.prop/for-all [bindvec ($.lisp.gen/any-but #{$.lisp.gen/vector})
;;                     sym     (TC.gen/elements ['if-let
;;                                               'when-let])]
;;     ($.break.eval/error-cast?* (~sym ~bindvec
;;                                     42))))



($.break.prop/deftest x-let--error-arity

  ;; `if-let` and `when-let` should only accept one binding.

  (TC.prop/for-all [binding+ ($.lisp.gen/binding+ 2
	                                              8)
                    sym      (TC.gen/elements ['if-let
                                               'when-let])]
    ($.break.eval/error-arity?* (~sym ~(into []
                                             (mapcat identity)
                                             binding+)
                                      42))))
