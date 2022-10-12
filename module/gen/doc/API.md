# Table of contents
-  [`convex.gen`](#convex.gen)  - <code>test.check</code> generators for cells.
    -  [`address`](#convex.gen/address) - Address cell.
    -  [`any`](#convex.gen/any) - Combines [[scalar]] and [[recursive]] to produce any CVM cell.
    -  [`any-coll`](#convex.gen/any-coll) - Any collection.
    -  [`any-list`](#convex.gen/any-list) - Recursive list cell where an item can be any cell.
    -  [`any-map`](#convex.gen/any-map) - Recursive hash map cell where an item can be any cell.
    -  [`any-set`](#convex.gen/any-set) - Recursive set cell where an item can be any cell.
    -  [`any-vector`](#convex.gen/any-vector) - Recursive vector cell where an item can be any cell.
    -  [`blob`](#convex.gen/blob) - Blob cell.
    -  [`blob-32`](#convex.gen/blob-32) - 32-byte blob cell.
    -  [`blob-map`](#convex.gen/blob-map) - Blob map here item are generated using <code>gen</code>.
    -  [`boolean`](#convex.gen/boolean) - Boolean cell.
    -  [`byte`](#convex.gen/byte) - Byte cell.
    -  [`char`](#convex.gen/char) - Char cell between 0 and 255 inclusive.
    -  [`char-alphanum`](#convex.gen/char-alphanum) - Like [[char]] but alphanumeric, hence always printable.
    -  [`double`](#convex.gen/double) - Double cell.
    -  [`double-bounded`](#convex.gen/double-bounded) - Like [[double]] but accept a map with <code>:min</code> and <code>:max</code> for setting boundaries.
    -  [`falsy`](#convex.gen/falsy) - False or nil.
    -  [`hex-string`](#convex.gen/hex-string) - A hex-string where each byte is written as two hex digits.
    -  [`keyword`](#convex.gen/keyword) - Keyword cell.
    -  [`list`](#convex.gen/list) - List cell where item are generated using <code>gen</code>.
    -  [`long`](#convex.gen/long) - Long cell.
    -  [`long-bounded`](#convex.gen/long-bounded) - Like [[long]] but accept a map with <code>:min</code> and <code>:max</code> for setting boundaries.
    -  [`map`](#convex.gen/map) - Map cell where item are generated using <code>gen</code>.
    -  [`nothing`](#convex.gen/nothing) - Generates nil.
    -  [`number`](#convex.gen/number) - Either [[double]] or [[long]].
    -  [`number-bounded`](#convex.gen/number-bounded) - Like [[number]] but accept a map with <code>:min</code> and <code>:max</code> for setting boundaries.
    -  [`quoted`](#convex.gen/quoted) - Wraps the given <code>gen</code> so that the output is wrapped in a <code>quote</code> form.
    -  [`recursive`](#convex.gen/recursive) - Base generators for recursive collection cells where an item of a collection can be a collection as well.
    -  [`scalar`](#convex.gen/scalar) - Any CVM cell that is not a collection: - [[address]] - [[blob]] - [[boolean]] - [[byte]] - [[char-alphanum]] - [[double]] - [[keyword]] - [[long]] - [[nothing]] - [[string-alphanum]] - [[symbol]] This excludes non-CVM cells such as the different transaction types.
    -  [`set`](#convex.gen/set) - Set cell where item are generated using <code>gen</code>.
    -  [`string`](#convex.gen/string) - String cell containing [[char]].
    -  [`string-alphanum`](#convex.gen/string-alphanum) - String cell containing [[char-alphanum]].
    -  [`string-symbolic`](#convex.gen/string-symbolic) - String that can be used to construct a keyword or a symbol.
    -  [`symbol`](#convex.gen/symbol) - Symbol cell.
    -  [`syntax`](#convex.gen/syntax) - Syntax cell.
    -  [`truthy`](#convex.gen/truthy) - Like <code>any</code> but neither false nor nil.
    -  [`tuple`](#convex.gen/tuple) - CVX vector where each of the given generator respectively produces an item.
    -  [`vector`](#convex.gen/vector) - Vector cell where item are generated using <code>gen</code>.

-----
# <a name="convex.gen">convex.gen</a>


`test.check` generators for cells.




## <a name="convex.gen/address">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L151-L156) `address`</a>

Address cell.

## <a name="convex.gen/any">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L743-L757) `any`</a>

Combines [`scalar`](#convex.gen/scalar) and [`recursive`](#convex.gen/recursive) to produce any CVM cell.
  
   Once in a while, generates a [`syntax`](#convex.gen/syntax) as well.

## <a name="convex.gen/any-coll">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L761-L766) `any-coll`</a>

Any collection.

## <a name="convex.gen/any-list">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L648-L665) `any-list`</a>

Recursive list cell where an item can be any cell.

## <a name="convex.gen/any-map">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L679-L697) `any-map`</a>

Recursive hash map cell where an item can be any cell.

## <a name="convex.gen/any-set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L701-L717) `any-set`</a>

Recursive set cell where an item can be any cell.

## <a name="convex.gen/any-vector">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L721-L737) `any-vector`</a>

Recursive vector cell where an item can be any cell.

## <a name="convex.gen/blob">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L160-L185) `blob`</a>
``` clojure

(blob)
(blob n)
(blob n-min n-max)
```


Blob cell.
  
   When length is not given, depends on current `test.check` size.

## <a name="convex.gen/blob-32">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L189-L195) `blob-32`</a>

32-byte blob cell.

   Useful for CVM hashes and keys.

## <a name="convex.gen/blob-map">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L490-L520) `blob-map`</a>
``` clojure

(blob-map gen-k gen-v)
(blob-map gen-k gen-v n)
(blob-map gen-k gen-v n-min n-max)
```


Blob map here item are generated using `gen`.
   
   Generator for keys must output [`blob`](#convex.gen/blob) or specialized blob like [`address`](#convex.gen/address).
  
   When length target is not provided, depends on current `test.check` size.

## <a name="convex.gen/boolean">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L199-L204) `boolean`</a>

Boolean cell.

## <a name="convex.gen/byte">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L208-L213) `byte`</a>

Byte cell.

## <a name="convex.gen/char">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L217-L222) `char`</a>

Char cell between 0 and 255 inclusive.

## <a name="convex.gen/char-alphanum">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L226-L231) `char-alphanum`</a>

Like [`char`](#convex.gen/char) but alphanumeric, hence always printable.

## <a name="convex.gen/double">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L235-L240) `double`</a>

Double cell.

## <a name="convex.gen/double-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L244-L253) `double-bounded`</a>
``` clojure

(double-bounded option+)
```


Like [`double`](#convex.gen/double) but accept a map with `:min` and `:max` for setting boundaries.
  
   Both are optional.

## <a name="convex.gen/falsy">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L803-L808) `falsy`</a>

False or nil.

## <a name="convex.gen/hex-string">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L265-L289) `hex-string`</a>
``` clojure

(hex-string)
(hex-string n-byte)
(hex-string n-byte-min n-byte-max)
```


A hex-string where each byte is written as two hex digits.

## <a name="convex.gen/keyword">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L293-L298) `keyword`</a>

Keyword cell.

## <a name="convex.gen/list">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L461-L486) `list`</a>
``` clojure

(list gen)
(list gen n)
(list gen n-min n-max)
```


List cell where item are generated using `gen`.
  
   When length target is not provided, depends on current `test.check` size.

## <a name="convex.gen/long">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L302-L307) `long`</a>

Long cell.

## <a name="convex.gen/long-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L311-L320) `long-bounded`</a>
``` clojure

(long-bounded option+)
```


Like [`long`](#convex.gen/long) but accept a map with `:min` and `:max` for setting boundaries.
  
   Both are optional.

## <a name="convex.gen/map">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L524-L552) `map`</a>
``` clojure

(map gen-k gen-v)
(map gen-k gen-v n)
(map gen-k gen-v n-min n-max)
```


Map cell where item are generated using `gen`.
  
   When length target is not provided, depends on current `test.check` size.

## <a name="convex.gen/nothing">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L345-L349) `nothing`</a>

Generates nil.

## <a name="convex.gen/number">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L323-L328) `number`</a>

Either [`double`](#convex.gen/double) or [`long`](#convex.gen/long).

## <a name="convex.gen/number-bounded">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L332-L341) `number-bounded`</a>
``` clojure

(number-bounded option+)
```


Like [`number`](#convex.gen/number) but accept a map with `:min` and `:max` for setting boundaries.
  
   Both are optional.

## <a name="convex.gen/quoted">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L136-L145) `quoted`</a>
``` clojure

(quoted gen)
```


Wraps the given `gen` so that the output is wrapped in a `quote` form.

## <a name="convex.gen/recursive">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L616-L644) `recursive`</a>

Base generators for recursive collection cells where an item of a collection can be a collection as well.
  
   Leaves are [`scalar`](#convex.gen/scalar) while containers can be:
  
   - [`blob-map`](#convex.gen/blob-map)
   - [`list`](#convex.gen/list)
   - [`map`](#convex.gen/map)
   - [`set`](#convex.gen/set)
   - [`vector`](#convex.gen/vector)
  
   Produces a [`scalar`](#convex.gen/scalar) in roughly 10% of outputs.

## <a name="convex.gen/scalar">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L427-L455) `scalar`</a>

Any CVM cell that is not a collection:

   - [`address`](#convex.gen/address)
   - [`blob`](#convex.gen/blob)
   - [`boolean`](#convex.gen/boolean)
   - [`byte`](#convex.gen/byte)
   - [`char-alphanum`](#convex.gen/char-alphanum)
   - [`double`](#convex.gen/double)
   - [`keyword`](#convex.gen/keyword)
   - [`long`](#convex.gen/long)
   - [`nothing`](#convex.gen/nothing)
   - [`string-alphanum`](#convex.gen/string-alphanum)
   - [`symbol`](#convex.gen/symbol)
  
  This excludes non-CVM cells such as the different transaction types.

## <a name="convex.gen/set">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L556-L581) `set`</a>
``` clojure

(set gen)
(set gen n)
(set gen n-min n-max)
```


Set cell where item are generated using `gen`.
  
   When length target is not provided, depends on current `test.check` size.

## <a name="convex.gen/string">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L353-L376) `string`</a>
``` clojure

(string)
(string n)
(string n-min n-max)
```


String cell containing [`char`](#convex.gen/char).

## <a name="convex.gen/string-alphanum">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L380-L403) `string-alphanum`</a>
``` clojure

(string-alphanum)
(string-alphanum n)
(string-alphanum n-min n-max)
```


String cell containing [`char-alphanum`](#convex.gen/char-alphanum).

## <a name="convex.gen/string-symbolic">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L407-L412) `string-symbolic`</a>

String that can be used to construct a keyword or a symbol.

## <a name="convex.gen/symbol">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L416-L421) `symbol`</a>

Symbol cell.

## <a name="convex.gen/syntax">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L772-L797) `syntax`</a>
``` clojure

(syntax)
(syntax gen-value)
(syntax gen-value gen-meta)
```


Syntax cell.
  
   By default, `gen-value` is [`any`](#convex.gen/any) and `gen-meta` is either [`any-map`](#convex.gen/any-map) or [`nothing`](#convex.gen/nothing).

## <a name="convex.gen/truthy">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L812-L820) `truthy`</a>

Like `any` but neither false nor nil.

## <a name="convex.gen/tuple">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L826-L834) `tuple`</a>
``` clojure

(tuple & generator+)
```


CVX vector where each of the given generator respectively produces an item.

## <a name="convex.gen/vector">[:page_facing_up:](https://github.com/Convex-Dev/convex.cljc/blob/main/module/gen/src/main/clj/convex/gen.clj#L585-L610) `vector`</a>
``` clojure

(vector gen)
(vector gen n)
(vector gen n-min n-max)
```


Vector cell where item are generated using `gen`.
  
   When length target is not provided, depends on current `test.check` size.

-----
