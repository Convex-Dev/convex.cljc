(ns convex.lisp.dev

  "CLJC playground during dev."

  {:author "Adam Helinski"}

  (:require [ajax.core   :as http]
            [convex.lisp :as $])
  (:import (convex.core.data Symbol)
           (convex.core.lang Reader)))


#?(:clj (set! *warn-on-reflection*
              true))

;;;;;;;;;;


(comment


  (http/ajax-request {:error-handler (fn [err]
                                       (println :err
                                                err))
                      :handler       (fn [resp]
                                       (println :resp
                                                resp))
                      :method        :get
                      :uri           "https://convex.world/api/v1/accounts/51"})




  (defn read-lisp

    ""

    [source]

    (let [parsed (Reader/readAll source)]
      (if (second parsed)
        (.cons parsed
               (Symbol/create "do"))
        (first parsed))))



  )
