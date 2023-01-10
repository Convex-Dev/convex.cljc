(ns convex.shell.dep        

  (:refer-clojure :exclude [read])
  (:require [convex.cell               :as $.cell]
            [convex.cvm                :as $.cvm]
            [convex.shell.dep.git      :as $.shell.dep.git]
            [convex.shell.dep.local    :as $.shell.dep.local]
            [convex.shell.dep.relative :as $.shell.dep.relative]
            [convex.shell.fail         :as $.shell.fail]
            [convex.shell.flow         :as $.shell.flow]
            [convex.shell.kw           :as $.shell.kw]
            [convex.shell.project      :as $.shell.project]
            [convex.shell.sym          :as $.shell.sym]
            [convex.std                :as $.std]))


(declare fetch)


;;;;;;;;;; Private


(defn- -jump

  [env dep-parent actor-sym actor-path]

  (-> env
      (assoc :convex.shell/dep          dep-parent
             :convex.shell.dep/required ($.cell/* [~actor-sym
                                                   ~($.std/next actor-path)]))
      (fetch)
      (assoc :convex.shell/dep
             (env :convex.shell/dep))))


;;;;;;;;;; Miscellaneous


(defn project

  [ctx dep dir]

  (let [fail    (fn [code message]
                  ($.shell.flow/fail ctx
                                     code
                                     ($.std/assoc message
                                                  ($.cell/* :dep)
                                                  dep)))
        project ($.shell.project/read dir
                                      fail)]
    ($.shell.project/dep+ project
                          fail)
    project))


;;;;;;;;;; Fetching actors


(defn fetch


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
                                    [$.shell.kw/deps
                                     project-sym])
             _              (when-not dep-parent
                              ($.shell.flow/fail (env :convex.shell/ctx)
                                                 ($.cell/code-std* :ARGUMENT)
                                                 ($.cell/* {:ancestry ~(env :convex.shell.dep/ancestry)
                                                            :message  ~($.cell/string (format "Dependency alias not found: %s"
                                                                                              project-sym))})))
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


  ([env required]


   (let [ancestry ($.cell/* [])
         ctx      (env :convex.shell/ctx)]
     ($.shell.dep.relative/validate-required ctx
                                             required
                                             ancestry)
     (fetch (-> env
                (update :convex.shell.dep/resolver+
                        (fn [resolver+]
                          (-> resolver+
                              (assoc $.shell.kw/git   $.shell.dep.git/fetch
                                     $.shell.kw/local $.shell.dep.local/fetch)
                              (update $.shell.kw/relative
                                      #(or %
                                           $.shell.dep.relative/fetch)))))
                (merge {:convex.shell/dep               $.shell.kw/root
                        :convex.shell.dep/ancestry      ancestry
                        :convex.shell.dep/dep->project  {$.shell.kw/root (project ctx
                                                                                  $.shell.kw/root
                                                                                  (str ($.cvm/look-up ctx
                                                                                                      ($.cell/address 8)
                                                                                                      ($.cell/* .dep.root))))}
                        :convex.shell.dep/fetch         fetch
                        :convex.shell.dep/foreign?      false
                        :convex.shell.dep/hash          $.shell.kw/root
                        :convex.shell.dep/jump          -jump
                        :convex.shell.dep/read-project  project
                        :convex.shell.dep/required      required
                        :convex.shell.dep.hash/pending+ #{}}))))))


;;;;;;;;;; Deploying actors


(defn deploy-actor

  [env hash code]

  (let [ctx     ($.cvm/deploy (env :convex.shell/ctx)
                              code)
        ex      ($.cvm/exception ctx)
        _       (when ex
                  (throw (ex-info ""
                         {:convex.shell/exception (-> ex
                                                      ($.shell.fail/mappify-cvm-ex)
                                                      ($.std/assoc $.shell.kw/ancestry
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
                                                 $.shell.sym/_)
                                           (conj actor-sym
                                                 (env-3 :convex.shell.dep/address))))))
                               (assoc env
                                      :convex.shell.dep/let
                                      [])
                               binding+)
              src     (get-in env
                              [:convex.shell.dep/hash->file
                               hash
                               ($.cell/* :src)])]
          (if src
            (-> (deploy-actor env-2
                               hash
                               ($.std/concat ($.cell/* (let ~($.cell/vector (env-2 :convex.shell.dep/let))))
                                             src))
                (assoc :convex.shell.dep/hash
                       hash))
            env-2))
        ;;
        (if-some [src (get-in env
                              [:convex.shell.dep/hash->file
                               hash
                               ($.cell/* :src)])]
          (deploy-actor env
                        hash
                        ($.std/cons $.shell.sym/do
                                    src))
          env)))))
