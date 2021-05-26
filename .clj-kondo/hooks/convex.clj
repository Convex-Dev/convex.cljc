(ns hooks.convex

  "Hook for templating Convex Lisp."

  {:author "Adam Helinski"}

  (:require [clj-kondo.hooks-api :as hook]))


;;;;;;;;;;


(defn -traverse

  "Traverses children and returns the `acc` with all unquoted forms and values."


  ([child]

   (when-some [vect (not-empty (-traverse []
                                          child))]
     (hook/vector-node vect)))


  ([acc child]

   (if (or (hook/list-node? child)
           (hook/vector-node? child))
     (reduce (fn [acc-2 child]
               (-traverse acc-2
                          child))
             acc
             (:children child))
     (or (when (not (hook/list-node? child))
           (let [sexpr (hook/sexpr child)]
             (when (seq? sexpr)
               (let [sym (first sexpr)]
                 (cond
                   (or (= sym
                          'unquote)
                       (= sym
                          'unquote-splicing)) (conj acc
                                                    (-> child
                                                        :children
                                                        first))
                   (= sym
                      'quote)                 (-traverse acc
                                                         (-> child
                                                             :children
                                                             first))
                   :else                      nil)))))
       acc))))


;;;;;;;;;;


(defn templ*

  "For [[convex.lisp.form/templ*]].

   Suitable for other macros. Among arguments, considers that the template is either:

   - First argument if argument count is odd
   - Second argument if argument count is even"

  [{:keys [node]}]

  (let [child+ (rest (:children node))
        n      (count child+)]
    {:node (hook/vector-node (cond
                               (zero? n) nil
                               (even? n) (concat [(first child+)
                                                  (-traverse (second child+))]
                                                 (drop 2
                                                       child+))
                               :else     (cons (-traverse (first child+))
                                               (rest child+))))}))
