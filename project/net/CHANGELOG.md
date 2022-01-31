# Change Log

All notable changes to this project will be documented in this file.



## [Unreleased]

### Added

- `convex.pfx/open`

### Changed

- Easier key store management
    - `convex.pfx/create` returns nil if a file at the given path already exists
    - `convex.pfx/load` returns nil if no file is found at the given path

### Fixed

### Removed



## [0.0.0-alph2] - 2021-10-03

### Added

- Merge former `:project/crypto` project, bringin new namespaces:
    - `$.pfx`
    - `$.sign`

### Changed

- Upgrade `:project/cvm` to `0.0.0-alpha3`



## [0.0.0-alpha1] - 2021-08-28

### Changed

- Upgrade `:project/crypto` to `0.0.0-alpha1`
- Upgrade `:project/cvm` to `0.0.0-alpha2`



## [0.0.0-alpha0] - 2021-08-21

### Added

- First API iteration
    - `$.client`
    - `$.server`
- Depends on
    - `convex-peer/0.7.0`
    - `crypto.clj/0.0.0-alpha0`
    - `cvm.clj/0.0.0-alpha1`



[Unreleased]:  https://github.com/helins/convex.lisp.cljc/compare/net/0.0.0-alpha2...HEAD
[0.0.0-alpha2]:  https://github.com/helins/convex.lisp.cljc/compare/net/0.0.0-alpha1...net/0.0.0-alpha2
[0.0.0-alpha1]:  https://github.com/helins/convex.lisp.cljc/compare/net/0.0.0-alpha0...net/0.0.0-alpha1
[0.0.0-alpha0]: https://github.com/helins/convex.lisp.cljc/releases/tag/net/0.0.0-alpha0
