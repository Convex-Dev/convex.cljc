(ns convex.cvm.test

  {:author "Adam Helinski"}

  (:require [clojure.test     :as t]
            [convex.cvm       :as $.cvm]
            [convex.lisp      :as $.lisp]
            [convex.lisp.form :as $.form])
  (:import convex.core.data.Syntax
           convex.core.data.prim.CVMByte
           convex.core.lang.impl.ErrorValue))


;;;;;;;;;;


(defn -as-clojure

  "Used by [[-as-clojure]]."

  [target-clojure target-convex message]

  (t/is (= target-clojure
           ($.cvm/as-clojure (cond->
                               target-convex
                               (string? target-convex)
                               $.lisp/read)))
        message))



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
               (Syntax/create ($.lisp/read "[:a 42]")
                              ($.lisp/read "{:foo :bar}"))
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

  (-as-clojure {:convex.error/code    {:a 42}
                :convex.error/message [:foo]
                :convex.error/trace   '("test-1"
                                        "test-2")}
               (doto (ErrorValue/createRaw ($.lisp/read "{:a 42}")
                                           ($.lisp/read "[:foo]"))
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
                 $.form/src
                 $.lisp/read
                 $.cvm/as-clojure))
          "Stress test")))
