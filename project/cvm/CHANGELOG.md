# Change Log

All notable changes to this project will be documented in this file.



## [Unreleased]

### Added

### Changed

### Fixed

### Removed




## [0.0.0-alpha3] - 2021-10-03

### Added

- Namespace `$.clj`, CVX -> CLJ type conversions
- Namespace `$.std`, Clojure-like functions for cells
- Printer for cells (previously, were printed as ugly Java objects)
- Merge former `:project/db`, bringing new namespaces:
    - `$.cvm.db`
    - `$.db` 

### Changed

- Moved type predicates functions from `$.cell` to `$.std`



## [0.0.0-alpha2] - 2021-09-28

### Added

- Converting Clojure directly to Convex (`$.cell/*` macro)

### Changed

- Upgrade `convex-core` to `0.7.1`

### Removed

- Namespace `$.form`, rendered obsolete by `$.cell/*` macro



## [0.0.0-alpha1] - 2021-09-10

### Added

- Creating transactions
    - `$.cell/call`
    - `$.cell/invoke`
    - `$.cell/transfer`
- Getting encodings and hashes:
    - `$.cell/blob<-hex`
    - `$.cell/encoding`
    - `$.cell/hash`
    - `$.cell/hash<-hex`
- Miscellaneous:
    - `$.cell/cvm-value?` 
- `$.cvm/fork-to`
- Namespace `$.form` for building common forms from cells (eg. `def`, `create-account`)

### Changed

- Depends on Java `convex-core/0.7.0`
- Move form building from `$.cell` to new namespace `$.form`

### Removed

- `$.cell/call?`
- `$.cell/import`



## [0.0.0-alpha0] - 2021-08-21

### Added

- First API iteration
    - Cell constructors and type predicate functions
    - Executing CVX code, handling CVM contextes
    - Converting CVX source into cells in various ways
    - Converting cells into CVX source in various ways
- Depends on Java `convex-core/0.7.0-rc1`



[Unreleased]:  https://github.com/helins/convex.lisp.cljc/compare/cvm/0.0.0-alpha3...HEAD
[0.0.0-alpha3]:  https://github.com/helins/convex.lisp.cljc/compare/cvm/0.0.0-alpha2...cvm/0.0.0-alpha3
[0.0.0-alpha2]:  https://github.com/helins/convex.lisp.cljc/compare/cvm/0.0.0-alpha1...cvm/0.0.0-alpha2
[0.0.0-alpha1]:  https://github.com/helins/convex.lisp.cljc/compare/cvm/0.0.0-alpha0...cvm/0.0.0-alpha1
[0.0.0-alpha0]: https://github.com/helins/convex.lisp.cljc/releases/tag/cvm/0.0.0-alpha0
