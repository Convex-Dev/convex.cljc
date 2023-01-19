# Changelog

All notable changes for `module/cvm` between stable releases of this
repository.


---


## {{ next-release }}

- Change
    - Remove anything related to `CVMByte` (since removed from newer Convex version)


---


## 2023-01-18

- Add
    - `$.cell/fake`
    - `$.cvm/exception-set`
    - `$.cvm/exception-trace`
    - `$.db/size`
    - `$.std/state?`


---


## 2022-10-24

- Add
    - `$.clj/blob->hex`


---


## Swich to [calver](https://calver.org)

Initially, the repository used semantic versioning. Below are the remainders of
this epoch.


---


## 0.0.0-alpha4 - 2022-09-22

- Add
    - Function fom extracting information from CVX exceptions
    - Support for unquote splicing in `$.cell/*`
    - `$.cvm/look-up`
    - `$.gen/any-coll`
    - `$.std/update`
- Change
    - Discard functions from `$.read` that read only one cell and rename other ones
    - Greatly simplify `$.db`
        - Prevent bad behavior by enforcing thread-local instances
        - Remove any dealing with refs (too low-level for normal operations)
    - Update to Convex 0.7.9
- Fix
    - `$.cell/*` properly converts namespaced symbols to CVX lookups
- Remove
    - `$.ref` namespace, obsolete since new `$.db` namespace and considered too low-level


---


## 0.0.0-alpha3 - 2021-10-03

- Add
    - Namespace `$.clj`, CVX -> CLJ type conversions
    - Namespace `$.std`, Clojure-like functions for cells
    - Printer for cells (previously, were printed as ugly Java objects)
    - Merge former `:module/db`, bringing new namespaces:
        - `$.cvm.db`
        - `$.db` 
- Change
    - Moved type predicates functions from `$.cell` to `$.std`


---


## 0.0.0-alpha2 - 2021-09-28

- Add
    - Converting Clojure directly to Convex (`$.cell/*` macro)
- Change
    - Update Convex to 0.7.1
- Remove
    - Namespace `$.form`, rendered obsolete by `$.cell/*` macro


---


## 0.0.0-alpha1 - 2021-09-10

- Add
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
- Change
    - Move form building from `$.cell` to new namespace `$.form`
    - Update to Convex 0.7.0
- Remove
    - `$.cell/call?`
    - `$.cell/import`


---


## 0.0.0-alpha0 - 2021-08-21

- Add
    - First API iteration
        - Cell constructors and type predicate functions
        - Executing CVX code, handling CVM contextes
        - Converting CVX source into cells in various ways
        - Converting cells into CVX source in various ways
        - Depends on Convex 0.7.0-rc1
