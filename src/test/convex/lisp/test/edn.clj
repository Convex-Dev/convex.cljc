(ns convex.lisp.test.edn

  "Testing core features."

  {:author "Adam Helinski"}

  (:require [clojure.test    :as t]
            [convex.lisp     :as $]
            [convex.lisp.edn :as $.edn]))


;; TODO. Test with whole state when this is resolved: https://github.com/Convex-Dev/convex/issues/53


;;;;;;;;;;


(t/deftest edn

  (t/is (= [:a
            (symbol "#51")
            '(blob 255)]
           (-> "[:a
                 #51
                 0xff]"
               $/read
               $.edn/write
               $.edn/read))))
