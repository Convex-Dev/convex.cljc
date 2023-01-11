(ns convex.shell.req.db

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

  [ctx message]

  ($.cvm/exception-set ctx
                       ($.cell/* :DB)
                       ($.cell/string message)))


;;;;;;;;;;


(defn- -db

  ;; Must be used to wrap Etch operations.
  ;; Ensures there is an instance and handles exceptions.

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


;;;;;;;;;;


(defn flush

  [ctx _arg+]

  (-db ctx
       (fn []
         ($.db/flush)
         nil)))



(defn open

  ;; Can be used only once so that users never mix cells from different stores.

  [ctx [path]]

  (or (when-not ($.std/string? path)
        ($.cvm/exception-set ctx
                             ($.cell/code-std* :ARGUMENT)
                             ($.cell/* "Path for opening Etch must be a string")))
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

  [ctx _arg+]

  (-db ctx
       (fn []
         ($.cell/string ($.db/path)))))



(defn read

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

  [ctx _arg+]

  (-db ctx
       (fn []
         ($.db/root-read))))



(defn root-write

  [ctx [cell]]

  (-db ctx
       (fn []
         ($.db/root-write cell))))



(defn write

  [ctx [cell]]

  (-db ctx
       (fn []
         ($.db/write cell))))
