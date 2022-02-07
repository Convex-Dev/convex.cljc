# Change Log

All notable changes to this project will be documented in this file.



## [Unreleased]

### Added

- New `convex.run.key-pair` library for key pair management

### Changed

### Fixed

### Removed



## [0.0.0-alpha3] - 2021-09-28

### Chanded

- Upgrade `:project/cvm` to `0.0.0-alpha2`



## [0.0.0-alpha2] - 2021-09-10

### Changed

- Upgrade `:project/cvm` to `0.0.0-alpha1`



## [0.0.0-alpha1] - 2021-08-21

### Changed

- Redeploy without bundling with `:project/cvm`



## [0.0.0-alpha0] - 2021-08-21

### Added

- First API iteration
    - Model for executing transactions and performing requests in-between
    - Error handling abstractions (`$.catch`)
    - File and stream abstractions (`$.file`, `$.stream`)
    - Help (`$/help`, `$.help`)
    - Metaprogramming by modifying transactions (`$.trx`)
    - Performance monitoring (`$.perf`)
    - REPL (`$.repl`)
    - Time travel (`$.time`)
    - Unit testing (`$.test`)
    - Miscellaneous utilities (`$.account`, `$.code`, `$.log`, `$.process`, `$.term`)
- Depends on `:project/cvm` 0.0.0-alpha0



[Unreleased]:  https://github.com/helins/convex.lisp.cljc/compare/run/0.0.0-alpha2...HEAD
[0.0.0-alpha3]:  https://github.com/helins/convex.lisp.cljc/compare/run/0.0.0-alpha2...run/0.0.0-alpha3
[0.0.0-alpha2]:  https://github.com/helins/convex.lisp.cljc/compare/run/0.0.0-alpha1...run/0.0.0-alpha2
[0.0.0-alpha1]:  https://github.com/helins/convex.lisp.cljc/compare/run/0.0.0-alpha0...run/0.0.0-alpha1
[0.0.0-alpha0]: https://github.com/helins/convex.lisp.cljc/releases/tag/run/0.0.0-alpha0
