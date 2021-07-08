(ns convex.break.test.ctrl

  "Testing flow control constructs."

  {:author "Adam Helinski"}

  (:require [clojure.string]
            [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break]
            [convex.cvm                    :as $.cvm]
            [convex.clj.eval               :as $.clj.eval]
            [convex.clj                    :as $.clj]
            [convex.clj.gen                :as $.clj.gen]
            [convex.data                   :as $.data]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Helpers


(defn- -nested-fn

  ;; Used for nesting functions that ultimately should end preemptively.


  ([n form]

   (if (<= n
           0)
     form
     ($.clj/templ* ((fn []
                      ~(-nested-fn (dec n)
                                   form))))))

  ([n form x-ploy]

   (-nested-fn n
               ($.clj/templ* (do
                               ~form
                               ~x-ploy))))


  ([n sym x-ploy x-return]

   (-nested-fn n
               ($.clj/templ* (~sym ~x-return))
               x-ploy)))



(def gen-nest

  "Generating `n` value for [[-nested-fn]]."

  (TC.gen/choose 1
                 16))


;;;;;;;;;; Tests


(mprop/deftest assert--

  {:ratio-num 10}

  (TC.prop/for-all [n        gen-nest
                    x-ploy   $.clj.gen/any
                    x-return $.clj.gen/truthy]
    ($.clj.eval/code? ($.data/code-std* :ASSERT)
                      (-nested-fn n
                                  ($.clj/templ* (assert (not ~x-return)))
                                  x-ploy))))



(mprop/deftest logic

  ;; Various `and` + `or` flavors.

  {:ratio-num 2}

  (TC.prop/for-all [[falsy+
                     mix+
                     truthy+] (TC.gen/bind (TC.gen/tuple (TC.gen/vector $.clj.gen/falsy
                                                                        1
                                                                        16)
                                                         (TC.gen/vector $.clj.gen/truthy
                                                                        1
                                                                        16))
                                           (fn [[falsy+ truthy+]]
                                             (TC.gen/tuple (TC.gen/return falsy+)
                                                           (TC.gen/shuffle (concat falsy+
                                                                                   truthy+))
                                                           (TC.gen/return truthy+))))]
    (mprop/mult

      "`and` on falsy"

      ($.clj.eval/result* (= ~(first falsy+)
                             (and ~@falsy+)))


      "`and` on mixed"

      ($.clj.eval/result* (= ~(first (filter (comp not
                                                   boolean)
                                             mix+))
                             (and ~@mix+)))


      "`and` on truthy"

      ($.clj.eval/result* (= ~(last truthy+)
                             (and ~@truthy+)))


      "`or` on falsy"

      ($.clj.eval/result* (= ~(last falsy+)
                             (or ~@falsy+)))

      
      "`or` on mixed"

      ($.clj.eval/result* (= ~(first (filter boolean
                                             mix+))
                             (or ~@mix+)))


      "`or` on truthy"

      ($.clj.eval/result* (= ~(first truthy+)
                             (or ~@truthy+))))))




(mprop/deftest cond--

  {:ratio-num 5}

  (TC.prop/for-all [else? $.clj.gen/boolean
                    x+    (TC.gen/vector (TC.gen/tuple $.clj.gen/boolean
                                                       (TC.gen/one-of [$.clj.gen/falsy
                                                                       $.clj.gen/truthy]))
                                         1
                                         16)]
    ($.clj.eval/result* (= ~(or (first (into []
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



(mprop/deftest fail--

  {:ratio-num 7}

  (TC.prop/for-all [n       gen-nest
                    code    (TC.gen/such-that some?
                                              $.clj.gen/any)
                    message $.clj.gen/any
                    x-ploy  $.clj.gen/any]
    (let [exec      (fn [form]
                      ($.clj.eval/exception (-nested-fn n
                                                        form
                                                        x-ploy)))
          message-2 ($.clj.eval/result message)]
      (mprop/mult

        "Without code"

        (let [ret (exec ($.clj/templ* (fail ~message)))]
          (mprop/mult

            "No code"

            (= :ASSERT
               (ret :convex.exception/code))


            "Message"

            ($.clj/= message-2
                     (ret :convex.exception/message))))


        "With code"

        (let [ret (exec ($.clj/templ* (fail ~code
                                             ~message)))]
          (mprop/mult

            "Code"

            ($.clj/= ($.clj.eval/result code)
                     (ret :convex.exception/code))


            "Message"

            ($.clj/= message-2
                     (ret :convex.exception/message))))))))



(mprop/deftest halting

  {:ratio-num 7}

  (TC.prop/for-all [n        gen-nest
                    x-ploy   $.clj.gen/any
                    x-return $.clj.gen/any]
    (mprop/mult

      "`halt`"

      ($.clj/= ($.clj.eval/result x-return)
               ($.clj.eval/result (-nested-fn n
                                              'halt
                                              x-ploy
                                              x-return)))


      "`return`"

      ($.clj/= ($.clj.eval/result* [~x-return
                                    ~x-ploy])
               ($.clj.eval/result* [~(-nested-fn n
                                                 'return
                                                 x-ploy
                                                 x-return)
                                    ~x-ploy])))))



(mprop/deftest if-like

  {:ratio-num 7}

  (TC.prop/for-all [sym    $.clj.gen/symbol
                    falsy  $.clj.gen/falsy
                    truthy $.clj.gen/truthy]
    (let [ctx ($.clj.eval/ctx* (do
                                 (def tag-false
                                      [:tag ~falsy])
                                 (def tag-true
                                      [:tag ~truthy])))]
     (mprop/mult

       "`if` false"

       ($.clj.eval/result* ctx
                           (= tag-false
                              (if ~falsy
                                tag-true
                                tag-false)))


       "`if` true"

       ($.clj.eval/result* ctx
                           (= tag-true
                              (if ~truthy
                                tag-true
                                tag-false)))


       "`if-let` false"

       ($.clj.eval/result* ctx
                           (= tag-false
                              (if-let [~sym ~falsy]
                                tag-true
                                tag-false)))


       "`if-let` true"

       ($.clj.eval/result* ctx
                           (= tag-true
                              (if-let [~sym ~truthy]
                                tag-true
                                tag-false)))


       "`when` false"

       ($.clj.eval/result* ctx
                           (nil? (when ~falsy
                                   tag-true)))


       "`when` true"

       ($.clj.eval/result* ctx
                           (= tag-true
                              (when ~truthy
                                tag-true)))


       "`when-let` false"

       ($.clj.eval/result* ctx
                           (nil? (when-let [~sym ~falsy]
                                   tag-true)))


       "`when-let` true"

       ($.clj.eval/result* ctx
                           (= tag-true
                              (when-let [~sym ~truthy]
                                tag-true)))


       "`when-not` false"

       ($.clj.eval/result* ctx
                           (= tag-false
                              (when-not ~falsy
                                tag-false)))

       "`when-not` true"

       ($.clj.eval/result* ctx
                           (nil? (when-not ~truthy
                                   tag-true)))))))



(mprop/deftest rollback--

  {:ratio-num 7}

  (TC.prop/for-all [n        gen-nest
                    sym      $.clj.gen/symbol
                    x-env    $.clj.gen/any
                    x-return $.clj.gen/any
                    x-ploy   $.clj.gen/any]
    (let [ctx ($.clj.eval/ctx* (do
                                 (def ~sym
                                      ~x-env)
                                 ~(-nested-fn n
                                              'rollback
                                              x-ploy
                                              x-return)
                                 ~x-ploy))]
      (mprop/mult

        "Returned value is the rollback value"

        ($.clj/= ($.clj.eval/result x-return)
                 (-> ctx
                     $.cvm/result
                     $.cvm/as-clojure))


        "State has been rolled back"

        (let [form '(hash (encoding *state*))]
          ($.clj/= ($.clj.eval/result form)
                   ($.clj.eval/result ctx
                                        form)))))))


;;;;;;;;;; Negative tests


;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/163
;;
;; (mprop/deftest x-let--error-cast
;; 
;;   ;; Any binding form that is not a vector should be rejected.
;; 
;;   (TC.prop/for-all [bindvec ($.clj.gen/any-but #{$.clj.gen/vector})
;;                     sym     (TC.gen/elements ['if-let
;;                                               'when-let])]
;;     ($.clj.eval/code?* :CAST
;;                        (~sym ~bindvec
;;                              42))))



(mprop/deftest x-let--error-arity

  ;; `if-let` and `when-let` should only accept one binding.

  {:ratio-num 5}

  (TC.prop/for-all [binding+ ($.clj.gen/binding+ 2
	                                             8)
                    sym      (TC.gen/elements ['if-let
                                               'when-let])]
    ($.clj.eval/code?* :ARITY
                       (~sym ~(into []
                                    (mapcat identity)
                                    binding+)
                             42))))
