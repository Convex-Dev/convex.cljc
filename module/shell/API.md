# Table of contents
-  [`convex.shell`](#convex.shell)  - CONVEX SHELL This is a whole application.
    -  [`-main`](#convex.shell/-main) - Reads and executes transactions.
    -  [`eval`](#convex.shell/eval) - Uses [[init]], reads the given <code>string</code> of transactions and starts executing them.
    -  [`init`](#convex.shell/init) - Used by [[eval]] to initiate <code>env</code>.
-  [`convex.shell.ctx`](#convex.shell.ctx)  - Altering and quering informations about the CVM context attached to an env.
    -  [`active-repl?`](#convex.shell.ctx/active-repl?) - Is the REPL currently running?.
    -  [`compiled-lib+`](#convex.shell.ctx/compiled-lib+) - Pre-compiled CVX Shell libraries.
    -  [`ctx-base`](#convex.shell.ctx/ctx-base) - Base CVM context for the CVX shell.
    -  [`ctx-genesis`](#convex.shell.ctx/ctx-genesis) - Genesis state with default Convex libraries.
    -  [`current-trx+`](#convex.shell.ctx/current-trx+) - Returns the current list of transactions under <code>$.trx/*list*</code>.
    -  [`def-current`](#convex.shell.ctx/def-current) - Defines symbols in the current, default account.
    -  [`def-result`](#convex.shell.ctx/def-result) - Defines <code>$/*result*</code> with the given CVX <code>result</code>.
    -  [`def-trx+`](#convex.shell.ctx/def-trx+) - Defines the given CVX list of transactions under <code>$.trx/*list*</code>.
    -  [`deploy-lib+`](#convex.shell.ctx/deploy-lib+) - Deploys [[compiled-lib+]] on the given CVM <code>ctx</code>.
    -  [`drop-trx`](#convex.shell.ctx/drop-trx) - Drops the next transaction under <code>$.trx/*list*</code>.
    -  [`exit`](#convex.shell.ctx/exit) - Prepares for a clean process exit.
    -  [`lib-address`](#convex.shell.ctx/lib-address) - Retrieves the address of a shell library by symbol.
    -  [`precat-trx+`](#convex.shell.ctx/precat-trx+) - Prepends the given CVX list of transactions to the current list under <code>$.trx/*list*</code>.
    -  [`prepend-trx`](#convex.shell.ctx/prepend-trx) - Prepends a single transaction to the current list under <code>$.trx/*list*</code>.
    -  [`result`](#convex.shell.ctx/result) - Retrieves the last result available to users.
-  [`convex.shell.err`](#convex.shell.err)  - Errors are CVX maps, either mappified CVM exceptions or built from scratch.
    -  [`arg`](#convex.shell.err/arg) - Error map for a bad argument.
    -  [`assoc-trx`](#convex.shell.err/assoc-trx) - Associates a transaction to the given <code>err</code> map.
    -  [`db`](#convex.shell.err/db) - Error map for a generic Etch error.
    -  [`filesystem`](#convex.shell.err/filesystem) - Error map for a generic filesystem error.
    -  [`mappify`](#convex.shell.err/mappify) - Transforms the given CVM exception into a map.
    -  [`reader-stream`](#convex.shell.err/reader-stream) - Creates a <code>:READER</code> error map, for when the CVX reader fails on a stream.
    -  [`reader-string`](#convex.shell.err/reader-string) - Creates a <code>:READER</code> error map, for when the CVX reader fails on a string.
    -  [`sreq`](#convex.shell.err/sreq) - Error map describing an error that occured when performing an operation for a request.
    -  [`state`](#convex.shell.err/state) - Error map for a state exception.
    -  [`state-load`](#convex.shell.err/state-load) - Error map for when library deployment fails when loading a new state.
    -  [`stream`](#convex.shell.err/stream) - Error map for a generic stream error.
-  [`convex.shell.exec`](#convex.shell.exec)  - All aspects of actually executing transactions.
    -  [`eval`](#convex.shell.exec/eval) - Evaluates <code>trx</code> after refilling juice.
    -  [`juice`](#convex.shell.exec/juice) - Computes consumed juice based on the current limit.
    -  [`max-juice`](#convex.shell.exec/max-juice) - Maximum juice value set on context prior to handling code.
    -  [`result`](#convex.shell.exec/result) - Extracts a result from the current context attached to <code>env</code>.
    -  [`sreq`](#convex.shell.exec/sreq) - After evaluating a transaction, the shell must check if the result is a special request.
    -  [`sreq-dispatch`](#convex.shell.exec/sreq-dispatch) - Dispatch function used by the [[sreq]] multimethod.
    -  [`trx`](#convex.shell.exec/trx) - Evaluates <code>trx</code> and forwards result to [[sreq]] unless an error occured.
    -  [`trx+`](#convex.shell.exec/trx+) - Executes transactions located in <code>$.trx/*list*</code> in the context until that list becomes empty.
    -  [`trx-track-juice`](#convex.shell.exec/trx-track-juice) - Similar to [[trx]] but requests are not performed, new state is discarded, and <code>$/*result*</code> is <code>[consumed-juice trx-result]</code>.
-  [`convex.shell.exec.fail`](#convex.shell.exec.fail)  - About handling different failure scenarios.
    -  [`err`](#convex.shell.exec.fail/err) - Must be called in case of failure related to executing CVX Lisp, <code>err</code> being an error map (see the [[convex.shell.err]] namespace).
    -  [`rethrow`](#convex.shell.exec.fail/rethrow) - Like [[err]] but assumes the error has already been prepared as an exception result to return and the exception on the CVM context has already been cleared.
    -  [`top-exception`](#convex.shell.exec.fail/top-exception) - Called when a JVM exception is caught at the very top level of the shell.
-  [`convex.shell.io`](#convex.shell.io)  - Basic IO utilities and STDIO.
    -  [`file-in`](#convex.shell.io/file-in) - Opens an input text stream for the file located under <code>path</code>.
    -  [`file-out`](#convex.shell.io/file-out) - Opens an output text stream for the file located under <code>path</code>.
    -  [`flush`](#convex.shell.io/flush) - Flushes the given <code>out</code>.
    -  [`newline`](#convex.shell.io/newline) - Writes a new line to the given text output stream.
    -  [`stderr`](#convex.shell.io/stderr) - File descriptor for STDERR.
    -  [`stderr-bin`](#convex.shell.io/stderr-bin) - Binary stream for STDERR.
    -  [`stderr-txt`](#convex.shell.io/stderr-txt) - Text stream for STDERR.
    -  [`stdin`](#convex.shell.io/stdin) - File descriptor for STDIN.
    -  [`stdin-bin`](#convex.shell.io/stdin-bin) - Binary stream for STDIN.
    -  [`stdin-txt`](#convex.shell.io/stdin-txt) - Text stream for STDIN.
    -  [`stdout`](#convex.shell.io/stdout) - File descriptor for STDOUT.
    -  [`stdout-bin`](#convex.shell.io/stdout-bin) - Binary stream for STDOUT.
    -  [`stdout-txt`](#convex.shell.io/stdout-txt) - Text stream for STDOUT.
-  [`convex.shell.kw`](#convex.shell.kw)  - CVX keywords used by the shell.
    -  [`arg`](#convex.shell.kw/arg)
    -  [`catch-rethrow`](#convex.shell.kw/catch-rethrow)
    -  [`cause`](#convex.shell.kw/cause)
    -  [`code-read+`](#convex.shell.kw/code-read+)
    -  [`cvm-sreq`](#convex.shell.kw/cvm-sreq)
    -  [`dev-fatal`](#convex.shell.kw/dev-fatal)
    -  [`err-db`](#convex.shell.kw/err-db)
    -  [`err-filesystem`](#convex.shell.kw/err-filesystem)
    -  [`err-reader`](#convex.shell.kw/err-reader)
    -  [`err-stream`](#convex.shell.kw/err-stream)
    -  [`etch-flush`](#convex.shell.kw/etch-flush)
    -  [`etch-open`](#convex.shell.kw/etch-open)
    -  [`etch-path`](#convex.shell.kw/etch-path)
    -  [`etch-read`](#convex.shell.kw/etch-read)
    -  [`etch-read-only`](#convex.shell.kw/etch-read-only)
    -  [`etch-read-only?`](#convex.shell.kw/etch-read-only?)
    -  [`etch-root-read`](#convex.shell.kw/etch-root-read)
    -  [`etch-root-write`](#convex.shell.kw/etch-root-write)
    -  [`etch-write`](#convex.shell.kw/etch-write)
    -  [`exception?`](#convex.shell.kw/exception?)
    -  [`exec`](#convex.shell.kw/exec)
    -  [`file-copy`](#convex.shell.kw/file-copy)
    -  [`file-delete`](#convex.shell.kw/file-delete)
    -  [`file-exists`](#convex.shell.kw/file-exists)
    -  [`file-stream-in`](#convex.shell.kw/file-stream-in)
    -  [`file-stream-out`](#convex.shell.kw/file-stream-out)
    -  [`file-tmp`](#convex.shell.kw/file-tmp)
    -  [`file-tmp-dir`](#convex.shell.kw/file-tmp-dir)
    -  [`form`](#convex.shell.kw/form)
    -  [`juice-limit`](#convex.shell.kw/juice-limit)
    -  [`juice-limit-set`](#convex.shell.kw/juice-limit-set)
    -  [`juice-track`](#convex.shell.kw/juice-track)
    -  [`library-path`](#convex.shell.kw/library-path)
    -  [`log-clear`](#convex.shell.kw/log-clear)
    -  [`log-get`](#convex.shell.kw/log-get)
    -  [`path`](#convex.shell.kw/path)
    -  [`process-env`](#convex.shell.kw/process-env)
    -  [`process-exit`](#convex.shell.kw/process-exit)
    -  [`report`](#convex.shell.kw/report)
    -  [`result`](#convex.shell.kw/result)
    -  [`splice`](#convex.shell.kw/splice)
    -  [`src`](#convex.shell.kw/src)
    -  [`state-genesis`](#convex.shell.kw/state-genesis)
    -  [`state-load`](#convex.shell.kw/state-load)
    -  [`state-safe`](#convex.shell.kw/state-safe)
    -  [`stderr`](#convex.shell.kw/stderr)
    -  [`stdin`](#convex.shell.kw/stdin)
    -  [`stdout`](#convex.shell.kw/stdout)
    -  [`stream`](#convex.shell.kw/stream)
    -  [`stream-close`](#convex.shell.kw/stream-close)
    -  [`stream-flush`](#convex.shell.kw/stream-flush)
    -  [`stream-in+`](#convex.shell.kw/stream-in+)
    -  [`stream-line`](#convex.shell.kw/stream-line)
    -  [`stream-open?`](#convex.shell.kw/stream-open?)
    -  [`stream-out`](#convex.shell.kw/stream-out)
    -  [`stream-outln`](#convex.shell.kw/stream-outln)
    -  [`stream-txt-in`](#convex.shell.kw/stream-txt-in)
    -  [`stream-txt-line`](#convex.shell.kw/stream-txt-line)
    -  [`stream-txt-out`](#convex.shell.kw/stream-txt-out)
    -  [`stream-txt-outln`](#convex.shell.kw/stream-txt-outln)
    -  [`time-advance`](#convex.shell.kw/time-advance)
    -  [`time-bench`](#convex.shell.kw/time-bench)
    -  [`time-iso->unix`](#convex.shell.kw/time-iso->unix)
    -  [`time-nano`](#convex.shell.kw/time-nano)
    -  [`time-unix`](#convex.shell.kw/time-unix)
    -  [`time-unix->iso`](#convex.shell.kw/time-unix->iso)
    -  [`trx`](#convex.shell.kw/trx)
-  [`convex.shell.sreq`](#convex.shell.sreq)  - Implementation of requests interpreted by the shell between transactions.
-  [`convex.shell.stream`](#convex.shell.stream)  - Handling files and STDIO streams.
    -  [`close`](#convex.shell.stream/close) - Closes the requested stream.
    -  [`file-in`](#convex.shell.stream/file-in) - Opens an input stream for file under <code>path</code>.
    -  [`file-out`](#convex.shell.stream/file-out) - Opens an output stream for file under <code>path</code>.
    -  [`flush`](#convex.shell.stream/flush) - Flushes the requested stream.
    -  [`in+`](#convex.shell.stream/in+) - Reads all available cells from the requested stream and closes it.
    -  [`line`](#convex.shell.stream/line) - Reads a line from the requested stream and parses it into a list of cells.
    -  [`operation`](#convex.shell.stream/operation) - Generic function for carrying out an operation.
    -  [`out`](#convex.shell.stream/out) - Writes <code>cell</code> to the requested stream.
    -  [`outln`](#convex.shell.stream/outln) - Like [[out]] but appends a new line and flushes the stream.
    -  [`txt-in`](#convex.shell.stream/txt-in) - Reads everything from the requested stream as text.
    -  [`txt-line`](#convex.shell.stream/txt-line) - Reads a line from the requested stream as text.
    -  [`txt-out`](#convex.shell.stream/txt-out) - Like [[out]] but if <code>cell</code> is a string, then it is not quoted.
    -  [`txt-outln`](#convex.shell.stream/txt-outln) - Is to [[outln]] what [[out-txt]] is to [[out]].
-  [`convex.shell.sym`](#convex.shell.sym)  - CVX symbols used by the shell.
    -  [`$`](#convex.shell.sym/$)
    -  [`$-account`](#convex.shell.sym/$-account)
    -  [`$-catch`](#convex.shell.sym/$-catch)
    -  [`$-code`](#convex.shell.sym/$-code)
    -  [`$-db`](#convex.shell.sym/$-db)
    -  [`$-file`](#convex.shell.sym/$-file)
    -  [`$-fs`](#convex.shell.sym/$-fs)
    -  [`$-help`](#convex.shell.sym/$-help)
    -  [`$-juice`](#convex.shell.sym/$-juice)
    -  [`$-log`](#convex.shell.sym/$-log)
    -  [`$-process`](#convex.shell.sym/$-process)
    -  [`$-repl`](#convex.shell.sym/$-repl)
    -  [`$-state`](#convex.shell.sym/$-state)
    -  [`$-stream`](#convex.shell.sym/$-stream)
    -  [`$-term`](#convex.shell.sym/$-term)
    -  [`$-test`](#convex.shell.sym/$-test)
    -  [`$-time`](#convex.shell.sym/$-time)
    -  [`$-trx`](#convex.shell.sym/$-trx)
    -  [`active?*`](#convex.shell.sym/active?*)
    -  [`genesis`](#convex.shell.sym/genesis)
    -  [`line`](#convex.shell.sym/line)
    -  [`list*`](#convex.shell.sym/list*)
    -  [`out*`](#convex.shell.sym/out*)
    -  [`result*`](#convex.shell.sym/result*)
    -  [`version`](#convex.shell.sym/version)
    -  [`version-convex`](#convex.shell.sym/version-convex)
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

   This is a whole application. It is available as a library in case it needs to be embedded. Then, only [`-main`](#convex.shell/-main) is really
   useful. It must be called only one at a time per thread otherwise Etch utilities may greatly misbehave.

   Executes each form as a transaction, moving from transaction to transaction.

   A transaction can return a request to perform operations beyond the scope of the CVM, such as file IO or
   advancing time. Those requests turn Convex Lisp, a somewhat limited and fully deterministic language, into
   a scripting facility.

   Requests are vectors following expected conventions and implementations can be found in the [`convex.shell.sreq`](#convex.shell.sreq)
   namespace.

   A series of CVX libraries is embedded, building on those requests and the way the shell generally operates,
   providing features such as reading CVX files, unit testing, a REPL, or time-travel. All features are self
   documented in the grand tradition of Lisp languages.
  
   Functions throughout these namespaces often refer to `env`. It is an environment map passed around containing
   everything that is need by an instance: current CVM context, opened streams, current error if any, etc.
  
   In case of error, [`convex.shell.exec.fail/err`](#convex.shell.exec.fail/err) must be used so that the error is reported to the CVX executing environment.
  
   List of transactions pending for execution is accessible in the CVX execution environment under `$.trx/*list*`. This list
   can be modified by the user, allowing for powerful metaprogramming. Besides above-mentioned requests, this feature is used
   to implement another series of useful utilities such as exception catching.




## <a name="convex.shell/-main">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell.clj#L97-L118) `-main`</a>
``` clojure

(-main & trx+)
```


Reads and executes transactions.
  
   If no transaction is provided, starts the REPL.
  
   ```clojure
   (-main "(+ 2 2)")
   ```

## <a name="convex.shell/eval">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell.clj#L72-L91) `eval`</a>
``` clojure

(eval string)
(eval env string)
```


Uses [`init`](#convex.shell/init), reads the given `string` of transactions and starts executing them.
  
   Used by [`-main`](#convex.shell/-main).

## <a name="convex.shell/init">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell.clj#L48-L66) `init`</a>
``` clojure

(init env)
```


Used by [`eval`](#convex.shell/eval) to initiate `env`.

   Notably, prepares:

   - STDIO streams
   - Initial CVM context

-----
# <a name="convex.shell.ctx">convex.shell.ctx</a>


Altering and quering informations about the CVM context attached to an env.
  
   All CVX Shell libraries are pre-compiled in advance and a base context is defined in a top-level
   form as well. This significantly improves the start-up time of native images since all of those
   are precomputed at build time instead of run time (~4x improvement).




## <a name="convex.shell.ctx/active-repl?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L309-L320) `active-repl?`</a>
``` clojure

(active-repl? env)
```


Is the REPL currently running?

## <a name="convex.shell.ctx/compiled-lib+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L58-L150) `compiled-lib+`</a>

Pre-compiled CVX Shell libraries.

## <a name="convex.shell.ctx/ctx-base">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L188-L198) `ctx-base`</a>

Base CVM context for the CVX shell.

## <a name="convex.shell.ctx/ctx-genesis">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L50-L54) `ctx-genesis`</a>

Genesis state with default Convex libraries.

## <a name="convex.shell.ctx/current-trx+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L254-L265) `current-trx+`</a>
``` clojure

(current-trx+ env)
```


Returns the current list of transactions under `$.trx/*list*`.

## <a name="convex.shell.ctx/def-current">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L204-L216) `def-current`</a>
``` clojure

(def-current env sym->value)
```


Defines symbols in the current, default account.
  
   Uses [[convex.cvm/def]].

## <a name="convex.shell.ctx/def-result">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L220-L232) `def-result`</a>
``` clojure

(def-result env result)
```


Defines `$/*result*` with the given CVX `result`.

## <a name="convex.shell.ctx/def-trx+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L236-L248) `def-trx+`</a>
``` clojure

(def-trx+ env trx+)
```


Defines the given CVX list of transactions under `$.trx/*list*`.

## <a name="convex.shell.ctx/deploy-lib+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L154-L184) `deploy-lib+`</a>
``` clojure

(deploy-lib+ ctx)
```


Deploys [`compiled-lib+`](#convex.shell.ctx/compiled-lib+) on the given CVM `ctx`.

## <a name="convex.shell.ctx/drop-trx">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L269-L277) `drop-trx`</a>
``` clojure

(drop-trx env)
```


Drops the next transaction under `$.trx/*list*`.

## <a name="convex.shell.ctx/exit">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L351-L361) `exit`</a>
``` clojure

(exit env exit-code)
```


Prepares for a clean process exit.

## <a name="convex.shell.ctx/lib-address">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L324-L331) `lib-address`</a>
``` clojure

(lib-address env sym-lib)
```


Retrieves the address of a shell library by symbol.

## <a name="convex.shell.ctx/precat-trx+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L281-L291) `precat-trx+`</a>
``` clojure

(precat-trx+ env trx+)
```


Prepends the given CVX list of transactions to the current list under `$.trx/*list*`.

## <a name="convex.shell.ctx/prepend-trx">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L295-L303) `prepend-trx`</a>
``` clojure

(prepend-trx env trx)
```


Prepends a single transaction to the current list under `$.trx/*list*`.

## <a name="convex.shell.ctx/result">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/ctx.clj#L335-L345) `result`</a>
``` clojure

(result env)
```


Retrieves the last result available to users.

-----
# <a name="convex.shell.err">convex.shell.err</a>


Errors are CVX maps, either mappified CVM exceptions or built from scratch.

   Using [`convex.shell.exec.fail/err`](#convex.shell.exec.fail/err), they are reported back to the CVX executing environment
   and can be handled from CVX.

   This namespace provides functions for building recurrent error maps.




## <a name="convex.shell.err/arg">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/err.clj#L39-L48) `arg`</a>
``` clojure

(arg message arg-symbol)
```


Error map for a bad argument.

## <a name="convex.shell.err/assoc-trx">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/err.clj#L25-L33) `assoc-trx`</a>
``` clojure

(assoc-trx err trx)
```


Associates a transaction to the given `err` map. under `:trx`.

## <a name="convex.shell.err/db">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/err.clj#L52-L59) `db`</a>
``` clojure

(db message)
```


Error map for a generic Etch error.

## <a name="convex.shell.err/filesystem">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/err.clj#L63-L70) `filesystem`</a>
``` clojure

(filesystem message)
```


Error map for a generic filesystem error.

## <a name="convex.shell.err/mappify">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/err.clj#L74-L85) `mappify`</a>
``` clojure

(mappify ex)
```


Transforms the given CVM exception into a map.
  
   If prodived, associates to the resulting error map a [[phase]] and the current transaction that caused this error.

## <a name="convex.shell.err/reader-stream">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/err.clj#L109-L126) `reader-stream`</a>
``` clojure

(reader-stream id-stream)
(reader-stream id-stream reason)
```


Creates a `:READER` error map, for when the CVX reader fails on a stream.

## <a name="convex.shell.err/reader-string">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/err.clj#L89-L105) `reader-string`</a>
``` clojure

(reader-string src)
(reader-string src reason)
```


Creates a `:READER` error map, for when the CVX reader fails on a string.

## <a name="convex.shell.err/sreq">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/err.clj#L155-L163) `sreq`</a>
``` clojure

(sreq code message trx)
```


Error map describing an error that occured when performing an operation for a request.

## <a name="convex.shell.err/state">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/err.clj#L130-L137) `state`</a>
``` clojure

(state message)
```


Error map for a state exception.

## <a name="convex.shell.err/state-load">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/err.clj#L141-L151) `state-load`</a>
``` clojure

(state-load library-path message ex)
```


Error map for when library deployment fails when loading a new state.

## <a name="convex.shell.err/stream">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/err.clj#L167-L176) `stream`</a>
``` clojure

(stream id-stream message)
```


Error map for a generic stream error.

-----
# <a name="convex.shell.exec">convex.shell.exec</a>


All aspects of actually executing transactions.
  
   When an error is detected, [`convex.shell.exec.fail/err`](#convex.shell.exec.fail/err) is called.




## <a name="convex.shell.exec/eval">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/exec.clj#L101-L124) `eval`</a>
``` clojure

(eval env)
(eval env trx)
```


Evaluates `trx` after refilling juice.

## <a name="convex.shell.exec/juice">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/exec.clj#L35-L42) `juice`</a>
``` clojure

(juice env)
```


Computes consumed juice based on the current limit.

## <a name="convex.shell.exec/max-juice">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/exec.clj#L25-L29) `max-juice`</a>

Maximum juice value set on context prior to handling code.

## <a name="convex.shell.exec/result">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/exec.clj#L46-L53) `result`</a>
``` clojure

(result env)
```


Extracts a result from the current context attached to `env`.

## <a name="convex.shell.exec/sreq">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/exec.clj#L83-L95) `sreq`</a>

After evaluating a transaction, the shell must check if the result is a special request.
  
   It uses [`sreq-dispatch`](#convex.shell.exec/sreq-dispatch) to forward the result to the appropriate special request implementation, an "unknown"
   implementation if it looks like a special request but is not implemented, or the "nil" implementation if it is not
   a special request.

   Implentations of special requests are in the [`convex.shell.sreq`](#convex.shell.sreq) namespace.

## <a name="convex.shell.exec/sreq-dispatch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/exec.clj#L59-L79) `sreq-dispatch`</a>
``` clojure

(sreq-dispatch result)
(sreq-dispatch _env result)
```


Dispatch function used by the [`sreq`](#convex.shell.exec/sreq) multimethod.
  
   Returns nil if the given result is not a special request.

## <a name="convex.shell.exec/trx">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/exec.clj#L128-L140) `trx`</a>
``` clojure

(trx env trx)
```


Evaluates `trx` and forwards result to [`sreq`](#convex.shell.exec/sreq) unless an error occured.

## <a name="convex.shell.exec/trx+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/exec.clj#L160-L190) `trx+`</a>
``` clojure

(trx+ env)
```


Executes transactions located in `$.trx/*list*` in the context until that list becomes empty.

## <a name="convex.shell.exec/trx-track-juice">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/exec.clj#L144-L156) `trx-track-juice`</a>
``` clojure

(trx-track-juice env trx)
```


Similar to [`trx`](#convex.shell.exec/trx) but requests are not performed, new state is discarded, and `$/*result*` is `[consumed-juice trx-result]`.

-----
# <a name="convex.shell.exec.fail">convex.shell.exec.fail</a>


About handling different failure scenarios.




## <a name="convex.shell.exec.fail/err">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/exec/fail.clj#L22-L43) `err`</a>
``` clojure

(err env err)
```


Must be called in case of failure related to executing CVX Lisp, `err` being an error map (see the [`convex.shell.err`](#convex.shell.err)
   namespace).
  
   Under CVX `$.catch/*stack*` in the context is a stack of error handling transactions. This functions pops
   the next error handling transaction and prepends it to CVX `$.trx/*list*`, the list of transactions pending
   for execution.

   Also, error becomes available under `$/*result*`.

   This simple scheme allows sophisticated exception handling to be implemented from CVX Lisp, as seen in the
   `$.catch` acccount.

## <a name="convex.shell.exec.fail/rethrow">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/exec/fail.clj#L49-L60) `rethrow`</a>
``` clojure

(rethrow env ex)
```


Like [`err`](#convex.shell.exec.fail/err) but assumes the error has already been prepared as an exception result to return and the exception
     on the CVM context has already been cleared.

## <a name="convex.shell.exec.fail/top-exception">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/exec/fail.clj#L64-L93) `top-exception`</a>
``` clojure

(top-exception ex)
```


Called when a JVM exception is caught at the very top level of the shell.
   No `env` is available at that point. This is last resort.

-----
# <a name="convex.shell.io">convex.shell.io</a>


Basic IO utilities and STDIO.
  
   Text streams are meant for reading characters (`Reader` and `Writer`) while binary streams are meant to handle
   raw bytes (`InputStream` and `OutputStream`).




## <a name="convex.shell.io/file-in">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L104-L110) `file-in`</a>
``` clojure

(file-in path)
```


Opens an input text stream for the file located under `path`.

## <a name="convex.shell.io/file-out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L114-L134) `file-out`</a>
``` clojure

(file-out path)
(file-out path append?)
```


Opens an output text stream for the file located under `path`.
   By default, overwrites any existing file. Writes will be appended to the end
   if `append?` is true.

## <a name="convex.shell.io/flush">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L140-L147) `flush`</a>
``` clojure

(flush out)
```


Flushes the given `out`.

## <a name="convex.shell.io/newline">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L151-L158) `newline`</a>
``` clojure

(newline out)
```


Writes a new line to the given text output stream.

## <a name="convex.shell.io/stderr">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L30-L34) `stderr`</a>

File descriptor for STDERR.

## <a name="convex.shell.io/stderr-bin">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L38-L42) `stderr-bin`</a>

Binary stream for STDERR.

## <a name="convex.shell.io/stderr-txt">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L46-L50) `stderr-txt`</a>

Text stream for STDERR.

## <a name="convex.shell.io/stdin">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L54-L58) `stdin`</a>

File descriptor for STDIN.

## <a name="convex.shell.io/stdin-bin">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L62-L66) `stdin-bin`</a>

Binary stream for STDIN.

## <a name="convex.shell.io/stdin-txt">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L70-L74) `stdin-txt`</a>

Text stream for STDIN.

## <a name="convex.shell.io/stdout">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L78-L82) `stdout`</a>

File descriptor for STDOUT.

## <a name="convex.shell.io/stdout-bin">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L86-L90) `stdout-bin`</a>

Binary stream for STDOUT.

## <a name="convex.shell.io/stdout-txt">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/io.clj#L94-L98) `stdout-txt`</a>

Text stream for STDOUT.

-----
# <a name="convex.shell.kw">convex.shell.kw</a>


CVX keywords used by the shell.




## <a name="convex.shell.kw/arg">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L16-L17) `arg`</a>

## <a name="convex.shell.kw/catch-rethrow">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L19-L20) `catch-rethrow`</a>

## <a name="convex.shell.kw/cause">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L22-L23) `cause`</a>

## <a name="convex.shell.kw/code-read+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L25-L26) `code-read+`</a>

## <a name="convex.shell.kw/cvm-sreq">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L28-L29) `cvm-sreq`</a>

## <a name="convex.shell.kw/dev-fatal">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L31-L32) `dev-fatal`</a>

## <a name="convex.shell.kw/err-db">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L34-L35) `err-db`</a>

## <a name="convex.shell.kw/err-filesystem">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L37-L38) `err-filesystem`</a>

## <a name="convex.shell.kw/err-reader">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L40-L41) `err-reader`</a>

## <a name="convex.shell.kw/err-stream">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L43-L44) `err-stream`</a>

## <a name="convex.shell.kw/etch-flush">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L46-L47) `etch-flush`</a>

## <a name="convex.shell.kw/etch-open">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L49-L50) `etch-open`</a>

## <a name="convex.shell.kw/etch-path">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L52-L53) `etch-path`</a>

## <a name="convex.shell.kw/etch-read">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L55-L56) `etch-read`</a>

## <a name="convex.shell.kw/etch-read-only">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L58-L59) `etch-read-only`</a>

## <a name="convex.shell.kw/etch-read-only?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L61-L62) `etch-read-only?`</a>

## <a name="convex.shell.kw/etch-root-read">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L67-L68) `etch-root-read`</a>

## <a name="convex.shell.kw/etch-root-write">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L70-L71) `etch-root-write`</a>

## <a name="convex.shell.kw/etch-write">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L64-L65) `etch-write`</a>

## <a name="convex.shell.kw/exception?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L73-L74) `exception?`</a>

## <a name="convex.shell.kw/exec">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L76-L77) `exec`</a>

## <a name="convex.shell.kw/file-copy">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L79-L80) `file-copy`</a>

## <a name="convex.shell.kw/file-delete">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L82-L83) `file-delete`</a>

## <a name="convex.shell.kw/file-exists">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L85-L86) `file-exists`</a>

## <a name="convex.shell.kw/file-stream-in">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L88-L89) `file-stream-in`</a>

## <a name="convex.shell.kw/file-stream-out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L91-L92) `file-stream-out`</a>

## <a name="convex.shell.kw/file-tmp">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L94-L95) `file-tmp`</a>

## <a name="convex.shell.kw/file-tmp-dir">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L97-L98) `file-tmp-dir`</a>

## <a name="convex.shell.kw/form">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L100-L101) `form`</a>

## <a name="convex.shell.kw/juice-limit">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L103-L104) `juice-limit`</a>

## <a name="convex.shell.kw/juice-limit-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L106-L107) `juice-limit-set`</a>

## <a name="convex.shell.kw/juice-track">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L109-L110) `juice-track`</a>

## <a name="convex.shell.kw/library-path">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L112-L113) `library-path`</a>

## <a name="convex.shell.kw/log-clear">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L115-L116) `log-clear`</a>

## <a name="convex.shell.kw/log-get">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L118-L119) `log-get`</a>

## <a name="convex.shell.kw/path">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L121-L122) `path`</a>

## <a name="convex.shell.kw/process-env">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L124-L125) `process-env`</a>

## <a name="convex.shell.kw/process-exit">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L127-L128) `process-exit`</a>

## <a name="convex.shell.kw/report">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L130-L131) `report`</a>

## <a name="convex.shell.kw/result">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L133-L134) `result`</a>

## <a name="convex.shell.kw/splice">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L136-L137) `splice`</a>

## <a name="convex.shell.kw/src">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L139-L140) `src`</a>

## <a name="convex.shell.kw/state-genesis">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L142-L143) `state-genesis`</a>

## <a name="convex.shell.kw/state-load">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L145-L146) `state-load`</a>

## <a name="convex.shell.kw/state-safe">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L148-L149) `state-safe`</a>

## <a name="convex.shell.kw/stderr">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L151-L152) `stderr`</a>

## <a name="convex.shell.kw/stdin">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L154-L155) `stdin`</a>

## <a name="convex.shell.kw/stdout">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L157-L158) `stdout`</a>

## <a name="convex.shell.kw/stream">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L160-L161) `stream`</a>

## <a name="convex.shell.kw/stream-close">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L163-L164) `stream-close`</a>

## <a name="convex.shell.kw/stream-flush">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L166-L167) `stream-flush`</a>

## <a name="convex.shell.kw/stream-in+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L169-L170) `stream-in+`</a>

## <a name="convex.shell.kw/stream-line">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L172-L173) `stream-line`</a>

## <a name="convex.shell.kw/stream-open?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L175-L176) `stream-open?`</a>

## <a name="convex.shell.kw/stream-out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L178-L179) `stream-out`</a>

## <a name="convex.shell.kw/stream-outln">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L181-L182) `stream-outln`</a>

## <a name="convex.shell.kw/stream-txt-in">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L184-L185) `stream-txt-in`</a>

## <a name="convex.shell.kw/stream-txt-line">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L187-L188) `stream-txt-line`</a>

## <a name="convex.shell.kw/stream-txt-out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L190-L191) `stream-txt-out`</a>

## <a name="convex.shell.kw/stream-txt-outln">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L193-L194) `stream-txt-outln`</a>

## <a name="convex.shell.kw/time-advance">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L196-L197) `time-advance`</a>

## <a name="convex.shell.kw/time-bench">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L199-L200) `time-bench`</a>

## <a name="convex.shell.kw/time-iso->unix">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L202-L203) `time-iso->unix`</a>

## <a name="convex.shell.kw/time-nano">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L205-L206) `time-nano`</a>

## <a name="convex.shell.kw/time-unix">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L208-L209) `time-unix`</a>

## <a name="convex.shell.kw/time-unix->iso">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L211-L212) `time-unix->iso`</a>

## <a name="convex.shell.kw/trx">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/kw.clj#L214-L215) `trx`</a>

-----

-----
# <a name="convex.shell.stream">convex.shell.stream</a>


Handling files and STDIO streams.

   A stream is an id that represents an opened file or a STDIO streams. Those ids are kept in env.

   All operations, such as closing a stream or reading one, rely on [`operation`](#convex.shell.stream/operation).

   Used for implementing IO requests.




## <a name="convex.shell.stream/close">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/stream.clj#L160-L181) `close`</a>
``` clojure

(close env handle)
(close env handle result)
```


Closes the requested stream.
   A result to propagate may be provided.

## <a name="convex.shell.stream/file-in">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/stream.clj#L382-L392) `file-in`</a>
``` clojure

(file-in env handle path)
```


Opens an input stream for file under `path`.

## <a name="convex.shell.stream/file-out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/stream.clj#L396-L408) `file-out`</a>
``` clojure

(file-out env handle path append?)
```


Opens an output stream for file under `path`.

## <a name="convex.shell.stream/flush">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/stream.clj#L185-L196) `flush`</a>
``` clojure

(flush env handle)
```


Flushes the requested stream.

## <a name="convex.shell.stream/in+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/stream.clj#L200-L210) `in+`</a>
``` clojure

(in+ env handle)
```


Reads all available cells from the requested stream and closes it.

## <a name="convex.shell.stream/line">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/stream.clj#L214-L224) `line`</a>
``` clojure

(line env handle)
```


Reads a line from the requested stream and parses it into a list of cells.

## <a name="convex.shell.stream/operation">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/stream.clj#L110-L154) `operation`</a>
``` clojure

(operation env handle op+ f)
```


Generic function for carrying out an operation.
  
   Retrieves the stream associated with `handle` and executes `(f env stream`).
  
   Takes care of failure.

## <a name="convex.shell.stream/out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/stream.clj#L245-L254) `out`</a>
``` clojure

(out env handle cell)
```


Writes `cell` to the requested stream.

## <a name="convex.shell.stream/outln">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/stream.clj#L278-L287) `outln`</a>
``` clojure

(outln env handle cell)
```


Like [`out`](#convex.shell.stream/out) but appends a new line and flushes the stream.

## <a name="convex.shell.stream/txt-in">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/stream.clj#L291-L307) `txt-in`</a>
``` clojure

(txt-in env handle)
```


Reads everything from the requested stream as text.

## <a name="convex.shell.stream/txt-line">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/stream.clj#L311-L323) `txt-line`</a>
``` clojure

(txt-line env handle)
```


Reads a line from the requested stream as text.

## <a name="convex.shell.stream/txt-out">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/stream.clj#L327-L336) `txt-out`</a>
``` clojure

(txt-out env handle cell)
```


Like [`out`](#convex.shell.stream/out) but if `cell` is a string, then it is not quoted.

## <a name="convex.shell.stream/txt-outln">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/stream.clj#L340-L349) `txt-outln`</a>
``` clojure

(txt-outln env handle cell)
```


Is to [`outln`](#convex.shell.stream/outln) what [[out-txt]] is to [`out`](#convex.shell.stream/out).

-----
# <a name="convex.shell.sym">convex.shell.sym</a>


CVX symbols used by the shell.




## <a name="convex.shell.sym/$">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L31-L32) `$`</a>

## <a name="convex.shell.sym/$-account">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L34-L35) `$-account`</a>

## <a name="convex.shell.sym/$-catch">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L37-L38) `$-catch`</a>

## <a name="convex.shell.sym/$-code">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L40-L41) `$-code`</a>

## <a name="convex.shell.sym/$-db">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L43-L44) `$-db`</a>

## <a name="convex.shell.sym/$-file">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L46-L47) `$-file`</a>

## <a name="convex.shell.sym/$-fs">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L49-L50) `$-fs`</a>

## <a name="convex.shell.sym/$-help">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L52-L53) `$-help`</a>

## <a name="convex.shell.sym/$-juice">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L55-L56) `$-juice`</a>

## <a name="convex.shell.sym/$-log">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L58-L59) `$-log`</a>

## <a name="convex.shell.sym/$-process">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L61-L62) `$-process`</a>

## <a name="convex.shell.sym/$-repl">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L64-L65) `$-repl`</a>

## <a name="convex.shell.sym/$-state">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L67-L68) `$-state`</a>

## <a name="convex.shell.sym/$-stream">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L70-L71) `$-stream`</a>

## <a name="convex.shell.sym/$-term">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L76-L77) `$-term`</a>

## <a name="convex.shell.sym/$-test">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L79-L80) `$-test`</a>

## <a name="convex.shell.sym/$-time">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L73-L74) `$-time`</a>

## <a name="convex.shell.sym/$-trx">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L82-L83) `$-trx`</a>

## <a name="convex.shell.sym/active?*">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L15-L16) `active?*`</a>

## <a name="convex.shell.sym/genesis">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L85-L86) `genesis`</a>

## <a name="convex.shell.sym/line">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L88-L89) `line`</a>

## <a name="convex.shell.sym/list*">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L18-L19) `list*`</a>

## <a name="convex.shell.sym/out*">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L21-L22) `out*`</a>

## <a name="convex.shell.sym/result*">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L24-L25) `result*`</a>

## <a name="convex.shell.sym/version">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L91-L92) `version`</a>

## <a name="convex.shell.sym/version-convex">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/shell/src/main/clj/convex/shell/sym.clj#L94-L95) `version-convex`</a>

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
