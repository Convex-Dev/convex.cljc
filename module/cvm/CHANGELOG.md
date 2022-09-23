# Change Log

All notable changes to this project will be documented in this file.



## [Unreleased]

### Added

- `$.clj/blob->hex`

### Changed

### Fixed

### Removed



## [0.0.0-alpha4] - 2022-09-22

### Added

- Function fom extracting information from CVX exceptions
- Support for unquote splicing in `$.cell/*`
- `$.cvm/look-up`
- `$.gen/any-coll`
- `$.std/update`

### Changed

- Discard functions from `$.read` that read only one cell and rename other ones
- Greatly simplify `$.db`
    - Prevent bad behavior by enforcing thread-local instances
    - Remove any dealing with refs (too low-level for normal operations)
- Update to Convex 0.7.9

### Fixed

- `$.cell/*` properly converts namespaced symbols to CVX lookups

### Removed

- `$.ref` namespace, obsolete since new `$.db` namespace and considered too low-level



## [0.0.0-alpha3] - 2021-10-03

### Added

- Namespace `$.clj`, CVX -> CLJ type conversions
- Namespace `$.std`, Clojure-like functions for cells
- Printer for cells (previously, were printed as ugly Java objects)
- Merge former `:module/db`, bringing new namespaces:
    - `$.cvm.db`
    - `$.db` 

### Changed

- Moved type predicates functions from `$.cell` to `$.std`



## [0.0.0-alpha2] - 2021-09-28

### Added

- Converting Clojure directly to Convex (`$.cell/*` macro)

### Changed

- Update Convex to 0.7.1

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

- Move form building from `$.cell` to new namespace `$.form`
- Update to Convex 0.7.0

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
- Depends on Convex 0.7.0-rc1



[Unreleased]:    https://github.com/convex-dec/convex.cljc/compare/release/cvm/0.0.0-alpha4...HEAD
[0.0.0-alpha4]:  https://github.com/convex-dev/convex.cljc/compare/release/cvm/0.0.0-alpha3...release/cvm/0.0.0-alpha4
[0.0.0-alpha3]:  https://github.com/convex-dev/convex.cljc/compare/release/cvm/0.0.0-alpha2...release/cvm/0.0.0-alpha3
[0.0.0-alpha2]:  https://github.com/convex-dev/convex.cljc/compare/release/cvm/0.0.0-alpha1...release/cvm/0.0.0-alpha2
[0.0.0-alpha1]:  https://github.com/convex-dev/convex.cljc/compare/release/cvm/0.0.0-alpha0...release/cvm/0.0.0-alpha1
[0.0.0-alpha0]:  https://github.com/convex-dev/convex.cljc/releases/tag/release/cvm/0.0.0-alpha0
