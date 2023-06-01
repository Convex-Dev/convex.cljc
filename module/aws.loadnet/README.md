# `module/aws.loadnet` - [API](doc/API.md)  - [CHANGES](doc/changelog.md)

Deploying and managing Convex networks as AWS CloudFormation stack sets for load testing.

```clojure
;; Add to dependencies in `deps.edn`:
;;
world.convex/aws.loadnet
{:deps/root "module/aws.loadnet"
 :git/sha   "06c7137"
 :git/tag   "stable/2023-01-18"
 :git/url   "https://github.com/convex-dev/convex.cljc"}
```

```clojure
;; Supported platforms:
;;
[:jvm]
```


---


Fully automated solution for deploying networks of Convex Peers across the globe
using [AWS CloudFormation](https://aws.amazon.com/cloudformation) as well as
Load Generators emulation Users, transacting according to simulation scenarios.

Those simulation scenarios are implemented in Convex Lisp in the [Lab.cvx
repository](https://github.com/Convex-Dev/lab.cvx) and run on the [Convex
Shell](https://github.com/Convex-Dev/convex.cljc/tree/main/module/shell). All
current scenarios are available [in this
directory](https://github.com/Convex-Dev/lab.cvx/tree/main/module/lib/src/main/sim/scenario).

