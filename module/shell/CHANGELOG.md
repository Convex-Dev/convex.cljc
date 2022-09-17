# Change Log

All notable changes to this project will be documented in this file.



## [Unreleased]

### Added

- Advanced time-traveling and state utilities via the new `$.state` library
- Full JVM-like try-catch semantics for CVX `$.catch/!.try` (formerly `$.catch/!.safe`)
- Functions for disabling and enabling text styling with `$.term`
- Hygenic symbol generation with CVX `$.account/gensym`
- Improve `$.time`
    - New requests for handling clocks
- New CVX libraries
    - `$.db` offering support for Etch (immutable database for cells)
    - `$.juice` for limiting juice and more precise juice tracking
    - `$.state` for advanced time traveling, CVM state utilities, god-mode, ...
- Report JVM exceptions in temporary files
- Requests in `$.file` and `$.stream`
    - Append mode
    - Basic filesystem utilities (copy, delete, create temporary files, ...)
    - Reading and writing text, append mode
    - Stream handles can be any cell and user-provided when opening file streams

### Changed

- CVX requests symbols are now prepended with `!.`
- Embed `:module/cvm` ; upgrade to Convex 0.7.8
- Move benchmarking to `$.time`
- Reading a stream to completion always closes it automatically
- Rename module from Convex Runner to Convex Shell

### Fixed

- Improve REPL behavior
    - More detailed execution error reporting
    - Better handling of the input stream

### Removed

- Stack-based time traveling from CVX `$.time` in favor of new CVX `$.state` library
- Unproven CVX `$.doc` library



## [0.0.0-alpha3] - 2021-09-28

### Changed

- Upgrade `:module/cvm` to `0.0.0-alpha2`



## [0.0.0-alpha2] - 2021-09-10

### Changed

- Upgrade `:module/cvm` to `0.0.0-alpha1`



## [0.0.0-alpha1] - 2021-08-21

### Changed

- Redeploy without bundling with `:module/cvm`



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
- Depends on `:module/cvm` 0.0.0-alpha0



[Unreleased]:   https://github.com/convex-dev/convex.cljc/compare/run/0.0.0-alpha2...HEAD
[0.0.0-alpha3]: https://github.com/convex-dev/convex.cljc/compare/run/0.0.0-alpha2...run/0.0.0-alpha3
[0.0.0-alpha2]: https://github.com/convex-dev/convex.cljc/compare/run/0.0.0-alpha1...run/0.0.0-alpha2
[0.0.0-alpha1]: https://github.com/convex-dev/convex.cljc/compare/run/0.0.0-alpha0...run/0.0.0-alpha1
[0.0.0-alpha0]: https://github.com/convex-dev/convex.cljc/releases/tag/run/0.0.0-alpha0
