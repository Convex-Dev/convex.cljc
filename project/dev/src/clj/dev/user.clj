(ns user

  "Tries to require the `dev` namespace.
  
   In practice, the `dev` namespace is a copy of the [[convex.dev]] namespace which can be used as needed.
   It will not be checkout out in the repository."

  {:author "Adam Helinski"}

  (:require [convex.dev]))


;;;;;;;;;;


(try
  (require 'dev)
  (catch Throwable _ex))


(comment



  )
