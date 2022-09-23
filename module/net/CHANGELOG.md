# Change Log

All notable changes to this project will be documented in this file.



## [Unreleased]

### Added

- Sign and verify hashes directly

### Changed

### Fixed

### Removed



## [0.0.0-alpha3] - 2022-09-22

### Added

- Signature verification in `$.key-pair` (formerly `$.sign`)
- Support for local clients

### Changed

- Peer servers now persist their data when stopping by default
- Prefix `$.client` functions for results with `result->`
- Rename `$.client/sequence` to `$.client/sequence-id`
- Rename `$.sign` to `$.key-pair`
- Rename `$.sign/signed` to `$.key-pair/sign`
- Update to `:module/cvm` 0.0.0-alpha4 and Convex 0.7.9



## [0.0.0-alph2] - 2021-10-03

### Added

- Merge former `:module/crypto` project, bringing new namespaces:
    - `$.pfx`
    - `$.sign`

### Changed

- Upgrade `:module/cvm` to `0.0.0-alpha3`



## [0.0.0-alpha1] - 2021-08-28

### Changed

- Upgrade `:module/crypto` to `0.0.0-alpha1`
- Upgrade `:module/cvm` to `0.0.0-alpha2`



## [0.0.0-alpha0] - 2021-08-21

### Added

- First API iteration
    - `$.client`
    - `$.server`
- Depends on
    - `convex-peer/0.7.0`
    - `crypto.clj/0.0.0-alpha0`
    - `cvm.clj/0.0.0-alpha1`



[Unreleased]:   https://github.com/convex-dev/convex.cljc/compare/release/net/0.0.0-alpha3...HEAD
[0.0.0-alpha2]: https://github.com/convex-dev/convex.cljc/compare/release/net/0.0.0-alpha2...release/net/0.0.0-alpha3
[0.0.0-alpha2]: https://github.com/convex-dev/convex.cljc/compare/release/net/0.0.0-alpha1...release/net/0.0.0-alpha2
[0.0.0-alpha1]: https://github.com/convex-dev/convex.cljc/compare/release/net/0.0.0-alpha0...release/net/0.0.0-alpha1
[0.0.0-alpha0]: https://github.com/convex-dev/convex.cljc/releases/tag/release/net/0.0.0-alpha0
