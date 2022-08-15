(ns hooks.convex

  "Hook for templating Convex Lisp."

  {:author "Adam Helinski"}

  (:require [clj-kondo.hooks-api :as hook]))


;;;;;;;;;;


(defn -traverse

  "Traverses children and returns the `acc` with all unquoted forms and values."


  ([child]

   (-traverse []
              child))


  ([acc child]

   (if (or (hook/list-node? child)
           (hook/vector-node? child))
     (reduce -traverse
             acc
             (:children child))
     (or (when (not (hook/list-node? child))
           (let [sexpr (hook/sexpr child)]
             (when (seq? sexpr)
               (let [sym (first sexpr)]
                 (cond
                   (or (= sym
                          'clojure.core/unquote)
                       (= sym
                          'clojure.core/unquote-splicing))
                   (conj acc
                         (-> child
                             :children
                             first))
                   ;;
                   (= sym
                      'quote)
                   (-traverse acc
                              (-> child
                                  :children
                                  first))
                   ;;
                   :else
                   nil)))))
         acc))))


;;;;;;;;;;


(defn cell*

  "For [[convex.cell/*]]."

  [{:keys [node]}]

  (when-some [form (second (:children node))]
    {:node (hook/vector-node (-traverse form))}))
