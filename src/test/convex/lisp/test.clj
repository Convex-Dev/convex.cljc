(ns convex.lisp.test

  "Testing core features."

  {:author "Adam Helinski"}

  (:require [clojure.test :as t]
            [convex.lisp  :as $])
  (:import convex.core.Init
           convex.core.data.Syntax
           (convex.core.data.prim CVMByte
                                  CVMChar)))


;;;;;;;;;;


(defn -to-clojure

  "Used by [[to-clojure]]."

  [target-clojure target-convex message]

  (t/is (= target-clojure
           ($/to-clojure (cond->
                           target-convex
                           (string? target-convex)
                           $/read)))
        message))



(t/deftest to-clojure

  (-to-clojure nil
               "nil"
               "Nil")

  (-to-clojure '(blob "ffff")
               "0xffff"
               "Blob")

  (-to-clojure (symbol "#51")
               "#51"
               "Address")

  (-to-clojure (list 1
                     :two
                     'three)
               "(1 :two three)"
               "List")

  (-to-clojure {:a  42
                "b" 84}
               "{:a    42
                 \"b\" 84}"
               "Map")

  (-to-clojure #{:a 'b}
               "#{:a b}"
               "Set")

  (-to-clojure "String"
               "\"String\""
               "String")

  (-to-clojure '[42.42 ok]
               "[42.42 ok]"
               "Vector")

  (-to-clojure :ok
               ":ok"
               "Keyword")

  (-to-clojure :ok
               ":ignored/ok"
               "Namespaced keyword (unsupported in CLisp)")

  (-to-clojure 'ok
               "ok"
               "Symbol")

  (-to-clojure 'ok/yes
               "ok/yes"
               "Namespaced symbol")

  (-to-clojure "ok"
               "\"ok\""
               "String")

  (-to-clojure '(syntax [:a 42]
                        {:foo :bar})
               (Syntax/create ($/read "[:a 42]")
                              ($/read "{:foo :bar}"))
               "Syntax")
  
  (-to-clojure true
               "true"
               "Boolean")

  (-to-clojure 42
               (CVMByte/create 42)
               "Byte")

  (-to-clojure \a
               "\\a"
               "Char")

  (-to-clojure 42.42
               "42.42"
               "Double")

  (-to-clojure 42
               "42"
               "Long")


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
              '(blob "11223344ff")
              (:a :b)
              [:a :b]
              #{:a :b}
              {:a 42
               :b 84}
              '(fn [x]
                 (inc x))]]
    (t/is (= code
             (-> code
                 str
                 $/read
                 $/to-clojure))
          "Stress test")))



(t/deftest edn

  (t/is (= [:a
            (symbol "#51")
            '(blob 255)]
           (-> "[:a
                 #51
                 0xff]"
               $/read
               $/to-edn
               $/read-edn))))



(t/deftest execution

  (let [form ($/read "(if true 42 0)")]
    (t/is (= ($/read "42")
             (-> form
                 $/eval
                 $/result)
             (-> form
                 $/expand
                 $/compile
                 $/run
                 $/result)
             (-> form
                 $/expand-compile
                 $/run
                 $/result)
             (-> form
                 $/expand-compile
                 $/query
                 $/result)))))



(t/deftest prepare-clojure

  (t/testing "Address"
    (t/is (= (symbol "#42")
             ($/prepare-clojure '(address 42))))
    (let [form '(address "42")]
      (t/is (= form
               ($/prepare-clojure form)))))

  (t/testing "NaN"
    (t/is (= '(unquote NaN)
             ($/prepare-clojure ##NaN)))
    (t/is (= '[(unquote NaN)]
             ($/prepare-clojure [##NaN])))))
