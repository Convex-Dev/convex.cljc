(ns convex.lisp.test.eval

  "Bridge to [[convex.lisp.eval]] but uses [[ctx]] when no context is provided."

  {:author "Adam Helinski"}

  (:require [convex.lisp.ctx  :as $.ctx]
            [convex.lisp.eval :as $.eval]))


(declare ctx-base)


;;;;;;;;;;


(defn ctx


  ([form]

   (convex.lisp.test.eval/ctx ctx-base
                              form))


  ([ctx form]

   ($.eval/ctx ctx
               form)))



(defn error?


  ([form]

   (error? ctx-base
           form))


  ([ctx form]

   ($.eval/error? ctx
                  form)))



(defn log


  ([form]

   (log ctx-base
        form))


  ([ctx form]

   ($.eval/log ctx
               form)))



(defn result


  ([form]

   (result ctx-base
           form))


  ([ctx form]

   ($.eval/result ctx
                  form)))


;;;;;;;;;;


(def ctx-base

  "Base context to use for testing."

  ;; TODO. Needs 2 transactions because of: https://github.com/Convex-Dev/convex/issues/107

  (-> ($.ctx/create-fake)
      ($.eval/ctx
        '(call *registry*
               (cns-update '$
                           (deploy
                             '(do
                                (defn every?

                                  [f coll]

                                  (boolean (reduce (fn [_acc x]
                                                     (or (f x)
                                                         (reduced false)))
                                                   true
                                                   coll)))

                                (defn every-index?

                                  [f sequential]

                                  (loop [i (dec (count sequential))]
                                    (if (> i
                                           0)
                                      (if (f sequential
                                             i)
                                        (recur (dec i))
                                        false)
                                      true)))

                                (defn some

                                  [f coll]

                                  (boolean (reduce (fn [_acc x]
                                                     (let [x-2 (f x)]
                                                       (if x-2
                                                         (reduced x-2)
                                                         false)))
                                                   true
                                                   coll)))
                                )))))
      ($.eval/ctx
        '(import $ :as $))))
