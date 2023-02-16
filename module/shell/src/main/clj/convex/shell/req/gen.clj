(ns convex.shell.req.gen

  ;; Needs refactoring, lots of duplication.

  (:refer-clojure :exclude [list
                            map
                            set
                            vector])
  (:require [clojure.test.check.generators :as TC.gen]
            [convex.cell                   :as $.cell]
            [convex.clj                    :as $.clj]
            [convex.cvm                    :as $.cvm]
            [convex.gen                    :as $.gen]
            [convex.shell.flow             :as $.shell.flow]
            [convex.shell.resrc            :as $.shell.resrc]
            [convex.std                    :as $.std]))


;;;;;;;;;;


(def ^:dynamic ^:no-doc -*ctx*

  nil)


;;;;;;;;;;


(defn -ensure-pos-num

  [ctx i]

  (if ($.std/long? i)
    (let [i-2 ($.clj/long i)]
      (if (>= i-2
              0)
        [true
         i-2]
        [false
         ($.cvm/exception-set ctx
                              ($.cell/code-std* :ARGUMENT)
                              ($.cell/* "Long must be >= 0"))]))
    [false
     ($.cvm/exception-set ctx
                          ($.cell/code-std* :ARGUMENT)
                          ($.cell/* "Expected a Long"))]))



(defn -ensure-bound

  [ctx min max]

  (let [[ok-min?
         x]      (-ensure-pos-num ctx
                                  min)]
    (if ok-min?
      (let [[ok-max?
             x-2]    (-ensure-pos-num ctx
                                      max)]
        (if ok-max?
          (if (<= x
                  x-2)
            [true
             [x
              x-2]]
            [false
             ($.cvm/exception-set ctx
                                  ($.cell/code-std* :ARGUMENT)
                                  ($.cell/* "Minimum must be <= Maximum"))])
          [false
           x-2]))
      [false
       x])))



(defn- -unwrap

  [ctx gen]

  (let [[ok?
         x]  ($.shell.resrc/unwrap ctx
                                   gen)]
    (if ok?
      (let [gen-2 x]
        (or (when-not (TC.gen/generator? gen-2)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Not a generator")))
            [true
             gen-2]))
      (let [ctx-2 x]
        [false
         ctx-2]))))


;;;;;;;;;;


(defn do-gen

  [ctx gen f]

  (let [[ok?
         x]  (-unwrap ctx
                      gen)]
    (if ok?
      (f ctx
         x)
      x)))



(defn do-gen+

  [ctx gen+ f]

  (or (when-not ($.std/vector? gen+)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Generators must be in a vector")))
      (when ($.std/empty? gen+)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Vector of generators cannot be empty")))
      (let [x (reduce (fn [acc gen]
                        (let [[ok?
                               x]  (-unwrap ctx
                                            gen)]
                          (if ok?
                            (let [gen-2 x]
                              (conj acc
                                    gen-2))
                            (let [ctx-2 x]
                              (reduced ctx-2)))))
                      []
                      gen+)]
        (if (vector? x)
          (f ctx
             x)
          x))))


;;;;;;;;;;


(defn gen

  [ctx [gen size seed]]

  (or (when-not ($.std/long? size)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Size must be a Long")))
      (when-not (or (nil? seed)
                    ($.std/long? seed))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Seed must be nil or a Long")))
      (do-gen ctx
              gen
              (fn [ctx-2 gen-2]
                (try
                  ;;
                  (binding [-*ctx* ctx-2]
                    ($.shell.flow/safe
                      (delay
                        ($.cvm/result-set ctx-2
                                          (if seed
                                            (TC.gen/generate gen-2
                                                             ($.clj/long size)
                                                             ($.clj/long seed))
                                            (TC.gen/generate gen-2
                                                             ($.clj/long size)))))))
                  ;;
                  (catch Exception _ex
                    ($.cvm/exception-set ctx
                                         ($.cell/* :SHELL.GEN)
                                         ($.cell/string "Unable to generate value, generator might be faulty"))))))))


;;;;;;;;;;


(defn always

  [ctx [x]]

  ($.cvm/result-set ctx
                    ($.shell.resrc/create (TC.gen/return x))))



(defn bind

  [ctx [gen f]]

  (or (when-not ($.std/fn? f)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Requires a function for mapping generated values")))
      (do-gen ctx
              gen
              (fn [ctx-2 gen-2]
                ($.cvm/result-set ctx-2
                                  ($.shell.resrc/create
                                    (TC.gen/bind gen-2
                                                 (fn [x]
                                                   (let [ctx-3 (-> -*ctx*
                                                                   ($.cvm/fork)
                                                                   ($.cvm/invoke f
                                                                                 ($.cvm/arg+* x)))]
                                                     (if ($.cvm/exception? ctx-3)
                                                       ($.shell.flow/return ctx-3)
                                                       (let [gen-3 ($.cvm/result ctx-3)]
                                                         (when-not (and ($.std/vector? gen-3)
                                                                        (= ($.std/count gen-3)
                                                                           2))
                                                           ($.shell.flow/return
                                                             ($.cvm/exception-set ($.cvm/fork -*ctx*)
                                                                                  ($.cell/code-std* :ARGUMENT)
                                                                                  ($.cell/* "Does not seem to be a generator"))))
                                                          (let [f*gen ($.std/nth gen-3
                                                                                 0)]
                                                            (when-not ($.cell/fake? f*gen)
                                                              ($.shell.flow/return
                                                                ($.cvm/exception-set ($.cvm/fork -*ctx*)
                                                                                     ($.cell/code-std* :ARGUMENT)
                                                                                     ($.cell/* "Either not a generator or a stale generator"))))
                                                            (let [gen-4 @f*gen]
                                                              (when-not (TC.gen/generator? gen-4)
                                                                ($.cvm/exception-set ($.cvm/fork -*ctx*)
                                                                                     ($.cell/code-std* :ARGUMENT)
                                                                                     ($.cell/* "Not a generator")))
                                                              gen-4)))))))))))))



(defn blob

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.shell.resrc/create ($.gen/blob))))



(defn blob-bounded

  [ctx [min max]]

  (let [[ok?
         x]  (-ensure-bound ctx
                            min
                            max)]
    (if ok?
      ($.cvm/result-set ctx
                        ($.shell.resrc/create ($.gen/blob (first x)
                                                          (second x))))
      x)))



(defn blob-fixed

  [ctx [n]]

  (let [[ok?
         x]  (-ensure-pos-num ctx
                             n)]
    (if ok?
      ($.cvm/result-set ctx
                        ($.shell.resrc/create ($.gen/blob x)))
      x)))



(defn blob-map

  [ctx [gen-k gen-v]]

  (do-gen ctx
          gen-k
          (fn [ctx-2 gen-k-2]
            (do-gen ctx-2
                    gen-v
                    (fn [ctx-3 gen-v-2]
                      ($.cvm/result-set ctx-3
                                        ($.shell.resrc/create ($.gen/blob-map gen-k-2
                                                                              gen-v-2))))))))



(defn blob-map-bounded

  [ctx [gen-k gen-v min max]]

  (let [[ok?
         x]  (-ensure-bound ctx
                            min
                            max)]
    (if ok?
      (do-gen ctx
              gen-k
              (fn [ctx-2 gen-k-2]
                (do-gen ctx-2
                        gen-v
                        (fn [ctx-3 gen-v-2]
                          ($.cvm/result-set ctx-3
                                            ($.shell.resrc/create ($.gen/blob-map gen-k-2
                                                                                  gen-v-2
                                                                                  (first x)
                                                                                  (second x))))))))
      x)))



(defn blob-map-fixed

  [ctx [gen-k gen-v n]]

  (let [[ok?
         x]  (-ensure-pos-num ctx
                              n)]
    (if ok?
      (do-gen ctx
              gen-k
              (fn [ctx-2 gen-k-2]
                (do-gen ctx-2
                        gen-v
                        (fn [ctx-3 gen-v-2]
                          ($.cvm/result-set ctx-3
                                            ($.shell.resrc/create ($.gen/blob-map gen-k-2
                                                                                  gen-v-2
                                                                                  x)))))))
      x)))



(defn double-bounded

  [ctx [min max infinite? nan?]]

  (or (when-not (or (nil? min)
                    ($.std/double? min))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Minimum must be Nil or a Double")))
      (when-not (or (nil? max)
                    ($.std/double? max))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Maximum must be Nil or a Double")))
      (let [max-2 (when max
                    ($.clj/double max))
            min-2 (when min
                    ($.clj/double min))]
        (or (when-not (or (nil? min-2)
                          (nil? max-2)
                          (<= min-2
                              max-2))
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Minimum must be <= Maximum")))
            ($.cvm/result-set ctx
                              ($.shell.resrc/create ($.gen/double-bounded {:infinite? ($.std/true? infinite?)
                                                                           :max       max-2
                                                                           :min       min-2
                                                                           :Nan?      ($.std/true? nan?)})))))))



(defn fmap

  [ctx [f gen]]

  (or (when-not ($.std/fn? f)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Requires a function for mapping generated values")))
      (do-gen ctx
              gen
              (fn [ctx-2 gen-2]
                ($.cvm/result-set ctx-2
                                  ($.shell.resrc/create (TC.gen/fmap (fn [x]
                                                                       (let [ctx-3 (-> -*ctx*
                                                                                       ($.cvm/fork)
                                                                                       ($.cvm/invoke f
                                                                                                     ($.cvm/arg+* x)))]
                                                                         (if ($.cvm/exception? ctx-3)
                                                                           ($.shell.flow/return ctx-3)
                                                                           ($.cvm/result ctx-3))))
                                                                     gen-2)))))))



(defn freq

  ;; Should frequencies always be positive?

  [ctx [pair+]]

  (or (when-not ($.std/vector? pair+)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Frequency pairs must be in a vector")))
      (when ($.std/empty? pair+)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "There must be at least 1 frequency pair")))
      (let [x (reduce (fn [acc pair]
                        (if (and ($.std/vector? pair)
                                 (= ($.std/count pair)
                                    2))
                          (let [i ($.std/nth pair
                                             0)]
                            (if ($.std/long? i)
                              (let [gen ($.std/nth pair
                                                   1)]
                                (or (when-not (and ($.std/vector? gen)
                                                   (= ($.std/count gen)
                                                      2))
                                      (reduced
                                        ($.cvm/exception-set ctx
                                                             ($.cell/code-std* :ARGUMENT)
                                                             ($.cell/* "Does not seem to be a generator"))))
                                    (let [f*gen ($.std/nth gen
                                                           0)]
                                      (or (when-not ($.cell/fake? f*gen)
                                            (reduced
                                              ($.cvm/exception-set ctx
                                                                   ($.cell/code-std* :ARGUMENT)
                                                                   ($.cell/* "Either not a generator or a stale generator"))))
                                          (let [gen-2 @f*gen]
                                            (or (when-not (TC.gen/generator? gen-2)
                                                  (reduced
                                                    ($.cvm/exception-set ctx
                                                                         ($.cell/code-std* :ARGUMENT)
                                                                         ($.cell/* "Not a generator"))))
                                                (conj acc
                                                      [($.clj/long i)
                                                       gen-2])))))))

                              (reduced ($.cvm/exception-set ctx
                                                            ($.cell/code-std* :ARGUMENT)
                                                            ($.cell/* "Frequency must be expressed as a Long")))))
                          
                          (reduced ($.cvm/exception-set ctx
                                                        ($.cell/code-std* :ARGUMENT)
                                                        ($.cell/* "Frequency pair must be a a vector of 2 items")))))
                      []
                      pair+)]
        (if (vector? x)
          ($.cvm/result-set ctx
                            ($.shell.resrc/create (TC.gen/frequency x)))
          x))))



(defn hex-string

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.shell.resrc/create ($.gen/hex-string))))



(defn hex-string-fixed

  [ctx [n]]

  (let [[ok?
         x]  (-ensure-pos-num ctx
                              n)]
    (if ok?
      ($.cvm/result-set ctx
                        ($.shell.resrc/create ($.gen/hex-string x)))
      x)))



(defn hex-string-bounded

  [ctx [min max]]

  (let [[ok?
         x]  (-ensure-bound ctx
                            min
                            max)]
    (if ok?
      ($.cvm/result-set ctx
                        ($.shell.resrc/create ($.gen/hex-string (first x)
                                                                (second x))))
      x)))



(defn list

  [ctx [gen]]

  (do-gen ctx
          gen
          (fn [ctx-2 gen-2]
            ($.cvm/result-set ctx-2
                              ($.shell.resrc/create ($.gen/list gen-2))))))



(defn list-bounded

  [ctx [gen min max]]

  (let [[ok?
         x]  (-ensure-bound ctx
                            min
                            max)]
    (if ok?
      (do-gen ctx
              gen
              (fn [ctx-2 gen-2]
                ($.cvm/result-set ctx-2
                                  ($.shell.resrc/create ($.gen/list gen-2
                                                                    (first x)
                                                                    (second x))))))
      x)))



(defn long-bounded

  [ctx [min max]]

  (or (when-not (or (nil? min)
                    ($.std/long? min))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Minimum must be Nil or a Long")))
      (when-not (or (nil? max)
                    ($.std/long? max))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Maximum must be Nil or a Long")))
      (let [max-2 (when max
                    ($.clj/long max))
            min-2 (when min
                    ($.clj/long min))]
        (or (when-not (or (nil? min-2)
                          (nil? max-2)
                          (<= min-2
                              max-2))
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Minimum must be <= Maximum")))
            ($.cvm/result-set ctx
                              ($.shell.resrc/create ($.gen/long-bounded {:max max-2
                                                                         :min min-2})))))))



(defn list-fixed

  [ctx [gen n]]

  (let [[ok?
         x]  (-ensure-pos-num ctx
                              n)]
    (if ok?
      (do-gen ctx
              gen
              (fn [ctx-2 gen-2]
                ($.cvm/result-set ctx-2
                                  ($.shell.resrc/create ($.gen/list gen-2
                                                                    x)))))
      x)))



(defn long-uniform

  [ctx [min max]]

  (or (when-not (or (nil? min)
                    ($.std/long? min))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Minimum must be Nil or a Long")))
      (when-not (or (nil? max)
                    ($.std/long? max))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Maximum must be Nil or a Long")))
      (let [max-2 (when max
                    ($.clj/long max))
            min-2 (when min
                    ($.clj/long min))]
        (or (when-not (or (nil? min-2)
                          (nil? max-2)
                          (<= min-2
                              max-2))
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Minimum must be <= Maximum")))
            ($.cvm/result-set ctx
                              ($.shell.resrc/create ($.gen/long-uniform min-2
                                                                        max-2)))))))



(defn map

  [ctx [gen-k gen-v]]

  (do-gen ctx
          gen-k
          (fn [ctx-2 gen-k-2]
            (do-gen ctx-2
                    gen-v
                    (fn [ctx-3 gen-v-2]
                      ($.cvm/result-set ctx-3
                                        ($.shell.resrc/create ($.gen/map gen-k-2
                                                                         gen-v-2))))))))



(defn map-bounded

  [ctx [gen-k gen-v min max]]

  (let [[ok?
         x]  (-ensure-bound ctx
                            min
                            max)]
    (if ok?
      (do-gen ctx
              gen-k
              (fn [ctx-2 gen-k-2]
                (do-gen ctx-2
                        gen-v
                        (fn [ctx-3 gen-v-2]
                          ($.cvm/result-set ctx-3
                                            ($.shell.resrc/create ($.gen/map gen-k-2
                                                                             gen-v-2
                                                                             (first x)
                                                                             (second x))))))))
      x)))



(defn map-fixed

  [ctx [gen-k gen-v n]]

  (let [[ok?
         x]  (-ensure-pos-num ctx
                              n)]
    (if ok?
      (do-gen ctx
              gen-k
              (fn [ctx-2 gen-k-2]
                (do-gen ctx-2
                        gen-v
                        (fn [ctx-3 gen-v-2]
                          ($.cvm/result-set ctx-3
                                            ($.shell.resrc/create ($.gen/map gen-k-2
                                                                             gen-v-2
                                                                             x)))))))
      x)))



(defn or-

  [ctx [gen+]]

  (do-gen+ ctx
           gen+
           (fn [ctx-2 gen-2+]
             ($.cvm/result-set ctx-2
                               ($.shell.resrc/create (TC.gen/one-of gen-2+))))))



(defn pick

  [ctx [x+]]

  (or (when-not ($.std/vector? x+)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Elements to pick from must be in a vector")))
      (when ($.std/empty? x+)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Vector of elements to pick cannot be empty")))
      ($.cvm/result-set ctx
                        ($.shell.resrc/create (TC.gen/elements x+)))))



(defn quoted

  [ctx [gen]]

  (do-gen ctx
          gen
          (fn [ctx-2 gen-2]
            ($.cvm/result-set ctx-2
                              ($.shell.resrc/create ($.gen/quoted gen-2))))))



(defn set

  [ctx [gen]]

  (do-gen ctx
          gen
          (fn [ctx-2 gen-2]
            ($.cvm/result-set ctx-2
                              ($.shell.resrc/create ($.gen/set gen-2))))))



(defn set-bounded

  [ctx [gen min max]]

  (let [[ok?
         x]  (-ensure-bound ctx
                            min
                            max)]
    (if ok?
      (do-gen ctx
              gen
              (fn [ctx-2 gen-2]
                ($.cvm/result-set ctx-2
                                  ($.shell.resrc/create ($.gen/set gen-2
                                                                   (first x)
                                                                   (second x))))))
      x)))



(defn set-fixed

  [ctx [gen n]]

  (let [[ok?
         x]  (-ensure-pos-num ctx
                              n)]
    (if ok?
      (do-gen ctx
              gen
              (fn [ctx-2 gen-2]
                ($.cvm/result-set ctx-2
                                  ($.shell.resrc/create ($.gen/set gen-2
                                                                   x)))))
      x)))



(defn string

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.shell.resrc/create ($.gen/string))))



(defn string-bounded

  [ctx [min max]]

  (let [[ok?
         x]  (-ensure-bound ctx
                            min
                            max)]
    (if ok?
      ($.cvm/result-set ctx
                        ($.shell.resrc/create ($.gen/string (first x)
                                                            (second x))))
      x)))



(defn string-fixed

  [ctx [n]]

  (let [[ok?
         x]  (-ensure-pos-num ctx
                              n)]
    (if ok?
      ($.cvm/result-set ctx
                        ($.shell.resrc/create ($.gen/string x)))
      x)))



(defn string-alphanum

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.shell.resrc/create ($.gen/string-alphanum))))



(defn string-alphanum-bounded

  [ctx [min max]]

  (let [[ok?
         x]  (-ensure-bound ctx
                            min
                            max)]
    (if ok?
      ($.cvm/result-set ctx
                        ($.shell.resrc/create ($.gen/string-alphanum (first x)
                                                                     (second x))))
      x)))



(defn string-alphanum-fixed

  [ctx [n]]

  (let [[ok?
         x]  (-ensure-pos-num ctx
                              n)]
    (if ok?
      ($.cvm/result-set ctx
                        ($.shell.resrc/create ($.gen/string-alphanum x)))
      x)))



(defn such-that

  [ctx [max-try f gen]]

  (or (when-not ($.std/fn? f)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Predicate must be a function")))
      (when-not ($.std/long? max-try)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Max trials must be a Long")))
      (let [max-try-2 ($.clj/long max-try)]
        (or (when-not (> max-try-2
                         1)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Max trials must be > 1")))
            (do-gen ctx
                    gen
                    (fn [ctx-2 gen-2]
                      ($.cvm/result-set ctx-2
                                        ($.shell.resrc/create
                                          (TC.gen/such-that (fn [x]
                                                              (let [ctx-3 (-> -*ctx*
                                                                              ($.cvm/fork)
                                                                              ($.cvm/invoke f
                                                                                            ($.cvm/arg+* x)))]
                                                                (if ($.cvm/exception? ctx-3)
                                                                  ($.shell.flow/return ctx-3)
                                                                  (let [result ($.cvm/result ctx-3)]
                                                                    (not (or (nil? result)
                                                                             ($.std/false? result)))))))
                                                            gen-2
                                                            max-try-2)))))))))



(defn syntax

  [ctx _arg+]

  ($.cvm/result-set ctx
                    ($.shell.resrc/create ($.gen/syntax))))



(defn syntax-with-meta

  [ctx [gen-v gen-meta]]

  (do-gen ctx
          gen-v
          (fn [ctx-2 gen-v-2]
            (do-gen ctx-2
                    gen-meta
                    (fn [ctx-3 gen-meta-2]
                      ($.cvm/result-set ctx-3
                                        ($.shell.resrc/create
                                          ($.gen/syntax gen-v-2
                                                        gen-meta-2))))))))



(defn syntax-with-value

  [ctx [gen-v]]

  (do-gen ctx
          gen-v
          (fn [ctx-2 gen-v-2]
            ($.cvm/result-set ctx-2
                              ($.shell.resrc/create ($.gen/syntax gen-v-2))))))



(defn tuple

  [ctx [gen+]]

  (do-gen+ ctx
           gen+
           (fn [ctx-2 gen-2+]
             ($.cvm/result-set ctx-2
                               ($.shell.resrc/create (apply $.gen/tuple
                                                            gen-2+))))))



(defn vector

  [ctx [gen]]

  (do-gen ctx
          gen
          (fn [ctx-2 gen-2]
            ($.cvm/result-set ctx-2
                              ($.shell.resrc/create ($.gen/vector gen-2))))))



(defn vector-bounded

  [ctx [gen min max]]

  (let [[ok?
         x]  (-ensure-bound ctx
                            min
                            max)]
    (if ok?
      (do-gen ctx
              gen
              (fn [ctx-2 gen-2]
                ($.cvm/result-set ctx-2
                                  ($.shell.resrc/create ($.gen/vector gen-2
                                                                      (first x)
                                                                      (second x))))))
      x)))



(defn vector-fixed

  [ctx [gen n]]

  (let [[ok?
         x]  (-ensure-pos-num ctx
                              n)]
    (if ok?
      (do-gen ctx
              gen
              (fn [ctx-2 gen-2]
                ($.cvm/result-set ctx-2
                                  ($.shell.resrc/create ($.gen/vector gen-2
                                                                      x)))))
      x)))
