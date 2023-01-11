(ns convex.shell.req.db

  (:import (java.io IOException)
           (java.nio.channels OverlappingFileLockException)
           (java.nio.file Files)
           (java.nio.file.attribute FileAttribute))
  (:refer-clojure :exclude [flush
                            read])
  (:require [convex.cell      :as $.cell]
            [convex.cvm       :as $.cvm]
            [convex.db        :as $.db]
            [convex.shell.env :as $.shell.env]
            [convex.std       :as $.std]))


;;;;;;;;;;


(defn- -db

  ;; Must be used to wrap Etch operations.
  ;; Ensures there is an instance and handles exceptions.

  [ctx f]

  (let [ctx-2 ($.shell.env/update ctx
                                  (fn [env]
                                    (update env
                                            :convex.shell.db/instance
                                            (fn [instance]
                                              (or instance
                                                  ($.db/current-set ($.db/open (str (Files/createTempFile "convex-shell-"
                                                                                                          ".etch"
                                                                                                          (make-array FileAttribute
                                                                                                                      0))))))))))]
    (try
      ($.cvm/result-set ctx-2
                        (f))
      (catch IOException ex
        ($.cvm/exception-set ctx-2
                             ($.cell/* :DB)
                             ($.cell/string (.getMessage ex)))))))



(defn- -fail-open

  [ctx message]

  ($.cvm/exception-set ctx
                       ($.cell/* :DB)
                       ($.cell/string message)))


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
      (let [path-new (str path)
            path-old (-> ctx
                         ($.shell.env/get)
                         (:convex.shell.db/instance))]
        (if (and path-old
                 (not= path-new
                       path-old))
          ($.cvm/exception-set ctx
                               ($.cell/* :DB)
                               ($.cell/string "Cannot open another database instance, one is already in use"))
          (try
            (when-not path-old
              (-> path-new
                  ($.db/open)
                  ($.db/current-set)))
            (-> ctx
                ($.shell.env/update (fn [env]
                                      (assoc env
                                             :convex.shell.db/instance
                                             path-new)))
                ($.cvm/result-set path))
            ;;
            (catch IOException ex
              (-fail-open ctx
                          (.getMessage ex)))
            ;;
            (catch OverlappingFileLockException ex
              (-fail-open ctx
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
