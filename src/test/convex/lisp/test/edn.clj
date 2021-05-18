(ns convex.lisp.test.edn

  "Testing core features."

  {:author "Adam Helinski"}

  #_(:require [clojure.test    :as t]
            [convex.lisp     :as $]
            [convex.lisp.edn :as $.edn]))


;; TODO. Test with whole state when this is resolved: https://github.com/Convex-Dev/convex/issues/53


;;;;;;;;;;


#_(t/deftest edn

  ;; TODO. Broken by https://github.com/Convex-Dev/convex/issues/91

  (t/is (= [:a
            (symbol "#51")
            '(blob 255)]
           (-> "[:a
                 #51
                 0xff]"
               $/read
               $.edn/write)
               $.edn/read)))
