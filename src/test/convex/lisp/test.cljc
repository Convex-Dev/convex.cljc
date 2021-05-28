(ns convex.lisp.test

  "Testing core features."

  {:author "Adam Helinski"}

  (:require [clojure.test :as t]
            [convex.lisp  :as $.lisp]))


;;;;;;;;;;


(t/deftest literal

  (t/testing "Address"
    (t/is (= (symbol "#42")
             ($.lisp/literal '(address 42))))
    (let [form '(address "42")]
      (t/is (= form
               ($.lisp/literal form)))))

  (t/testing "Blob"
    (let [blob (symbol "0x42")
          form (list 'blob
                     blob)]
      (t/is (= blob
               ($.lisp/literal '(blob "42"))))
      (t/is (= form
               ($.lisp/literal form))))))
