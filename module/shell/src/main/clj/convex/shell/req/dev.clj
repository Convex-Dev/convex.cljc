(ns convex.shell.req.dev
  
  "Requests only used for dev purposes."

  {:author "Adam Helinski"})


;;;;;;;;;;


(defn fatal

  "Request for throwing a JVM exception, which should result in a fatal
   error in the Shell."

  [_ctx [message]]

  (throw (Exception. (str message))))
