(ns convex.lisp.test

  "Testing core namespace."

  {:author "Adam Helinski"}

  (:require [clojure.test     :as t]
            [convex.lisp      :as $]
            [convex.lisp.ctx  :as $.ctx]
            [convex.lisp.form :as $.form])
  (:import convex.core.data.Syntax
           convex.core.data.prim.CVMByte))


;;;;;;;;;;


(defn -datafy

  "Used by [[datafy]]."

  [target-clojure target-convex message]

  (t/is (= target-clojure
           ($/datafy (cond->
                       target-convex
                       (string? target-convex)
                       $/read)))
        message))



(t/deftest datafy

  (-datafy nil
           "nil"
           "Nil")

  (-datafy (symbol "0xffff")
                   "0xffff"
                   "Blob")

  (-datafy (symbol "#51")
                   "#51"
                   "Address")

  (-datafy (list 1
                 :two
                 'three)
           "(1 :two three)"
           "List")

  (-datafy {:a  42
            "b" 84}
           "{:a    42
             \"b\" 84}"
           "Map")

  (-datafy #{:a 'b}
           "#{:a b}"
           "Set")

  (-datafy "String"
           "\"String\""
           "String")

  (-datafy '[42.42 ok]
           "[42.42 ok]"
           "Vector")

  (-datafy :ok
           ":ok"
           "Keyword")

  (-datafy :ok
           ":ignored/ok"
           "Namespaced keyword (unsupported in CLisp)")

  (-datafy 'ok
           "ok"
           "Symbol")

  (-datafy 'ok/yes
           "ok/yes"
           "Namespaced symbol")

  (-datafy "ok"
           "\"ok\""
           "String")

  (-datafy '(syntax [:a 42]
                    {:foo :bar})
           (Syntax/create ($/read "[:a 42]")
                          ($/read "{:foo :bar}"))
           "Syntax")
  
  (-datafy true
           "true"
           "Boolean")

  (-datafy 42
           (CVMByte/create 42)
           "Byte")

  (-datafy \a
           "\\a"
           "Char")

  (-datafy 42.42
           "42.42"
           "Double")

  (-datafy 42
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
                 $.form/source
                 $/read
                 $/datafy))
          "Stress test")))



(t/deftest execution

  (let [form ($/read "(if true 42 0)")]
    (t/is (= ($/read "42")
             (-> form
                 $/eval
                 $.ctx/result)
             (-> form
                 $/expand
                 $/compile
                 $/run
                 $.ctx/result)
             (-> form
                 $/expand-compile
                 $/run
                 $.ctx/result)
             (-> form
                 $/expand-compile
                 $/query
                 $.ctx/result)))))
