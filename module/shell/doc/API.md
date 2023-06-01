# Table of contents
-  [`convex.shell`](#convex.shell)  - CONVEX SHELL Convex Virtual Machine extended with side-effects.
    -  [`-main`](#convex.shell/-main) - Main entry point for using Convex Shell as a terminal application.
    -  [`init`](#convex.shell/init) - Initializes a genesis context, forking [[convex.shell.ctx/genesis]].
    -  [`transact`](#convex.shell/transact) - Applies the given transaction (a cell) to the given context.
    -  [`transact-main`](#convex.shell/transact-main) - Core implementation of [[-main]].
-  [`convex.shell.async`](#convex.shell.async)  - Helpers for async requests.
    -  [`failure`](#convex.shell.async/failure) - Indicating failure from an async value.
    -  [`return`](#convex.shell.async/return) - Attaches an async value to <code>ctx</code>.
    -  [`success`](#convex.shell.async/success) - Indicating success from an async value.
-  [`convex.shell.ctx`](#convex.shell.ctx)  - Preparing the genesis context used by the Shell.
    -  [`core-env`](#convex.shell.ctx/core-env) - Genesis Core environment.
    -  [`core-meta`](#convex.shell.ctx/core-meta) - Genesis Core metadata.
    -  [`genesis`](#convex.shell.ctx/genesis) - Genesis context prepared for the Shell.
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
-  [`convex.shell.log`](#convex.shell.log)  - Handles logging done via Timbre.
    -  [`out`](#convex.shell.log/out) - Returns the stream currently used for logging.
    -  [`out-set`](#convex.shell.log/out-set) - Sets [[out]] to the given <code>stream</code>.
    -  [`throwable`](#convex.shell.log/throwable) - Turns a <code>Throwable</code> into a cell.
-  [`convex.shell.project`](#convex.shell.project)  - Convex Lisp projects may have a <code>project.cvx</code> file which contains useful data for the Shell.
    -  [`dep+`](#convex.shell.project/dep+) - Validates and returns <code>:deps</code> found in a <code>project.cvx</code>.
    -  [`read`](#convex.shell.project/read) - Reads the <code>project.cvx</code> file found in <code>dir</code>.
-  [`convex.shell.req`](#convex.shell.req)  - All extra features offered by the Shell, over the Convex Virtual Machine, have a single entry point: the <code>.shell.invoke</code> function injected in the core account.
    -  [`core`](#convex.shell.req/core) - All core requests.
    -  [`ex-rethrow`](#convex.shell.req/ex-rethrow) - Request for rethrowing an exception captured in the Shell.
    -  [`invoker`](#convex.shell.req/invoker) - Returns an [[invoker]].
-  [`convex.shell.req.account`](#convex.shell.req.account)  - Requests relating to accounts.
    -  [`switch`](#convex.shell.req.account/switch) - Requests for switching the context to another address.
-  [`convex.shell.req.async`](#convex.shell.req.async)  - Requests for async programming.
    -  [`do-`](#convex.shell.req.async/do-) - Request for executing a CVX function in a forked context on a separate thread.
    -  [`take`](#convex.shell.req.async/take) - Request for awaiting a promise.
    -  [`take-timeout`](#convex.shell.req.async/take-timeout) - Like [[take]] but with a timeout.
-  [`convex.shell.req.bench`](#convex.shell.req.bench)  - Requests related to benchmarking.
    -  [`eval`](#convex.shell.req.bench/eval) - Request for benchmarking some code using Criterium.
    -  [`trx`](#convex.shell.req.bench/trx) - Request for benchmarking a transaction.
    -  [`trx-gen`](#convex.shell.req.bench/trx-gen) - Request for benchmarking generated transaction (without throwing away the state).
-  [`convex.shell.req.cell`](#convex.shell.req.cell)  - More advanced requestes relating to cells.
    -  [`compile`](#convex.shell.req.cell/compile) - Request for pre-compiling a <code>cell</code> for the given <code>address</code> which might not exist in the Shell.
    -  [`ref-stat`](#convex.shell.req.cell/ref-stat) - Requests for providing stats about the <code>cell</code>'s refs.
    -  [`size`](#convex.shell.req.cell/size) - Requests for getting the full memory size of a cell.
    -  [`str`](#convex.shell.req.cell/str) - Request for printing a cell to a string with a user given size limit instead of the default one.
-  [`convex.shell.req.client`](#convex.shell.req.client)  - Requests relating to the binary client.
    -  [`close`](#convex.shell.req.client/close) - Request for closing a client.
    -  [`connect`](#convex.shell.req.client/connect) - Request for connecting to a peer.
    -  [`peer-endpoint`](#convex.shell.req.client/peer-endpoint) - Request for retrieving the endpoint the client is connected to.
    -  [`peer-status`](#convex.shell.req.client/peer-status) - Request for retrieving the current peer status.
    -  [`query`](#convex.shell.req.client/query) - Request for issuing a query.
    -  [`query-state`](#convex.shell.req.client/query-state) - Request for fetching the peer's <code>State</code>.
    -  [`resolve`](#convex.shell.req.client/resolve) - Request for resolving a hash to a cell.
    -  [`sequence`](#convex.shell.req.client/sequence) - Request for retrieving the next sequence ID.
    -  [`transact`](#convex.shell.req.client/transact) - Request for signing and issuing a transaction.
    -  [`transact-signed`](#convex.shell.req.client/transact-signed) - Request for issuing a signed transaction.
-  [`convex.shell.req.cvmlog`](#convex.shell.req.cvmlog)  - Requests relating to the CVM log.
    -  [`clear`](#convex.shell.req.cvmlog/clear) - Request for clearing the CVM log.
    -  [`get`](#convex.shell.req.cvmlog/get) - Request for retrieving the CVM log.
-  [`convex.shell.req.db`](#convex.shell.req.db)  - Requests relating to Etch.
    -  [`allow-open?`](#convex.shell.req.db/allow-open?)
    -  [`flush`](#convex.shell.req.db/flush) - Request for flushing Etch.
    -  [`open`](#convex.shell.req.db/open) - Request for opening an Etch instance.
    -  [`path`](#convex.shell.req.db/path) - Request for getting the path of the currently open instance (or nil).
    -  [`read`](#convex.shell.req.db/read) - Request for reading a cell by hash.
    -  [`root-read`](#convex.shell.req.db/root-read) - Request for reading from the root.
    -  [`root-write`](#convex.shell.req.db/root-write) - Request for writing to the root.
    -  [`size`](#convex.shell.req.db/size) - Request for returning the precise data size of the Etch instance.
    -  [`write`](#convex.shell.req.db/write) - Request for writing a cell.
-  [`convex.shell.req.dep`](#convex.shell.req.dep)  - Requests for the experimental dependency management framework.
    -  [`deploy`](#convex.shell.req.dep/deploy) - Request for deploying a deploy vector.
    -  [`fetch`](#convex.shell.req.dep/fetch) - Request for fetching required dependencies given a deploy vector.
    -  [`read`](#convex.shell.req.dep/read) - Request for reading CVX files resolved from a deploy vector.
-  [`convex.shell.req.dev`](#convex.shell.req.dev)  - Requests only used for dev purposes.
    -  [`fatal`](#convex.shell.req.dev/fatal) - Request for throwing a JVM exception, which should result in a fatal error in the Shell.
-  [`convex.shell.req.file`](#convex.shell.req.file)  - Requests relating to file utils.
    -  [`lock`](#convex.shell.req.file/lock) - Request for getting an exclusive file lock.
    -  [`lock-release`](#convex.shell.req.file/lock-release) - Request for releasing an exclusive file lock.
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
-  [`convex.shell.req.gen`](#convex.shell.req.gen) 
    -  [`-ensure-bound`](#convex.shell.req.gen/-ensure-bound)
    -  [`-ensure-pos-num`](#convex.shell.req.gen/-ensure-pos-num)
    -  [`always`](#convex.shell.req.gen/always)
    -  [`bind`](#convex.shell.req.gen/bind)
    -  [`blob`](#convex.shell.req.gen/blob)
    -  [`blob-bounded`](#convex.shell.req.gen/blob-bounded)
    -  [`blob-fixed`](#convex.shell.req.gen/blob-fixed)
    -  [`blob-map`](#convex.shell.req.gen/blob-map)
    -  [`blob-map-bounded`](#convex.shell.req.gen/blob-map-bounded)
    -  [`blob-map-fixed`](#convex.shell.req.gen/blob-map-fixed)
    -  [`check`](#convex.shell.req.gen/check)
    -  [`do-gen`](#convex.shell.req.gen/do-gen)
    -  [`do-gen+`](#convex.shell.req.gen/do-gen+)
    -  [`double-bounded`](#convex.shell.req.gen/double-bounded)
    -  [`fmap`](#convex.shell.req.gen/fmap)
    -  [`freq`](#convex.shell.req.gen/freq)
    -  [`gen`](#convex.shell.req.gen/gen)
    -  [`hex-string`](#convex.shell.req.gen/hex-string)
    -  [`hex-string-bounded`](#convex.shell.req.gen/hex-string-bounded)
    -  [`hex-string-fixed`](#convex.shell.req.gen/hex-string-fixed)
    -  [`list`](#convex.shell.req.gen/list)
    -  [`list-bounded`](#convex.shell.req.gen/list-bounded)
    -  [`list-fixed`](#convex.shell.req.gen/list-fixed)
    -  [`long-bounded`](#convex.shell.req.gen/long-bounded)
    -  [`long-uniform`](#convex.shell.req.gen/long-uniform)
    -  [`map`](#convex.shell.req.gen/map)
    -  [`map-bounded`](#convex.shell.req.gen/map-bounded)
    -  [`map-fixed`](#convex.shell.req.gen/map-fixed)
    -  [`or-`](#convex.shell.req.gen/or-)
    -  [`pick`](#convex.shell.req.gen/pick)
    -  [`quoted`](#convex.shell.req.gen/quoted)
    -  [`set`](#convex.shell.req.gen/set)
    -  [`set-bounded`](#convex.shell.req.gen/set-bounded)
    -  [`set-fixed`](#convex.shell.req.gen/set-fixed)
    -  [`string`](#convex.shell.req.gen/string)
    -  [`string-alphanum`](#convex.shell.req.gen/string-alphanum)
    -  [`string-alphanum-bounded`](#convex.shell.req.gen/string-alphanum-bounded)
    -  [`string-alphanum-fixed`](#convex.shell.req.gen/string-alphanum-fixed)
    -  [`string-bounded`](#convex.shell.req.gen/string-bounded)
    -  [`string-fixed`](#convex.shell.req.gen/string-fixed)
    -  [`such-that`](#convex.shell.req.gen/such-that)
    -  [`syntax`](#convex.shell.req.gen/syntax)
    -  [`syntax-with-meta`](#convex.shell.req.gen/syntax-with-meta)
    -  [`syntax-with-value`](#convex.shell.req.gen/syntax-with-value)
    -  [`tuple`](#convex.shell.req.gen/tuple)
    -  [`vector`](#convex.shell.req.gen/vector)
    -  [`vector-bounded`](#convex.shell.req.gen/vector-bounded)
    -  [`vector-fixed`](#convex.shell.req.gen/vector-fixed)
-  [`convex.shell.req.gen.static`](#convex.shell.req.gen.static) 
    -  [`address`](#convex.shell.req.gen.static/address)
    -  [`any`](#convex.shell.req.gen.static/any)
    -  [`any-coll`](#convex.shell.req.gen.static/any-coll)
    -  [`any-list`](#convex.shell.req.gen.static/any-list)
    -  [`any-map`](#convex.shell.req.gen.static/any-map)
    -  [`any-set`](#convex.shell.req.gen.static/any-set)
    -  [`any-vector`](#convex.shell.req.gen.static/any-vector)
    -  [`bigint`](#convex.shell.req.gen.static/bigint)
    -  [`blob-32`](#convex.shell.req.gen.static/blob-32)
    -  [`boolean`](#convex.shell.req.gen.static/boolean)
    -  [`char`](#convex.shell.req.gen.static/char)
    -  [`char-alphanum`](#convex.shell.req.gen.static/char-alphanum)
    -  [`double`](#convex.shell.req.gen.static/double)
    -  [`falsy`](#convex.shell.req.gen.static/falsy)
    -  [`keyword`](#convex.shell.req.gen.static/keyword)
    -  [`long`](#convex.shell.req.gen.static/long)
    -  [`nothing`](#convex.shell.req.gen.static/nothing)
    -  [`number`](#convex.shell.req.gen.static/number)
    -  [`scalar`](#convex.shell.req.gen.static/scalar)
    -  [`symbol`](#convex.shell.req.gen.static/symbol)
    -  [`truthy`](#convex.shell.req.gen.static/truthy)
-  [`convex.shell.req.juice`](#convex.shell.req.juice)  - Requests relating to juice.
    -  [`set`](#convex.shell.req.juice/set) - Request for setting the current juice value.
    -  [`track`](#convex.shell.req.juice/track) - Request for tracking juice cost of a transaction.
-  [`convex.shell.req.kp`](#convex.shell.req.kp)  - Requests relating to key pairs.
    -  [`create`](#convex.shell.req.kp/create) - Request for creating a key pair from a random seed.
    -  [`create-from-seed`](#convex.shell.req.kp/create-from-seed) - Request for creating a key pair from a given seed.
    -  [`do-kp`](#convex.shell.req.kp/do-kp) - Unwraps a key pair from a resource.
    -  [`pubkey`](#convex.shell.req.kp/pubkey) - Request for retrieving the public key of the given key pair.
    -  [`seed`](#convex.shell.req.kp/seed) - Request for retrieving the seed of the given key pair.
    -  [`sign`](#convex.shell.req.kp/sign) - Request for signing a <code>cell</code>.
    -  [`verify`](#convex.shell.req.kp/verify) - Request for verifying a signature.
-  [`convex.shell.req.log`](#convex.shell.req.log) 
    -  [`level`](#convex.shell.req.log/level)
    -  [`level-set`](#convex.shell.req.log/level-set)
    -  [`log`](#convex.shell.req.log/log)
    -  [`out`](#convex.shell.req.log/out)
    -  [`out-set`](#convex.shell.req.log/out-set)
-  [`convex.shell.req.peer`](#convex.shell.req.peer) 
    -  [`-do-peer`](#convex.shell.req.peer/-do-peer)
    -  [`connection+`](#convex.shell.req.peer/connection+)
    -  [`controller`](#convex.shell.req.peer/controller)
    -  [`data`](#convex.shell.req.peer/data)
    -  [`endpoint`](#convex.shell.req.peer/endpoint)
    -  [`init-db`](#convex.shell.req.peer/init-db)
    -  [`init-state`](#convex.shell.req.peer/init-state)
    -  [`init-sync`](#convex.shell.req.peer/init-sync)
    -  [`n-belief-received`](#convex.shell.req.peer/n-belief-received)
    -  [`n-belief-sent`](#convex.shell.req.peer/n-belief-sent)
    -  [`persist`](#convex.shell.req.peer/persist)
    -  [`pubkey`](#convex.shell.req.peer/pubkey)
    -  [`start`](#convex.shell.req.peer/start)
    -  [`state`](#convex.shell.req.peer/state)
    -  [`status`](#convex.shell.req.peer/status)
    -  [`stop`](#convex.shell.req.peer/stop)
-  [`convex.shell.req.pfx`](#convex.shell.req.pfx)  - Requests relating to PFX stores for key pairs.
    -  [`-ensure-alias`](#convex.shell.req.pfx/-ensure-alias)
    -  [`-ensure-passphrase`](#convex.shell.req.pfx/-ensure-passphrase)
    -  [`-ensure-path`](#convex.shell.req.pfx/-ensure-path)
    -  [`alias+`](#convex.shell.req.pfx/alias+) - Request for getting the set of available alias in the given store.
    -  [`create`](#convex.shell.req.pfx/create) - Request for creating a new store.
    -  [`do-store`](#convex.shell.req.pfx/do-store) - Unwraps a PFX store from a resource.
    -  [`kp-get`](#convex.shell.req.pfx/kp-get) - Request for retrieving a key pair from a store.
    -  [`kp-rm`](#convex.shell.req.pfx/kp-rm) - Request for removing a key pair from a store.
    -  [`kp-set`](#convex.shell.req.pfx/kp-set) - Request for adding a key pair to a store.
    -  [`load`](#convex.shell.req.pfx/load) - Request for loading an existing store from a file.
    -  [`save`](#convex.shell.req.pfx/save) - Request for saving a store to a file.
-  [`convex.shell.req.process`](#convex.shell.req.process) 
    -  [`-do-stream`](#convex.shell.req.process/-do-stream)
    -  [`kill`](#convex.shell.req.process/kill)
    -  [`run`](#convex.shell.req.process/run)
-  [`convex.shell.req.reader`](#convex.shell.req.reader)  - Requests relating to the CVX reader.
    -  [`form+`](#convex.shell.req.reader/form+) - Request for reading cells from a string.
-  [`convex.shell.req.state`](#convex.shell.req.state)  - Requests relating to the global state.
    -  [`core-vanilla`](#convex.shell.req.state/core-vanilla) - Request for restoring genesis env and metadata in the core account in the given <code>state</code>.
    -  [`do-`](#convex.shell.req.state/do-) - Request similar to [[safe]] but returns only a boolean (<code>false</code> in case of an exception).
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
    -  [`stderr`](#convex.shell.req.stream/stderr) - Request for returning STDERR.
    -  [`stdin`](#convex.shell.req.stream/stdin) - Request for returning STDIN.
    -  [`stdout`](#convex.shell.req.stream/stdout) - Request for returning STDOUT.
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
    -  [`n-cpu`](#convex.shell.req.sys/n-cpu) - Request for returning the number of available cores.
    -  [`os`](#convex.shell.req.sys/os) - Request for returning a tuple <code>[OS Version]</code>.
    -  [`pid`](#convex.shell.req.sys/pid) - Request for returning the PID of this process.
    -  [`pid-command`](#convex.shell.req.sys/pid-command) - Request for retrieving by PID the command that launched a process.
-  [`convex.shell.req.testnet`](#convex.shell.req.testnet)  - Requests for REST methods provided by <code>convex.world</code>.
    -  [`create-account`](#convex.shell.req.testnet/create-account) - Request for creating a new account.
    -  [`faucet`](#convex.shell.req.testnet/faucet) - Request for receiving Convex Coins.
-  [`convex.shell.req.time`](#convex.shell.req.time)  - Requests relating to time.
    -  [`-millis`](#convex.shell.req.time/-millis)
    -  [`advance`](#convex.shell.req.time/advance) - Request for moving forward the CVM timestamp.
    -  [`iso->unix`](#convex.shell.req.time/iso->unix) - Request for converting an ISO 8601 UTC string into a Unix timestamp.
    -  [`nano`](#convex.shell.req.time/nano) - Request for returning the current time according to the JVM high-resolution timer.
    -  [`sleep`](#convex.shell.req.time/sleep) - Request for temporarily blocking execution.
    -  [`unix`](#convex.shell.req.time/unix) - Request for returning the current Unix timestamp of the machine.
    -  [`unix->iso`](#convex.shell.req.time/unix->iso) - Opposite of [[iso->unix]].
-  [`convex.shell.req.trx`](#convex.shell.req.trx)  - Requests relating to creating and applying transactions.
    -  [`new-call`](#convex.shell.req.trx/new-call) - Request for creating a new call transaction.
    -  [`new-invoke`](#convex.shell.req.trx/new-invoke) - Request for creating a new invoke transaction.
    -  [`new-transfer`](#convex.shell.req.trx/new-transfer) - Request for creating a new transfer transaction.
    -  [`trx`](#convex.shell.req.trx/trx) - Request for applying an unsigned transaction.
    -  [`trx-noop`](#convex.shell.req.trx/trx-noop) - Request with the same overhead as [[trx]] but does not apply the transaction.
    -  [`with-sequence`](#convex.shell.req.trx/with-sequence) - Request for returning <code>trx</code> as a new transaction with an updated sequence ID.
-  [`convex.shell.resrc`](#convex.shell.resrc)  - Disguising external resources as (fake) cells.
    -  [`create`](#convex.shell.resrc/create) - Returns a vector cell that wraps <code>x</code> (can be anything).
    -  [`unwrap`](#convex.shell.resrc/unwrap) - Unwraps <code>resrc</code> which should have been created with [[create]].
    -  [`unwrap-with`](#convex.shell.resrc/unwrap-with) - Based on [[unwrap]], calls <code>f</code> with the unwraped resource or returns <code>ctx</code> in an exceptional state.
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




## <a name="convex.shell/-main">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell.clj#L147-L161) `-main`</a>
``` clojure

(-main & txt-cell+)
```


Main entry point for using Convex Shell as a terminal application.
  
   Expects cells as text to wrap and execute in a `(do)`.
   See [`transact-main`](#convex.shell/transact-main) for a reusable implementation.
  
   ```clojure
   (-main "(+ 2 2)")
   ```

## <a name="convex.shell/init">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell.clj#L34-L62) `init`</a>
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

## <a name="convex.shell/transact">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell.clj#L66-L86) `transact`</a>
``` clojure

(transact ctx trx)
```


Applies the given transaction (a cell) to the given context.
  
   Context should come from [`init`](#convex.shell/init).
  
   Returns a context with a result or an exception attached.

## <a name="convex.shell/transact-main">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell.clj#L90-L141) `transact-main`</a>
``` clojure

(transact-main ctx txt-cell+)
```


Core implementation of [`-main`](#convex.shell/-main).
  
   Passes the text cells to the `.shell.main` CVX function defined in the core account.
  
   `ctx` should come from [`init`](#convex.shell/init) and will be passed to [`transact`](#convex.shell/transact).

   In case of a result, prints its to STDOUT and terminates with a 0 code.
   In case of an exception, prints it to STDERR and terminates with a non-0 code.

-----

-----
# <a name="convex.shell.async">convex.shell.async</a>


Helpers for async requests.




## <a name="convex.shell.async/failure">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/async.clj#L18-L36) `failure`</a>
``` clojure

(failure ctx [error-code error-message])
(failure ctx error-code error-message)
```


Indicating failure from an async value.

## <a name="convex.shell.async/return">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/async.clj#L53-L72) `return`</a>
``` clojure

(return ctx d*future f-catch)
```


Attaches an async value to `ctx`.
  
   Async value is produced in a delay for error handling.
   `f-catch` is used if either the delay or the async process throws.

## <a name="convex.shell.async/success">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/async.clj#L40-L47) `success`</a>
``` clojure

(success result)
```


Indicating success from an async value.

-----
# <a name="convex.shell.ctx">convex.shell.ctx</a>


Preparing the genesis context used by the Shell.
  
   The Shell CVX library is executed in the core account so that all those functions
   are accessible from any account.




## <a name="convex.shell.ctx/core-env">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L51-L55) `core-env`</a>

Genesis Core environment.

## <a name="convex.shell.ctx/core-meta">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L59-L63) `core-meta`</a>

Genesis Core metadata.

## <a name="convex.shell.ctx/genesis">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L66-L86) `genesis`</a>

Genesis context prepared for the Shell.

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
# <a name="convex.shell.log">convex.shell.log</a>


Handles logging done via Timbre.
  
   Note: SLF4J from the core Java libraries is being redirected to Timbre.




## <a name="convex.shell.log/out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/log.clj#L33-L39) `out`</a>
``` clojure

(out)
```


Returns the stream currently used for logging.

## <a name="convex.shell.log/out-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/log.clj#L43-L50) `out-set`</a>
``` clojure

(out-set stream)
```


Sets [`out`](#convex.shell.log/out) to the given `stream`.

## <a name="convex.shell.log/throwable">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/log.clj#L53-L65) `throwable`</a>
``` clojure

(throwable ex)
```


Turns a `Throwable` into a cell.

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




## <a name="convex.shell.req/core">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req.clj#L111-L315) `core`</a>

All core requests.
  
   A map of CVX symbols pointing to a Clojure implementations.

## <a name="convex.shell.req/ex-rethrow">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req.clj#L60-L105) `ex-rethrow`</a>
``` clojure

(ex-rethrow ctx [ex-map])
```


Request for rethrowing an exception captured in the Shell.

## <a name="convex.shell.req/invoker">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req.clj#L362-L417) `invoker`</a>
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
# <a name="convex.shell.req.async">convex.shell.req.async</a>


Requests for async programming.
  
   The word "promise" is used for any kind of async value.




## <a name="convex.shell.req.async/do-">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/async.clj#L45-L71) `do-`</a>
``` clojure

(do- ctx [f])
```


Request for executing a CVX function in a forked context on a separate thread.
   Returns a promise.

## <a name="convex.shell.req.async/take">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/async.clj#L75-L90) `take`</a>
``` clojure

(take ctx [promise])
```


Request for awaiting a promise.

## <a name="convex.shell.req.async/take-timeout">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/async.clj#L94-L120) `take-timeout`</a>
``` clojure

(take-timeout ctx [promise timeout-millis timeout-val])
```


Like [`take`](#convex.shell.req.async/take) but with a timeout.

-----
# <a name="convex.shell.req.bench">convex.shell.req.bench</a>


Requests related to benchmarking.




## <a name="convex.shell.req.bench/eval">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/bench.clj#L73-L85) `eval`</a>
``` clojure

(eval ctx [code])
```


Request for benchmarking some code using Criterium.

## <a name="convex.shell.req.bench/trx">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/bench.clj#L89-L103) `trx`</a>
``` clojure

(trx ctx [trx])
```


Request for benchmarking a transaction.

## <a name="convex.shell.req.bench/trx-gen">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/bench.clj#L107-L138) `trx-gen`</a>
``` clojure

(trx-gen ctx [gen sample-count sample-time])
```


Request for benchmarking generated transaction (without throwing away the state).

-----
# <a name="convex.shell.req.cell">convex.shell.req.cell</a>


More advanced requestes relating to cells.




## <a name="convex.shell.req.cell/compile">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/cell.clj#L21-L41) `compile`</a>
``` clojure

(compile ctx [state addr cell])
```


Request for pre-compiling a `cell` for the given `address` which might
   not exist in the Shell.

## <a name="convex.shell.req.cell/ref-stat">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/cell.clj#L45-L52) `ref-stat`</a>
``` clojure

(ref-stat ctx [cell])
```


Requests for providing stats about the `cell`'s refs.

## <a name="convex.shell.req.cell/size">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/cell.clj#L56-L67) `size`</a>
``` clojure

(size ctx [cell])
```


Requests for getting the full memory size of a cell.

## <a name="convex.shell.req.cell/str">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/cell.clj#L71-L87) `str`</a>
``` clojure

(str ctx [limit cell])
```


Request for printing a cell to a string with a user given size limit
   instead of the default one.
  
   Also, chars and strings print in their cell form.

-----
# <a name="convex.shell.req.client">convex.shell.req.client</a>


Requests relating to the binary client.




## <a name="convex.shell.req.client/close">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/client.clj#L84-L102) `close`</a>
``` clojure

(close ctx [client])
```


Request for closing a client.

## <a name="convex.shell.req.client/connect">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/client.clj#L106-L139) `connect`</a>
``` clojure

(connect ctx [host port])
```


Request for connecting to a peer.

## <a name="convex.shell.req.client/peer-endpoint">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/client.clj#L143-L155) `peer-endpoint`</a>
``` clojure

(peer-endpoint ctx [client])
```


Request for retrieving the endpoint the client is connected to.

## <a name="convex.shell.req.client/peer-status">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/client.clj#L159-L171) `peer-status`</a>
``` clojure

(peer-status ctx [client])
```


Request for retrieving the current peer status

## <a name="convex.shell.req.client/query">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/client.clj#L175-L193) `query`</a>
``` clojure

(query ctx [client address code])
```


Request for issuing a query.

## <a name="convex.shell.req.client/query-state">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/client.clj#L197-L209) `query-state`</a>
``` clojure

(query-state ctx [client])
```


Request for fetching the peer's `State`.

## <a name="convex.shell.req.client/resolve">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/client.clj#L213-L232) `resolve`</a>
``` clojure

(resolve ctx [client hash])
```


Request for resolving a hash to a cell.

## <a name="convex.shell.req.client/sequence">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/client.clj#L238-L259) `sequence`</a>
``` clojure

(sequence ctx [client address])
```


Request for retrieving the next sequence ID.

## <a name="convex.shell.req.client/transact">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/client.clj#L263-L283) `transact`</a>
``` clojure

(transact ctx [client kp trx])
```


Request for signing and issuing a transaction.

## <a name="convex.shell.req.client/transact-signed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/client.clj#L287-L304) `transact-signed`</a>
``` clojure

(transact-signed ctx [client signed-trx])
```


Request for issuing a signed transaction.

-----
# <a name="convex.shell.req.cvmlog">convex.shell.req.cvmlog</a>


Requests relating to the CVM log.




## <a name="convex.shell.req.cvmlog/clear">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/cvmlog.clj#L14-L22) `clear`</a>
``` clojure

(clear ctx _arg+)
```


Request for clearing the CVM log.

## <a name="convex.shell.req.cvmlog/get">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/cvmlog.clj#L26-L33) `get`</a>
``` clojure

(get ctx _arg+)
```


Request for retrieving the CVM log.

-----
# <a name="convex.shell.req.db">convex.shell.req.db</a>


Requests relating to Etch.




## <a name="convex.shell.req.db/allow-open?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L36-L43) `allow-open?`</a>

## <a name="convex.shell.req.db/flush">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L81-L90) `flush`</a>
``` clojure

(flush ctx _arg+)
```


Request for flushing Etch.

## <a name="convex.shell.req.db/open">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L94-L140) `open`</a>
``` clojure

(open ctx [path])
```


Request for opening an Etch instance.
  
   Only one instance can be open per Shell, so that the user cannot possible
   mingle cells coming from different instances.
   Idempotent nonetheless if the user provides the same path.

## <a name="convex.shell.req.db/path">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L144-L152) `path`</a>
``` clojure

(path ctx _arg+)
```


Request for getting the path of the currently open instance (or nil).

## <a name="convex.shell.req.db/read">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L156-L170) `read`</a>
``` clojure

(read ctx [hash])
```


Request for reading a cell by hash.

## <a name="convex.shell.req.db/root-read">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L174-L182) `root-read`</a>
``` clojure

(root-read ctx _arg+)
```


Request for reading from the root.

## <a name="convex.shell.req.db/root-write">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L186-L194) `root-write`</a>
``` clojure

(root-write ctx [cell])
```


Request for writing to the root.

## <a name="convex.shell.req.db/size">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L198-L206) `size`</a>
``` clojure

(size ctx _arg+)
```


Request for returning the precise data size of the Etch instance.

## <a name="convex.shell.req.db/write">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/db.clj#L210-L218) `write`</a>
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




## <a name="convex.shell.req.file/lock">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/file.clj#L56-L82) `lock`</a>
``` clojure

(lock ctx [path])
```


Request for getting an exclusive file lock.

## <a name="convex.shell.req.file/lock-release">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/file.clj#L86-L111) `lock-release`</a>
``` clojure

(lock-release ctx [lock])
```


Request for releasing an exclusive file lock.

## <a name="convex.shell.req.file/stream-in">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/file.clj#L115-L124) `stream-in`</a>
``` clojure

(stream-in ctx [path])
```


Request for opening an input stream for file under `path`.

## <a name="convex.shell.req.file/stream-out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/file.clj#L128-L143) `stream-out`</a>
``` clojure

(stream-out ctx [path append?])
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
# <a name="convex.shell.req.gen">convex.shell.req.gen</a>






## <a name="convex.shell.req.gen/-ensure-bound">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L56-L80) `-ensure-bound`</a>
``` clojure

(-ensure-bound ctx min max)
```


## <a name="convex.shell.req.gen/-ensure-pos-num">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L35-L52) `-ensure-pos-num`</a>
``` clojure

(-ensure-pos-num ctx i)
```


## <a name="convex.shell.req.gen/always">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L203-L208) `always`</a>
``` clojure

(always ctx [x])
```


## <a name="convex.shell.req.gen/bind">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L212-L253) `bind`</a>
``` clojure

(bind ctx [gen f])
```


## <a name="convex.shell.req.gen/blob">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L257-L262) `blob`</a>
``` clojure

(blob ctx _arg+)
```


## <a name="convex.shell.req.gen/blob-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L266-L278) `blob-bounded`</a>
``` clojure

(blob-bounded ctx [min max])
```


## <a name="convex.shell.req.gen/blob-fixed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L282-L292) `blob-fixed`</a>
``` clojure

(blob-fixed ctx [n])
```


## <a name="convex.shell.req.gen/blob-map">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L296-L308) `blob-map`</a>
``` clojure

(blob-map ctx [gen-k gen-v])
```


## <a name="convex.shell.req.gen/blob-map-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L312-L332) `blob-map-bounded`</a>
``` clojure

(blob-map-bounded ctx [gen-k gen-v min max])
```


## <a name="convex.shell.req.gen/blob-map-fixed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L336-L354) `blob-map-fixed`</a>
``` clojure

(blob-map-fixed ctx [gen-k gen-v n])
```


## <a name="convex.shell.req.gen/check">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L1001-L1076) `check`</a>
``` clojure

(check ctx [gen+ f size-max n-trial seed])
```


## <a name="convex.shell.req.gen/do-gen">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L107-L117) `do-gen`</a>
``` clojure

(do-gen ctx gen f)
```


## <a name="convex.shell.req.gen/do-gen+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L121-L148) `do-gen+`</a>
``` clojure

(do-gen+ ctx gen+ f)
```


## <a name="convex.shell.req.gen/double-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L358-L387) `double-bounded`</a>
``` clojure

(double-bounded ctx [min max infinite? nan?])
```


## <a name="convex.shell.req.gen/fmap">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L391-L411) `fmap`</a>
``` clojure

(fmap ctx [f gen])
```


## <a name="convex.shell.req.gen/freq">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L415-L474) `freq`</a>
``` clojure

(freq ctx [pair+])
```


## <a name="convex.shell.req.gen/gen">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L172-L197) `gen`</a>
``` clojure

(gen ctx [gen size seed])
```


## <a name="convex.shell.req.gen/hex-string">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L478-L483) `hex-string`</a>
``` clojure

(hex-string ctx _arg+)
```


## <a name="convex.shell.req.gen/hex-string-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L501-L513) `hex-string-bounded`</a>
``` clojure

(hex-string-bounded ctx [min max])
```


## <a name="convex.shell.req.gen/hex-string-fixed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L487-L497) `hex-string-fixed`</a>
``` clojure

(hex-string-fixed ctx [n])
```


## <a name="convex.shell.req.gen/list">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L517-L525) `list`</a>
``` clojure

(list ctx [gen])
```


## <a name="convex.shell.req.gen/list-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L529-L545) `list-bounded`</a>
``` clojure

(list-bounded ctx [gen min max])
```


## <a name="convex.shell.req.gen/list-fixed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L580-L594) `list-fixed`</a>
``` clojure

(list-fixed ctx [gen n])
```


## <a name="convex.shell.req.gen/long-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L549-L576) `long-bounded`</a>
``` clojure

(long-bounded ctx [min max])
```


## <a name="convex.shell.req.gen/long-uniform">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L598-L625) `long-uniform`</a>
``` clojure

(long-uniform ctx [min max])
```


## <a name="convex.shell.req.gen/map">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L629-L641) `map`</a>
``` clojure

(map ctx [gen-k gen-v])
```


## <a name="convex.shell.req.gen/map-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L645-L665) `map-bounded`</a>
``` clojure

(map-bounded ctx [gen-k gen-v min max])
```


## <a name="convex.shell.req.gen/map-fixed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L669-L687) `map-fixed`</a>
``` clojure

(map-fixed ctx [gen-k gen-v n])
```


## <a name="convex.shell.req.gen/or-">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L691-L699) `or-`</a>
``` clojure

(or- ctx [gen+])
```


## <a name="convex.shell.req.gen/pick">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L703-L716) `pick`</a>
``` clojure

(pick ctx [x+])
```


## <a name="convex.shell.req.gen/quoted">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L720-L728) `quoted`</a>
``` clojure

(quoted ctx [gen])
```


## <a name="convex.shell.req.gen/set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L732-L740) `set`</a>
``` clojure

(set ctx [gen])
```


## <a name="convex.shell.req.gen/set-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L744-L760) `set-bounded`</a>
``` clojure

(set-bounded ctx [gen min max])
```


## <a name="convex.shell.req.gen/set-fixed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L764-L778) `set-fixed`</a>
``` clojure

(set-fixed ctx [gen n])
```


## <a name="convex.shell.req.gen/string">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L782-L787) `string`</a>
``` clojure

(string ctx _arg+)
```


## <a name="convex.shell.req.gen/string-alphanum">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L821-L826) `string-alphanum`</a>
``` clojure

(string-alphanum ctx _arg+)
```


## <a name="convex.shell.req.gen/string-alphanum-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L830-L842) `string-alphanum-bounded`</a>
``` clojure

(string-alphanum-bounded ctx [min max])
```


## <a name="convex.shell.req.gen/string-alphanum-fixed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L846-L856) `string-alphanum-fixed`</a>
``` clojure

(string-alphanum-fixed ctx [n])
```


## <a name="convex.shell.req.gen/string-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L791-L803) `string-bounded`</a>
``` clojure

(string-bounded ctx [min max])
```


## <a name="convex.shell.req.gen/string-fixed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L807-L817) `string-fixed`</a>
``` clojure

(string-fixed ctx [n])
```


## <a name="convex.shell.req.gen/such-that">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L860-L894) `such-that`</a>
``` clojure

(such-that ctx [max-try f gen])
```


## <a name="convex.shell.req.gen/syntax">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L898-L903) `syntax`</a>
``` clojure

(syntax ctx _arg+)
```


## <a name="convex.shell.req.gen/syntax-with-meta">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L907-L920) `syntax-with-meta`</a>
``` clojure

(syntax-with-meta ctx [gen-v gen-meta])
```


## <a name="convex.shell.req.gen/syntax-with-value">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L924-L932) `syntax-with-value`</a>
``` clojure

(syntax-with-value ctx [gen-v])
```


## <a name="convex.shell.req.gen/tuple">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L936-L945) `tuple`</a>
``` clojure

(tuple ctx [gen+])
```


## <a name="convex.shell.req.gen/vector">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L949-L957) `vector`</a>
``` clojure

(vector ctx [gen])
```


## <a name="convex.shell.req.gen/vector-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L961-L977) `vector-bounded`</a>
``` clojure

(vector-bounded ctx [gen min max])
```


## <a name="convex.shell.req.gen/vector-fixed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen.clj#L981-L995) `vector-fixed`</a>
``` clojure

(vector-fixed ctx [gen n])
```


-----
# <a name="convex.shell.req.gen.static">convex.shell.req.gen.static</a>






## <a name="convex.shell.req.gen.static/address">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L20-L25) `address`</a>
``` clojure

(address ctx _arg+)
```


## <a name="convex.shell.req.gen.static/any">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L31-L36) `any`</a>
``` clojure

(any ctx _arg+)
```


## <a name="convex.shell.req.gen.static/any-coll">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L41-L46) `any-coll`</a>
``` clojure

(any-coll ctx _arg+)
```


## <a name="convex.shell.req.gen.static/any-list">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L52-L57) `any-list`</a>
``` clojure

(any-list ctx _arg+)
```


## <a name="convex.shell.req.gen.static/any-map">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L63-L68) `any-map`</a>
``` clojure

(any-map ctx _arg+)
```


## <a name="convex.shell.req.gen.static/any-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L74-L79) `any-set`</a>
``` clojure

(any-set ctx _arg+)
```


## <a name="convex.shell.req.gen.static/any-vector">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L85-L90) `any-vector`</a>
``` clojure

(any-vector ctx _arg+)
```


## <a name="convex.shell.req.gen.static/bigint">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L96-L101) `bigint`</a>
``` clojure

(bigint ctx _arg+)
```


## <a name="convex.shell.req.gen.static/blob-32">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L107-L112) `blob-32`</a>
``` clojure

(blob-32 ctx _arg+)
```


## <a name="convex.shell.req.gen.static/boolean">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L118-L123) `boolean`</a>
``` clojure

(boolean ctx _arg+)
```


## <a name="convex.shell.req.gen.static/char">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L129-L134) `char`</a>
``` clojure

(char ctx _arg+)
```


## <a name="convex.shell.req.gen.static/char-alphanum">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L140-L145) `char-alphanum`</a>
``` clojure

(char-alphanum ctx _arg+)
```


## <a name="convex.shell.req.gen.static/double">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L151-L156) `double`</a>
``` clojure

(double ctx _arg+)
```


## <a name="convex.shell.req.gen.static/falsy">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L162-L167) `falsy`</a>
``` clojure

(falsy ctx _arg+)
```


## <a name="convex.shell.req.gen.static/keyword">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L173-L178) `keyword`</a>
``` clojure

(keyword ctx _arg+)
```


## <a name="convex.shell.req.gen.static/long">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L183-L188) `long`</a>
``` clojure

(long ctx _arg+)
```


## <a name="convex.shell.req.gen.static/nothing">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L204-L209) `nothing`</a>
``` clojure

(nothing ctx _arg+)
```


## <a name="convex.shell.req.gen.static/number">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L193-L198) `number`</a>
``` clojure

(number ctx _arg+)
```


## <a name="convex.shell.req.gen.static/scalar">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L214-L219) `scalar`</a>
``` clojure

(scalar ctx _arg+)
```


## <a name="convex.shell.req.gen.static/symbol">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L225-L230) `symbol`</a>
``` clojure

(symbol ctx _arg+)
```


## <a name="convex.shell.req.gen.static/truthy">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/gen/static.clj#L235-L240) `truthy`</a>
``` clojure

(truthy ctx _arg+)
```


-----
# <a name="convex.shell.req.juice">convex.shell.req.juice</a>


Requests relating to juice.




## <a name="convex.shell.req.juice/set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/juice.clj#L17-L38) `set`</a>
``` clojure

(set ctx [n-unit])
```


Request for setting the current juice value.

## <a name="convex.shell.req.juice/track">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/juice.clj#L42-L57) `track`</a>
``` clojure

(track ctx [trx])
```


Request for tracking juice cost of a transaction.
  
   See `.juice.track`.

-----
# <a name="convex.shell.req.kp">convex.shell.req.kp</a>


Requests relating to key pairs.
  
   Key pairs are represented as resources (see [`convex.shell.resrc`](#convex.shell.resrc)).




## <a name="convex.shell.req.kp/create">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/kp.clj#L41-L48) `create`</a>
``` clojure

(create ctx _arg+)
```


Request for creating a key pair from a random seed.

## <a name="convex.shell.req.kp/create-from-seed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/kp.clj#L52-L65) `create-from-seed`</a>
``` clojure

(create-from-seed ctx [seed])
```


Request for creating a key pair from a given seed.

## <a name="convex.shell.req.kp/do-kp">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/kp.clj#L19-L35) `do-kp`</a>
``` clojure

(do-kp ctx kp f)
```


Unwraps a key pair from a resource.

## <a name="convex.shell.req.kp/pubkey">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/kp.clj#L69-L79) `pubkey`</a>
``` clojure

(pubkey ctx [kp])
```


Request for retrieving the public key of the given key pair.

## <a name="convex.shell.req.kp/seed">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/kp.clj#L83-L93) `seed`</a>
``` clojure

(seed ctx [kp])
```


Request for retrieving the seed of the given key pair.

## <a name="convex.shell.req.kp/sign">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/kp.clj#L97-L108) `sign`</a>
``` clojure

(sign ctx [kp cell])
```


Request for signing a `cell`.

## <a name="convex.shell.req.kp/verify">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/kp.clj#L112-L133) `verify`</a>
``` clojure

(verify ctx [signature public-key x])
```


Request for verifying a signature

-----
# <a name="convex.shell.req.log">convex.shell.req.log</a>






## <a name="convex.shell.req.log/level">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/log.clj#L40-L49) `level`</a>
``` clojure

(level ctx _arg+)
```


## <a name="convex.shell.req.log/level-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/log.clj#L53-L79) `level-set`</a>
``` clojure

(level-set ctx [level])
```


## <a name="convex.shell.req.log/log">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/log.clj#L83-L96) `log`</a>
``` clojure

(log ctx [level arg])
```


## <a name="convex.shell.req.log/out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/log.clj#L100-L105) `out`</a>
``` clojure

(out ctx _arg+)
```


## <a name="convex.shell.req.log/out-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/log.clj#L109-L128) `out-set`</a>
``` clojure

(out-set ctx [stream])
```


-----
# <a name="convex.shell.req.peer">convex.shell.req.peer</a>






## <a name="convex.shell.req.peer/-do-peer">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L17-L35) `-do-peer`</a>
``` clojure

(-do-peer ctx peer f)
```


## <a name="convex.shell.req.peer/connection+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L138-L150) `connection+`</a>
``` clojure

(connection+ ctx [peer])
```


## <a name="convex.shell.req.peer/controller">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L154-L162) `controller`</a>
``` clojure

(controller ctx [peer])
```


## <a name="convex.shell.req.peer/data">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L166-L174) `data`</a>
``` clojure

(data ctx [peer])
```


## <a name="convex.shell.req.peer/endpoint">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L178-L186) `endpoint`</a>
``` clojure

(endpoint ctx [peer])
```


## <a name="convex.shell.req.peer/init-db">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L190-L199) `init-db`</a>
``` clojure

(init-db ctx arg+)
```


## <a name="convex.shell.req.peer/init-state">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L203-L216) `init-state`</a>
``` clojure

(init-state ctx [state & arg+])
```


## <a name="convex.shell.req.peer/init-sync">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L220-L245) `init-sync`</a>
``` clojure

(init-sync ctx [remote-host remote-port & arg+])
```


## <a name="convex.shell.req.peer/n-belief-received">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L114-L122) `n-belief-received`</a>
``` clojure

(n-belief-received ctx [peer])
```


## <a name="convex.shell.req.peer/n-belief-sent">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L126-L134) `n-belief-sent`</a>
``` clojure

(n-belief-sent ctx [peer])
```


## <a name="convex.shell.req.peer/persist">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L249-L261) `persist`</a>
``` clojure

(persist ctx [peer])
```


## <a name="convex.shell.req.peer/pubkey">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L265-L273) `pubkey`</a>
``` clojure

(pubkey ctx [peer])
```


## <a name="convex.shell.req.peer/start">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L277-L293) `start`</a>
``` clojure

(start ctx [peer])
```


## <a name="convex.shell.req.peer/state">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L297-L305) `state`</a>
``` clojure

(state ctx [peer])
```


## <a name="convex.shell.req.peer/status">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L309-L317) `status`</a>
``` clojure

(status ctx [peer])
```


## <a name="convex.shell.req.peer/stop">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/peer.clj#L321-L337) `stop`</a>
``` clojure

(stop ctx [peer])
```


-----
# <a name="convex.shell.req.pfx">convex.shell.req.pfx</a>


Requests relating to PFX stores for key pairs.




## <a name="convex.shell.req.pfx/-ensure-alias">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/pfx.clj#L21-L28) `-ensure-alias`</a>
``` clojure

(-ensure-alias ctx alias)
```


## <a name="convex.shell.req.pfx/-ensure-passphrase">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/pfx.clj#L32-L39) `-ensure-passphrase`</a>
``` clojure

(-ensure-passphrase ctx passphrase)
```


## <a name="convex.shell.req.pfx/-ensure-path">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/pfx.clj#L43-L50) `-ensure-path`</a>
``` clojure

(-ensure-path ctx path)
```


## <a name="convex.shell.req.pfx/alias+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/pfx.clj#L79-L90) `alias+`</a>
``` clojure

(alias+ ctx [store])
```


Request for getting the set of available alias in the given store.

## <a name="convex.shell.req.pfx/create">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/pfx.clj#L94-L114) `create`</a>
``` clojure

(create ctx [path passphrase])
```


Request for creating a new store.

## <a name="convex.shell.req.pfx/do-store">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/pfx.clj#L56-L73) `do-store`</a>
``` clojure

(do-store ctx store f)
```


Unwraps a PFX store from a resource.

## <a name="convex.shell.req.pfx/kp-get">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/pfx.clj#L118-L140) `kp-get`</a>
``` clojure

(kp-get ctx [store alias passphrase])
```


Request for retrieving a key pair from a store.

## <a name="convex.shell.req.pfx/kp-rm">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/pfx.clj#L144-L165) `kp-rm`</a>
``` clojure

(kp-rm ctx [store alias])
```


Request for removing a key pair from a store.

## <a name="convex.shell.req.pfx/kp-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/pfx.clj#L169-L198) `kp-set`</a>
``` clojure

(kp-set ctx [store alias kp passphrase])
```


Request for adding a key pair to a store.

## <a name="convex.shell.req.pfx/load">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/pfx.clj#L202-L219) `load`</a>
``` clojure

(load ctx [path passphrase])
```


Request for loading an existing store from a file.

## <a name="convex.shell.req.pfx/save">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/pfx.clj#L223-L247) `save`</a>
``` clojure

(save ctx [store path passphrase])
```


Request for saving a store to a file.

-----
# <a name="convex.shell.req.process">convex.shell.req.process</a>






## <a name="convex.shell.req.process/-do-stream">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/process.clj#L22-L36) `-do-stream`</a>
``` clojure

(-do-stream ctx class stream f)
```


## <a name="convex.shell.req.process/kill">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/process.clj#L54-L75) `kill`</a>
``` clojure

(kill ctx [process])
```


## <a name="convex.shell.req.process/run">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/process.clj#L79-L168) `run`</a>
``` clojure

(run ctx [command dir env env-extra err in out])
```


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




## <a name="convex.shell.req.state/core-vanilla">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/state.clj#L77-L90) `core-vanilla`</a>
``` clojure

(core-vanilla ctx [state])
```


Request for restoring genesis env and metadata in the core account in the given `state`.

## <a name="convex.shell.req.state/do-">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/state.clj#L94-L109) `do-`</a>
``` clojure

(do- ctx [f])
```


Request similar to [`safe`](#convex.shell.req.state/safe) but returns only a boolean (`false` in case of an exception).
  
   Avoids some overhead when dealing with exceptions (undesirable for situations like benchmarking).

## <a name="convex.shell.req.state/genesis">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/state.clj#L113-L145) `genesis`</a>
``` clojure

(genesis ctx [key+])
```


Request for generating a genesis state.

## <a name="convex.shell.req.state/safe">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/state.clj#L149-L160) `safe`</a>
``` clojure

(safe ctx [f])
```


Request for executing code in a safe way.
  
   In case of an exception, state is reverted.

## <a name="convex.shell.req.state/switch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/state.clj#L164-L190) `switch`</a>
``` clojure

(switch ctx [address state])
```


Request for switching a context to the given state.

## <a name="convex.shell.req.state/tmp">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/state.clj#L194-L203) `tmp`</a>
``` clojure

(tmp ctx [f])
```


Exactly like [`safe`](#convex.shell.req.state/safe) but the state is always reverted, even in case of success.

-----
# <a name="convex.shell.req.str">convex.shell.req.str</a>


Requests relating to strings.




## <a name="convex.shell.req.str/sort">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/str.clj#L21-L35) `sort`</a>
``` clojure

(sort ctx [str+])
```


Secret request for sorting a vector of strings.

## <a name="convex.shell.req.str/stream-in">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/str.clj#L39-L53) `stream-in`</a>
``` clojure

(stream-in ctx [string])
```


Request for turning a string into an input stream.

## <a name="convex.shell.req.str/stream-out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/str.clj#L57-L64) `stream-out`</a>
``` clojure

(stream-out ctx _arg+)
```


Request for creating an output stream backed by a string.

## <a name="convex.shell.req.str/stream-unwrap">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/str.clj#L68-L79) `stream-unwrap`</a>
``` clojure

(stream-unwrap ctx [handle])
```


Request for extracting the string inside a [`stream-out`](#convex.shell.req.str/stream-out).

-----
# <a name="convex.shell.req.stream">convex.shell.req.stream</a>


Requests relating to IO streams.




## <a name="convex.shell.req.stream/close">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L159-L181) `close`</a>
``` clojure

(close ctx arg+)
(close ctx [handle] result)
```


Request for closing the given stream.

   A result to propagate may be provided.

## <a name="convex.shell.req.stream/flush">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L185-L197) `flush`</a>
``` clojure

(flush ctx [handle])
```


Request for flushing the requested stream.

## <a name="convex.shell.req.stream/in+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L201-L212) `in+`</a>
``` clojure

(in+ ctx [handle])
```


Request for reading all available cells from the given stream and closing it.

## <a name="convex.shell.req.stream/line">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L216-L228) `line`</a>
``` clojure

(line ctx [handle])
```


Request for reading a line from the given stream and parsing it into a list of cells.

## <a name="convex.shell.req.stream/operation">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L120-L153) `operation`</a>
``` clojure

(operation ctx handle op+ f)
```


Generic function for carrying out an operation.

   Handles failure.

## <a name="convex.shell.req.stream/out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L250-L259) `out`</a>
``` clojure

(out ctx [handle cell])
```


Request for writing a `cell` to the given stream.

## <a name="convex.shell.req.stream/outln">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L284-L293) `outln`</a>
``` clojure

(outln env [handle cell])
```


Like [`out`](#convex.shell.req.stream/out) but appends a new line and flushes the stream.

## <a name="convex.shell.req.stream/stderr">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L29-L36) `stderr`</a>
``` clojure

(stderr ctx _arg+)
```


Request for returning STDERR.

## <a name="convex.shell.req.stream/stdin">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L42-L49) `stdin`</a>
``` clojure

(stdin ctx _arg+)
```


Request for returning STDIN.

## <a name="convex.shell.req.stream/stdout">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L55-L62) `stdout`</a>
``` clojure

(stdout ctx _arg+)
```


Request for returning STDOUT.

## <a name="convex.shell.req.stream/txt-in">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L297-L313) `txt-in`</a>
``` clojure

(txt-in ctx [handle])
```


Request for reading everything from the given stream as text.

## <a name="convex.shell.req.stream/txt-line">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L317-L330) `txt-line`</a>
``` clojure

(txt-line ctx [handle])
```


Request for reading a line from the given stream as text.

## <a name="convex.shell.req.stream/txt-out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L334-L343) `txt-out`</a>
``` clojure

(txt-out ctx [handle cell])
```


Like [`out`](#convex.shell.req.stream/out) but if `cell` is a string, then it is not double-quoted.

## <a name="convex.shell.req.stream/txt-outln">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/stream.clj#L347-L356) `txt-outln`</a>
``` clojure

(txt-outln ctx [handle cell])
```


Is to [`outln`](#convex.shell.req.stream/outln) what [[out-txt]] is to [`out`](#convex.shell.req.stream/out).

-----
# <a name="convex.shell.req.sys">convex.shell.req.sys</a>


Requests relating to basic system utilities.




## <a name="convex.shell.req.sys/arch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L22-L29) `arch`</a>
``` clojure

(arch ctx _arg+)
```


Request for returning the chip architecture as a string.

## <a name="convex.shell.req.sys/cwd">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L33-L40) `cwd`</a>
``` clojure

(cwd ctx _arg+)
```


Request for returning the current working directory (where the Shell started).

## <a name="convex.shell.req.sys/env">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L44-L54) `env`</a>
``` clojure

(env ctx _arg+)
```


Request for returning the map of process environment variables.

## <a name="convex.shell.req.sys/env-var">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L58-L70) `env-var`</a>
``` clojure

(env-var ctx [env-var])
```


Request for returning the value for a single process environment variable.

## <a name="convex.shell.req.sys/exit">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L74-L92) `exit`</a>
``` clojure

(exit ctx [code])
```


Request for terminating the process.

## <a name="convex.shell.req.sys/home">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L96-L103) `home`</a>
``` clojure

(home ctx _arg+)
```


Request for returning the home directory.

## <a name="convex.shell.req.sys/n-cpu">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L107-L114) `n-cpu`</a>
``` clojure

(n-cpu ctx _arg+)
```


Request for returning the number of available cores.

## <a name="convex.shell.req.sys/os">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L118-L126) `os`</a>
``` clojure

(os ctx _arg+)
```


Request for returning a tuple `[OS Version]`.

## <a name="convex.shell.req.sys/pid">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L130-L137) `pid`</a>
``` clojure

(pid ctx _arg+)
```


Request for returning the PID of this process.

## <a name="convex.shell.req.sys/pid-command">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/sys.clj#L141-L157) `pid-command`</a>
``` clojure

(pid-command ctx [pid])
```


Request for retrieving by PID the command that launched a process.

-----
# <a name="convex.shell.req.testnet">convex.shell.req.testnet</a>


Requests for REST methods provided by `convex.world`.




## <a name="convex.shell.req.testnet/create-account">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/testnet.clj#L50-L69) `create-account`</a>
``` clojure

(create-account ctx [public-key])
```


Request for creating a new account.

## <a name="convex.shell.req.testnet/faucet">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/testnet.clj#L73-L105) `faucet`</a>
``` clojure

(faucet ctx [address amount])
```


Request for receiving Convex Coins.

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

## <a name="convex.shell.req.time/sleep">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/time.clj#L125-L153) `sleep`</a>
``` clojure

(sleep ctx [millis nanos])
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
# <a name="convex.shell.req.trx">convex.shell.req.trx</a>


Requests relating to creating and applying transactions.




## <a name="convex.shell.req.trx/new-call">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/trx.clj#L89-L127) `new-call`</a>
``` clojure

(new-call ctx [origin sequence-id target offer function arg+])
```


Request for creating a new call transaction.

## <a name="convex.shell.req.trx/new-invoke">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/trx.clj#L131-L144) `new-invoke`</a>
``` clojure

(new-invoke ctx [origin sequence-id command])
```


Request for creating a new invoke transaction.

## <a name="convex.shell.req.trx/new-transfer">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/trx.clj#L148-L176) `new-transfer`</a>
``` clojure

(new-transfer ctx [origin sequence-id target amount])
```


Request for creating a new transfer transaction.

## <a name="convex.shell.req.trx/trx">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/trx.clj#L60-L69) `trx`</a>
``` clojure

(trx ctx [trx])
```


Request for applying an unsigned transaction.

## <a name="convex.shell.req.trx/trx-noop">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/trx.clj#L73-L83) `trx-noop`</a>
``` clojure

(trx-noop ctx [trx])
```


Request with the same overhead as [`trx`](#convex.shell.req.trx/trx) but does not apply the transaction.
   Probably only useful for benchmarking.

## <a name="convex.shell.req.trx/with-sequence">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/req/trx.clj#L182-L194) `with-sequence`</a>
``` clojure

(with-sequence ctx [trx sequence-id])
```


Request for returning `trx` as a new transaction with an updated sequence ID.

-----
# <a name="convex.shell.resrc">convex.shell.resrc</a>


Disguising external resources as (fake) cells.




## <a name="convex.shell.resrc/create">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/resrc.clj#L21-L34) `create`</a>
``` clojure

(create x)
```


Returns a vector cell that wraps `x` (can be anything).

   See [`unwrap`](#convex.shell.resrc/unwrap).

## <a name="convex.shell.resrc/unwrap">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/resrc.clj#L38-L63) `unwrap`</a>
``` clojure

(unwrap ctx resrc)
```


Unwraps `resrc` which should have been created with [`create`](#convex.shell.resrc/create).
  
   Returns a 2-tuple where the first item is a boolean indicating success and the second
   item is either the unwrapped value (in case of success) or a failed context (in case
   of failure).

## <a name="convex.shell.resrc/unwrap-with">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/resrc.clj#L67-L81) `unwrap-with`</a>
``` clojure

(unwrap-with ctx resrc f)
```


Based on [`unwrap`](#convex.shell.resrc/unwrap), calls `f` with the unwraped resource or returns `ctx`
   in an exceptional state.

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
