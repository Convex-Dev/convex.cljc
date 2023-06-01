
Fully automated solution for deploying networks of Convex Peers across the globe
using [AWS CloudFormation](https://aws.amazon.com/cloudformation) as well as
Load Generators emulation Users, transacting according to simulation scenarios.

Those simulation scenarios are implemented in Convex Lisp in the [Lab.cvx
repository](https://github.com/Convex-Dev/lab.cvx) and run on the [Convex
Shell](https://github.com/Convex-Dev/convex.cljc/tree/main/module/shell). All
current scenarios are available [in this
directory](https://github.com/Convex-Dev/lab.cvx/tree/main/module/lib/src/main/sim/scenario).

Public API consists resides in the [convex.aws.loadnet](./doc/API.md#convex.aws.loadnet) namespace:

- [create](./doc/API.md#convex.aws.loadnet/create)
- [stop](./doc/API.md#convex.aws.loadnet/stop)
