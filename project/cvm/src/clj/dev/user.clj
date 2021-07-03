(ns user

  "Require `./dev.clj` if it exists.")


;;;;;;;;;;


(try
  (require 'dev)
  (catch Throwable _ex))
