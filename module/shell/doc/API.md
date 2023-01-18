# Table of contents
-  [`convex.shell`](#convex.shell)  - CONVEX SHELL Convex Virtual Machine extended with side-effects.
    -  [`-main`](#convex.shell/-main) - Main entry point for using Convex Shell as a terminal application.
    -  [`init`](#convex.shell/init) - Initializes a genesis context, forking [[convex.shell.ctx/genesis]].
    -  [`transact`](#convex.shell/transact) - Applies the given transaction (a cell) to the given context.
    -  [`transact-main`](#convex.shell/transact-main) - Core implementation of [[-main]].
-  [`convex.shell.ctx`](#convex.shell.ctx)  - Preparing the genesis context used by the Shell.
    -  [`genesis`](#convex.shell.ctx/genesis)
-  [`convex.shell.dep`](#convex.shell.dep)  - Experimental dependency management framework for Convex Lisp.
    -  [`deploy-actor`](#convex.shell.dep/deploy-actor) - Used in [[deploy-fetched]] for deploying a single actor in the Shell.
    -  [`deploy-fetched`](#convex.shell.dep/deploy-fetched) - Deploys actors that have prefetched with [[fetched]].
    -  [`fetch`](#convex.shell.dep/fetch) - Main function for fetching dependencies (Convex Lisp files), which may or may not be deployed as actors in a latter step.
    -  [`project`](#convex.shell.dep/project) - Returns a <code>project.cvx</code> file where dependencies reside.
-  [`convex.shell.dep.fail`](#convex.shell.dep.fail)  - Builds on [[convex.shell.flow]] for returning CVM exceptions relating to [[convex.shell.dep]].
    -  [`rethrow-with-ancestry`](#convex.shell.dep.fail/rethrow-with-ancestry)
    -  [`with-ancestry`](#convex.shell.dep.fail/with-ancestry)
-  [`convex.shell.dep.git`](#convex.shell.dep.git)  - Git dependencies are a convenient way of exposing Convex Lisp project over the Internet.
    -  [`fetch`](#convex.shell.dep.git/fetch) - Used in [[convex.shell.dep/fetch]] for fetching Git dependencies.
    -  [`path-cache-repo`](#convex.shell.dep.git/path-cache-repo)
    -  [`re-scp`](#convex.shell.dep.git/re-scp)
    -  [`re-url`](#convex.shell.dep.git/re-url)
    -  [`worktree`](#convex.shell.dep.git/worktree) - Clones a repo and creates a worktree for the desired SHA (if none of this hasn't been done already.
-  [`convex.shell.dep.local`](#convex.shell.dep.local)  - A local dependency points to another local directory which contains its own <code>project.cvx</code>.
    -  [`fetch`](#convex.shell.dep.local/fetch) - Used in [[convex.shell.dep/fetch]] for fetching local dependencies.
-  [`convex.shell.dep.relative`](#convex.shell.dep.relative)  - "Relative" dependency resolution mechanism.
    -  [`content`](#convex.shell.dep.relative/content) - Retrieves the content of a relative actor.
    -  [`fetch`](#convex.shell.dep.relative/fetch) - Used in [[convex.shell.dep/fetch]] for fetching relative dependencies.
    -  [`path`](#convex.shell.dep.relative/path) - Produces an actual file path form an actor path.
    -  [`read`](#convex.shell.dep.relative/read) - Reads a Convex Lisp file.
    -  [`validate-required`](#convex.shell.dep.relative/validate-required) - Validates a deploy vector.
-  [`convex.shell.fail`](#convex.shell.fail)  - Helpers for handling Shell failures.
    -  [`mappify-cvm-ex`](#convex.shell.fail/mappify-cvm-ex) - Transforms the given CVM exception into a CVX map.
    -  [`top-exception`](#convex.shell.fail/top-exception) - Called when an unforeseen JVM exception is caught.
-  [`convex.shell.flow`](#convex.shell.flow)  - Sometimes, when failing to execute a request for any reason, it is easier throwing the context in an exception caught and returned to the user at a strategic point.
    -  [`fail`](#convex.shell.flow/fail) - Attaches a CVM exception to the context and forwards it to [[return]].
    -  [`return`](#convex.shell.flow/return) - Throws the context in an exception that can be catched using [[safe]].
    -  [`safe`](#convex.shell.flow/safe)
-  [`convex.shell.io`](#convex.shell.io)  - Basic IO utilities and STDIO.
    -  [`file-in`](#convex.shell.io/file-in) - Opens an input text stream for the file located under <code>path</code>.
    -  [`file-out`](#convex.shell.io/file-out) - Opens an output text stream for the file located under <code>path</code>.
    -  [`flush`](#convex.shell.io/flush) - Flushes the given <code>out</code>.
    -  [`newline`](#convex.shell.io/newline) - Writes a new line to the given text output stream.
    -  [`stderr`](#convex.shell.io/stderr) - File descriptor for STDERR.
    -  [`stderr-txt`](#convex.shell.io/stderr-txt) - Text stream for STDERR.
    -  [`stdin`](#convex.shell.io/stdin) - File descriptor for STDIN.
    -  [`stdin-txt`](#convex.shell.io/stdin-txt) - Text stream for STDIN.
    -  [`stdout`](#convex.shell.io/stdout) - File descriptor for STDOUT.
    -  [`stdout-txt`](#convex.shell.io/stdout-txt) - Text stream for STDOUT.
-  [`convex.shell.project`](#convex.shell.project)  - Convex Lisp projects may have a <code>project.cvx</code> file which contains useful data for the Shell.
    -  [`dep+`](#convex.shell.project/dep+) - Validates and returns <code>:deps</code> found in a <code>project.cvx</code>.
    -  [`read`](#convex.shell.project/read) - Reads the <code>project.cvx</code> file found in <code>dir</code>.
-  [`convex.shell.req`](#convex.shell.req)  - All extra features offered by the Shell, over the Convex Virtual Machine, have a single entry point: the <code>.shell.invoke</code> function injected in the core account.
    -  [`core`](#convex.shell.req/core) - All core requests.
    -  [`ex-rethrow`](#convex.shell.req/ex-rethrow) - Request for rethrowing an exception captured in the Shell.
    -  [`invoker`](#convex.shell.req/invoker) - Returns an [[invoker]].
-  [`convex.shell.req.account`](#convex.shell.req.account)  - Requests relating to accounts.
    -  [`switch`](#convex.shell.req.account/switch) - Requests for switching the context to another address.
-  [`convex.shell.req.bench`](#convex.shell.req.bench)  - Requests related to benchmarking.
    -  [`trx`](#convex.shell.req.bench/trx) - Request for benchmarking a single transaction using Criterium.
-  [`convex.shell.req.db`](#convex.shell.req.db)  - Requests relating to Etch.
    -  [`flush`](#convex.shell.req.db/flush) - Request for flushing Etch.
    -  [`open`](#convex.shell.req.db/open) - Request for opening an Etch instance.
    -  [`path`](#convex.shell.req.db/path) - Request for getting the path of the currently open instance (or nil).
    -  [`read`](#convex.shell.req.db/read) - Request for reading a cell by hash.
    -  [`root-read`](#convex.shell.req.db/root-read) - Request for reading from the root.
    -  [`root-write`](#convex.shell.req.db/root-write) - Request for writing to the root.
    -  [`write`](#convex.shell.req.db/write) - Request for writing a cell.
-  [`convex.shell.req.dep`](#convex.shell.req.dep)  - Requests for the experimental dependency management framework.
    -  [`deploy`](#convex.shell.req.dep/deploy) - Request for deploying a deploy vector.
    -  [`fetch`](#convex.shell.req.dep/fetch) - Request for fetching required dependencies given a deploy vector.
    -  [`read`](#convex.shell.req.dep/read) - Request for reading CVX files resolved from a deploy vector.
-  [`convex.shell.req.dev`](#convex.shell.req.dev)  - Requests only used for dev purposes.
    -  [`fatal`](#convex.shell.req.dev/fatal) - Request for throwing a JVM exception, which should result in a fatal error in the Shell.
-  [`convex.shell.req.file`](#convex.shell.req.file)  - Requests relating to file utils.
    -  [`stream-in`](#convex.shell.req.file/stream-in) - Request for opening an input stream for file under <code>path</code>.
    -  [`stream-out`](#convex.shell.req.file/stream-out) - Request for opening an output stream for file under <code>path</code>.
-  [`convex.shell.req.fs`](#convex.shell.req.fs)  - Requests relating to filesystem utilities.
    -  [`copy`](#convex.shell.req.fs/copy) - Request for copying files and directories like Unix's <code>cp</code>.
    -  [`delete`](#convex.shell.req.fs/delete) - Request for deleting a file or an empty directory.
    -  [`dir?`](#convex.shell.req.fs/dir?) - Request returning <code>true</code> if <code>path</code> is an actual directory.
    -  [`exists?`](#convex.shell.req.fs/exists?) - Request returning <code>true</code> if <code>path</code> exists.
    -  [`file?`](#convex.shell.req.fs/file?) - Request returning <code>true</code> if <code>file</code> is an actual, regular file.
    -  [`resolve`](#convex.shell.req.fs/resolve) - Request for resolving a filename to a canonical form.
    -  [`size`](#convex.shell.req.fs/size) - Request for returning a filesize in bytes.
    -  [`tmp`](#convex.shell.req.fs/tmp) - Request for creating a temporary file.
    -  [`tmp-dir`](#convex.shell.req.fs/tmp-dir) - Request for creating a temporary directory.
-  [`convex.shell.req.juice`](#convex.shell.req.juice)  - Requests relating to juice.
    -  [`set`](#convex.shell.req.juice/set) - Request for setting the current juice value.
    -  [`track`](#convex.shell.req.juice/track) - Request for tracking juice cost of a transaction.
-  [`convex.shell.req.log`](#convex.shell.req.log)  - Requests relating to the CVM log.
    -  [`clear`](#convex.shell.req.log/clear) - Request for clearing the CVM log.
    -  [`get`](#convex.shell.req.log/get) - Request for retrieving the CVM log.
-  [`convex.shell.req.reader`](#convex.shell.req.reader)  - Requests relating to the CVX reader.
    -  [`form+`](#convex.shell.req.reader/form+) - Request for reading cells from a string.
-  [`convex.shell.req.state`](#convex.shell.req.state)  - Requests relating to the global state.
    -  [`genesis`](#convex.shell.req.state/genesis) - Request for generating a genesis state.
    -  [`safe`](#convex.shell.req.state/safe) - Request for executing code in a safe way.
    -  [`switch`](#convex.shell.req.state/switch) - Request for switching a context to the given state.
    -  [`tmp`](#convex.shell.req.state/tmp) - Exactly like [[safe]] but the state is always reverted, even in case of success.
-  [`convex.shell.req.str`](#convex.shell.req.str)  - Requests relating to strings.
    -  [`sort`](#convex.shell.req.str/sort) - Secret request for sorting a vector of strings.
    -  [`stream-in`](#convex.shell.req.str/stream-in) - Request for turning a string into an input stream.
    -  [`stream-out`](#convex.shell.req.str/stream-out) - Request for creating an output stream backed by a string.
    -  [`stream-unwrap`](#convex.shell.req.str/stream-unwrap) - Request for extracting the string inside a [[stream-out]].
-  [`convex.shell.req.stream`](#convex.shell.req.stream)  - Requests relating to IO streams.
    -  [`close`](#convex.shell.req.stream/close) - Request for closing the given stream.
    -  [`flush`](#convex.shell.req.stream/flush) - Request for flushing the requested stream.
    -  [`in+`](#convex.shell.req.stream/in+) - Request for reading all available cells from the given stream and closing it.
    -  [`line`](#convex.shell.req.stream/line) - Request for reading a line from the given stream and parsing it into a list of cells.
    -  [`operation`](#convex.shell.req.stream/operation) - Generic function for carrying out an operation.
    -  [`out`](#convex.shell.req.stream/out) - Request for writing a <code>cell</code> to the given stream.
    -  [`outln`](#convex.shell.req.stream/outln) - Like [[out]] but appends a new line and flushes the stream.
    -  [`stderr`](#convex.shell.req.stream/stderr) - Wraps STDERR to make it accessible to the CVM.
    -  [`stdin`](#convex.shell.req.stream/stdin) - Wraps STDIN to make it accessible to the CVM.
    -  [`stdout`](#convex.shell.req.stream/stdout) - Wraps STDOUT to make it accessible to the CVM.
    -  [`txt-in`](#convex.shell.req.stream/txt-in) - Request for reading everything from the given stream as text.
    -  [`txt-line`](#convex.shell.req.stream/txt-line) - Request for reading a line from the given stream as text.
    -  [`txt-out`](#convex.shell.req.stream/txt-out) - Like [[out]] but if <code>cell</code> is a string, then it is not double-quoted.
    -  [`txt-outln`](#convex.shell.req.stream/txt-outln) - Is to [[outln]] what [[out-txt]] is to [[out]].
-  [`convex.shell.req.sys`](#convex.shell.req.sys)  - Requests relating to basic system utilities.
    -  [`arch`](#convex.shell.req.sys/arch) - Request for returning the chip architecture as a string.
    -  [`cwd`](#convex.shell.req.sys/cwd) - Request for returning the current working directory (where the Shell started).
    -  [`env`](#convex.shell.req.sys/env) - Request for returning the map of process environment variables.
    -  [`env-var`](#convex.shell.req.sys/env-var) - Request for returning the value for a single process environment variable.
    -  [`exit`](#convex.shell.req.sys/exit) - Request for terminating the process.
    -  [`home`](#convex.shell.req.sys/home) - Request for returning the home directory.
    -  [`os`](#convex.shell.req.sys/os) - Request for returning a tuple <code>[OS Version]</code>.
-  [`convex.shell.req.time`](#convex.shell.req.time)  - Requests relating to time.
    -  [`-millis`](#convex.shell.req.time/-millis)
    -  [`advance`](#convex.shell.req.time/advance) - Request for moving forward the CVM timestamp.
    -  [`iso->unix`](#convex.shell.req.time/iso->unix) - Request for converting an ISO 8601 UTC string into a Unix timestamp.
    -  [`nano`](#convex.shell.req.time/nano) - Request for returning the current time according to the JVM high-resolution timer.
    -  [`sleep`](#convex.shell.req.time/sleep) - Request for temporarily blocking execution.
    -  [`unix`](#convex.shell.req.time/unix) - Request for returning the current Unix timestamp of the machine.
    -  [`unix->iso`](#convex.shell.req.time/unix->iso) - Opposite of [[iso->unix]].
-  [`convex.shell.time`](#convex.shell.time)  - Miscellaneous time utilities and conversions.
    -  [`instant->iso`](#convex.shell.time/instant->iso) - Converts an <code>Instant</code> to an ISO 8601 string (UTC).
    -  [`instant->unix`](#convex.shell.time/instant->unix) - Converts an <code>Instant</code> into a Unix timestamp.
    -  [`iso->instant`](#convex.shell.time/iso->instant) - Converts an ISO 8601 string to an <code>Instant</code>.
    -  [`iso->unix`](#convex.shell.time/iso->unix) - Converts an ISO 8601 string (UTC) to a Unix timestamp.
    -  [`nano`](#convex.shell.time/nano) - High-resolution timer.
    -  [`unix`](#convex.shell.time/unix) - Current Unix timestamp in milliseconds.
    -  [`unix->instant`](#convex.shell.time/unix->instant) - Converts a Unix timestamp to an <code>Instant</code>.
    -  [`unix->iso`](#convex.shell.time/unix->iso) - Converts a Unix timestamp to an ISO 8601 string (UTC).

-----
# <a name="convex.shell">convex.shell</a>


CONVEX SHELL

   Convex Virtual Machine extended with side-effects.

   For using as a terminal application, see [`-main`](#convex.shell/-main).

   For using as a library, see [`transact`](#convex.shell/transact).
  
   Assumes knowledge of `:module/cvm`.




## <a name="convex.shell/-main">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell.clj#L138-L152) `-main`</a>
``` clojure

(-main & txt-cell+)
```


Main entry point for using Convex Shell as a terminal application.
  
   Expects cells as text to wrap and execute in a `(do)`.
   See [`transact-main`](#convex.shell/transact-main) for a reusable implementation.
  
   ```clojure
   (-main "(+ 2 2)")
   ```

## <a name="convex.shell/init">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell.clj#L30-L56) `init`</a>
``` clojure

(init)
(init option+)
```


Initializes a genesis context, forking [`convex.shell.ctx/genesis`](#convex.shell.ctx/genesis).
   It is important that each such context is initialized and used in a dedicated
   thread.
  
   Options may be:

   | Key                     | Value                            |
   |-------------------------|----------------------------------|
   | `:convex.shell/invoker` | See [`convex.shell.req/invoker`](#convex.shell.req/invoker) |

## <a name="convex.shell/transact">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell.clj#L60-L80) `transact`</a>
``` clojure

(transact ctx trx)
```


Applies the given transaction (a cell) to the given context.
  
   Context should come from [`init`](#convex.shell/init).
  
   Returns a context with a result or an exception attached.

## <a name="convex.shell/transact-main">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell.clj#L84-L132) `transact-main`</a>
``` clojure

(transact-main ctx txt-cell+)
```


Core implementation of [`-main`](#convex.shell/-main).
  
   Passes the text cells to the `.shell.main` CVX function defined in the core account.
  
   `ctx` should come from [`init`](#convex.shell/init) and will be passed to [`transact`](#convex.shell/transact).

   In case of a result, prints its to STDOUT and terminates with a 0 code.
   In case of an exception, prints it to STDERR and terminates with a non-0 code.

-----
# <a name="convex.shell.ctx">convex.shell.ctx</a>


Preparing the genesis context used by the Shell.
  
   The Shell CVX library is executed in the core account so that all those functions
   are accessible from any account.




## <a name="convex.shell.ctx/genesis">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L40-L70) `genesis`</a>

-----
# <a name="convex.shell.dep">convex.shell.dep</a>


Experimental dependency management framework for Convex Lisp.
  
   In the Shell, see `(?.shell '.dep)`.




## <a name="convex.shell.dep/deploy-actor">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep.clj#L162-L186) `deploy-actor`</a>
``` clojure

(deploy-actor env hash code)
```


Used in [`deploy-fetched`](#convex.shell.dep/deploy-fetched) for deploying a single actor in the Shell.

## <a name="convex.shell.dep/deploy-fetched">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep.clj#L190-L248) `deploy-fetched`</a>
``` clojure

(deploy-fetched env)
```


Deploys actors that have prefetched with [[fetched]].

## <a name="convex.shell.dep/fetch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep.clj#L73-L156) `fetch`</a>
``` clojure

(fetch env)
(fetch env required)
```


Main function for fetching dependencies (Convex Lisp files), which may or may not be
   deployed as actors in a latter step.

## <a name="convex.shell.dep/project">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep.clj#L49-L67) `project`</a>
``` clojure

(project ctx dep dir)
```


Returns a `project.cvx` file where dependencies reside.
  
   Also validates it.

-----
# <a name="convex.shell.dep.fail">convex.shell.dep.fail</a>


Builds on [`convex.shell.flow`](#convex.shell.flow) for returning CVM exceptions relating
   to [`convex.shell.dep`](#convex.shell.dep).




## <a name="convex.shell.dep.fail/rethrow-with-ancestry">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep/fail.clj#L34-L40) `rethrow-with-ancestry`</a>
``` clojure

(rethrow-with-ancestry ctx ex ancestry)
```


## <a name="convex.shell.dep.fail/with-ancestry">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep/fail.clj#L44-L52) `with-ancestry`</a>
``` clojure

(with-ancestry ctx code message ancestry)
```


-----
# <a name="convex.shell.dep.git">convex.shell.dep.git</a>


Git dependencies are a convenient way of exposing Convex Lisp project
   over the Internet.




## <a name="convex.shell.dep.git/fetch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep/git.clj#L183-L212) `fetch`</a>
``` clojure

(fetch env project-child dep-parent actor-sym actor-path)
```


Used in [`convex.shell.dep/fetch`](#convex.shell.dep/fetch) for fetching Git dependencies.

## <a name="convex.shell.dep.git/path-cache-repo">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep/git.clj#L35-L94) `path-cache-repo`</a>
``` clojure

(path-cache-repo dir-project-parent url)
```


## <a name="convex.shell.dep.git/re-scp">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep/git.clj#L22-L23) `re-scp`</a>

## <a name="convex.shell.dep.git/re-url">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep/git.clj#L27-L29) `re-url`</a>

## <a name="convex.shell.dep.git/worktree">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep/git.clj#L98-L177) `worktree`</a>
``` clojure

(worktree env dir-project-parent url sha)
```


Clones a repo and creates a worktree for the desired SHA (if none of this hasn't been
   done already.

-----
# <a name="convex.shell.dep.local">convex.shell.dep.local</a>


A local dependency points to another local directory which contains
   its own `project.cvx`.




## <a name="convex.shell.dep.local/fetch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep/local.clj#L18-L52) `fetch`</a>
``` clojure

(fetch env project-child dep-parent actor-sym actor-path)
```


Used in [`convex.shell.dep/fetch`](#convex.shell.dep/fetch) for fetching local dependencies.

-----
# <a name="convex.shell.dep.relative">convex.shell.dep.relative</a>


"Relative" dependency resolution mechanism.




## <a name="convex.shell.dep.relative/content">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep/relative.clj#L109-L126) `content`</a>
``` clojure

(content env project-child dep-parent actor-sym actor-path)
```


Retrieves the content of a relative actor.

## <a name="convex.shell.dep.relative/fetch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep/relative.clj#L130-L180) `fetch`</a>
``` clojure

(fetch env project-child dep-parent actor-sym actor-path)
```


Used in [`convex.shell.dep/fetch`](#convex.shell.dep/fetch) for fetching relative dependencies.

## <a name="convex.shell.dep.relative/path">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep/relative.clj#L20-L31) `path`</a>
``` clojure

(path project-child dep-parent actor-path)
```


Produces an actual file path form an actor path.

## <a name="convex.shell.dep.relative/read">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep/relative.clj#L35-L71) `read`</a>
``` clojure

(read env path)
```


Reads a Convex Lisp file.

## <a name="convex.shell.dep.relative/validate-required">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/dep/relative.clj#L75-L103) `validate-required`</a>
``` clojure

(validate-required ctx required ancestry)
```


Validates a deploy vector.

-----
# <a name="convex.shell.fail">convex.shell.fail</a>


Helpers for handling Shell failures.




## <a name="convex.shell.fail/mappify-cvm-ex">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/fail.clj#L18-L28) `mappify-cvm-ex`</a>
``` clojure

(mappify-cvm-ex ex)
```


Transforms the given CVM exception into a CVX map.

## <a name="convex.shell.fail/top-exception">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/fail.clj#L32-L50) `top-exception`</a>
``` clojure

(top-exception ctx ex)
```


Called when an unforeseen JVM exception is caught.
   Prints the exception to a tmp EDN file the user can inspect and
   report as this would be almost certainly about an actual bug in
   the Shell.

-----
# <a name="convex.shell.flow">convex.shell.flow</a>


Sometimes, when failing to execute a request for any reason, it is easier
   throwing the context in an exception caught and returned to the user at
   a strategic point.




## <a name="convex.shell.flow/fail">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/flow.clj#L21-L36) `fail`</a>
``` clojure

(fail ctx cvm-ex)
(fail ctx code message)
```


Attaches a CVM exception to the context and forwards it to [`return`](#convex.shell.flow/return).

## <a name="convex.shell.flow/return">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/flow.clj#L40-L47) `return`</a>
``` clojure

(return ctx)
```


Throws the context in an exception that can be catched using [`safe`](#convex.shell.flow/safe).

## <a name="convex.shell.flow/safe">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/flow.clj#L51-L59) `safe`</a>
``` clojure

(safe *d)
```


-----
# <a name="convex.shell.io">convex.shell.io</a>


Basic IO utilities and STDIO.




## <a name="convex.shell.io/file-in">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L75-L81) `file-in`</a>
``` clojure

(file-in path)
```


Opens an input text stream for the file located under `path`.

## <a name="convex.shell.io/file-out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L85-L104) `file-out`</a>
``` clojure

(file-out path)
(file-out path append?)
```


Opens an output text stream for the file located under `path`.
   By default, overwrites any existing file. Writes will be appended to the end
   if `append?` is true.

## <a name="convex.shell.io/flush">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L110-L117) `flush`</a>
``` clojure

(flush out)
```


Flushes the given `out`.

## <a name="convex.shell.io/newline">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L121-L128) `newline`</a>
``` clojure

(newline out)
```


Writes a new line to the given text output stream.

## <a name="convex.shell.io/stderr">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L25-L29) `stderr`</a>

File descriptor for STDERR.

## <a name="convex.shell.io/stderr-txt">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L33-L37) `stderr-txt`</a>

Text stream for STDERR.

## <a name="convex.shell.io/stdin">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L41-L45) `stdin`</a>

File descriptor for STDIN.

## <a name="convex.shell.io/stdin-txt">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L49-L53) `stdin-txt`</a>

Text stream for STDIN.

## <a name="convex.shell.io/stdout">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L57-L61) `stdout`</a>

File descriptor for STDOUT.

## <a name="convex.shell.io/stdout-txt">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L65-L69) `stdout-txt`</a>

Text stream for STDOUT.

-----
# <a name="convex.shell.project">convex.shell.project</a>


Convex Lisp projects may have a `project.cvx` file which contains useful
   data for the Shell.
  
   For the time being, this is only used for dependencies (see [`convex.shell.req`](#convex.shell.req)).




## <a name="convex.shell.project/dep+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/project.clj#L64-L143) `dep+`</a>
``` clojure

(dep+ project fail)
```


Validates and returns `:deps` found in a `project.cvx`.

## <a name="convex.shell.project/read">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/project.clj#L23-L58) `read`</a>
``` clojure

(read dir fail)
```


Reads the `project.cvx` file found in `dir`.

-----
# <a name="convex.shell.req">convex.shell.req</a>


All extra features offered by the Shell, over the Convex Virtual Machine, have
   a single entry point: the `.shell.invoke` function injected in the core account.

   AKA the [`invoker`](#convex.shell.req/invoker).

   The various side effects, implemented in Clojure and made available through the
   [`invoker`](#convex.shell.req/invoker), are known as "requests".




## <a name="convex.shell.req/core">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req.clj#L99-L165) `core`</a>

All core requests.
  
   A map of CVX symbols pointing to a Clojure implementations.

## <a name="convex.shell.req/ex-rethrow">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req.clj#L48-L93) `ex-rethrow`</a>
``` clojure

(ex-rethrow ctx [ex-map])
```


Request for rethrowing an exception captured in the Shell.

## <a name="convex.shell.req/invoker">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req.clj#L212-L267) `invoker`</a>
``` clojure

(invoker)
(invoker dispatch-table)
```


Returns an [`invoker`](#convex.shell.req/invoker).

   Disguised as a CVM core function, the [`invoker`](#convex.shell.req/invoker) is a variadic CVM function where
   the first argument is a CVX symbol resolving to a Clojure implementation that will
   produce the desired request, such as opening a file output stream.

   The symbol is resolved using a "dispatch table".
   Defaults to [`core`](#convex.shell.req/core) but one may want to extend it in order to provide additional
   features.
  
   It will be injected in the core account of the context used by the Shell, under
   `.shell.invoke`.

-----
# <a name="convex.shell.req.account">convex.shell.req.account</a>


Requests relating to accounts.




## <a name="convex.shell.req.account/switch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/account.clj#L19-L42) `switch`</a>
``` clojure

(switch ctx [address])
```


Requests for switching the context to another address.
  
   Returns a context which has lost local bindings and such useful information.
   However, this is fine when this request is called via `.account.switch`, a real
   CVX function which will automatically restore all that. Things do go wrong if the
   user calls `(.shell.invoke 'account.switch ...)` directly (but shouldn't have to do
   that.

-----
# <a name="convex.shell.req.bench">convex.shell.req.bench</a>


Requests related to benchmarking.




## <a name="convex.shell.req.bench/trx">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/bench.clj#L15-L28) `trx`</a>
``` clojure

(trx ctx [trx])
```


Request for benchmarking a single transaction using Criterium.

-----
# <a name="convex.shell.req.db">convex.shell.req.db</a>


Requests relating to Etch.




## <a name="convex.shell.req.db/flush">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L57-L66) `flush`</a>
``` clojure

(flush ctx _arg+)
```


Request for flushing Etch.

## <a name="convex.shell.req.db/open">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L70-L112) `open`</a>
``` clojure

(open ctx [path])
```


Request for opening an Etch instance.
  
   Only one instance can be open per Shell, so that the user cannot possible
   mingle cells coming from different instances.
   Idempotent nonetheless if the user provides the same path.

## <a name="convex.shell.req.db/path">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L116-L124) `path`</a>
``` clojure

(path ctx _arg+)
```


Request for getting the path of the currently open instance (or nil).

## <a name="convex.shell.req.db/read">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L128-L142) `read`</a>
``` clojure

(read ctx [hash])
```


Request for reading a cell by hash.

## <a name="convex.shell.req.db/root-read">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L146-L154) `root-read`</a>
``` clojure

(root-read ctx _arg+)
```


Request for reading from the root.

## <a name="convex.shell.req.db/root-write">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L158-L166) `root-write`</a>
``` clojure

(root-write ctx [cell])
```


Request for writing to the root.

## <a name="convex.shell.req.db/write">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L170-L178) `write`</a>
``` clojure

(write ctx [cell])
```


Request for writing a cell.

-----
# <a name="convex.shell.req.dep">convex.shell.req.dep</a>


Requests for the experimental dependency management framework.
  
   See [`convex.shell.dep`](#convex.shell.dep).




## <a name="convex.shell.req.dep/deploy">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/dep.clj#L18-L31) `deploy`</a>
``` clojure

(deploy ctx [required])
```


Request for deploying a deploy vector.

## <a name="convex.shell.req.dep/fetch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/dep.clj#L35-L52) `fetch`</a>
``` clojure

(fetch ctx [required])
```


Request for fetching required dependencies given a deploy vector.
   Does not execute nor deploy anything in the Shell.

## <a name="convex.shell.req.dep/read">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/dep.clj#L56-L70) `read`</a>
``` clojure

(read ctx [required])
```


Request for reading CVX files resolved from a deploy vector.

-----
# <a name="convex.shell.req.dev">convex.shell.req.dev</a>


Requests only used for dev purposes.




## <a name="convex.shell.req.dev/fatal">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/dev.clj#L11-L18) `fatal`</a>
``` clojure

(fatal _ctx [message])
```


Request for throwing a JVM exception, which should result in a fatal
   error in the Shell.

-----
# <a name="convex.shell.req.file">convex.shell.req.file</a>


Requests relating to file utils.
  
   For the time being, only about opening streams. All other utilities are
   written in Convex Lisp.




## <a name="convex.shell.req.file/stream-in">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/file.clj#L49-L59) `stream-in`</a>
``` clojure

(stream-in ctx [id path])
```


Request for opening an input stream for file under `path`.

## <a name="convex.shell.req.file/stream-out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/file.clj#L63-L75) `stream-out`</a>
``` clojure

(stream-out ctx [id path append?])
```


Request for opening an output stream for file under `path`.

-----
# <a name="convex.shell.req.fs">convex.shell.req.fs</a>


Requests relating to filesystem utilities.




## <a name="convex.shell.req.fs/copy">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/fs.clj#L24-L69) `copy`</a>
``` clojure

(copy ctx [source destination])
```


Request for copying files and directories like Unix's `cp`.

## <a name="convex.shell.req.fs/delete">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/fs.clj#L73-L104) `delete`</a>
``` clojure

(delete ctx [path])
```


Request for deleting a file or an empty directory.

## <a name="convex.shell.req.fs/dir?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/fs.clj#L108-L119) `dir?`</a>
``` clojure

(dir? ctx [path])
```


Request returning `true` if `path` is an actual directory.

## <a name="convex.shell.req.fs/exists?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/fs.clj#L123-L143) `exists?`</a>
``` clojure

(exists? ctx [path])
```


Request returning `true` if `path` exists.

## <a name="convex.shell.req.fs/file?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/fs.clj#L147-L158) `file?`</a>
``` clojure

(file? ctx [path])
```


Request returning `true` if `file` is an actual, regular file.

## <a name="convex.shell.req.fs/resolve">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/fs.clj#L162-L178) `resolve`</a>
``` clojure

(resolve ctx [path])
```


Request for resolving a filename to a canonical form.

## <a name="convex.shell.req.fs/size">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/fs.clj#L182-L207) `size`</a>
``` clojure

(size ctx [path])
```


Request for returning a filesize in bytes.

## <a name="convex.shell.req.fs/tmp">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/fs.clj#L211-L236) `tmp`</a>
``` clojure

(tmp ctx [prefix suffix])
```


Request for creating a temporary file.

## <a name="convex.shell.req.fs/tmp-dir">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/fs.clj#L240-L260) `tmp-dir`</a>
``` clojure

(tmp-dir ctx [prefix])
```


Request for creating a temporary directory.

-----
# <a name="convex.shell.req.juice">convex.shell.req.juice</a>


Requests relating to juice.




## <a name="convex.shell.req.juice/set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/juice.clj#L17-L33) `set`</a>
``` clojure

(set ctx [n-unit])
```


Request for setting the current juice value.

## <a name="convex.shell.req.juice/track">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/juice.clj#L37-L52) `track`</a>
``` clojure

(track ctx [trx])
```


Request for tracking juice cost of a transaction.
  
   See `.juice.track`.

-----
# <a name="convex.shell.req.log">convex.shell.req.log</a>


Requests relating to the CVM log.




## <a name="convex.shell.req.log/clear">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/log.clj#L14-L22) `clear`</a>
``` clojure

(clear ctx _arg+)
```


Request for clearing the CVM log.

## <a name="convex.shell.req.log/get">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/log.clj#L26-L33) `get`</a>
``` clojure

(get ctx _arg+)
```


Request for retrieving the CVM log.

-----
# <a name="convex.shell.req.reader">convex.shell.req.reader</a>


Requests relating to the CVX reader.




## <a name="convex.shell.req.reader/form+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/reader.clj#L17-L41) `form+`</a>
``` clojure

(form+ ctx [src])
```


Request for reading cells from a string.

-----
# <a name="convex.shell.req.state">convex.shell.req.state</a>


Requests relating to the global state.




## <a name="convex.shell.req.state/genesis">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/state.clj#L46-L78) `genesis`</a>
``` clojure

(genesis ctx [key+])
```


Request for generating a genesis state.

## <a name="convex.shell.req.state/safe">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/state.clj#L82-L93) `safe`</a>
``` clojure

(safe ctx [f])
```


Request for executing code in a safe way.
  
   In case of an exception, state is reverted.

## <a name="convex.shell.req.state/switch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/state.clj#L97-L126) `switch`</a>
``` clojure

(switch ctx [address state])
```


Request for switching a context to the given state.

## <a name="convex.shell.req.state/tmp">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/state.clj#L130-L139) `tmp`</a>
``` clojure

(tmp ctx [f])
```


Exactly like [`safe`](#convex.shell.req.state/safe) but the state is always reverted, even in case of success.

-----
# <a name="convex.shell.req.str">convex.shell.req.str</a>


Requests relating to strings.




## <a name="convex.shell.req.str/sort">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/str.clj#L20-L34) `sort`</a>
``` clojure

(sort ctx [str+])
```


Secret request for sorting a vector of strings.

## <a name="convex.shell.req.str/stream-in">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/str.clj#L38-L56) `stream-in`</a>
``` clojure

(stream-in ctx [id string])
```


Request for turning a string into an input stream.

## <a name="convex.shell.req.str/stream-out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/str.clj#L60-L70) `stream-out`</a>
``` clojure

(stream-out ctx [id])
```


Request for creating an output stream backed by a string.

## <a name="convex.shell.req.str/stream-unwrap">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/str.clj#L73-L84) `stream-unwrap`</a>
``` clojure

(stream-unwrap ctx [handle])
```


Request for extracting the string inside a [`stream-out`](#convex.shell.req.str/stream-out).

-----
# <a name="convex.shell.req.stream">convex.shell.req.stream</a>


Requests relating to IO streams.




## <a name="convex.shell.req.stream/close">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L155-L177) `close`</a>
``` clojure

(close ctx arg+)
(close ctx [handle] result)
```


Request for closing the given stream.

   A result to propagate may be provided.

## <a name="convex.shell.req.stream/flush">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L181-L193) `flush`</a>
``` clojure

(flush ctx [handle])
```


Request for flushing the requested stream.

## <a name="convex.shell.req.stream/in+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L197-L208) `in+`</a>
``` clojure

(in+ ctx [handle])
```


Request for reading all available cells from the given stream and closing it.

## <a name="convex.shell.req.stream/line">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L212-L224) `line`</a>
``` clojure

(line ctx [handle])
```


Request for reading a line from the given stream and parsing it into a list of cells.

## <a name="convex.shell.req.stream/operation">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L105-L149) `operation`</a>
``` clojure

(operation ctx handle op+ f)
```


Generic function for carrying out an operation.

   Handles failure.

## <a name="convex.shell.req.stream/out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L246-L255) `out`</a>
``` clojure

(out ctx [handle cell])
```


Request for writing a `cell` to the given stream.

## <a name="convex.shell.req.stream/outln">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L280-L289) `outln`</a>
``` clojure

(outln env [handle cell])
```


Like [`out`](#convex.shell.req.stream/out) but appends a new line and flushes the stream.

## <a name="convex.shell.req.stream/stderr">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L26-L30) `stderr`</a>

Wraps STDERR to make it accessible to the CVM.

## <a name="convex.shell.req.stream/stdin">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L34-L38) `stdin`</a>

Wraps STDIN to make it accessible to the CVM.

## <a name="convex.shell.req.stream/stdout">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L42-L46) `stdout`</a>

Wraps STDOUT to make it accessible to the CVM.

## <a name="convex.shell.req.stream/txt-in">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L293-L309) `txt-in`</a>
``` clojure

(txt-in ctx [handle])
```


Request for reading everything from the given stream as text.

## <a name="convex.shell.req.stream/txt-line">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L313-L326) `txt-line`</a>
``` clojure

(txt-line ctx [handle])
```


Request for reading a line from the given stream as text.

## <a name="convex.shell.req.stream/txt-out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L330-L339) `txt-out`</a>
``` clojure

(txt-out ctx [handle cell])
```


Like [`out`](#convex.shell.req.stream/out) but if `cell` is a string, then it is not double-quoted.

## <a name="convex.shell.req.stream/txt-outln">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L343-L352) `txt-outln`</a>
``` clojure

(txt-outln ctx [handle cell])
```


Is to [`outln`](#convex.shell.req.stream/outln) what [[out-txt]] is to [`out`](#convex.shell.req.stream/out).

-----
# <a name="convex.shell.req.sys">convex.shell.req.sys</a>


Requests relating to basic system utilities.




## <a name="convex.shell.req.sys/arch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L16-L23) `arch`</a>
``` clojure

(arch ctx _arg+)
```


Request for returning the chip architecture as a string.

## <a name="convex.shell.req.sys/cwd">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L27-L34) `cwd`</a>
``` clojure

(cwd ctx _arg+)
```


Request for returning the current working directory (where the Shell started).

## <a name="convex.shell.req.sys/env">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L38-L48) `env`</a>
``` clojure

(env ctx _arg+)
```


Request for returning the map of process environment variables.

## <a name="convex.shell.req.sys/env-var">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L52-L64) `env-var`</a>
``` clojure

(env-var ctx [env-var])
```


Request for returning the value for a single process environment variable.

## <a name="convex.shell.req.sys/exit">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L68-L86) `exit`</a>
``` clojure

(exit ctx [code])
```


Request for terminating the process.

## <a name="convex.shell.req.sys/home">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L90-L97) `home`</a>
``` clojure

(home ctx _arg+)
```


Request for returning the home directory.

## <a name="convex.shell.req.sys/os">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L101-L109) `os`</a>
``` clojure

(os ctx _arg+)
```


Request for returning a tuple `[OS Version]`.

-----
# <a name="convex.shell.req.time">convex.shell.req.time</a>


Requests relating to time.




## <a name="convex.shell.req.time/-millis">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/time.clj#L17-L35) `-millis`</a>
``` clojure

(-millis ctx millis)
```


## <a name="convex.shell.req.time/advance">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/time.clj#L41-L57) `advance`</a>
``` clojure

(advance ctx [millis])
```


Request for moving forward the CVM timestamp.

## <a name="convex.shell.req.time/iso->unix">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/time.clj#L88-L102) `iso->unix`</a>
``` clojure

(iso->unix ctx [iso-string])
```


Request for converting an ISO 8601 UTC string into a Unix timestamp.

## <a name="convex.shell.req.time/nano">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/time.clj#L63-L71) `nano`</a>
``` clojure

(nano ctx _arg+)
```


Request for returning the current time according to the JVM high-resolution
   timer.

## <a name="convex.shell.req.time/sleep">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/time.clj#L125-L138) `sleep`</a>
``` clojure

(sleep ctx [millis])
```


Request for temporarily blocking execution.

## <a name="convex.shell.req.time/unix">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/time.clj#L75-L82) `unix`</a>
``` clojure

(unix ctx _arg+)
```


Request for returning the current Unix timestamp of the machine.

## <a name="convex.shell.req.time/unix->iso">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/time.clj#L105-L119) `unix->iso`</a>
``` clojure

(unix->iso ctx [time-unix])
```


Opposite of [`iso->unix`](#convex.shell.req.time/iso->unix).

-----
# <a name="convex.shell.time">convex.shell.time</a>


Miscellaneous time utilities and conversions.




## <a name="convex.shell.time/instant->iso">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/time.clj#L50-L57) `instant->iso`</a>
``` clojure

(instant->iso instant)
```


Converts an `Instant` to an ISO 8601 string (UTC).

## <a name="convex.shell.time/instant->unix">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/time.clj#L81-L87) `instant->unix`</a>
``` clojure

(instant->unix instant)
```


Converts an `Instant` into a Unix timestamp.

## <a name="convex.shell.time/iso->instant">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/time.clj#L61-L75) `iso->instant`</a>
``` clojure

(iso->instant iso)
```


Converts an ISO 8601 string to an `Instant`.
    
     Returns nil if the string cannot be parsed.

## <a name="convex.shell.time/iso->unix">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/time.clj#L104-L112) `iso->unix`</a>
``` clojure

(iso->unix iso)
```


Converts an ISO 8601 string (UTC) to a Unix timestamp.

## <a name="convex.shell.time/nano">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/time.clj#L21-L27) `nano`</a>
``` clojure

(nano)
```


High-resolution timer.

## <a name="convex.shell.time/unix">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/time.clj#L31-L37) `unix`</a>
``` clojure

(unix)
```


Current Unix timestamp in milliseconds.

## <a name="convex.shell.time/unix->instant">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/time.clj#L90-L98) `unix->instant`</a>
``` clojure

(unix->instant unix)
```


Converts a Unix timestamp to an `Instant`.

## <a name="convex.shell.time/unix->iso">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/time.clj#L116-L124) `unix->iso`</a>
``` clojure

(unix->iso unix)
```


Converts a Unix timestamp to an ISO 8601 string (UTC).
