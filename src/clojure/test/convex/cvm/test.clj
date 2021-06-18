(ns convex.cvm.test

  {:author "Adam Helinski"}

  (:require [clojure.test :as t]
            [convex.cvm   :as $.cvm]
            [convex.clj   :as $.clj])
  (:import convex.core.data.Syntax
           convex.core.data.prim.CVMByte
           convex.core.lang.impl.ErrorValue))


;;;;;;;;;; From expansion to execution


(t/deftest execution

  (let [form (first ($.cvm/read "(if true 42 0)"))]
    (t/is (= (first ($.cvm/read "42"))
             (->> form
                  ($.cvm/eval ($.cvm/ctx))
                  $.cvm/result)
             (->> form
                  ($.cvm/eval ($.cvm/ctx))
                  $.cvm/result)
             (->> form
                  ($.cvm/expand ($.cvm/ctx))
                  $.cvm/compile
                  $.cvm/run
                  $.cvm/result)
             (->> form
                  ($.cvm/expand-compile ($.cvm/ctx))
                  $.cvm/run
                  $.cvm/result)
             (->> form
                  ($.cvm/expand-compile ($.cvm/ctx))
                  $.cvm/query
                  $.cvm/result)))))


;;;;;;;;;; Convex -> Clojure


(let [-as-clojure (fn [target-clojure target-convex message]
                    (t/is (= target-clojure
                             ($.cvm/as-clojure (cond->
                                                 target-convex
                                                 (string? target-convex)
                                                 (-> $.cvm/read
                                                     first))))
                          message))]
  (t/deftest as-clojure

    (-as-clojure nil
                 "nil"
                 "Nil")

    (-as-clojure (symbol "0xffff")
                         "0xffff"
                         "Blob")

    (-as-clojure (symbol "#51")
                         "#51"
                         "Address")

    (-as-clojure (list 1
                      :two
                      'three)
                "(1 :two three)"
                "List")

    (-as-clojure {:a  42
                  "b" 84}
                 "{:a    42
                   \"b\" 84}"
                 "Map")

    (-as-clojure #{:a 'b}
                 "#{:a b}"
                 "Set")

    (-as-clojure "String"
                 "\"String\""
                 "String")

    (-as-clojure '[42.42 ok]
                 "[42.42 ok]"
                 "Vector")

    (-as-clojure :ok
                 ":ok"
                 "Keyword")

    (-as-clojure :ok
                 ":ignored/ok"
                 "Namespaced keyword (unsupported in CLisp)")

    (-as-clojure 'ok
                 "ok"
                 "Symbol")
      
    (-as-clojure 'ok/yes
                 "ok/yes"
                 "Namespaced symbol")

    (-as-clojure "ok"
                 "\"ok\""
                 "String")

    (-as-clojure '(syntax [:a 42]
                          {:foo :bar})
                 (Syntax/create (first ($.cvm/read "[:a 42]"))
                                (first ($.cvm/read "{:foo :bar}")))
                 "Syntax")
    
    (-as-clojure true
                 "true"
                 "Boolean")

    (-as-clojure 42
                 (CVMByte/create 42)
                 "Byte")

    (-as-clojure \a
                 "\\a"
                 "Char")

    (-as-clojure 42.42
                 "42.42"
                 "Double")

    (-as-clojure 42
                 "42"
                 "Long")

    (-as-clojure {:convex.exception/code    {:a 42}
                  :convex.exception/message [:foo]
                  :convex.exception/trace   '("test-1"
                                              "test-2")}
                 (doto (ErrorValue/createRaw (first ($.cvm/read "{:a 42}"))
                                             (first ($.cvm/read "[:foo]")))
                   (.addTrace "test-1")
                   (.addTrace "test-2"))
                 "Error")


    (let [code [nil
                42
                42.42
                true
                false
                "ok"
                :ok
                'ok
                'ok/super
                (symbol "#42")
                (symbol "0x11223344ff")
                (:a :b)
                [:a :b]
                #{:a :b}
                {:a 42
                 :b 84}
                '(fn [x]
                   (inc x))]]
      (t/is (= code
               (-> code
                   $.clj/src
                   $.cvm/read
                   first
                   $.cvm/as-clojure))
            "Stress test"))))
