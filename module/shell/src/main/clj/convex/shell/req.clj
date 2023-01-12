(ns convex.shell.req

  (:import (convex.core.exceptions ParseException)
           (convex.core.lang Context)
           (convex.core.lang.impl CoreFn
                                  ErrorValue))
  (:require [convex.cell              :as $.cell]
            [convex.cvm               :as $.cvm]
            [convex.read              :as $.read]
            [convex.shell.ctx.core    :as $.shell.ctx.core]
            [convex.shell.req.account :as $.shell.req.account]
            [convex.shell.req.bench   :as $.shell.req.bench]
            [convex.shell.req.db      :as $.shell.req.db]
            [convex.shell.req.dep     :as $.shell.req.dep]
            [convex.shell.req.dev     :as $.shell.req.dev]
            [convex.shell.req.file    :as $.shell.req.file]
            [convex.shell.req.fs      :as $.shell.req.fs]
            [convex.shell.req.juice   :as $.shell.req.juice]
            [convex.shell.req.log     :as $.shell.req.log]
            [convex.shell.req.state   :as $.shell.req.state]
            [convex.shell.req.stream  :as $.shell.req.stream]
            [convex.shell.req.sys     :as $.shell.req.sys]
            [convex.shell.req.time    :as $.shell.req.time]
            [convex.std               :as $.std]))


(declare invoker)


;;;;;;;;;;


(defn read+

  [ctx arg+]

  (let [src (first arg+)]
    (or (when-not ($.std/string? src)
          ($.cvm/exception-set ctx
                               ($.cell/code-std* :ARGUMENT)
                               ($.cell/string "Source to read is not a string")))
        (try
          ($.cvm/result-set ctx
                            (-> (first arg+)
                                (str)
                                ($.read/string)))
          ;;
          (catch ParseException ex
            ($.cvm/exception-set ctx
                                 ($.cell/* :READER)
                                 ($.cell/string (.getMessage ex))))
          ;;
          (catch Throwable _ex
            ($.cvm/exception-set ctx
                                 ($.cell/* :READER)
                                 ($.cell/string "Unable to read string as Convex data")))))))


;;;;;;;;;;


(def core

  {($.cell/* .account.control)  $.shell.req.account/control
   ($.cell/* .account.switch)   $.shell.req.account/switch
   ($.cell/* .bench.trx)        $.shell.req.bench/trx
   ($.cell/* .db.flush)         $.shell.req.db/flush
   ($.cell/* .db.open)          $.shell.req.db/open
   ($.cell/* .db.path)          $.shell.req.db/path
   ($.cell/* .db.read)          $.shell.req.db/read
   ($.cell/* .db.root.read)     $.shell.req.db/root-read
   ($.cell/* .db.root.write)    $.shell.req.db/root-write
   ($.cell/* .db.write)         $.shell.req.db/write
   ($.cell/* .dep.deploy)       $.shell.req.dep/deploy
   ($.cell/* .dep.fetch)        $.shell.req.dep/fetch
   ($.cell/* .dep.read)         $.shell.req.dep/read
   ($.cell/* .dev.fatal)        $.shell.req.dev/fatal
   ($.cell/* .file.stream.in)   $.shell.req.file/stream-in
   ($.cell/* .file.stream.out)  $.shell.req.file/stream-out
   ($.cell/* .fs.copy)          $.shell.req.fs/copy
   ($.cell/* .fs.delete)        $.shell.req.fs/delete
   ($.cell/* .fs.exists?)       $.shell.req.fs/exists?
   ($.cell/* .fs.resolve)       $.shell.req.fs/resolve
   ($.cell/* .fs.size)          $.shell.req.fs/size
   ($.cell/* .fs.tmp)           $.shell.req.fs/tmp
   ($.cell/* .fs.tmp.dir)       $.shell.req.fs/tmp-dir
   ($.cell/* .juice.get)        $.shell.req.juice/get
   ($.cell/* .juice.set)        $.shell.req.juice/set
   ($.cell/* .juice.track)      $.shell.req.juice/track
   ($.cell/* .log.clear)        $.shell.req.log/clear
   ($.cell/* .log.get)          $.shell.req.log/get
   ($.cell/* .read+)            read+
   ($.cell/* .state.genesis)    $.shell.req.state/genesis
   ($.cell/* .state.safe)       $.shell.req.state/safe
   ($.cell/* .state.switch)     $.shell.req.state/switch
   ($.cell/* .state.tmp)        $.shell.req.state/tmp
   ($.cell/* .stream.close)     $.shell.req.stream/close
   ($.cell/* .stream.flush)     $.shell.req.stream/flush
   ($.cell/* .stream.in+)       $.shell.req.stream/in+
   ($.cell/* .stream.line)      $.shell.req.stream/line
   ($.cell/* .stream.out)       $.shell.req.stream/out
   ($.cell/* .stream.outln)     $.shell.req.stream/outln
   ($.cell/* .stream.txt.in)    $.shell.req.stream/txt-in
   ($.cell/* .stream.txt.line)  $.shell.req.stream/txt-line
   ($.cell/* .stream.txt.out)   $.shell.req.stream/txt-out
   ($.cell/* .stream.txt.outln) $.shell.req.stream/txt-outln
   ($.cell/* .sys.arch)         $.shell.req.sys/arch
   ($.cell/* .sys.cwd)          $.shell.req.sys/cwd
   ($.cell/* .sys.exit)         $.shell.req.sys/exit
   ($.cell/* .sys.env)          $.shell.req.sys/env
   ($.cell/* .sys.env.var)      $.shell.req.sys/env-var
   ($.cell/* .sys.home)         $.shell.req.sys/home
   ($.cell/* .sys.os)           $.shell.req.sys/os
   ($.cell/* .time.advance)     $.shell.req.time/advance
   ($.cell/* .time.iso->unix)   $.shell.req.time/iso->unix
   ($.cell/* .time.nano)        $.shell.req.time/nano
   ($.cell/* .time.unix)        $.shell.req.time/unix
   ($.cell/* .time.unix->iso)   $.shell.req.time/unix->iso})


;;;;;;;;;;


(defn- -inspect

  [dispatch-table ctx _arg+]

  ($.cvm/result-set ctx
                    ($.cell/set (keys dispatch-table))))



(defn- -limit

  [dispatch-table ctx [feature-set]]

  (or (when-not ($.std/set? feature-set)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Given feature set is not a set")))
      ;; Must dissoc first due to how fake cells compare.
      (-> ctx
          ($.cvm/undef $.shell.ctx.core/address
                       [($.cell/* .shell.invoke)])
          ($.cvm/def $.shell.ctx.core/address
                     ($.cell/* {.shell.invoke ~(invoker (into {}
                                                              (filter (comp (partial $.std/contains?
                                                                                     feature-set)
                                                                            first))
                                                              dispatch-table))}))
          ($.cvm/result-set nil))))


;;;;;;;;;;


(defn invoker


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
