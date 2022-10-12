# Table of contents
-  [`convex.cell`](#convex.cell)  - Constructors for CVM cells and related type predicate functions.
    -  [`*`](#convex.cell/*) - Macro for translating Clojure types to Convex types.
    -  [`IEquivalent`](#convex.cell/IEquivalent) - Translates Clojure types to equivalent Convex types.
    -  [`address`](#convex.cell/address) - Creates an address from a long.
    -  [`any`](#convex.cell/any)
    -  [`blob`](#convex.cell/blob) - Creates a blob from a byte array.
    -  [`blob-map`](#convex.cell/blob-map) - Creates a blob map from a collection of <code>[blob value]</code>.
    -  [`blob<-hex`](#convex.cell/blob<-hex) - Creates a blob from a hex string.
    -  [`boolean`](#convex.cell/boolean) - Creates a boolean cell given a falsy or truthy value.
    -  [`byte`](#convex.cell/byte) - Creates a byte cell from a value between 0 and 255 inclusive.
    -  [`call`](#convex.cell/call) - Creates a transaction for invoking a callable function.
    -  [`char`](#convex.cell/char) - Creates a character cell from a regular character.
    -  [`code-std*`](#convex.cell/code-std*) - Given a Clojure keyword, returns the corresponding standard error code (any of the Convex keyword the CVM itself uses): - <code>:ARGUMENT</code> - <code>:ARITY</code> - <code>:ASSERT</code> - <code>:BOUNDS</code> - <code>:CAST</code> - <code>:COMPILE</code> - <code>:DEPTH</code> - <code>:EXCEPTION</code> - <code>:EXPAND</code> - <code>:FATAL</code> - <code>:FUNDS</code> - <code>:HALT</code> - <code>:JUICE</code> - <code>:MEMORY</code> - <code>:NOBODY</code> - <code>:RECUR</code> - <code>:REDUCED</code> - <code>:RETURN</code> - <code>:ROLLBACK</code> - <code>:SEQUENCE</code> - <code>:SIGNATURE</code> - <code>:STATE</code> - <code>:TAILCALL</code> - <code>:TODO</code> - <code>:TRUST</code> - <code>:UNDECLARED</code> - <code>:UNEXPECTED</code> Throws if keyword does not match any of those.
    -  [`double`](#convex.cell/double) - Creates a double cell.
    -  [`encoding`](#convex.cell/encoding) - Returns a [[blob]] representing the encoding of the given <code>cell</code>.
    -  [`error`](#convex.cell/error) - An error value as Convex data.
    -  [`hash`](#convex.cell/hash) - Returns the hash of the given <code>cell</code>.
    -  [`hash<-blob`](#convex.cell/hash<-blob) - Converts a 32-byte [[blob]] to a [[hash]].
    -  [`hash<-hex`](#convex.cell/hash<-hex) - Creates a [[hash]] from a hex string.
    -  [`invoke`](#convex.cell/invoke) - Creates a transaction for invoking code (a cell).
    -  [`key`](#convex.cell/key) - Creates an account key from a 32-byte [[blob]].
    -  [`key-fake`](#convex.cell/key-fake) - Zeroed [[key]] that can be used during dev and testing so that an account is considered as a user, not an actor.
    -  [`keyword`](#convex.cell/keyword) - Creates a keyword cell from a string.
    -  [`list`](#convex.cell/list) - Creates a list cell from a collection of cells.
    -  [`long`](#convex.cell/long) - Creates a long cell.
    -  [`map`](#convex.cell/map) - Creates a map cell from a collection of <code>[key value]</code>.
    -  [`quoted`](#convex.cell/quoted) - Wraps <code>x</code> in <code>quote</code>.
    -  [`set`](#convex.cell/set) - Creates a set cell from a collection of items cell.
    -  [`string`](#convex.cell/string) - Creates a string cell from a regular string.
    -  [`symbol`](#convex.cell/symbol) - Creates a symbol cell from a string.
    -  [`syntax`](#convex.cell/syntax) - Creates a syntax cell.
    -  [`transfer`](#convex.cell/transfer) - Creates a transaction for transferring Convex Coins.
    -  [`vector`](#convex.cell/vector) - Creates a vector cell from a collection of items cell.
-  [`convex.clj`](#convex.clj)  - Convert cells to Clojure types.
    -  [`IClojuresque`](#convex.clj/IClojuresque) - Generic function for converting a cell to a Clojure representation.
    -  [`address`](#convex.clj/address) - Returns the given <code>address</code> as a JVM long.
    -  [`any`](#convex.clj/any)
    -  [`blob`](#convex.clj/blob) - Returns the given <code>blob</code> as a byte array.
    -  [`blob->hex`](#convex.clj/blob->hex) - Returns the given <code>blob</code> as a hex string.
    -  [`boolean`](#convex.clj/boolean) - Returns the given <code>boolean</code> cell as a JVM boolean.
    -  [`byte`](#convex.clj/byte) - Returns the given <code>byte</code> cell as a JVM long.
    -  [`char`](#convex.clj/char) - Returns the given <code>char</code> cell as a JVM char.
    -  [`double`](#convex.clj/double) - Returns the given <code>double</code> cell as a JVM double.
    -  [`keyword`](#convex.clj/keyword) - Returns the given <code>keyword</code> cell as a Clojure keyword.
    -  [`list`](#convex.clj/list) - Returns the given <code>list</code> cell as a Clojure list.
    -  [`long`](#convex.clj/long) - Returns the given <code>long</code> cell as a JVM long.
    -  [`map`](#convex.clj/map) - Returns the given <code>map</code> cell (hash map or blob map) as a Clojure map.
    -  [`set`](#convex.clj/set) - Returns the given <code>set</code> cell as a Clojure set.
    -  [`string`](#convex.clj/string) - Returns the given <code>string</code> cell as a JVM string.
    -  [`symbol`](#convex.clj/symbol) - Returns the given <code>symbol</code> cell as a Clojure symbol.
    -  [`syntax`](#convex.clj/syntax) - Returns the given <code>syntax</code> cell as a Clojure map such as: | Key | Value | |---|---| | <code>:meta</code> | Clojure map of metadata | | <code>:value</code> | Value wrapped, converted as well |.
    -  [`vector`](#convex.clj/vector) - Returns the given <code>vector</code> cell as a Clojure vector.
-  [`convex.cvm`](#convex.cvm)  - Code execution in the Convex Virtual Machine, altering its state, and gaining insights.
    -  [`account`](#convex.cvm/account) - Returns the account for the given <code>address</code> (or the address associated with <code>ctx</code>).
    -  [`account-create`](#convex.cvm/account-create) - Creates an new account, with a <code>key</code> (user) or without (actor).
    -  [`address`](#convex.cvm/address) - Returns the executing address of the given <code>ctx</code>.
    -  [`arg+*`](#convex.cvm/arg+*) - See [[invoke]].
    -  [`compile`](#convex.cvm/compile) - Compiles the <code>canonical-cell</code> into executable code.
    -  [`ctx`](#convex.cvm/ctx) - Creates an execution context.
    -  [`def`](#convex.cvm/def) - Like calling <code>(def sym value)</code> in Convex Lisp, either in the current address of the given one.
    -  [`deploy`](#convex.cvm/deploy) - Deploys the given <code>code</code> as an actor.
    -  [`env`](#convex.cvm/env) - Returns the environment of the executing account attached to <code>ctx</code>.
    -  [`eval`](#convex.cvm/eval) - Evaluates the given <code>cell</code> after forking the <code>ctx</code>, going efficiently through [[expand]], [[compile]], and [[exec]].
    -  [`exception`](#convex.cvm/exception) - The CVM enters in exceptional state in case of error or particular patterns such as halting or doing a rollback.
    -  [`exception-clear`](#convex.cvm/exception-clear) - Removes the currently attached exception from the given <code>ctx</code>.
    -  [`exception-code`](#convex.cvm/exception-code) - Returns the code associated with the given [[exception]].
    -  [`exception-message`](#convex.cvm/exception-message) - Returns the message associated with the given [[exception]].
    -  [`exception?`](#convex.cvm/exception?) - Returns true if the given <code>ctx</code> is in an exceptional state.
    -  [`exec`](#convex.cvm/exec) - Executes compiled code.
    -  [`expand`](#convex.cvm/expand) - Expands <code>cell</code> into a <code>canonical cell</code> by applying macros.
    -  [`expand-compile`](#convex.cvm/expand-compile) - Chains [[expand]] and [[compile]] in a slightly more efficient fashion than calling both separately.
    -  [`fake-key`](#convex.cvm/fake-key) - Fake key (all zeroes) meant for testing.
    -  [`fork`](#convex.cvm/fork) - Duplicates the given [[ctx]] (very cheap).
    -  [`fork-to`](#convex.cvm/fork-to) - Like [[fork]] but switches the executing account.
    -  [`genesis-user`](#convex.cvm/genesis-user) - Address of the first genesis user when the CVM [[state]] is created in [[ctx]].
    -  [`invoke`](#convex.cvm/invoke) - Invokes the given CVM <code>f</code>unction using the given <code>ctx</code>.
    -  [`juice`](#convex.cvm/juice) - Returns the remaining amount of juice available for the executing account.
    -  [`juice-preserve`](#convex.cvm/juice-preserve) - Executes <code>(f ctx)</code>, <code>f</code> being a function <code>ctx</code> -> <code>ctx</code>.
    -  [`juice-refill`](#convex.cvm/juice-refill) - Refills juice to maximum.
    -  [`juice-set`](#convex.cvm/juice-set) - Sets the juice of the given <code>ctx</code> to the requested <code>amount</code>.
    -  [`key`](#convex.cvm/key) - Returns the key of the given <code>address</code> (or the address associated with <code>ctx</code>).
    -  [`key-set`](#convex.cvm/key-set) - Sets <code>key</code> on the address curently associated with <code>ctx</code>.
    -  [`log`](#convex.cvm/log) - Returns the log of <code>ctx</code> (a vector cell of size 2 vectors containing a logging address and a logged value).
    -  [`look-up`](#convex.cvm/look-up) - Returns the cell associated with the given <code>sym</code> in the environment of the given <code>address</code> (or the currently used one).
    -  [`result`](#convex.cvm/result) - Extracts the result (eg.
    -  [`result-set`](#convex.cvm/result-set) - Attaches the given <code>result</code> to <code>ctx</code>, as if it was the result of a transaction.
    -  [`state`](#convex.cvm/state) - Returns the whole CVM state associated with <code>ctx</code>.
    -  [`state-set`](#convex.cvm/state-set) - Replaces the CVM state in the <code>ctx</code> with the given one.
    -  [`time`](#convex.cvm/time) - Returns the current timestamp (Unix epoch in milliseconds as long cell) assigned to the state in the given <code>ctx</code>.
    -  [`time-advance`](#convex.cvm/time-advance) - Advances the timestamp in the state of <code>ctx</code> by <code>millis</code> milliseconds.
    -  [`undef`](#convex.cvm/undef) - Like calling <code>(undef sym)</code> in Convex Lisp, either in the current account or the given one, repeatedly on any symbol cell in <code>sym+</code>.
-  [`convex.db`](#convex.db)  - Etch is a fast, immutable, embedded database tailored for cells.
    -  [`close`](#convex.db/close) - Flushes and closes the thread-local instance.
    -  [`current`](#convex.db/current) - Returns the thread-local instance (or nil).
    -  [`current-set`](#convex.db/current-set) - Binds the given <code>instance</code> to the current thread.
    -  [`flush`](#convex.db/flush) - Flushes the thread-local instance, ensuring all changes are persisted to disk.
    -  [`global-set`](#convex.db/global-set) - When an instance is used in more than one thread, it is a good idea using this function.
    -  [`open`](#convex.db/open) - Opens an instance at the given <code>path</code>.
    -  [`open-tmp`](#convex.db/open-tmp) - Like [[open]] but creates a temporary file.
    -  [`path`](#convex.db/path) - Returns the path of thread-local instance.
    -  [`read`](#convex.db/read) - Reads from the thread-local instance and returns the cell for the given <code>hash</code> (or nil if not found).
    -  [`root-read`](#convex.db/root-read) - Returns the cell stored at the root of the thread-local instance.
    -  [`root-write`](#convex.db/root-write) - Writes the given <code>cell</code> to the root of the thread-local instance and returns its hash.
    -  [`write`](#convex.db/write) - Writes the given <code>cell</code> to the thread-local instance and returns its hash.
-  [`convex.eval`](#convex.eval)  - Quick helpers built on top of [[convex.cvm/eval]].
    -  [`ctx`](#convex.eval/ctx) - Evaluates the given <code>cell</code> and returns <code>ctx</code>.
    -  [`exception`](#convex.eval/exception) - Like [[ctx]] but returns the current exception or nil if there is none.
    -  [`exception-code`](#convex.eval/exception-code) - Shortcut on top of [[exception]].
    -  [`result`](#convex.eval/result) - Like [[ctx]] but returns the result.
    -  [`true?`](#convex.eval/true?) - Shortcut on top of [[result]].
-  [`convex.read`](#convex.read)  - Reading, parsing various kind of sources into CVX cells without any evaluation.
    -  [`file`](#convex.read/file) - Reads all cells from the given <code>filename</code> and returns them in a CVX list.
    -  [`line`](#convex.read/line) - Reads a line from the given <code>java.io.BufferedReader</code> and parses the result as a CVX list of cells.
    -  [`resource`](#convex.read/resource) - Reads one cell from resource located under <code>path</code> on the classpath.
    -  [`stream`](#convex.read/stream) - Reads all cells from the given <code>java.io.Reader</code> (parent class of text streams) and returns them in a CVX list.
    -  [`string`](#convex.read/string) - Reads all cells from the given <code>string</code> and returns them in a CVX list.
-  [`convex.std`](#convex.std)  - Provides an API for cells with classic <code>convex.core</code> functions such as [[conj]].
    -  [`*`](#convex.std/*) - Like classic <code>*</code> but for numeric cells.
    -  [`+`](#convex.std/+) - Like classic <code>+</code> but for numeric cells.
    -  [`-`](#convex.std/-) - Like classic <code>-</code> but for numeric cells.
    -  [`<`](#convex.std/<) - Like classic <code><</code> but with numeric cells.
    -  [`<=`](#convex.std/<=) - Like classic <code><=</code> but with numeric cells.
    -  [`==`](#convex.std/==) - Like classic <code>==</code> but with numeric cells.
    -  [`>`](#convex.std/>) - Like classic <code>></code> but with numeric cells.
    -  [`>=`](#convex.std/>=) - Like classic <code>>=</code> but with numeric cells.
    -  [`abs`](#convex.std/abs) - Returns the absolute value of <code>x</code>.
    -  [`account-key`](#convex.std/account-key) - Coerces the given <code>cell</code> to an account key or return nil.
    -  [`address`](#convex.std/address) - Coerces the given <code>cell</code> to an address or return nil.
    -  [`address?`](#convex.std/address?) - Is <code>x</code> an address?.
    -  [`assoc`](#convex.std/assoc) - Like classic <code>assoc</code> but for collection cells.
    -  [`blob`](#convex.std/blob) - Coerces the given <code>cell</code> to a blob or return nil.
    -  [`blob-map`](#convex.std/blob-map) - Builds a blob map from key-values (keys must be blobs).
    -  [`blob-map?`](#convex.std/blob-map?) - Is <code>x</code> a blob map?.
    -  [`blob?`](#convex.std/blob?) - Is <code>x</code> a blob?.
    -  [`boolean?`](#convex.std/boolean?) - Is <code>x</code> a CVM boolean?.
    -  [`byte`](#convex.std/byte) - Coerces the given <code>cell</code> to a byte or return nil.
    -  [`byte?`](#convex.std/byte?) - Is <code>x</code> a byte cell?.
    -  [`ceil`](#convex.std/ceil) - Returns a double cell ceiling the value of <code>number</code>.
    -  [`cell?`](#convex.std/cell?) - Is <code>x</code> a cell?.
    -  [`char`](#convex.std/char) - Coerces the given <code>cell</code> to a char or return nil.
    -  [`char?`](#convex.std/char?) - Is <code>x</code> a char cell?.
    -  [`coll?`](#convex.std/coll?) - Is <code>x</code> a collection cell?.
    -  [`concat`](#convex.std/concat) - Like classic <code>concat</code> but for collection cells.
    -  [`conj`](#convex.std/conj) - Akin to classic <code>conj</code> but for collection cells.
    -  [`cons`](#convex.std/cons) - Like classic <code>cons</code> but for collection cells.
    -  [`contains?`](#convex.std/contains?) - Like classic <code>contains?</code> but for collection cells.
    -  [`count`](#convex.std/count) - Returns a JVM long representing the number of itms in the given cell.
    -  [`cvm-value?`](#convex.std/cvm-value?) - Is <code>x</code> a CVM value? Returns false if <code>x</code> is not accessible to the CVM and meant to be used outside (eg.
    -  [`dec`](#convex.std/dec) - Like classic <code>dec</code> but for long cells.
    -  [`difference`](#convex.std/difference) - Like <code>clojure.set/difference</code> but for set cells.
    -  [`dissoc`](#convex.std/dissoc) - Like classic <code>dissoc</code> but for map cells.
    -  [`div`](#convex.std/div) - Like classic <code>/</code> but for numeric cells.
    -  [`double`](#convex.std/double) - Coerces the given <code>cell</code> to a double or return nil.
    -  [`double?`](#convex.std/double?) - Is <code>x</code> a double cell?.
    -  [`empty`](#convex.std/empty) - Like classic <code>empty</code> but for collection cells.
    -  [`empty?`](#convex.std/empty?) - Is the given <code>countable</code> empty? See [[count]].
    -  [`exp`](#convex.std/exp) - Returns <code>e</code> raised to the power of the given numeric cell.
    -  [`false?`](#convex.std/false?) - Is <code>x</code> a <code>false</code> cell?.
    -  [`find`](#convex.std/find) - Like classic <code>find</code>` but for map cells.
    -  [`floor`](#convex.std/floor) - Returns a double cell flooring the value of <code>x</code>.
    -  [`get`](#convex.std/get) - Like classic <code>get</code> but for collection cells.
    -  [`hash-map`](#convex.std/hash-map) - Builds a map from key-values.
    -  [`hash-map?`](#convex.std/hash-map?) - Is <code>x</code> a hash map cell?.
    -  [`hash-set`](#convex.std/hash-set) - Builds a set from the given cells.
    -  [`hash-set?`](#convex.std/hash-set?) - Is <code>x</code> a hash set cell? Currently at least, hast sets are the only kind of available sets.
    -  [`inc`](#convex.std/inc) - Like classic <code>inc</code> but for long cells.
    -  [`intersection`](#convex.std/intersection) - Like <code>clojure.set/intersection</code> but for set cells.
    -  [`into`](#convex.std/into) - Like classic <code>into</code> but <code>to</code> is a collection cell.
    -  [`keys`](#convex.std/keys) - Like classic <code>keys</code> but for map cells.
    -  [`keyword`](#convex.std/keyword) - Coerces the given <code>cell</code> to a keyword or return nil.
    -  [`keyword?`](#convex.std/keyword?) - Is <code>x</code> a keyword cell?.
    -  [`list`](#convex.std/list) - Buildsa list from the given cells.
    -  [`list?`](#convex.std/list?) - Is <code>x</code> a list cell?.
    -  [`long`](#convex.std/long) - Coerces the given <code>cell</code> to a long or return nil.
    -  [`long?`](#convex.std/long?) - Is <code>x</code> a long cell?.
    -  [`map?`](#convex.std/map?) - Is <code>x</code> a map cell?.
    -  [`merge`](#convex.std/merge) - Like classic <code>merge</code> but for hash map cells (not blob maps).
    -  [`mod`](#convex.std/mod) - Returns the integer modulus of a numerator divided by a divisor.
    -  [`name`](#convex.std/name) - Like classic <code>name</code> but for keyword and symbol cells.
    -  [`nan?`](#convex.std/nan?) - Is the given <code>cell</code> NaN?.
    -  [`next`](#convex.std/next) - Like classic <code>next</code> but for collection cells.
    -  [`nth`](#convex.std/nth) - Like classic <code>nth</code> but for countables.
    -  [`number?`](#convex.std/number?) - Is <code>x</code> a numeric cell? Either a long or a double.
    -  [`pow`](#convex.std/pow) - Returns a CVM double, <code>x</code> raised to the power of <code>y</code>.
    -  [`reverse`](#convex.std/reverse) - Like classic <code>reverse</code> but for sequential cells (list or vector cells).
    -  [`set`](#convex.std/set) - Coerces the given <code>cell</code> to a set or return nil.
    -  [`set?`](#convex.std/set?) - Is <code>x</code> a set cell? Currently at least, hast sets are the only kind of available sets.
    -  [`signum`](#convex.std/signum) - Returns the sign of the number: - <code>-1</code> if negative - <code>0</code> if 0 - <code>1</code> if positive As a long cell if input is a long, double cell if it is a double.
    -  [`sqrt`](#convex.std/sqrt) - Returns a double cell, the square root of the given <code>number</code> cell.
    -  [`str`](#convex.std/str) - Stringifies the given cell(s).
    -  [`string?`](#convex.std/string?) - Is <code>x</code> a string cell?.
    -  [`subset?`](#convex.std/subset?) - Like <code>clojure.set/subset?</code> but for set cells.
    -  [`symbol`](#convex.std/symbol) - Coerces the given <code>cell</code> to a symbol or return nil.
    -  [`symbol?`](#convex.std/symbol?) - Is <code>x</code> a symbol cell?.
    -  [`syntax?`](#convex.std/syntax?) - Is <code>x</code> a syntax cell?.
    -  [`true?`](#convex.std/true?) - Is <code>x</code> a <code>true</code> cell?.
    -  [`union`](#convex.std/union) - Like <code>clojure.set/union</code> but for set cells.
    -  [`update`](#convex.std/update) - Akin to classic <code>update</code> but for collection cell.
    -  [`vals`](#convex.std/vals) - Like classic <code>vals</code> but for map cells.
    -  [`vec`](#convex.std/vec) - Coerces the given <code>cell</code> to a vector or return nil.
    -  [`vector`](#convex.std/vector) - Builds a vector from the given cells.
    -  [`vector?`](#convex.std/vector?) - Is <code>x</code> a vector cell?.
    -  [`zero?`](#convex.std/zero?) - Like classic <code>zero?</code> but for cells.
-  [`convex.write`](#convex.write)  - Writing, encoding CVX cells various kind of sources.
    -  [`stream`](#convex.write/stream) - Writes the given <code>cell</code> to the given <code>java.io.Writer</code> (parent class of text streams).
    -  [`string`](#convex.write/string) - Prints the given <code>cell</code> as a string cell.

-----
# <a name="convex.cell">convex.cell</a>


Constructors for CVM cells and related type predicate functions.




## <a name="convex.cell/*">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L763-L782) `*`</a>
``` clojure

(* x)
```


Macro.


Macro for translating Clojure types to Convex types.
  
   Convex types can be inserted using `~`, especially useful for inserting values dynamically or inserting types
   that have no equivalent in Clojure (eg. `address`).

   Also understands `~@` (aka unquote splicing).
   

   ```clojure
   ;; Cell for `(transfer #42 500000)`
   ;;
   (* (transfer ~(address 42)
                500000))
   ```

## <a name="convex.cell/IEquivalent">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L603-L613) `IEquivalent`</a>

Translates Clojure types to equivalent Convex types. Other objects remain as they are.

   However, the [`*`](#convex.cell/*) macro is usually preferred for performance.

   ```clojure
   (any {:a ['b]})
   ```

## <a name="convex.cell/address">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L114-L122) `address`</a>
``` clojure

(address long)
```


Creates an address from a long.

## <a name="convex.cell/any">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L603-L613) `any`</a>
``` clojure

(any data)
```


## <a name="convex.cell/blob">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L126-L134) `blob`</a>
``` clojure

(blob byte-array)
```


Creates a blob from a byte array.

## <a name="convex.cell/blob-map">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L150-L169) `blob-map`</a>
``` clojure

(blob-map)
(blob-map kvs)
```


Creates a blob map from a collection of `[blob value]`.

## <a name="convex.cell/blob<-hex">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L138-L146) `blob<-hex`</a>
``` clojure

(blob<-hex hex-string)
```


Creates a blob from a hex string.

## <a name="convex.cell/boolean">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L173-L181) `boolean`</a>
``` clojure

(boolean x)
```


Creates a boolean cell given a falsy or truthy value.

## <a name="convex.cell/byte">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L185-L193) `byte`</a>
``` clojure

(byte b)
```


Creates a byte cell from a value between 0 and 255 inclusive.

## <a name="convex.cell/call">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L197-L218) `call`</a>
``` clojure

(call address sequence address-callable function-name args)
(call address sequence address-callable offer function-name args)
```


Creates a transaction for invoking a callable function.

## <a name="convex.cell/char">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L295-L303) `char`</a>
``` clojure

(char ch)
```


Creates a character cell from a regular character.

## <a name="convex.cell/code-std*">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L222-L291) `code-std*`</a>
``` clojure

(code-std* kw)
```


Macro.


Given a Clojure keyword, returns the corresponding standard error code (any of the Convex keyword the CVM itself
   uses):
  
   - `:ARGUMENT`
   - `:ARITY`
   - `:ASSERT`
   - `:BOUNDS`
   - `:CAST`
   - `:COMPILE`
   - `:DEPTH`
   - `:EXCEPTION`
   - `:EXPAND`
   - `:FATAL`
   - `:FUNDS`
   - `:HALT`
   - `:JUICE`
   - `:MEMORY`
   - `:NOBODY`
   - `:RECUR`
   - `:REDUCED`
   - `:RETURN`
   - `:ROLLBACK`
   - `:SEQUENCE`
   - `:SIGNATURE`
   - `:STATE`
   - `:TAILCALL`
   - `:TODO`
   - `:TRUST`
   - `:UNDECLARED`
   - `:UNEXPECTED`
  
   Throws if keyword does not match any of those.
  
   Note that in user functions, codes can be anything, any type, using those codes is not at all mandatory.

## <a name="convex.cell/double">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L307-L315) `double`</a>
``` clojure

(double x)
```


Creates a double cell.

## <a name="convex.cell/encoding">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L319-L329) `encoding`</a>
``` clojure

(encoding cell)
```


Returns a [`blob`](#convex.cell/blob) representing the encoding of the given `cell`.

   This encoding is meant for incremental updates.

## <a name="convex.cell/error">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L423-L447) `error`</a>
``` clojure

(error message)
(error code message)
(error code message trace)
```


An error value as Convex data.

     `code` is often a keyword cell (`:ASSERT` by default), `message` could be any cell (albeit often a human-readable
     string), and `trace` is an optional stacktrace (vector cell of string cells).

## <a name="convex.cell/hash">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L333-L343) `hash`</a>
``` clojure

(hash cell)
```


Returns the hash of the given `cell`.
  
   A hash is a specialized 32-byte [`blob`](#convex.cell/blob).

## <a name="convex.cell/hash<-blob">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L347-L355) `hash<-blob`</a>
``` clojure

(hash<-blob blob)
```


Converts a 32-byte [`blob`](#convex.cell/blob) to a [`hash`](#convex.cell/hash).

## <a name="convex.cell/hash<-hex">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L359-L369) `hash<-hex`</a>
``` clojure

(hash<-hex hex-string)
```


Creates a [`hash`](#convex.cell/hash) from a hex string.
  
   Returns nil if hex string is of wrong format.

## <a name="convex.cell/invoke">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L373-L383) `invoke`</a>
``` clojure

(invoke address sequence-id cell)
```


Creates a transaction for invoking code (a cell).

## <a name="convex.cell/key">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L387-L397) `key`</a>
``` clojure

(key blob)
```


Creates an account key from a 32-byte [`blob`](#convex.cell/blob).

   Returns nil if the given [`blob`](#convex.cell/blob) is of wrong size.

## <a name="convex.cell/key-fake">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L401-L405) `key-fake`</a>

Zeroed [`key`](#convex.cell/key) that can be used during dev and testing so that an account is considered as a user, not an actor.

## <a name="convex.cell/keyword">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L409-L417) `keyword`</a>
``` clojure

(keyword string)
```


Creates a keyword cell from a string.

## <a name="convex.cell/list">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L451-L463) `list`</a>
``` clojure

(list)
(list x)
```


Creates a list cell from a collection of cells.

## <a name="convex.cell/long">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L467-L475) `long`</a>
``` clojure

(long x)
```


Creates a long cell.

## <a name="convex.cell/map">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L479-L494) `map`</a>
``` clojure

(map)
(map kvs)
```


Creates a map cell from a collection of `[key value]`.

## <a name="convex.cell/quoted">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L590-L597) `quoted`</a>
``` clojure

(quoted x)
```


Wraps `x` in `quote`.

## <a name="convex.cell/set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L498-L510) `set`</a>
``` clojure

(set)
(set x)
```


Creates a set cell from a collection of items cell.

## <a name="convex.cell/string">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L514-L522) `string`</a>
``` clojure

(string string)
```


Creates a string cell from a regular string.

## <a name="convex.cell/symbol">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L526-L534) `symbol`</a>
``` clojure

(symbol string)
```


Creates a symbol cell from a string.

## <a name="convex.cell/syntax">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L538-L553) `syntax`</a>
``` clojure

(syntax cell)
(syntax cell metadata)
```


Creates a syntax cell.

   It wraps the given `cell` and allow attaching a metadata [`map`](#convex.cell/map).

## <a name="convex.cell/transfer">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L557-L568) `transfer`</a>
``` clojure

(transfer address sequence address-receiver amount)
```


Creates a transaction for transferring Convex Coins.

## <a name="convex.cell/vector">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cell.clj#L572-L584) `vector`</a>
``` clojure

(vector)
(vector x)
```


Creates a vector cell from a collection of items cell.

-----
# <a name="convex.clj">convex.clj</a>


Convert cells to Clojure types.
  
   Sometimes lossy since some cells do not have equivalents in Clojure. For instance, addresses are converted to long.
   Recursive when it comes to collection.
  
   Mainly useful for a deeper Clojure integration.




## <a name="convex.clj/IClojuresque">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L235-L245) `IClojuresque`</a>

Generic function for converting a cell to a Clojure representation.
  
   Relies all other functions from this namespace.

   ```clojure
   (any (convex.cell/* {:a [:b]}))
   ```

## <a name="convex.clj/address">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L46-L52) `address`</a>
``` clojure

(address address)
```


Returns the given `address` as a JVM long.

## <a name="convex.clj/any">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L235-L245) `any`</a>
``` clojure

(any cell)
```


## <a name="convex.clj/blob">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L56-L62) `blob`</a>
``` clojure

(blob blob)
```


Returns the given `blob` as a byte array.

## <a name="convex.clj/blob->hex">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L66-L72) `blob->hex`</a>
``` clojure

(blob->hex blob)
```


Returns the given `blob` as a hex string.

## <a name="convex.clj/boolean">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L76-L82) `boolean`</a>
``` clojure

(boolean boolean)
```


Returns the given `boolean` cell as a JVM boolean.

## <a name="convex.clj/byte">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L86-L92) `byte`</a>
``` clojure

(byte cell)
```


Returns the given `byte` cell as a JVM long.

## <a name="convex.clj/char">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L96-L102) `char`</a>
``` clojure

(char char)
```


Returns the given `char` cell as a JVM char.

## <a name="convex.clj/double">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L106-L112) `double`</a>
``` clojure

(double double)
```


Returns the given `double` cell as a JVM double.

## <a name="convex.clj/keyword">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L116-L122) `keyword`</a>
``` clojure

(keyword keyword)
```


Returns the given `keyword` cell as a Clojure keyword.

## <a name="convex.clj/list">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L126-L133) `list`</a>
``` clojure

(list list)
```


Returns the given `list` cell as a Clojure list.

## <a name="convex.clj/long">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L137-L143) `long`</a>
``` clojure

(long long)
```


Returns the given `long` cell as a JVM long.

## <a name="convex.clj/map">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L147-L169) `map`</a>
``` clojure

(map map)
```


Returns the given `map` cell (hash map or blob map) as a Clojure map.
  
   Attention, in Clojure maps, sequential types containg the same items are equivalent but
   not in Convex. Hence, a clash could happen in the rare case where different sequential types
   are used as keys. For instance, the following is possible in Convex but not in Clojure (would
   complain about duplicate keys:

   ```clojure
   {[:a]  :foo
    '(:a) :foo}
   ```

## <a name="convex.clj/set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L173-L183) `set`</a>
``` clojure

(set set)
```


Returns the given `set` cell as a Clojure set.
  
   Same comment about sequential types as in [`map`](#convex.clj/map) applies here.

## <a name="convex.clj/string">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L187-L193) `string`</a>
``` clojure

(string string)
```


Returns the given `string` cell as a JVM string.

## <a name="convex.clj/symbol">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L197-L203) `symbol`</a>
``` clojure

(symbol symbol)
```


Returns the given `symbol` cell as a Clojure symbol.

## <a name="convex.clj/syntax">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L207-L219) `syntax`</a>
``` clojure

(syntax syntax)
```


Returns the given `syntax` cell as a Clojure map such as:

   | Key | Value |
   |---|---|
   | `:meta` | Clojure map of metadata |
   | `:value` | Value wrapped, converted as well |

## <a name="convex.clj/vector">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/clj.clj#L222-L229) `vector`</a>
``` clojure

(vector vector)
```


Returns the given `vector` cell as a Clojure vector.

-----
# <a name="convex.cvm">convex.cvm</a>


Code execution in the Convex Virtual Machine, altering its state, and gaining insights.

   The central entity of this namespace is the execution context created by [`ctx`](#convex.cvm/ctx). They embed a [`state`](#convex.cvm/state) and allow
   executing code to alter it.

   All other functions revolve around contextes. While the design of a context is mostly immutable, whenever an altering function
   is applied (eg. [`juice-set`](#convex.cvm/juice-set)) or code is handled in any way (eg. [`eval`](#convex.cvm/eval)), the old context must be discarded and only the
   returned one should be used.

   Cheap copies can be created using [`fork`](#convex.cvm/fork).

   Actions involving code (eg. [`compile`](#convex.cvm/compile), [`exec`](#convex.cvm/exec), ...) return a new context which holds either a [`result`](#convex.cvm/result) or an [`exception`](#convex.cvm/exception).
   Those actions always consume [`juice`](#convex.cvm/juice).

   Given that a "cell" is the term reserved for CVM data and objects, execution consists of the following steps:

   | Step | Function    | Does                                                |
   |------|-------------|-----------------------------------------------------|
   | 1    | [`expand`](#convex.cvm/expand)  | `cell` -> `canonical cell`, applies macros          |
   | 2    | [`compile`](#convex.cvm/compile) | `canonical cell` -> `op`, preparing executable code |
   | 3    | [`exec`](#convex.cvm/exec)    | Executes compiled code                              |

   Any cell can be applied safely to those functions, worse that can happen is nothing (e.g. providing an already compiled cell to
   [`compile`](#convex.cvm/compile)).

   If fine-grained control is not needed and if source is not compiled anyways, a simpler alternative is to use [`eval`](#convex.cvm/eval) which does
   all the job.




## <a name="convex.cvm/account">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L170-L183) `account`</a>
``` clojure

(account ctx)
(account ctx address)
```


Returns the account for the given `address` (or the address associated with `ctx`).

## <a name="convex.cvm/account-create">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L406-L424) `account-create`</a>
``` clojure

(account-create ctx)
(account-create ctx key)
```


Creates an new account, with a `key` (user) or without (actor).

   See [`convex.cell/key`](#convex.cell/key).
  
   Address is attached as a result in the returned context.

## <a name="convex.cvm/address">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L187-L195) `address`</a>
``` clojure

(address ctx)
```


Returns the executing address of the given `ctx`.

## <a name="convex.cvm/arg+*">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L751-L766) `arg+*`</a>
``` clojure

(arg+* & arg+)
```


Macro.


See [`invoke`](#convex.cvm/invoke).

## <a name="convex.cvm/compile">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L658-L677) `compile`</a>
``` clojure

(compile ctx)
(compile ctx canonical-cell)
```


Compiles the `canonical-cell` into executable code.

   Fetched using [`result`](#convex.cvm/result) if not given.

   Returns a new `ctx` with a [`result`](#convex.cvm/result) ready for [`exec`](#convex.cvm/exec) or an [`exception`](#convex.cvm/exception) in case of
   failure.

## <a name="convex.cvm/ctx">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L89-L118) `ctx`</a>
``` clojure

(ctx)
(ctx option+)
```


Creates an execution context.
  
   An optional map of options may be provided:

   | Key                        | Value                                           | Default                                            |
   |----------------------------|-------------------------------------------------|----------------------------------------------------|
   | `:convex.cvm/address`      | Address of the executing account                | [`genesis-user`](#convex.cvm/genesis-user)                                   |
   | `:convex.cvm/genesis-key+` | Vector of keys for genesis users (at least one) | Vector with only [`fake-key`](#convex.cvm/fake-key) for [`genesis-user`](#convex.cvm/genesis-user) |
   | `:convex.cvm/state`        | State (see [`state`](#convex.cvm/state))                           | Initial state with Convex actors and libraries     |
  
   More than one genesis key can be provided in order to create more users than [`genesis-user`](#convex.cvm/genesis-user).
   However, it is important those public keys are different otherwise an exception is thrown.

   See [`convex.cell/key`](#convex.cell/key) about creating public keys.

## <a name="convex.cvm/def">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L428-L458) `def`</a>
``` clojure

(def ctx sym->value)
(def ctx addr sym->value)
```


Like calling `(def sym value)` in Convex Lisp, either in the current address of the given one.

   Argument is a map of `symbol cell` -> `cell`.

## <a name="convex.cvm/deploy">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L462-L474) `deploy`</a>
``` clojure

(deploy ctx code)
```


Deploys the given `code` as an actor.
  
   Returns a context that is either [`exception`](#convex.cvm/exception)al or has the address of the successfully created actor
   attached as a [`result`](#convex.cvm/result).

## <a name="convex.cvm/env">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L199-L212) `env`</a>
``` clojure

(env ctx)
(env ctx address)
```


Returns the environment of the executing account attached to `ctx`.

## <a name="convex.cvm/eval">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L725-L745) `eval`</a>
``` clojure

(eval ctx)
(eval ctx cell)
```


Evaluates the given `cell` after forking the `ctx`, going efficiently through [`expand`](#convex.cvm/expand), [`compile`](#convex.cvm/compile), and [`exec`](#convex.cvm/exec).

   Works with any kind of `cell` and is sufficient when there is no need for fine-grained control.

   An important difference with the aforementioned cycle is that the cell passes through `*lang*`, a function
   possibly set by the user for intercepting a cell (eg. modifying the cell and evaluating explicitley).

   Returns the forked `ctx` with a [`result`](#convex.cvm/result) or an [`exception`](#convex.cvm/exception) in case of failure.

## <a name="convex.cvm/exception">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L216-L243) `exception`</a>
``` clojure

(exception ctx)
(exception code ctx)
```


The CVM enters in exceptional state in case of error or particular patterns such as
   halting or doing a rollback.

   Returns the current exception or nil if `ctx` is not in such a state meaning that [`result`](#convex.cvm/result)
   can be safely used.
  
   An exception code can be provided as a filter, meaning that even if an exception occured, this
   functions will return nil unless that exception has the given `code`.
  
   Also see [`convex.cell/code-std*`](#convex.cell/code-std*) for easily retrieving an official error code. Note that in practice, unlike the CVM
   itself or any of the core function, a user Convex function can return anything as a code.

## <a name="convex.cvm/exception-clear">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L478-L487) `exception-clear`</a>
``` clojure

(exception-clear ctx)
```


Removes the currently attached exception from the given `ctx`.

## <a name="convex.cvm/exception-code">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L268-L278) `exception-code`</a>
``` clojure

(exception-code exception)
```


Returns the code associated with the given [`exception`](#convex.cvm/exception).
  
   Often a CVX keyword but could be any CVX value.

## <a name="convex.cvm/exception-message">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L282-L292) `exception-message`</a>
``` clojure

(exception-message exception)
```


Returns the message associated with the given [`exception`](#convex.cvm/exception).

   Often a CVX string but could be any CVX value.

## <a name="convex.cvm/exception?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L247-L264) `exception?`</a>
``` clojure

(exception? ctx)
(exception? code ctx)
```


Returns true if the given `ctx` is in an exceptional state.

   See [`exception`](#convex.cvm/exception).

## <a name="convex.cvm/exec">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L701-L719) `exec`</a>
``` clojure

(exec ctx)
(exec ctx op)
```


Executes compiled code.
  
   Usually run after [`compile`](#convex.cvm/compile).
  
   Returns a new `ctx` with a [`result`](#convex.cvm/result) or an [`exception`](#convex.cvm/exception) in case of failure.

## <a name="convex.cvm/expand">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L635-L654) `expand`</a>
``` clojure

(expand ctx)
(expand ctx cell)
```


Expands `cell` into a `canonical cell` by applying macros.
  
   Fetched using [`result`](#convex.cvm/result) if not given.

   Returns a new `ctx` with a [`result`](#convex.cvm/result) ready for [`compile`](#convex.cvm/compile) or an [`exception`](#convex.cvm/exception) in case
   of failure.

## <a name="convex.cvm/expand-compile">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L681-L695) `expand-compile`</a>
``` clojure

(expand-compile ctx)
(expand-compile ctx cell)
```


Chains [`expand`](#convex.cvm/expand) and [`compile`](#convex.cvm/compile) in a slightly more efficient fashion than calling both separately.

## <a name="convex.cvm/fake-key">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L68-L72) `fake-key`</a>

Fake key (all zeroes) meant for testing.

## <a name="convex.cvm/fork">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L122-L134) `fork`</a>
``` clojure

(fork ctx)
```


Duplicates the given [`ctx`](#convex.cvm/ctx) (very cheap).

   Any operation on the returned copy has no impact on the original context.
  
   Attention, forking a `ctx` looses any attached [`result`](#convex.cvm/result) or [`exception`](#convex.cvm/exception).

## <a name="convex.cvm/fork-to">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L138-L149) `fork-to`</a>
``` clojure

(fork-to ctx address)
```


Like [`fork`](#convex.cvm/fork) but switches the executing account.
  
   Note: CVM log is lost.

## <a name="convex.cvm/genesis-user">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L76-L83) `genesis-user`</a>

Address of the first genesis user when the CVM [`state`](#convex.cvm/state) is created in [`ctx`](#convex.cvm/ctx).
   Might change in the future.

   It receives half of the funds reserved for all users in the state.

## <a name="convex.cvm/invoke">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L770-L791) `invoke`</a>
``` clojure

(invoke ctx f arg+)
```


Invokes the given CVM `f`unction using the given `ctx`.

   `arg+` is a Java array of cells. See [`arg+*`](#convex.cvm/arg+*) for easily and efficiently creating one.
  
   Returns a new `ctx` with a [`result`](#convex.cvm/result) or an [`exception`](#convex.cvm/exception) in case of failure.

## <a name="convex.cvm/juice">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L296-L304) `juice`</a>
``` clojure

(juice ctx)
```


Returns the remaining amount of juice available for the executing account.
  
   Also see [`juice-set`](#convex.cvm/juice-set).

## <a name="convex.cvm/juice-preserve">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L491-L503) `juice-preserve`</a>
``` clojure

(juice-preserve ctx f)
```


Executes `(f ctx)`, `f` being a function `ctx` -> `ctx`.
  
   The returned `ctx` will have the same amount of juice as the original.

## <a name="convex.cvm/juice-refill">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L507-L518) `juice-refill`</a>
``` clojure

(juice-refill ctx)
```


Refills juice to maximum.

   Also see [`juice-set`](#convex.cvm/juice-set).

## <a name="convex.cvm/juice-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L522-L533) `juice-set`</a>
``` clojure

(juice-set ctx amount)
```


Sets the juice of the given `ctx` to the requested `amount`.
  
   Also see [`juice`](#convex.cvm/juice), [`juice-refill`](#convex.cvm/juice-refill).

## <a name="convex.cvm/key">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L308-L320) `key`</a>
``` clojure

(key ctx)
(key ctx address)
```


Returns the key of the given `address` (or the address associated with `ctx`).

## <a name="convex.cvm/key-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L537-L546) `key-set`</a>
``` clojure

(key-set ctx key)
```


Sets `key` on the address curently associated with `ctx`.

## <a name="convex.cvm/log">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L325-L334) `log`</a>
``` clojure

(log ctx)
```


Returns the log of `ctx` (a vector cell of size 2 vectors containing a logging address
   and a logged value).

## <a name="convex.cvm/look-up">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L338-L355) `look-up`</a>
``` clojure

(look-up ctx sym)
(look-up ctx address sym)
```


Returns the cell associated with the given `sym` in the environment of the given `address`
   (or the currently used one).

## <a name="convex.cvm/result">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L359-L367) `result`</a>
``` clojure

(result ctx)
```


Extracts the result (eg. after expansion, compilation, execution, ...) wrapped in a `ctx`.
  
   Throws if the `ctx` is in an exceptional state. See [`exception`](#convex.cvm/exception).

## <a name="convex.cvm/result-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L550-L559) `result-set`</a>
``` clojure

(result-set ctx result)
```


Attaches the given `result` to `ctx`, as if it was the result of a transaction.

## <a name="convex.cvm/state">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L371-L384) `state`</a>
``` clojure

(state ctx)
```


Returns the whole CVM state associated with `ctx`.

   It is a special type of cell behaving like a map cell. It notably holds all accounts and can be explored
   using [`convex.std`](#convex.std) map functions.
  
   Also see [`state-set`](#convex.cvm/state-set).

## <a name="convex.cvm/state-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L563-L574) `state-set`</a>
``` clojure

(state-set ctx state)
```


Replaces the CVM state in the `ctx` with the given one.
  
   See [`state`](#convex.cvm/state).

## <a name="convex.cvm/time">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L388-L400) `time`</a>
``` clojure

(time ctx)
```


Returns the current timestamp (Unix epoch in milliseconds as long cell) assigned to the state in the given `ctx`.
  
   Also see [[time-set]].

## <a name="convex.cvm/time-advance">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L578-L597) `time-advance`</a>
``` clojure

(time-advance ctx millis)
```


Advances the timestamp in the state of `ctx` by `millis` milliseconds.
   Scheduled transactions will be executed if necessary.
  
   Does not do anything if `millis` is < 0.
  
   See [`time`](#convex.cvm/time).

## <a name="convex.cvm/undef">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/cvm.clj#L601-L629) `undef`</a>
``` clojure

(undef ctx sym+)
(undef ctx addr sym+)
```


Like calling `(undef sym)` in Convex Lisp, either in the current account or the given one, repeatedly
   on any symbol cell in `sym+`.

-----

-----
# <a name="convex.db">convex.db</a>


Etch is a fast, immutable, embedded database tailored for cells.

   It can be understood as a data store where keys are hashes of the cells they point to.
   Hence, the API is pretty simple. [`write`](#convex.db/write) takes a cell and returns a hash while [`read`](#convex.db/read)
   takes a hash and returns a cell (or nil if not found).

   Most of the time, usage is even simpler by using [`root-write`](#convex.db/root-write) and [`root-read`](#convex.db/root-read) to persist
   the state of a whole application at once (only new values are effectively written).

   Data is retrieved semi-lazily. For instance, in the case of a large vector, only the 
   the top structure of that vector is fetched. Elements are read from disk when actually accessed
   then cached using a clever system of soft references under the hood. This explains why data
   larger than memory can be retrieved and handled since the JVM can garbage-collect those soft
   references ; their value will be read from disk again if required. Nonetheless, users only deal
   with cells and all this process is completely transparent.

   Attention, although this namespace is straightforward, one rule must be followed at all time:
   cells read from an instance can only be written back to that instance. In other words, one
   must never mix cells read from different instances with the intent of writing them anywhere.
   This will result in some of the data not being written. Everything, from cells to Etch, has
   been heavily optimized for Convex peers that only ever handle 1 instance at a time. It is
   fine using several stores in the same process as long as operations never cross-over.

   Convex tooling, whenever an instance is needed, will always look for the instance associated 
   with the current thread (if any). The typical workflow is to call [`current-set`](#convex.db/current-set) after [`open`](#convex.db/open):

   ```clojure
   (convex.db/current-set (convex.db/open "my/instance.etch"))
   (convex.db/read (convex.db/write (convex.cell/* [:a :b 42])))
   (convex.db/close)
   ```

   If no instance is bound to the current thread explicitely, a temporary one is created whenever needed.
   See [`global-set`](#convex.db/global-set) for improving the workflow when an instance is needed in more than one thread.
  
   When using a [`convex.cvm/ctx`](#convex.cvm/ctx), its state is initially hold in memory. After opening an Etch
   instance and setting it as thread-local, this state can be retrieved at any point using [`convex.cvm/state`](#convex.cvm/state)
   and persisted to disk since it is a cell. This renders that state garbage-collecteable as exposed
   above. Of course, it is important not to close the instance before stopping all operations on that
   context and its state.




## <a name="convex.db/close">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/db.clj#L107-L118) `close`</a>
``` clojure

(close)
```


Flushes and closes the thread-local instance. Also unbinds it from the current thread.
   
   Note that all instances are also cleanly closed on JVM shutdown but it is
   more predictable doing it manually.

## <a name="convex.db/current">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/db.clj#L62-L71) `current`</a>
``` clojure

(current)
```


Returns the thread-local instance (or nil).
   See [`current-set`](#convex.db/current-set).

## <a name="convex.db/current-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/db.clj#L75-L86) `current-set`</a>
``` clojure

(current-set instance)
```


Binds the given `instance` to the current thread.
   Returns the `instance`.
   See [`current`](#convex.db/current).

## <a name="convex.db/flush">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/db.clj#L122-L131) `flush`</a>
``` clojure

(flush)
```


Flushes the thread-local instance, ensuring all changes are persisted to disk.

## <a name="convex.db/global-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/db.clj#L90-L101) `global-set`</a>
``` clojure

(global-set instance)
```


When an instance is used in more than one thread, it is a good idea using this function.
   Convex tooling will then use the given `instance` in all thread automatically unless it is
   overwritten with [`current-set`](#convex.db/current-set) on a thread per thread basis.

## <a name="convex.db/open">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/db.clj#L135-L148) `open`</a>
``` clojure

(open path)
```


Opens an instance at the given `path`.
   File is created if needed.

## <a name="convex.db/open-tmp">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/db.clj#L152-L165) `open-tmp`</a>
``` clojure

(open-tmp)
(open-tmp prefix)
```


Like [`open`](#convex.db/open) but creates a temporary file.
   A prefix string may be provided for the filename.

## <a name="convex.db/path">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/db.clj#L169-L175) `path`</a>
``` clojure

(path)
```


Returns the path of thread-local instance.

## <a name="convex.db/read">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/db.clj#L181-L192) `read`</a>
``` clojure

(read hash)
```


Reads from the thread-local instance and returns the cell for the given `hash` (or nil
   if not found).

## <a name="convex.db/root-read">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/db.clj#L219-L234) `root-read`</a>
``` clojure

(root-read)
```


Returns the cell stored at the root of the thread-local instance.

   The root is a place in the instance that can be read without providing a hash. It is commonly
   used for storing the whole state of an application or at least some sort of index containing
   hashes of other data in the instance. This makes Etch self-sufficient as no hash must be stored
   externally.

   See [[write-root]].

## <a name="convex.db/root-write">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/db.clj#L238-L252) `root-write`</a>
``` clojure

(root-write cell)
```


Writes the given `cell` to the root of the thread-local instance and returns its hash.
   Behaves like [`write`](#convex.db/write).

   See [[read-root]].

## <a name="convex.db/write">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/db.clj#L196-L213) `write`</a>
``` clojure

(write cell)
```


Writes the given `cell` to the thread-local instance and returns its hash.

   Very basic cells are not persisted because that would be inefficient and hardly ever happens.
   They are typically embedded in collections. Hence, this function will return nil for:

     - Address
     - Empty collections
     - Primitives (boolean, byte, double, long)
     - Symbolic (keywords and symbols)

-----
# <a name="convex.eval">convex.eval</a>


Quick helpers built on top of [`convex.cvm/eval`](#convex.cvm/eval).
  
   Systematically forks the used context before any operation so that it remains intact.
  
   Notably useful when writing tests.




## <a name="convex.eval/ctx">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/eval.clj#L17-L24) `ctx`</a>
``` clojure

(ctx ctx cell)
```


Evaluates the given `cell` and returns `ctx`.

## <a name="convex.eval/exception">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/eval.clj#L30-L38) `exception`</a>
``` clojure

(exception ctx cell)
```


Like [`ctx`](#convex.eval/ctx) but returns the current exception or nil if there is none.

## <a name="convex.eval/exception-code">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/eval.clj#L41-L50) `exception-code`</a>
``` clojure

(exception-code ctx cell)
```


Shortcut on top of [`exception`](#convex.eval/exception). Returns the code of the exception associated with `ctx` or
   nil if no exception occured.

## <a name="convex.eval/result">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/eval.clj#L54-L61) `result`</a>
``` clojure

(result ctx cell)
```


Like [`ctx`](#convex.eval/ctx) but returns the result.

## <a name="convex.eval/true?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/eval.clj#L64-L73) `true?`</a>
``` clojure

(true? ctx cell)
```


Shortcut on top of [`result`](#convex.eval/result). Returns true if the result is CVX true.
  
   Notably useful for test assertions.

-----
# <a name="convex.read">convex.read</a>


Reading, parsing various kind of sources into CVX cells without any evaluation.

   Attention, currently, functions that read only one cell fail when the input contains more than one.
   In the future, behavior should be improved. For instance, consuming cells one by one from a stream.

   Also see the [`convex.write`](#convex.write) namespace for the opposite idea.




## <a name="convex.read/file">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/read.clj#L32-L40) `file`</a>
``` clojure

(file filename)
```


Reads all cells from the given `filename` and returns them in a CVX list.

## <a name="convex.read/line">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/read.clj#L44-L51) `line`</a>
``` clojure

(line buffered-reader)
```


Reads a line from the given `java.io.BufferedReader` and parses the result as a CVX list of cells.

## <a name="convex.read/resource">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/read.clj#L55-L67) `resource`</a>
``` clojure

(resource path)
```


Reads one cell from resource located under `path` on the classpath.

## <a name="convex.read/stream">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/read.clj#L71-L80) `stream`</a>
``` clojure

(stream reader)
```


Reads all cells from the given `java.io.Reader` (parent class of text streams) and returns them
   in a CVX list.

## <a name="convex.read/string">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/read.clj#L84-L92) `string`</a>
``` clojure

(string string)
```


Reads all cells from the given `string` and returns them in a CVX list.

-----
# <a name="convex.std">convex.std</a>


Provides an API for cells with classic `convex.core` functions such as [`conj`](#convex.std/conj).

   All `clojure.core` functions related to sequences usually understand Convex collections, making them
   easy to handle. Some of those (eg. `cons`, `next`) have counterparts in this namespace in case the return
   value must be a cell instead of a Clojure sequence.

   Functions take and return cells unless specified otherwise. Predicates return JVM booleans.

   Sometimes, it can be useful converting cells to Clojure data, such as unwrapping blob to byte arrays,
   which is the purpose of the [`convex.clj`](#convex.clj) namespace.

   Lastly, in the rare cases where all of this would not be enough, Java interop can be used:

     https://www.javadoc.io/doc/world.convex/convex-core/latest/convex/core/data/package-summary.html




## <a name="convex.std/*">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L785-L796) `*`</a>
``` clojure

(* & xs)
```


Like classic `*` but for numeric cells.

## <a name="convex.std/+">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L755-L766) `+`</a>
``` clojure

(+ & xs)
```


Like classic `+` but for numeric cells.

## <a name="convex.std/-">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L770-L781) `-`</a>
``` clojure

(- & xs)
```


Like classic `-` but for numeric cells.

## <a name="convex.std/<">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L418-L428) `<`</a>
``` clojure

(< & xs)
```


Like classic `<` but with numeric cells.

## <a name="convex.std/<=">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L432-L442) `<=`</a>
``` clojure

(<= & xs)
```


Like classic `<=` but with numeric cells.

## <a name="convex.std/==">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L446-L456) `==`</a>
``` clojure

(== & xs)
```


Like classic `==` but with numeric cells.

## <a name="convex.std/>">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L474-L484) `>`</a>
``` clojure

(> & xs)
```


Like classic `>` but with numeric cells.

## <a name="convex.std/>=">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L460-L470) `>=`</a>
``` clojure

(>= & xs)
```


Like classic `>=` but with numeric cells.

## <a name="convex.std/abs">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L800-L810) `abs`</a>
``` clojure

(abs number)
```


Returns the absolute value of `x`.
  
   Same type as `x`.

## <a name="convex.std/account-key">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L136-L149) `account-key`</a>
``` clojure

(account-key cell)
```


Coerces the given `cell` to an account key or return nil.
  
   Works with:

   - 64-char hex-string cell
   - 32-byte blob

## <a name="convex.std/address">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L153-L167) `address`</a>
``` clojure

(address cell)
```


Coerces the given `cell` to an address or return nil.
  
   Works with:

   - Long cell
   - 16-char hex-string cell
   - 8-byte blob

## <a name="convex.std/address?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1088-L1095) `address?`</a>
``` clojure

(address? x)
```


Is `x` an address?

## <a name="convex.std/assoc">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L543-L553) `assoc`</a>
``` clojure

(assoc coll k v)
```


Like classic `assoc` but for collection cells.

## <a name="convex.std/blob">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L171-L185) `blob`</a>
``` clojure

(blob cell)
```


Coerces the given `cell` to a blob or return nil.
  
   Works with:

   - Any kind of blob (eg. hash)
   - Long cell
   - Hex-string cell

## <a name="convex.std/blob-map">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L315-L329) `blob-map`</a>
``` clojure

(blob-map & kvs)
```


Builds a blob map from key-values (keys must be blobs).

## <a name="convex.std/blob-map?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1110-L1117) `blob-map?`</a>
``` clojure

(blob-map? x)
```


Is `x` a blob map?

## <a name="convex.std/blob?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1099-L1106) `blob?`</a>
``` clojure

(blob? x)
```


Is `x` a blob?

## <a name="convex.std/boolean?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1121-L1128) `boolean?`</a>
``` clojure

(boolean? x)
```


Is `x` a CVM boolean?

## <a name="convex.std/byte">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L189-L197) `byte`</a>
``` clojure

(byte cell)
```


Coerces the given `cell` to a byte or return nil.

## <a name="convex.std/byte?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1132-L1139) `byte?`</a>
``` clojure

(byte? x)
```


Is `x` a byte cell?

## <a name="convex.std/ceil">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L814-L822) `ceil`</a>
``` clojure

(ceil number)
```


Returns a double cell ceiling the value of `number`.

## <a name="convex.std/cell?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1154-L1161) `cell?`</a>
``` clojure

(cell? x)
```


Is `x` a cell?

## <a name="convex.std/char">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L201-L209) `char`</a>
``` clojure

(char cell)
```


Coerces the given `cell` to a char or return nil.

## <a name="convex.std/char?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1143-L1150) `char?`</a>
``` clojure

(char? x)
```


Is `x` a char cell?

## <a name="convex.std/coll?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1165-L1172) `coll?`</a>
``` clojure

(coll? x)
```


Is `x` a collection cell?

## <a name="convex.std/concat">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L956-L971) `concat`</a>
``` clojure

(concat x y)
```


Like classic `concat` but for collection cells.

   Return type is the same as `x`.

## <a name="convex.std/conj">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L557-L575) `conj`</a>
``` clojure

(conj)
(conj coll)
(conj coll v)
```


Akin to classic `conj` but for collection cells.

## <a name="convex.std/cons">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L939-L952) `cons`</a>
``` clojure

(cons x coll)
```


Like classic `cons` but for collection cells.
  
   Returns a list cell.

## <a name="convex.std/contains?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L579-L586) `contains?`</a>
``` clojure

(contains? coll k)
```


Like classic `contains?` but for collection cells.

## <a name="convex.std/count">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L490-L506) `count`</a>
``` clojure

(count countable)
```


Returns a JVM long representing the number of itms in the given cell.
  
   A countable is either:

   - Blob
   - Blob map
   - Map
   - List
   - Set
   - String
   - Vector

## <a name="convex.std/cvm-value?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1175-L1183) `cvm-value?`</a>
``` clojure

(cvm-value? x)
```


Is `x` a CVM value?

   Returns false if `x` is not accessible to the CVM and meant to be used outside (eg. networking).

## <a name="convex.std/dec">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L637-L645) `dec`</a>
``` clojure

(dec long)
```


Like classic `dec` but for long cells.

## <a name="convex.std/difference">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1006-L1018) `difference`</a>
``` clojure

(difference set-1 set-2)
```


Like `clojure.set/difference` but for set cells.

## <a name="convex.std/dissoc">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L678-L689) `dissoc`</a>
``` clojure

(dissoc map k)
```


Like classic `dissoc` but for map cells.

## <a name="convex.std/div">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L826-L837) `div`</a>
``` clojure

(div & xs)
```


Like classic `/` but for numeric cells.

## <a name="convex.std/double">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L213-L221) `double`</a>
``` clojure

(double cell)
```


Coerces the given `cell` to a double or return nil.

## <a name="convex.std/double?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1187-L1194) `double?`</a>
``` clojure

(double? x)
```


Is `x` a double cell?

## <a name="convex.std/empty">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L590-L598) `empty`</a>
``` clojure

(empty coll)
```


Like classic `empty` but for collection cells.

## <a name="convex.std/empty?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L510-L520) `empty?`</a>
``` clojure

(empty? countable)
```


Is the given `countable` empty?
  
   See [`count`](#convex.std/count).

## <a name="convex.std/exp">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L841-L849) `exp`</a>
``` clojure

(exp number)
```


Returns `e` raised to the power of the given numeric cell.

## <a name="convex.std/false?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1198-L1205) `false?`</a>
``` clojure

(false? x)
```


Is `x` a `false` cell?

## <a name="convex.std/find">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L693-L703) `find`</a>
``` clojure

(find map k)
```


Like classic `find`` but for map cells.

## <a name="convex.std/floor">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L853-L861) `floor`</a>
``` clojure

(floor x)
```


Returns a double cell flooring the value of `x`.

## <a name="convex.std/get">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L602-L616) `get`</a>
``` clojure

(get coll k)
(get coll k not-found)
```


Like classic `get` but for collection cells.

## <a name="convex.std/hash-map">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L333-L347) `hash-map`</a>
``` clojure

(hash-map & kvs)
```


Builds a map from key-values.

## <a name="convex.std/hash-map?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1208-L1215) `hash-map?`</a>
``` clojure

(hash-map? x)
```


Is `x` a hash map cell?

## <a name="convex.std/hash-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L351-L361) `hash-set`</a>
``` clojure

(hash-set & cell+)
```


Builds a set from the given cells.

## <a name="convex.std/hash-set?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1219-L1228) `hash-set?`</a>
``` clojure

(hash-set? x)
```


Is `x` a hash set cell?
  
   Currently at least, hast sets are the only kind of available sets.

## <a name="convex.std/inc">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L664-L672) `inc`</a>
``` clojure

(inc long)
```


Like classic `inc` but for long cells.

## <a name="convex.std/intersection">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1022-L1034) `intersection`</a>
``` clojure

(intersection set-1 set-2)
```


Like `clojure.set/intersection` but for set cells.

## <a name="convex.std/into">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L395-L412) `into`</a>
``` clojure

(into to from)
(into to xform from)
```


Like classic `into` but `to` is a collection cell.

## <a name="convex.std/keys">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L707-L718) `keys`</a>
``` clojure

(keys map)
```


Like classic `keys` but for map cells.

   Returns an eager vector cell.

## <a name="convex.std/keyword">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L225-L238) `keyword`</a>
``` clojure

(keyword cell)
```


Coerces the given `cell` to a keyword or return nil.
  
   Works with:

   - Max 64-char string cell
   - Symbol

## <a name="convex.std/keyword?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1231-L1238) `keyword?`</a>
``` clojure

(keyword? x)
```


Is `x` a keyword cell?

## <a name="convex.std/list">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L365-L375) `list`</a>
``` clojure

(list & cell+)
```


Buildsa list from the given cells.

## <a name="convex.std/list?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1242-L1249) `list?`</a>
``` clojure

(list? x)
```


Is `x` a list cell?

## <a name="convex.std/long">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L242-L250) `long`</a>
``` clojure

(long cell)
```


Coerces the given `cell` to a long or return nil.

## <a name="convex.std/long?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1253-L1260) `long?`</a>
``` clojure

(long? x)
```


Is `x` a long cell?

## <a name="convex.std/map?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1264-L1271) `map?`</a>
``` clojure

(map? x)
```


Is `x` a map cell?

## <a name="convex.std/merge">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L722-L735) `merge`</a>
``` clojure

(merge map-1 map-2)
```


Like classic `merge` but for hash map cells (not blob maps).

## <a name="convex.std/mod">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L649-L660) `mod`</a>
``` clojure

(mod a b)
```


Returns the integer modulus of a numerator divided by a divisor.
  
   Result will always be positive and consistent with Euclidean Divsion.

## <a name="convex.std/name">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1071-L1082) `name`</a>
``` clojure

(name symbolic)
```


Like classic `name` but for keyword and symbol cells.
  
   Returns a string cell.

## <a name="convex.std/nan?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L865-L871) `nan?`</a>
``` clojure

(nan? cell)
```


Is the given `cell` NaN?

## <a name="convex.std/next">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L975-L987) `next`</a>
``` clojure

(next coll)
```


Like classic `next` but for collection cells.
  
   Return type is a list cell if `coll` is a list, a vector cell otherwise.

## <a name="convex.std/nth">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L524-L537) `nth`</a>
``` clojure

(nth countable index)
```


Like classic `nth` but for countables.

   Index must be a JVM long.

   See [`count`](#convex.std/count).

## <a name="convex.std/number?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1275-L1283) `number?`</a>
``` clojure

(number? x)
```


Is `x` a numeric cell?
  
   Either a long or a double.

## <a name="convex.std/pow">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L875-L889) `pow`</a>
``` clojure

(pow x y)
```


Returns a CVM double, `x` raised to the power of `y`.

## <a name="convex.std/reverse">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L991-L1000) `reverse`</a>
``` clojure

(reverse sq)
```


Like classic `reverse` but for sequential cells (list or vector cells).

## <a name="convex.std/set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L254-L264) `set`</a>
``` clojure

(set cell)
```


Coerces the given `cell` to a set or return nil.
  
   Works with any collection.

## <a name="convex.std/set?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1287-L1296) `set?`</a>
``` clojure

(set? x)
```


Is `x` a set cell?

   Currently at least, hast sets are the only kind of available sets.

## <a name="convex.std/signum">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L893-L907) `signum`</a>
``` clojure

(signum number)
```


Returns the sign of the number:
  
   - `-1` if negative
   - `0` if 0
   - `1` if positive
  
   As a long cell if input is a long, double cell if it is a double.

## <a name="convex.std/sqrt">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L912-L920) `sqrt`</a>
``` clojure

(sqrt number)
```


Returns a double cell, the square root of the given `number` cell.

## <a name="convex.std/str">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L268-L277) `str`</a>
``` clojure

(str & cell+)
```


Stringifies the given cell(s).

## <a name="convex.std/string?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1300-L1307) `string?`</a>
``` clojure

(string? x)
```


Is `x` a string cell?

## <a name="convex.std/subset?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1038-L1048) `subset?`</a>
``` clojure

(subset? set-1 set-2)
```


Like `clojure.set/subset?` but for set cells.

## <a name="convex.std/symbol">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L281-L295) `symbol`</a>
``` clojure

(symbol cell)
```


Coerces the given `cell` to a symbol or return nil.

   Works with:

   - Max 64-char string cell
   - Symbol

## <a name="convex.std/symbol?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1311-L1318) `symbol?`</a>
``` clojure

(symbol? x)
```


Is `x` a symbol cell?

## <a name="convex.std/syntax?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1322-L1329) `syntax?`</a>
``` clojure

(syntax? x)
```


Is `x` a syntax cell?

## <a name="convex.std/true?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1333-L1340) `true?`</a>
``` clojure

(true? x)
```


Is `x` a `true` cell?

## <a name="convex.std/union">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1052-L1065) `union`</a>
``` clojure

(union set-1 set-2)
```


Like `clojure.set/union` but for set cells.

## <a name="convex.std/update">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L620-L631) `update`</a>
``` clojure

(update coll k f)
```


Akin to classic `update` but for collection cell.

## <a name="convex.std/vals">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L739-L749) `vals`</a>
``` clojure

(vals map)
```


Like classic `vals` but for map cells.

   Returns an eager vector cell.

## <a name="convex.std/vec">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L299-L309) `vec`</a>
``` clojure

(vec cell)
```


Coerces the given `cell` to a vector or return nil.
  
   Works with any countable (see [`count`](#convex.std/count)).

## <a name="convex.std/vector">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L379-L389) `vector`</a>
``` clojure

(vector & cell+)
```


Builds a vector from the given cells.

## <a name="convex.std/vector?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L1344-L1351) `vector?`</a>
``` clojure

(vector? x)
```


Is `x` a vector cell?

## <a name="convex.std/zero?">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/std.clj#L924-L933) `zero?`</a>
``` clojure

(zero? x)
```


Like classic `zero?` but for cells.

-----
# <a name="convex.write">convex.write</a>


Writing, encoding CVX cells various kind of sources.

   Binary is big-endian and text is UTF-8.

   Also see [`convex.read`](#convex.read) for the opposite idea.




## <a name="convex.write/stream">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/write.clj#L19-L39) `stream`</a>
``` clojure

(stream writer cell)
(stream writer stringify cell)
```


Writes the given `cell` to the given `java.io.Writer` (parent class of text streams).

   By default, standard `str` is used for stringifying `cell`. See [`string`](#convex.write/string) for implications.

## <a name="convex.write/string">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/cvm/src/main/clj/convex/write.clj#L43-L65) `string`</a>
``` clojure

(string cell)
```


Prints the given `cell` as a string cell.
  
   While standard `str` is sufficient for other type of cells, this function ensures that CVX strings are escaped
   so that reading produces a CVX string as well.
  
   For instance, CVX string "foo" produces the following:

   | Function                  | Cell after reading | Type |
   |---------------------------|--------------------|------|
   | Clojure `str`             | `"foo"`          | JVM  |
   | This namespace's `string` | `""(+ 1 2)""`  | Cell |
