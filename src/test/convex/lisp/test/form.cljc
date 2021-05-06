(ns convex.lisp.test

  "Testing core features."

  {:author "Adam Helinski"}

  (:require [clojure.test     :as t]
            [convex.lisp.form :as $.form]))


;;;;;;;;;;


(t/deftest literal

  (t/testing "Address"
    (t/is (= (symbol "#42")
             ($.form/literal '(address 42))))
    (let [form '(address "42")]
      (t/is (= form
               ($.form/literal form)))))

  (t/testing "Blob"
    (let [blob (symbol "0x42")
          form (list 'blob
                     blob)]
      (t/is (= blob
               ($.form/literal '(blob "42"))))
      (t/is (= form
               ($.form/literal form))))))
