This library hosts generators for [test.check](https://github.com/clojure/test.check) and is useful for advanced
generative testing of smart contracts. It is meant for users already familiar with `test.check`. The overall API tends
to resemble the `clojure.test.generators` namespace.

```clojure
(require '[clojure.test.generators :as TC.gen]
         '[convex.gen              :as $.gen])


(TC.gen/generate $.gen/any)
```

[`:module/break`](../../module/break) is an extensive generative test suite
targeting the Convex Virtual Machine.
