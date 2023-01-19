(ns convex.shell.req

  "All extra features offered by the Shell, over the Convex Virtual Machine, have
   a single entry point: the `.shell.invoke` function injected in the core account.

   AKA the [[invoker]].

   The various side effects, implemented in Clojure and made available through the
   [[invoker]], are known as \"requests\"."

  {:author "Adam Helinski"}

  (:import (convex.core.data ACell)
           (convex.core.init Init)
           (convex.core.lang Context)
           (convex.core.lang.impl CoreFn
                                  ErrorValue))
  (:require [convex.cell              :as $.cell]
            [convex.cvm               :as $.cvm]
            [convex.shell.req.account :as $.shell.req.account]
            [convex.shell.req.bench   :as $.shell.req.bench]
            [convex.shell.req.db      :as $.shell.req.db]
            [convex.shell.req.dep     :as $.shell.req.dep]
            [convex.shell.req.dev     :as $.shell.req.dev]
            [convex.shell.req.file    :as $.shell.req.file]
            [convex.shell.req.fs      :as $.shell.req.fs]
            [convex.shell.req.juice   :as $.shell.req.juice]
            [convex.shell.req.gen     :as $.shell.req.gen]
            [convex.shell.req.log     :as $.shell.req.log]
            [convex.shell.req.reader  :as $.shell.req.reader]
            [convex.shell.req.state   :as $.shell.req.state]
            [convex.shell.req.str     :as $.shell.req.str]
            [convex.shell.req.stream  :as $.shell.req.stream]
            [convex.shell.req.sys     :as $.shell.req.sys]
            [convex.shell.req.time    :as $.shell.req.time]
            [convex.std               :as $.std]))


(set! *warn-on-reflection*
      true)


(declare invoker)


;;;;;;;;;;


(defn ex-rethrow

  "Request for rethrowing an exception captured in the Shell."

  [ctx [ex-map]]

  (or (when-not ($.std/map? ex-map)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Given exception must be a map")))
      (let [^ACell code ($.std/get ex-map
                                   ($.cell/* :code))]
        (or (when (nil? code)
              ($.cvm/exception-set ctx
                                   ($.cell/code-std* :ARGUMENT)
                                   ($.cell/* "Exception `:code` cannot be nil")))
            (let [address ($.std/get ex-map
                                     ($.cell/* :address))]
              (or (when-not (or (nil? address)
                                ($.std/address? address))
                    ($.cvm/exception-set ctx
                                         ($.cell/code-std* :ARGUMENT)
                                         ($.cell/* "Exception `:address` must be a valid address")))
                  (let [trace ($.std/get ex-map
                                         ($.cell/* :trace))]
                    (or (when-not (or (nil? trace)
                                      ($.std/vector? trace))
                          ($.cvm/exception-set ctx
                                               ($.cell/code-std* :ARGUMENT)
                                               ($.cell/* "Exception `:trace` must be a vector of strings")))
                        (let [ex (ErrorValue/createRaw code
                                                       ^ACell ($.std/get ex-map
                                                                         ($.cell/* :message)))]
                          (when address
                            (.setAddress ex
                                         address))
                          (.addLog ex
                                   ($.cvm/log ctx))
                          (doseq [cell trace]
                            (.addTrace ex
                                       (str cell)))
                          (.addTrace ex
                                     (format "Rethrowing exception in %s"
                                             ($.cvm/address ctx)))
                          ($.cvm/exception-set ctx
                                               ex))))))))))


;;;;;;;;;;


(def core

  "All core requests.
  
   A map of CVX symbols pointing to a Clojure implementations."

  {($.cell/* .account.switch)              $.shell.req.account/switch
   ($.cell/* .bench.trx)                   $.shell.req.bench/trx
   ($.cell/* .db.flush)                    $.shell.req.db/flush
   ($.cell/* .db.open)                     $.shell.req.db/open
   ($.cell/* .db.path)                     $.shell.req.db/path
   ($.cell/* .db.read)                     $.shell.req.db/read
   ($.cell/* .db.root.read)                $.shell.req.db/root-read
   ($.cell/* .db.root.write)               $.shell.req.db/root-write
   ($.cell/* .db.write)                    $.shell.req.db/write
   ($.cell/* .dep.deploy)                  $.shell.req.dep/deploy
   ($.cell/* .dep.fetch)                   $.shell.req.dep/fetch
   ($.cell/* .dep.read)                    $.shell.req.dep/read
   ($.cell/* .dev.fatal)                   $.shell.req.dev/fatal
   ($.cell/* .ex.rethrow)                  ex-rethrow
   ($.cell/* .file.stream.in)              $.shell.req.file/stream-in
   ($.cell/* .file.stream.out)             $.shell.req.file/stream-out
   ($.cell/* .fs.copy)                     $.shell.req.fs/copy
   ($.cell/* .fs.delete)                   $.shell.req.fs/delete
   ($.cell/* .fs.dir?)                     $.shell.req.fs/dir?
   ($.cell/* .fs.exists?)                  $.shell.req.fs/exists?
   ($.cell/* .fs.file?)                    $.shell.req.fs/file?
   ($.cell/* .fs.resolve)                  $.shell.req.fs/resolve
   ($.cell/* .fs.size)                     $.shell.req.fs/size
   ($.cell/* .fs.tmp)                      $.shell.req.fs/tmp
   ($.cell/* .fs.tmp.dir)                  $.shell.req.fs/tmp-dir
   ($.cell/* .gen.blob)                    $.shell.req.gen/blob
   ($.cell/* .gen.blob.bounded)            $.shell.req.gen/blob-bounded
   ($.cell/* .gen.blob.fixed)              $.shell.req.gen/blob-fixed
   ($.cell/* .gen.blob-map)                $.shell.req.gen/blob-map
   ($.cell/* .gen.blob-map.bounded)        $.shell.req.gen/blob-map-bounded
   ($.cell/* .gen.blob-map.fixed)          $.shell.req.gen/blob-map-fixed
   ($.cell/* .gen.double.bounded)          $.shell.req.gen/double-bounded
   ($.cell/* .gen.hex-string)              $.shell.req.gen/hex-string
   ($.cell/* .gen.hex-string.fixed)        $.shell.req.gen/hex-string-fixed
   ($.cell/* .gen.hex-string.bounded)      $.shell.req.gen/hex-string-bounded
   ($.cell/* .gen.list)                    $.shell.req.gen/list
   ($.cell/* .gen.list.bounded)            $.shell.req.gen/list-bounded
   ($.cell/* .gen.list.fixed)              $.shell.req.gen/list-fixed
   ($.cell/* .gen.long.bounded)            $.shell.req.gen/long-bounded
   ($.cell/* .gen.make)                    $.shell.req.gen/make
   ($.cell/* .gen.map)                     $.shell.req.gen/map
   ($.cell/* .gen.map.bounded)             $.shell.req.gen/map-bounded
   ($.cell/* .gen.map.fixed)               $.shell.req.gen/map-fixed
   ($.cell/* .gen.quoted)                  $.shell.req.gen/quoted
   ($.cell/* .gen.set)                     $.shell.req.gen/set
   ($.cell/* .gen.set.bounded)             $.shell.req.gen/set-bounded
   ($.cell/* .gen.set.fixed)               $.shell.req.gen/set-fixed
   ($.cell/* .gen.string)                  $.shell.req.gen/string
   ($.cell/* .gen.string.bounded)          $.shell.req.gen/string-bounded
   ($.cell/* .gen.string.fixed)            $.shell.req.gen/string-fixed
   ($.cell/* .gen.string.alphanum)         $.shell.req.gen/string-alphanum
   ($.cell/* .gen.string.alphanum.bounded) $.shell.req.gen/string-alphanum-bounded
   ($.cell/* .gen.string.alphanum.fixed)   $.shell.req.gen/string-alphanum-fixed
   ($.cell/* .gen.syntax)                  $.shell.req.gen/syntax
   ($.cell/* .gen.syntax.with-meta)        $.shell.req.gen/syntax-with-meta
   ($.cell/* .gen.syntax.with-value)       $.shell.req.gen/syntax-with-value
   ($.cell/* .gen.tuple)                   $.shell.req.gen/tuple
   ($.cell/* .gen.vector)                  $.shell.req.gen/vector
   ($.cell/* .gen.vector.bounded)          $.shell.req.gen/vector-bounded
   ($.cell/* .gen.vector.fixed)            $.shell.req.gen/vector-fixed
   ($.cell/* .juice.set)                   $.shell.req.juice/set
   ($.cell/* .juice.track)                 $.shell.req.juice/track
   ($.cell/* .log.clear)                   $.shell.req.log/clear
   ($.cell/* .log.get)                     $.shell.req.log/get
   ($.cell/* .reader.form+)                $.shell.req.reader/form+
   ($.cell/* .state.genesis)               $.shell.req.state/genesis
   ($.cell/* .state.safe)                  $.shell.req.state/safe
   ($.cell/* .state.switch)                $.shell.req.state/switch
   ($.cell/* .state.tmp)                   $.shell.req.state/tmp
   ($.cell/* .str.sort)                    $.shell.req.str/sort
   ($.cell/* .str.stream.in)               $.shell.req.str/stream-in
   ($.cell/* .str.stream.out)              $.shell.req.str/stream-out
   ($.cell/* .str.stream.unwrap)           $.shell.req.str/stream-unwrap
   ($.cell/* .stream.close)                $.shell.req.stream/close
   ($.cell/* .stream.flush)                $.shell.req.stream/flush
   ($.cell/* .stream.in+)                  $.shell.req.stream/in+
   ($.cell/* .stream.line)                 $.shell.req.stream/line
   ($.cell/* .stream.out)                  $.shell.req.stream/out
   ($.cell/* .stream.outln)                $.shell.req.stream/outln
   ($.cell/* .stream.txt.in)               $.shell.req.stream/txt-in
   ($.cell/* .stream.txt.line)             $.shell.req.stream/txt-line
   ($.cell/* .stream.txt.out)              $.shell.req.stream/txt-out
   ($.cell/* .stream.txt.outln)            $.shell.req.stream/txt-outln
   ($.cell/* .sys.arch)                    $.shell.req.sys/arch
   ($.cell/* .sys.cwd)                     $.shell.req.sys/cwd
   ($.cell/* .sys.exit)                    $.shell.req.sys/exit
   ($.cell/* .sys.env)                     $.shell.req.sys/env
   ($.cell/* .sys.env.var)                 $.shell.req.sys/env-var
   ($.cell/* .sys.home)                    $.shell.req.sys/home
   ($.cell/* .sys.os)                      $.shell.req.sys/os
   ($.cell/* .time.advance)                $.shell.req.time/advance
   ($.cell/* .time.iso->unix)              $.shell.req.time/iso->unix
   ($.cell/* .time.nano)                   $.shell.req.time/nano
   ($.cell/* .time.sleep)                  $.shell.req.time/sleep
   ($.cell/* .time.unix)                   $.shell.req.time/unix
   ($.cell/* .time.unix->iso)              $.shell.req.time/unix->iso})


;;;;;;;;;;


(defn- -inspect

  "Request for returning all the requests current available in the Shell.
  
   See [[invoker]]."

  [dispatch-table ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/set (keys dispatch-table))))



(defn- -limit

  "Request for rebuilding the [[invoker]], limiting the available requests.
  
   Akin to how Linux users can selectively forbid some syscalls."

  [dispatch-table ctx [feature-set]]

  (or (when-not ($.std/set? feature-set)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Given feature set is not a set")))
      ;; Must dissoc first due to how fake cells compare.
      (-> ctx
          ($.cvm/undef Init/CORE_ADDRESS
                       [($.cell/* .shell.invoke)])
          ($.cvm/def Init/CORE_ADDRESS
                     ($.cell/* {.shell.invoke ~(invoker (into {}
                                                              (filter (comp (partial $.std/contains?
                                                                                     feature-set)
                                                                            first))
                                                              dispatch-table))}))
          ($.cvm/result-set nil))))


;;;;;;;;;;


(defn invoker

  "Returns an [[invoker]].

   Disguised as a CVM core function, the [[invoker]] is a variadic CVM function where
   the first argument is a CVX symbol resolving to a Clojure implementation that will
   produce the desired request, such as opening a file output stream.

   The symbol is resolved using a \"dispatch table\".
   Defaults to [[core]] but one may want to extend it in order to provide additional
   features.
  
   It will be injected in the core account of the context used by the Shell, under
   `.shell.invoke`."


  ([]

   (invoker nil))


  ([dispatch-table]

   (let [dispatch-table-2 (or dispatch-table
                              core)
         dispatch-table-3 (assoc dispatch-table-2
                                 ($.cell/* .shell.inspect) (partial -inspect
                                                                    dispatch-table-2)
                                 ($.cell/* .shell.limit)   (partial -limit
                                                                    dispatch-table-2))]
     (proxy

       [CoreFn]

       [($.cell/* log)]

       (invoke [ctx arg+]
         (let [sym (first arg+)]
           (if ($.std/symbol? sym)
             (if-some [f (dispatch-table-3 sym)]
               (let [^Context    ctx-2 (f ctx
                                          (rest arg+))
                     ^ErrorValue ex    ($.cvm/exception ctx-2)]
                 (if ex
                   (.withException ctx-2
                                   (doto ex
                                     (.addTrace (format "In Convex Shell request: %s"
                                                        sym))))
                   ctx-2))
               ($.cvm/exception-set ctx
                                    ($.cell/code-std* :ARGUMENT)
                                    ($.cell/string (format "Unknown Convex Shell request: %s"
                                                           sym))))
             ($.cvm/exception-set ctx
                                  ($.cell/code-std* :ARGUMENT)
                                  ($.cell/string "Convex Shell invocation require a symbol")))))))))
