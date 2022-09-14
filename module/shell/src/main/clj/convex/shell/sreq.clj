(ns convex.shell.sreq

  "Implementation of requests interpreted by the shell between transactions.
  
   A reqest is merely a CVX vector following some particular convention that the
   shell follows for producing effects beyond the scope of the CVM."

  {:author "Adam Helinski"}

  (:import (convex.core State)
           (convex.core.data AVector)
           (convex.core.data.prim CVMLong)
           (convex.core.exceptions ParseException)
           (java.io File
                    IOException)
           (java.nio.file DirectoryNotEmptyException
                          Files
                          StandardCopyOption
                          Path)
           (java.nio.file.attribute FileAttribute))
  (:require [convex.cell            :as $.cell]
            [convex.clj             :as $.clj]
            [convex.cvm             :as $.cvm]
            [convex.db              :as $.db]
            [convex.read            :as $.read]
            [convex.shell.ctx       :as $.shell.ctx]
            [convex.shell.err       :as $.shell.err]
            [convex.shell.exec      :as $.shell.exec]
            [convex.shell.exec.fail :as $.shell.exec.fail]
            [convex.shell.kw        :as $.shell.kw]
            [convex.shell.stream    :as $.shell.stream]
            [convex.shell.sym       :as $.shell.sym]
            [convex.shell.time      :as $.shell.time]
            [criterium.core         :as criterium]))


(set! *warn-on-reflection*
      true)


;;;;;;;;;; Setup


(defmethod $.shell.exec/sreq
  
  nil

  ;; No request, simply finalizes a regular transactions.

  [env result]

  ($.shell.ctx/def-result env
                          result))



(defmethod $.shell.exec/sreq
  
  :unknown

  ;; Unknown request, consided as failure.

  [env tuple]

  ($.shell.exec.fail/err env
                         ($.shell.err/sreq ($.cell/code-std* :ARGUMENT)
                                           ($.cell/string "Unsupported request")
                                           tuple)))


;;;;;;;;;; Catch


(defmethod $.shell.exec/sreq

  $.shell.kw/catch-rethrow

  ;; Rethrows an exception map.

  [env ^AVector tuple]

  ($.shell.exec.fail/rethrow env
                             (.get tuple
                                   2)))


;;;;;;;;;; Code


(defmethod $.shell.exec/sreq

  $.shell.kw/code-read+

  ;; Reads the given string and parses it to a list of forms.

  ;; TODO. Improve error reporting.
  
  [env ^AVector tuple]

  (let [src (.get tuple
                  2)]
    (try
      ($.shell.ctx/def-result env
                              (-> src
                                  (str)
                                  ($.read/string)))
      ;;
      (catch ParseException ex
        ($.shell.exec.fail/err env
                               ($.shell.err/reader-string src
                                                          ($.cell/string (.getMessage ex)))))
      ;;
      (catch Throwable _ex
        ($.shell.exec.fail/err env
                               ($.shell.err/reader-string src))))))


;;;;;;;;;; Dev


(defmethod $.shell.exec/sreq

  $.shell.kw/dev-fatal

  [_env _tuple]

  (throw (Exception. "This is a simulated exception")))


;;;;;;;;;; Etch


(defn- -db

  ;; Must be used to wrap Etch operations.
  ;; Ensures there is an instance and handles exceptions.

  [env f]

  (let [env-2 (update env
                      :convex.shell.db/instance
                      (fn [instance]
                        (or instance
                            ($.db/current-set ($.db/open (str (Files/createTempFile "convex-shell-"
                                                                                    ".etch"
                                                                                    (make-array FileAttribute
                                                                                                0))))))))]
    (try
      ($.shell.ctx/def-result env-2
                              (f))
      (catch IOException ex
        ($.shell.exec.fail/err env
                               ($.shell.err/db ($.cell/string (.getMessage ex))))))))



(defn- -ensure-not-read-only

  ;;

  [env]

  (when (env :convex.shell.etch/read-only?)
    ($.shell.exec.fail/err env
                           ($.shell.err/db ($.cell/string "Etch is set to read-only")))))



(defmethod $.shell.exec/sreq

  $.shell.kw/etch-flush

  ;;

  [env _tuple]

  (-db env
       (fn []
         ($.db/flush)
         nil)))



(defmethod $.shell.exec/sreq

  $.shell.kw/etch-open

  ;;

  ;; Can be used only once so that users never mix cells from different stores.

  [env ^AVector tuple]

  (let [path     (.get tuple
                       2)
        path-old (env :convex.shell.db/instance)]
    (if (and path-old
             (not= path
                   path-old))
      ($.shell.exec.fail/err env
                             ($.shell.err/db ($.cell/string "Cannot open another database instance, one is already in use")))
      (try
        (when-not path-old
          (-> path
             ($.clj/string)
             ($.db/open)
             ($.db/current-set)))
        (-> env
            (assoc :convex.shell.db/instance
                   path)
            ($.shell.ctx/def-result path))
        ;;
        (catch IOException ex
          ($.shell.exec.fail/err env
                                 ($.shell.err/db ($.cell/string (.getMessage ex)))))))))



(defmethod $.shell.exec/sreq

  $.shell.kw/etch-path

  ;;

  [env _tuple]

  (-db env
       (fn []
         ($.cell/string ($.db/path)))))



(defmethod $.shell.exec/sreq

  $.shell.kw/etch-read

  ;;

  [env ^AVector tuple]

  (-db env
       (fn []
         ($.db/read ($.cell/hash<-blob (.get tuple
                                             2))))))


(defmethod $.shell.exec/sreq

  $.shell.kw/etch-read-only

  ;;

  [env ^AVector tuple]

  (-> env
      (assoc :convex.shell.etch/read-only?
             ($.clj/boolean (.get tuple
                                  2)))
      ($.shell.ctx/def-result nil)))



(defmethod $.shell.exec/sreq

  $.shell.kw/etch-read-only?

  ;;

  [env _tuple]

  ($.shell.ctx/def-result env
                          ($.cell/boolean (env :convex.shell.etch/read-only?))))


(defmethod $.shell.exec/sreq

  $.shell.kw/etch-root-read

  ;;

  [env _tuple]

  (-db env
       (fn []
         ($.db/root-read))))



(defmethod $.shell.exec/sreq

  $.shell.kw/etch-root-write

  ;;

  [env ^AVector tuple]

  (or (-ensure-not-read-only env)
      (-db env
           (fn []
             ($.db/root-write (.get tuple
                                    2))))))



(defmethod $.shell.exec/sreq

  $.shell.kw/etch-write

  ;;

  [env ^AVector tuple]

  (or (-ensure-not-read-only env)
      (-db env
           (fn []
             ($.db/write (.get tuple
                               2))))))


;;;;;;;;;; File


(defmethod $.shell.exec/sreq

  $.shell.kw/file-copy

  ;; Behaves like Unix `cp`.

  [env ^AVector tuple]

  (try
    (let [^String source           ($.clj/string (.get tuple
                                                       2))
          ^Path   source-path      (.toPath (File. source))
                  copy             (fn [^File destination-file]
                                     (Files/copy ^Path source-path
                                                 (.toPath destination-file)
                                                 ^"[Ljava.nio.file.StandardCopyOption;"
                                                 (into-array StandardCopyOption
                                                             [StandardCopyOption/REPLACE_EXISTING
                                                              StandardCopyOption/COPY_ATTRIBUTES]))
                                     ($.shell.ctx/def-result env
                                                             nil))
          ^String destination           ($.clj/string (.get tuple
                                                            3))
          ^File   destination-file (File. destination)]
      (if (.isDirectory destination-file)
        (let [^String destination-2      (str destination
                                              "/"
                                              (.getFileName source-path))
              ^File   destination-file-2 (File. destination-2)]
          (if (.isDirectory destination-file-2)
            ($.shell.exec.fail/err env
                                   ($.shell.err/filesystem ($.cell/string (format "Cannot overwrite directory '%s' with non directory '%s'"
                                                                                  destination-2
                                                                                  source))))
            (copy destination-file-2)))
        (copy destination-file)))
    ;;
    (catch Throwable ex
      ($.shell.exec.fail/err env
                             ($.shell.err/filesystem ($.cell/string (.getMessage ex)))))))



(defmethod $.shell.exec/sreq

  $.shell.kw/file-delete

  ;; Deletes file or empty directory.

  ;; TODO. Prints absolute path in errors.
  ;;       Make recursive over populated directories? Or too dangerous?

  [env ^AVector tuple]

  (let [^String path (-> tuple
                         (.get 2)
                         ($.clj/string))]
    (try
      ($.shell.ctx/def-result env
                              (-> path
                                  (File.)
                                  (.toPath)
                                  (Files/deleteIfExists)
                                  ($.cell/boolean)))
      ;;
      (catch DirectoryNotEmptyException _ex
        ($.shell.exec.fail/err env
                               ($.shell.err/filesystem ($.cell/string (str "Cannot delete non-empty directory: "
                                                                           path)))))
      ;;
      (catch Throwable ex
        ($.shell.exec.fail/err env
                               ($.shell.err/filesystem (.getMessage ex)))))))



(defmethod $.shell.exec/sreq

  $.shell.kw/file-exists

  ;; Testing if a file exists.

  [env ^AVector tuple]

  (try
    ($.shell.ctx/def-result env
                            (-> (.get tuple
                                      2)
                                ^String ($.clj/string)
                                (File.)
                                (.exists)
                                ($.cell/boolean)))
    (catch Throwable ex
      ($.shell.exec.fail/err env
                             ($.shell.err/filesystem ($.cell/string (.getMessage ex)))))))



(defmethod $.shell.exec/sreq

  $.shell.kw/file-stream-in

  ;; Opens a file for reading.

  [env ^AVector tuple]

  ($.shell.stream/file-in env
                          (.get tuple
                                2)
                          (str (.get tuple
                                     3))))



(defmethod $.shell.exec/sreq

  $.shell.kw/file-stream-out

  ;; Opens a file for writing.

  [env ^AVector tuple]

  ($.shell.stream/file-out env
                           (.get tuple
                                 2)
                           (str (.get tuple
                                       3))
                           ($.clj/boolean (.get tuple
                                                4))))



(defmethod $.shell.exec/sreq

  $.shell.kw/file-tmp

  ;; Creates a temporary file.

  [env ^AVector tuple]

  (try
    ($.shell.ctx/def-result
      env
      (-> (Files/createTempFile ($.clj/string (.get tuple
                                                    2))
                                ($.clj/string (.get tuple
                                                    3))
                                (make-array FileAttribute
                                            0))
          (str)
          ($.cell/string)))
    (catch Throwable ex
      ($.shell.exec.fail/err env
                             ($.shell.err/filesystem ($.cell/string (.getMessage ex)))))))



(defmethod $.shell.exec/sreq

  $.shell.kw/file-tmp-dir

  ;; Creates a temporary directory.

  [env ^AVector tuple]

  (try
    ($.shell.ctx/def-result
      env
      (-> (Files/createTempDirectory ($.clj/string (.get tuple
                                                         2))
                                     (make-array FileAttribute
                                                 0))
          (str)
          ($.cell/string)))
    (catch Throwable ex
      ($.shell.exec.fail/err env
                             ($.shell.err/filesystem ($.cell/string (.getMessage ex)))))))


;;;;;;;;;; Juice


(defmethod $.shell.exec/sreq

  $.shell.kw/juice-limit

  ;; Retrieves the current juice limit.

  [env _tuple]

  ($.shell.ctx/def-result env
                          ($.cell/long (env :convex.shell.juice/limit))))



(defmethod $.shell.exec/sreq

  $.shell.kw/juice-limit-set

  ;; Sets the current juice limit.

  [env ^AVector tuple]

  (-> env
      (assoc :convex.shell.juice/limit
             (if-some [limit (.get tuple
                                   2)]
               ($.clj/long limit)
               Long/MAX_VALUE))
      ($.shell.ctx/def-result nil)))



(defmethod $.shell.exec/sreq

  $.shell.kw/juice-track

  ;; Tracks juice consumption of the given transaction.

  [env ^AVector tuple]

  ($.shell.exec/trx-track-juice env
                                (.get tuple
                                      2)))


;;;;;;;;;; Logging


(defmethod $.shell.exec/sreq
  
  $.shell.kw/log-clear

  ;; Clears the CVM log.

  [env _tuple]

  (let [ctx   (env :convex.shell/ctx)
        ctx-2 ($.cvm/ctx {:convex.cvm/address ($.cvm/address ctx)
                          :convex.cvm/state   ($.cvm/state ctx)})]
    (-> env
        (assoc :convex.shell/ctx
               ctx-2)
        ($.shell.ctx/def-result ($.cvm/log ctx-2)))))



(defmethod $.shell.exec/sreq
  
  $.shell.kw/log-get

  ;; Interns the current state of the CVM log under `$/*result*`.

  [env _tuple]

  ($.shell.ctx/def-result env
                          ($.cvm/log (env :convex.shell/ctx))))


;;;;;;;;;; Process


(defmethod $.shell.exec/sreq

  $.shell.kw/process-exit

  ;; Exits process with the user given status code.

  [env ^AVector tuple]

  ($.shell.ctx/exit env
                    (.longValue ^CVMLong (.get tuple
                                         2))))



(defmethod $.shell.exec/sreq

  $.shell.kw/process-env
  
  ;; Interns under `$/*result*` the process environment map or a single requested variable.

  [env ^AVector tuple]

  ($.shell.ctx/def-result env
                          (if-some [env-var (.get tuple
                                                  2)]
                            (some-> (System/getenv (str env-var))
                                    ($.cell/string))
                            ($.cell/map (map (fn [[k v]]
                                               [($.cell/string k)
                                                ($.cell/string v)])
                                             (System/getenv))))))


;;;;;;;;;; State


(defn- -ensure-state

  ;; Returns `env` with an error if `x` is not a `State`.

  [env x]

  (when-not (instance? State
                       x)
    ($.shell.exec.fail/err env
                            ($.shell.err/arg ($.cell/string "Argument is not a valid CVM state")
                                             ($.cell/* state)))$.shell.exec.fail/err env
                            ($.shell.err/arg ($.cell/string "Argument is not a valid CVM state")
                                             ($.cell/* state))))



(defmethod $.shell.exec/sreq

  $.shell.kw/state-genesis

  ;; Produces a genesis state given genesis keys.

  [env ^AVector tuple]

  (try
    ($.shell.ctx/def-result env
                            (-> ($.cvm/ctx {:convex.cvm/genesis-key+ (map $.cell/key
                                                                          (.get tuple
                                                                                2))})
                                ($.cvm/state)))
    (catch Throwable ex
      ($.shell.exec.fail/err env
                             ($.shell.err/state ($.cell/string (.getMessage ex)))))))



(defmethod $.shell.exec/sreq

  $.shell.kw/state-load

  ;; Restores the given state and executes the given transaction afterwards.
  ;; This transaction is mostly useful for rememberings from state to state.
  ;;
  ;; There are quite a few ways to guess if a state is "shell-ready" and have access
  ;; to CVX Shell libraries (things will go horribly wrong if it does not).
  ;;
  ;; However, checking should be efficient. Hence, the code below is minimalistic.
  ;; We cannot prevent a user from screwing things on purpose anyways.

  [env ^AVector tuple]

  (let [state (.get tuple
                    2)]
    (or (-ensure-state env
                       state)
        (let [trx   (.get tuple
                          3)
              ctx   (env :convex.shell/ctx)
              ctx-2 (-> ctx
                        ($.cvm/fork)
                        ($.cvm/state-set (.get tuple
                                               2)))
              $     ($.cvm/look-up ctx-2
                                   $.shell.sym/$)
              ok    (fn [env ctx]
                      (-> env
                          (assoc :convex.shell/ctx
                                 ctx)
                          (cond->
                            trx
                            ($.shell.ctx/prepend-trx trx))
                          ($.shell.ctx/def-result nil)))]
          (if (and $
                   ($.cvm/look-up ctx-2
                                  $
                                  $.shell.sym/version))
            (ok env
                ctx-2)
            (let [x   ($.shell.ctx/deploy-lib+ ctx-2)
                  err (::$.shell.ctx/err x)]
              (if err
                ($.shell.exec.fail/err env
                                       ($.shell.err/state-load ($.cell/string (err :path))
                                                               ($.cell/string "Reverting state, cannot deploy shell library on new one")
                                                               (err :cvm-exception)))
                (ok env
                    x))))))))



(defmethod $.shell.exec/sreq

  $.shell.kw/state-safe

  ;; Evaluates a transaction in the given state, without preparing it in any way.

  [env ^AVector tuple]

  (let [state (.get tuple
                    2)]
    (or (-ensure-state env
                       state)
        (let [address (.get tuple
                            3)
              ctx     (env :convex.shell/ctx)
              ctx-2   (if address
                        ($.cvm/fork-to ctx
                                       address)
                        ($.cvm/fork ctx))
              ctx-3   (-> ctx-2
                          ($.cvm/state-set state)
                          ($.cvm/juice-set ($.clj/long (.get tuple
                                                             4)))
                          ($.cvm/eval (.get tuple
                                            5)))
              ex      ($.cvm/exception ctx-3)]
          (if ex
            ($.shell.exec.fail/err env
                                   ($.shell.err/mappify ex))
            ($.shell.ctx/def-result env
                                    ($.cell/* [~($.cvm/result ctx-3)
                                               ~($.cvm/state ctx-3)])))))))


;;;;;;;;;; Streams


(defmethod $.shell.exec/sreq

  $.shell.kw/stream-close

  ;; Closes the given stream.

  [env ^AVector tuple]

  ($.shell.stream/close env
                        (.get tuple
                              2)))



(defmethod $.shell.exec/sreq

  $.shell.kw/stream-flush

  ;; Flushes the given stream.

  [env ^AVector tuple]

  ($.shell.stream/flush env
                        (.get tuple
                              2)))



(defmethod $.shell.exec/sreq

  $.shell.kw/stream-in+

  ;; Reads all available cells from the given stream.

  [env ^AVector tuple]

  ($.shell.stream/in+ env
                      (.get tuple
                            2)))



(defmethod $.shell.exec/sreq

  $.shell.kw/stream-line

  ;; Reads line from the given stream and extracts all available cells.

  [env ^AVector tuple]

  ($.shell.stream/line env
                       (.get tuple
                             2)))



(defmethod $.shell.exec/sreq

  $.shell.kw/stream-out

  ;; Writes a cell to the given stream.

  [env ^AVector tuple]

  ($.shell.stream/out env
                      (.get tuple
                            2)
                      (.get tuple
                            3)))



(defmethod $.shell.exec/sreq

  $.shell.kw/stream-open?

  ;; Is a given stream open?

  [env ^AVector tuple]

  ($.shell.ctx/def-result env
                          (-> (env :convex.shell/stream+)
                              (contains? (.get tuple
                                               2))
                              ($.cell/boolean))))



(defmethod $.shell.exec/sreq


  $.shell.kw/stream-outln

  ;; Writes a cell to the given stream, appends a new line, and flushes everything.

  [env ^AVector tuple]

  ($.shell.stream/outln env
                        (.get tuple
                              2)
                        (.get tuple
                              3)))



(defmethod $.shell.exec/sreq

  $.shell.kw/stream-txt-in

  ;; Reads everything as text.

  [env ^AVector tuple]

  ($.shell.stream/txt-in env
                         (.get tuple
                               2)))



(defmethod $.shell.exec/sreq

  $.shell.kw/stream-txt-line

  ;; Reads a line of text.

  [env ^AVector tuple]

  ($.shell.stream/txt-line env
                           (.get tuple
                                 2)))



(defmethod $.shell.exec/sreq

  $.shell.kw/stream-txt-out

  ;; Do not double quote top-level strings.

  [env ^AVector tuple]

  ($.shell.stream/txt-out env
                          (.get tuple
                                2)
                          (.get tuple
                                3)))



(defmethod $.shell.exec/sreq

  $.shell.kw/stream-txt-outln

  ;; Do not double quote top-level strings.

  [env ^AVector tuple]

  ($.shell.stream/txt-outln env
                            (.get tuple
                                  2)
                            (.get tuple
                                  3)))


;;;;;;;;;; Time


(defmethod $.shell.exec/sreq

  $.shell.kw/time-advance

  ;; Advances the timestamp.

  [env ^AVector tuple]

  (let [^CVMLong interval (.get tuple
                                2)]
    (-> env
        (update :convex.shell/ctx
                (fn [ctx]
                  ($.cvm/time-advance ctx
                                      (.longValue interval))))
        ($.shell.ctx/def-result interval))))



(defmethod $.shell.exec/sreq

  $.shell.kw/time-bench

  ;; Benchmarks a transaction using Criterium.

  [env ^AVector tuple]

  (let [ctx   ($.cvm/fork (env :convex.shell/ctx))
        cell  (.get tuple
                    2)
        stat+ (criterium/benchmark* (fn []
                                      (-> ctx
                                          ($.cvm/fork)
                                          ($.cvm/eval cell)))
                                    {})]
    ($.shell.ctx/def-result env
                            ($.cell/* {:mean   ~($.cell/double (first (stat+ :mean)))
                                       :stddev ~($.cell/double (Math/sqrt ^double (first (stat+ :variance))))}))))



(defmethod $.shell.exec/sreq

  $.shell.kw/time-iso->unix

  ;; Convers ISO string to Unix timestamp.

  [env ^AVector tuple]

  ($.shell.ctx/def-result env
                          (-> (.get tuple
                                    2)
                              ($.clj/string)
                              ($.shell.time/iso->unix)
                              (some-> ($.cell/long)))))



(defmethod $.shell.exec/sreq

  $.shell.kw/time-nano

  ;; High-resolution timer.

  [env _tuple]

  ($.shell.ctx/def-result env
                          ($.cell/long ($.shell.time/nano))))



(defmethod $.shell.exec/sreq

  $.shell.kw/time-unix

  ;; Returns UNIX timestamp.

  [env _tuple]

  ($.shell.ctx/def-result env
                          ($.cell/long ($.shell.time/unix))))



(defmethod $.shell.exec/sreq

  $.shell.kw/time-unix->iso

  ;; Convers Unix timestamp to ISO string.

  [env ^AVector tuple]

  ($.shell.ctx/def-result env
                          (-> (.get tuple
                                    2)
                              ($.clj/long)
                              ($.shell.time/unix->iso)
                              ($.cell/string))))
