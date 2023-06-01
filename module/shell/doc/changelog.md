# Changelog

All notable changes for `module/shell` between stable releases of this
repository.


---


## 2023-06-01

- Add
    - `.cell.size`
    - `.cell.softness`
    - `.cell.str`
    - `.kp.seed.dev`
    - `.file.lock`
    - `.file.lock.release`
    - `.time.do*` 
    - `.state.core.vanilla`
    - `.state.do`
    - `.sys.n-cpu`
    - `.sys.pid`
    - `.sys.vthread`
    - Shelling out processes, see `(?.shell '.process)`
    - Connecting to Convex Shell via STDIO, see `(?.shell '.worker)`
    - Clients utilities for querying and transacting over Convex pers, see `(?.shell '.client)`
    - Creating transactions, see `(?.shell '.trx)`
    - Simple async utilities useful for clients, see `(?.shell '.a)`
    - Running and managing Convex peers, see `(?.shell '.peer)`
    - Logging utitities, see `(?.shell '.log)`
    - Generating random values and generative testing, see `(?.shell '.gen)`
- Change
    - Previous `.log.*` functions renamed to `.cvmlog.*`
        - To prevent clashing with the new logging utilities
    - Move `.version.*` values to `.sys.version.*`

---


## 2023-01-18

Significant rewrite simplify using by a magnitude. All Shell features are now
regular function or macro calls, just as expected. They are defined in the core
account, meaning they are available from anywhere, any account.

Significant changes and additions include:

- Brand new, much simpler unit testing library
- Creation of string-based streams
- Easier exception handling
- Experimental dependency management for Convex Lisp projects
    - With support for cloning sources from foreign Git repositories 
- Ability to selectively remove Shell features for safer execution
- General improvements and minor additions in many already existing features


---


## Swich to [calver](https://calver.org)

Initially, the repository used semantic versioning. Below are the remainders of
this epoch.


---


## 0.0.0-alpha4 - 2022-09-22

- Add
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
- Change
    - CVX requests symbols are now prepended with `!.`
    - Embed `:module/cvm` ; update to Convex 0.7.9
    - Move benchmarking to `$.time`
    - Reading a stream to completion always closes it automatically
    - Rename module from Convex Runner to Convex Shell
- Fix
    - Improve REPL behavior
        - More detailed execution error reporting
        - Better handling of the input stream
- Remove
    - Stack-based time traveling from CVX `$.time` in favor of new CVX `$.state` library
    - Unproven CVX `$.doc` library


---


## 0.0.0-alpha3 - 2021-09-28

- Change
    - Upgrade `:module/cvm` to `0.0.0-alpha2`


---


## 0.0.0-alpha2 - 2021-09-10

- Change
    - Upgrade `:module/cvm` to `0.0.0-alpha1`


---


## 0.0.0-alpha1 - 2021-08-21

- Change
    - Redeploy without bundling with `:module/cvm`


---


## 0.0.0-alpha0 - 2021-08-21

- Add
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
