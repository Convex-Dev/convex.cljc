(ns convex.shell.req.db

  "Requests relating to Etch."

  {:author "Adam Helinski"}

  (:import (java.io IOException)
           (java.nio.channels OverlappingFileLockException))
  (:refer-clojure :exclude [flush
                            read])
  (:require [babashka.fs :as bb.fs]
            [convex.cell :as $.cell]
            [convex.cvm  :as $.cvm]
            [convex.db   :as $.db]
            [convex.std  :as $.std]))


;;;;;;;;;;


(defn- -fail

  ;; Returns the CVM in an exceptional state when an Etch related
  ;; request cannot be performed.

  [ctx message]

  ($.cvm/exception-set ctx
                       ($.cell/* :DB)
                       ($.cell/string message)))


;;;;;;;;;;


(def  ^ThreadLocal allow-open?

  ;; Must be set to `true` for [[open]] to work.
  ;;
  ;; In a multithreaded situation, used to prevent new threads from opening an instance
  ;; when the main thread did not.

  (ThreadLocal.))



(defn- -db

  ;; Used for carrying out Etch requests.
  ;; Ensures there is an instance and handles failures.

  [ctx f]

  (if ($.db/current)
    (try
      ($.cvm/result-set ctx
                        (f))
      (catch IOException ex
        (-fail ctx
               (.getMessage ex))))
    (-fail ctx
           "No Etch instance has been opened")))



(defn- -write

  [ctx cell f-write]

  (or (when (nil? cell)
        (-fail ctx
               "Cell to write cannot be Nil"))
      (-db ctx
           (fn []
             (f-write cell)))))


;;;;;;;;;;


(defn flush

  "Request for flushing Etch."

  [ctx _arg+]

  (-db ctx
       (fn []
         ($.db/flush)
         nil)))



(defn open

  "Request for opening an Etch instance.
  
   Only one instance can be open per Shell, so that the user cannot possible
   mingle cells coming from different instances.
   Idempotent nonetheless if the user provides the same path."

  [ctx [path]]

  (or (when-not ($.std/string? path)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Path for opening Etch must be a string")))
      (when-not (.get allow-open?)
        ($.cvm/exception-set ctx
                             ($.cell/* :DB)
                             ($.cell/* "Cannot open an instance because the main thread did not")))
      (let [path-new (-> path
                         (str)
                         (bb.fs/expand-home)
                         (bb.fs/canonicalize)
                         (str))
            path-old (when ($.db/current)
                       (str ($.db/path)))]
        (if (and path-old
                 (not= path-new
                       path-old))
          ($.cvm/exception-set ctx
                               ($.cell/* :DB)
                               ($.cell/string "Cannot open another Etch instance, one is already in use"))
          (try
            ;;
            (when-not path-old
              (-> path-new
                  ($.db/open)
                  ($.db/current-set)))
            ($.cvm/result-set ctx
                              ($.cell/string path-new))
            ;;
            (catch IOException ex
              (-fail ctx
                     (.getMessage ex)))
            ;;
            (catch OverlappingFileLockException _ex
              (-fail ctx
                     "File lock failed")))))))



(defn path

  "Request for getting the path of the currently open instance (or nil)."

  [ctx _arg+]

  ($.cvm/result-set ctx
                    (when ($.db/current)
                      ($.cell/string ($.db/path)))))



(defn read

  "Request for reading a cell by hash."

  [ctx [hash]]

  (or (when-not (and ($.std/blob? hash)
                     (= ($.std/count hash)
                        32))
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "A hash (32-byte blob) is required for reading from Etch")))
      (-db ctx
           (fn []
             ($.db/read ($.cell/hash<-blob hash))))))



(defn root-read

  "Request for reading from the root."

  [ctx _arg+]

  (-db ctx
       (fn []
         ($.db/root-read))))



(defn root-write

  "Request for writing to the root."

  [ctx [cell]]

  (-write ctx
          cell
          $.db/root-write))



(defn size

  "Request for returning the precise data size of the Etch instance."

  [ctx _arg+]

  (-db ctx
       (fn []
         ($.cell/long ($.db/size)))))



(defn write

  "Request for writing a cell."

  [ctx [cell]]

  (-write ctx
          cell
          $.db/write))
