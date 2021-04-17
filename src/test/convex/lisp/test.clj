(ns convex.lisp.test

  "Testing core features."

  {:author "Adam Helinski"}

  (:require [clojure.test :as t]
            [convex.lisp  :as $])
  (:import (convex.core.data.prim CVMByte
                                  CVMChar)))


;;;;;;;;;;


(defn -convex->clojure

  "Used by [[convex->clojure]]."

  [target-clojure target-convex message]

  (t/is (= target-clojure
           ($/convex->clojure (cond->
                                target-convex
                                (string? target-convex)
                                $/read-string)))
        message))



(t/deftest convex->clojure

  (-convex->clojure nil
                    "nil"
                    "Nil")

  (-convex->clojure '(blob "ffff")
                    "0xffff"
                    "Blob")

  (-convex->clojure '(address 51)
                    "#51"
                    "Address")

  (-convex->clojure (list 1
                          :two
                          'three)
                    "(1 :two three)"
                    "List")

  (-convex->clojure {:a  42
                     "b" 84}
                    "{:a    42
                      \"b\" 84}"
                    "Map")

  (-convex->clojure #{:a 'b}
                    "#{:a b}"
                    "Set")

  (-convex->clojure "String"
                    "\"String\""
                    "String")

  (-convex->clojure '[42.42 ok]
                    "[42.42 ok]"
                    "Vector")

  (-convex->clojure :ok
                    ":ok"
                    "Keyword")

  (-convex->clojure :ok
                    ":ignored/ok"
                    "Namespaced keyword (unsupported in CLisp)")

  (-convex->clojure 'ok
                    "ok"
                    "Symbol")

  (-convex->clojure 'ok/yes
                    "ok/yes"
                    "Namespaced symbol")

  (-convex->clojure "ok"
                    "\"ok\""
                    "String")

  (-convex->clojure true
                    "true"
                    "Boolean")

  (-convex->clojure 42
                    (CVMByte/create 42)
                    "Byte")

  (-convex->clojure 42
                    (CVMChar/create 42)
                    "Char")

  (-convex->clojure 42.42
                    "42.42"
                    "Double")

  (-convex->clojure 42
                    "42"
                    "Long")

  (let [code '(do
                nil
                42
                42.42
                true
                false
                "ok"
                :ok
                ok
                ok/super
                (:a :b)
                [:a :b]
                #{:a :b}
                {:a 42
                 :b 84}
                (fn [x]
                  (inc x)))]
    (t/is (= code
             (-> code
                 str
                 $/read-string
                 $/convex->clojure))
          "Stress test"))

  )
