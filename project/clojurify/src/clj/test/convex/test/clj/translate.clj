(ns convex.test.clj.translate

  {:author "Adam Helinski"}

  (:require [clojure.test         :as t]
            [convex.clj           :as $.clj]
            [convex.clj.translate :as $.clj.translate]
            [convex.read          :as $.read])
  (:import convex.core.data.Syntax
           convex.core.data.prim.CVMByte
           convex.core.lang.impl.ErrorValue))

;;;;;;;;;; Convex -> Clojure


(defn -cvx->clj

  ;; Used by [[cvx->clj]].
  
  [target-clojure target-convex message]

  (t/is (= target-clojure
           ($.clj.translate/cvx->clj (cond->
                                       target-convex
                                       (string? target-convex)
                                       $.read/string)))
        message))



(t/deftest cvx->clj

  (-cvx->clj nil
               "nil"
               "Nil")

  (-cvx->clj (symbol "0xffff")
                       "0xffff"
                       "Blob")

  (-cvx->clj (symbol "#51")
                       "#51"
                       "Address")

  (-cvx->clj (list 1
                    :two
                    'three)
              "(1 :two three)"
              "List")

  (-cvx->clj {:a  42
                "b" 84}
               "{:a    42
                 \"b\" 84}"
               "Map")

  (-cvx->clj #{:a 'b}
               "#{:a b}"
               "Set")

  (-cvx->clj "String"
               "\"String\""
               "String")

  (-cvx->clj '[42.42 ok]
               "[42.42 ok]"
               "Vector")

  (-cvx->clj :ok
               ":ok"
               "Keyword")

  (-cvx->clj 'ok
               "ok"
               "Symbol")

  (-cvx->clj "ok"
               "\"ok\""
               "String")

  (-cvx->clj '(syntax [:a 42]
                        {:foo :bar})
               (Syntax/create ($.read/string "[:a 42]")
                              ($.read/string "{:foo :bar}"))
               "Syntax")
  
  (-cvx->clj true
               "true"
               "Boolean")

  (-cvx->clj 42
               (CVMByte/create 42)
               "Byte")

  (-cvx->clj \a
               "\\a"
               "Char")

  (-cvx->clj 42.42
               "42.42"
               "Double")

  (-cvx->clj 42
               "42"
               "Long")

  (-cvx->clj {:convex.exception/code    {:a 42}
                :convex.exception/message [:foo]
                :convex.exception/trace   '("test-1"
                                            "test-2")}
               (doto (ErrorValue/createRaw ($.read/string "{:a 42}")
                                           ($.read/string "[:foo]"))
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
                 $.read/string
                 $.clj.translate/cvx->clj))
          "Stress test")))
