(ns convex.test.break.ctrl

  "Testing flow control constructs."

  {:author "Adam Helinski"}

  (:require [clojure.test.check.generators :as TC.gen]
            [clojure.test.check.properties :as TC.prop]
            [convex.break                  :as $.break]
            [convex.break.gen              :as $.break.gen]
            [convex.cell                   :as $.cell]
            [convex.cvm                    :as $.cvm]
            [convex.eval                   :as $.eval]
            [convex.gen                    :as $.gen]
            [helins.mprop                  :as mprop]))


;;;;;;;;;; Helpers


(defn- -nested-fn

  ;; Used for nesting functions that ultimately should end preemptively.


  ([n form]

   (if (<= n
           0)
     form
     ($.cell/* ((fn []
                  ~(-nested-fn (dec n)
                               form))))))

  ([n form x-ploy]

   (-nested-fn n
               ($.cell/* (do
                           ~form
                           ~x-ploy))))


  ([n sym x-ploy x-return]

   (-nested-fn n
               ($.cell/list [sym
                             x-return])
               x-ploy)))



(def gen-nest

  "Generating `n` value for [[-nested-fn]]."

  (TC.gen/choose 1
                 16))


;;;;;;;;;; Tests


(mprop/deftest assert--

  {:ratio-num 10}

  (TC.prop/for-all [n        gen-nest
                    x-ploy   $.gen/any
                    x-return ($.gen/quoted $.gen/truthy)]
    (= ($.cell/code-std* :ASSERT)
       ($.eval/exception-code $.break/ctx
                              (-nested-fn n
                                          ($.cell/* (assert (not ~x-return)))
                                          x-ploy)))))



(mprop/deftest logic

  ;; Various `and` + `or` flavors.

  {:ratio-num 2}

  (TC.prop/for-all [[falsy+
                     mix+
                     truthy+] (TC.gen/fmap (fn [[falsy+ truthy+]]
                                             [falsy+
                                              ($.cell/vector (shuffle (concat falsy+
                                                                              truthy+)))
                                              truthy+])
                                           (TC.gen/tuple ($.gen/vector $.gen/falsy
                                                                       1
                                                                       16)
                                                         ($.gen/vector ($.gen/quoted $.gen/truthy)
                                                                       1
                                                                       16)))]
    (mprop/mult

      "`and` on falsy"

      ($.eval/true? $.break/ctx
                    ($.cell/* (= ~(first falsy+)
                                (and ~@falsy+))))


      "`and` on mixed"

      ($.eval/true? $.break/ctx
                    ($.cell/* (= (first (filter (comp not
                                                      boolean)
                                                ~mix+))
                                 (and ~@mix+))))


      "`and` on truthy"

      ($.eval/true? $.break/ctx
                    ($.cell/* (= ~(last truthy+)
                                 (and ~@truthy+))))


      "`or` on falsy"

      ($.eval/true? $.break/ctx
                    ($.cell/* (= ~(last falsy+)
                                 (or ~@falsy+))))

      
      "`or` on mixed"

      ($.eval/true? $.break/ctx
                    ($.cell/* (= (first (filter boolean
                                                ~mix+))
                                 (or ~@mix+))))


      "`or` on truthy"

      ($.eval/true? $.break/ctx
                    ($.cell/* (= ~(first truthy+)
                                 (or ~@truthy+)))))))



(mprop/deftest cond--

  {:ratio-num 5}

  (TC.prop/for-all [else? TC.gen/boolean
                    x+    (TC.gen/vector (TC.gen/tuple TC.gen/boolean
                                                       (TC.gen/one-of [$.gen/falsy
                                                                       ($.gen/quoted $.gen/truthy)]))
                                          1
                                          16)]
    ($.eval/true? $.break/ctx
                  ($.cell/* (= (or (let [truthy-out+ (filter boolean
                                                             ~($.cell/vector (map second
                                                                                  x+)))]
                                     (when (not (empty? truthy-out+))
                                       (first truthy-out+)))
                                   ~(when else?
                                      (second (peek x+))))
                               (cond
                                 ~@(cond->
                                     (mapcat (fn [[identity? x]]
                                                 [(if identity?
                                                    ($.cell/* (identity ~x))
                                                    x)
                                                  x])
                                             x+)
                                     else?
                                     butlast)))))))



(mprop/deftest fail--

  {:ratio-num 7}

  (TC.prop/for-all [n       gen-nest
                    code    (TC.gen/such-that some?
                                              $.gen/any
                                              100)
                    message $.gen/any
                    x-ploy  $.gen/any]
    (let [exec (fn [form]
                 ($.eval/exception $.break/ctx
                                   (-nested-fn n
                                               form
                                               x-ploy)))]
      (mprop/mult

        "Without code"

        (let [ex (exec ($.cell/* (fail (quote ~message))))]
          (mprop/mult

            "Default code"

            (= ($.cell/code-std* :ASSERT)
               ($.cvm/exception-code ex))


            "Message"

            (= message
               ($.cvm/exception-message ex))))


        "With code"

        (let [ex (exec ($.cell/* (fail (quote ~code)
                                       (quote ~message))))]
          (mprop/mult

            "Code"

            (= code
               ($.cvm/exception-code ex))


            "Message"

            (= message
               ($.cvm/exception-message ex))))))))



(mprop/deftest halting

  {:ratio-num 7}

  (TC.prop/for-all [n        gen-nest
                    x-ploy   $.gen/any
                    x-return $.gen/any]
    (mprop/mult

      "`halt`"

      (= x-return
         ($.eval/result $.break/ctx
                        (-nested-fn n
                                    ($.cell/* halt)
                                    x-ploy
                                    ($.cell/quoted x-return))))


      "`return`"

      (= ($.cell/* [~x-return
                    ~x-ploy])
         ($.eval/result $.break/ctx
                        ($.cell/vector [(-nested-fn n
                                                    ($.cell/* return)
                                                    ($.cell/quoted x-ploy)
                                                    ($.cell/quoted x-return))
                                        ($.cell/quoted x-ploy)]))))))



(mprop/deftest if-like

  {:ratio-num 7}

  (TC.prop/for-all [sym    $.gen/symbol
                    falsy  $.gen/falsy
                    truthy ($.gen/quoted $.gen/truthy)]
    (let [ctx ($.eval/ctx $.break/ctx
                          ($.cell/* (do
                                      (def tag-false
                                           [:tag ~falsy])
                                      (def tag-true
                                           [:tag ~truthy]))))]
     (mprop/mult

       "`if` false"

       ($.eval/true? ctx
                     ($.cell/* (= tag-false
                                  (if ~falsy
                                    tag-true
                                    tag-false))))


       "`if` true"

       ($.eval/result ctx
                      ($.cell/* (= tag-true
                                   (if ~truthy
                                     tag-true
                                     tag-false))))


       "`if-let` false"

       ($.eval/result ctx
                      ($.cell/* (= tag-false
                                   (if-let [~sym ~falsy]
                                     tag-true
                                     tag-false))))


       "`if-let` true"

       ($.eval/result ctx
                      ($.cell/* (= tag-true
                                   (if-let [~sym ~truthy]
                                     tag-true
                                     tag-false))))


       "`when` false"

       ($.eval/result ctx
                      ($.cell/* (nil? (when ~falsy
                                        tag-true))))


       "`when` true"

       ($.eval/result ctx
                      ($.cell/* (= tag-true
                                   (when ~truthy
                                     tag-true))))


       "`when-let` false"

       ($.eval/result ctx
                      ($.cell/* (nil? (when-let [~sym ~falsy]
                                        tag-true))))


       "`when-let` true"

       ($.eval/result ctx
                      ($.cell/* (= tag-true
                                   (when-let [~sym ~truthy]
                                     tag-true))))


       "`when-not` false"

       ($.eval/result ctx
                      ($.cell/* (= tag-false
                                   (when-not ~falsy
                                     tag-false))))

       "`when-not` true"

       ($.eval/result ctx
                      ($.cell/* (nil? (when-not ~truthy
                                        tag-true))))))))



(mprop/deftest rollback--

  {:ratio-num 7}

  (TC.prop/for-all [n        gen-nest
                    sym      $.gen/symbol
                    x-env    $.gen/any
                    x-return $.gen/any
                    x-ploy   $.gen/any]
    (let [ctx ($.eval/ctx $.break/ctx
                          ($.cell/* (do
                                      (def ~sym
                                           (quote ~x-env))
                                      ~(-nested-fn n
                                                   ($.cell/* rollback)
                                                   x-ploy
                                                   ($.cell/quoted x-return))
                                      (quote ~x-ploy))))]
      (mprop/mult

        "Returned value is the rollback value"

        (= x-return
           ($.cvm/result ctx))


        "State has been rolled back"

        (let [form ($.cell/* (hash (encoding *state*)))]
          (= ($.eval/result $.break/ctx
                            form)
             ($.eval/result ctx
                            form)))))))


;;;;;;;;;; Negative tests


;; TODO. Fails because of: https://github.com/Convex-Dev/convex/issues/163
;; 
;; (mprop/deftest x-let--error-cast
;; 
;;   ;; Any binding form that is not a vector should be rejected.
;; 
;;   (TC.prop/for-all [bindvec (TC.gen/such-that #(not ($.std/vector? %))
;;                                               $.gen/any
;;                                               100)
;;                     sym     (TC.gen/elements [($.cell/* if-let)
;;                                               ($.cell/* when-let)])]
;;                    (println :got ($.eval/exception-code $.break/ctx
;;                               ($.cell/* (~sym ~bindvec
;;                                               42))))
;;     (= ($.cell/code-std* :CAST)
;;        ($.eval/exception-code $.break/ctx
;;                               ($.cell/* (~sym ~bindvec
;;                                               42))))))



(mprop/deftest x-let--error-arity

  ;; `if-let` and `when-let` should only accept one binding.

  ;; TODO. Test non-symbolic bindings.

  {:ratio-num 5}

  (TC.prop/for-all [binding+ ($.break.gen/binding+ 2
	                                               8)
                    sym      (TC.gen/elements [($.cell/* if-let)
                                               ($.cell/* when-let)])]
    (= ($.cell/code-std* :ARITY)
       ($.eval/exception-code $.break/ctx
                              ($.cell/* (~sym ~binding+
                                              42))))))
