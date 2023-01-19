# Changelog

All notable changes for `module/net` between stable releases of this
repository.


---


## {{ next-release }}

- Remove
    - `$.key-pair/sign-hash` and `$.key-pair/verify-hash` which where somewhat redundant


---


## 2022-10-24

- Add
  - Sign and verify hashes directly


---


## Swich to [calver](https://calver.org)

Initially, the repository used semantic versioning. Below are the remainders of
this epoch.


---


## 0.0.0-alpha3 - 2022-09-22

- Add
    - Signature verification in `$.key-pair` (formerly `$.sign`)
    - Support for local clients
- Change
    - Peer servers now persist their data when stopping by default
    - Prefix `$.client` functions for results with `result->`
    - Rename `$.client/sequence` to `$.client/sequence-id`
    - Rename `$.sign` to `$.key-pair`
    - Rename `$.sign/signed` to `$.key-pair/sign`
    - Update to `:module/cvm` 0.0.0-alpha4 and Convex 0.7.9


---


## 0.0.0-alph2 - 2021-10-03

- Add
    - Merge former `:module/crypto` project, bringing new namespaces:
        - `$.pfx`
        - `$.sign`
- Change
    - Upgrade `:module/cvm` to `0.0.0-alpha3`


---


## 0.0.0-alpha1 - 2021-08-28

- Change
    - Upgrade `:module/crypto` to `0.0.0-alpha1`
    - Upgrade `:module/cvm` to `0.0.0-alpha2`


---


## 0.0.0-alpha0 - 2021-08-21

- Add
    - First API iteration
        - `$.client`
        - `$.server`
    - Depends on
        - `convex-peer/0.7.0`
        - `crypto.clj/0.0.0-alpha0`
        - `cvm.clj/0.0.0-alpha1`
