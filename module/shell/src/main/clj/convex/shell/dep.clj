(ns convex.shell.dep        

  (:refer-clojure :exclude [read])
  (:require [convex.cell               :as $.cell]
            [convex.cvm                :as $.cvm]
            [convex.shell.ctx          :as $.shell.ctx]
            [convex.shell.dep.git      :as $.shell.dep.git]
            [convex.shell.dep.relative :as $.shell.dep.relative]
            [convex.shell.err          :as $.shell.err]
            [convex.shell.exec.fail    :as $.shell.exec.fail]
            [convex.shell.kw           :as $.shell.kw]
            [convex.shell.project      :as $.shell.project]
            [convex.std                :as $.std]))


;;;;;;;;;; Private


(defn- -safe

  [env *d]

  (try
    @*d
    (catch clojure.lang.ExceptionInfo ex
      (if-some [shell-ex (:convex.shell/exception (ex-data ex))]
        ($.shell.exec.fail/err env
                               shell-ex)
        (throw ex)))))


;;;;;;;;;; Miscellaneous


(defn project

  [dep dir]

  (let [fail    (fn [shell-ex]
                  (throw (ex-info ""
                                  {:convex.shell/exception
                                   ($.std/assoc shell-ex
                                                $.shell.kw/project
                                                dep)})))

        project ($.shell.project/read dir
                                      fail)]
    ($.shell.project/dep+ project
                          fail)
    project))


;;;;;;;;;; Fetching actors


(defn- -fetch


  ([env]

   (if-some [required (env :convex.shell.dep/required)]
     ;; 
     ;; Need to resolve an actor path.
     ;;
     (recur
       (let [[actor-sym
              actor-path
              & required-2] required
             dep-child      (env :convex.shell/dep)
             project-child  (get-in env
                                    [:convex.shell.dep/dep->project
                                     dep-child])
             project-sym    (first actor-path)
             dep-parent     (get-in project-child
                                    [($.cell/* :deps)
                                     project-sym])
             _              (when-not dep-parent
                              (throw (ex-info ""
                                              {:convex.shell/exception
                                               (-> ($.cell/error ($.cell/code-std* :ARGUMENT)
                                                                 ($.cell/string (format "Dependency alias not found: %s"
                                                                                        project-sym)))
                                                   ($.std/assoc ($.cell/* :ancestry)
                                                                (env :convex.shell.dep/ancestry)))})))
             fetch-parent  (get-in env
                                   [:convex.shell.dep/resolver+
                                    (first dep-parent)])]
         (-> env
             (update :convex.shell.dep/ancestry
                     $.std/conj
                     ($.cell/* [~(env :convex.shell/dep)
                                ~actor-path]))
             (fetch-parent project-child
                           dep-parent
                           actor-sym
                           actor-path)
             (assoc :convex.shell.dep/ancestry (env :convex.shell.dep/ancestry)
                    :convex.shell.dep/required required-2))))
     ;;
     ;; No actor is required.
     ;;
     env))


  ([env dir-project required]

   (let [ancestry ($.cell/* [])]
     ($.shell.dep.relative/validate-required required
                                             ancestry)
     (-fetch (-> env
                 (update-in [:convex.shell.dep/resolver+
                             ($.cell/* :relative)]
                            #(or %
                                 $.shell.dep.relative/fetch))
                 (assoc-in [:convex.shell.dep/resolver+
                            ($.cell/* :git)]
                           $.shell.dep.git/fetch)
                 (merge {:convex.shell/dep               $.shell.kw/root
                         :convex.shell.dep/ancestry      ancestry
                         :convex.shell.dep/dep->project  {$.shell.kw/root (project $.shell.kw/root
                                                                                   dir-project)}
                         :convex.shell.dep/fetch         -fetch
                         :convex.shell.dep/foreign?      false
                         :convex.shell.dep/hash          $.shell.kw/root
                         :convex.shell.dep/read-project  project
                         :convex.shell.dep/required      required
                         :convex.shell.dep.hash/pending+ #{}}))))))



(defn fetch

  [env dir-project required]

  (-safe env
         (delay
           (-fetch env
                   dir-project
                   required)
           ($.shell.ctx/def-result env
                                   nil))))


;;;;;;;;;; Retrieving source


(defn content

  [env dir-project required]

  (-safe env
         (delay
           ($.shell.ctx/def-result env
                                   (-> env
                                       (assoc-in [:convex.shell.dep/resolver+
                                                  ($.cell/* :relative)]
                                                 $.shell.dep.relative/content)
                                       (-fetch dir-project
                                               required)
                                       (:convex.shell.dep/content))))))


;;;;;;;;;; Deploying actors


(defn deploy-actor

  [env hash code]

  (let [ctx     ($.cvm/deploy (env :convex.shell/ctx)
                              code)
        ex      ($.cvm/exception ctx)
        _       (when ex
                  (throw (ex-info ""
                         {:convex.shell/exception (-> ex
                                                      ($.shell.err/mappify)
                                                      ($.std/assoc ($.cell/* :ancestry)
                                                                   (get-in env
                                                                           [:convex.shell.dep/hash->ancestry
                                                                            hash])))})))
        address ($.cvm/result ctx)]
    (-> env
        (assoc :convex.shell/ctx         ctx
               :convex.shell.dep/address address)
        (assoc-in [:convex.shell.dep/hash->address
                   hash]
                  address))))



(defn deploy-fetched

  [env]

  (let [hash    (env :convex.shell.dep/hash)
        address (get-in env
                        [:convex.shell.dep/hash->address
                         hash])]
    (if address
      ;;
      (assoc env
             :convex.shell.dep/address
             address)
      ;;
      (if-some [binding+ (get-in env
                                 [:convex.shell.dep/hash->binding+
                                  hash])]
        ;;
        (let [env-2   (reduce (fn [env-2 [actor-sym hash]]
                                (let [env-3 (-> env-2
                                                (assoc :convex.shell.dep/hash
                                                       hash)
                                                (deploy-fetched))]
                                  (assoc env-3
                                         :convex.shell.dep/let
                                         (cond->
                                           (env-2 :convex.shell.dep/let)
                                           (not= actor-sym
                                                 ($.cell/* _))
                                           (conj actor-sym
                                                 (env-3 :convex.shell.dep/address))))))
                               (assoc env
                                      :convex.shell.dep/let
                                      [])
                               binding+)
              hash-src (get-in env
                               [:convex.shell.dep/hash->src hash])]
          (if hash-src
            (-> (deploy-actor env-2
                               hash
                               ($.std/concat ($.cell/* (let ~($.cell/vector (env-2 :convex.shell.dep/let))))
                                             hash-src))
                (assoc :convex.shell.dep/hash
                       hash))
            env-2))
        ;;
        (if-some [hash-src (get-in env
                                   [:convex.shell.dep/hash->src
                                    hash])]
          (deploy-actor env
                        hash
                        ($.std/cons ($.cell/* do)
                                    hash-src))
          env)))))



(defn deploy

  [env dir-project required]

  (-safe env
         (delay
           (let [env-2 (-> (-fetch env
                                   dir-project
                                   required)
                           (assoc :convex.shell/ctx
                                  (env :convex.shell/ctx))
                           (deploy-fetched))]
             (-> env
                 (assoc :convex.shell/ctx
                        (env-2 :convex.shell/ctx))
                 ($.shell.ctx/def-current (partition 2
                                                     (env-2 :convex.shell.dep/let)))
                 ($.shell.ctx/def-result nil))))))
