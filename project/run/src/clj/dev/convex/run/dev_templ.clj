(ns convex.run.dev

  "Daydreaming at the REPL."

  {:clj-kondo/config '{:linters {:unused-import    {:level :off}
                                 :unused-namespace {:level :off}}}}

  (:require [clojure.pprint]
            [convex.cvm       :as $.cvm]
            [convex.data      :as $.data]
            [convex.run       :as $.run]
            [convex.run.ctx   :as $.run.ctx]
            [convex.run.err   :as $.run.err]
            [convex.run.exec  :as $.run.exec]
            [convex.run.kw    :as $.run.kw]
            [convex.run.sym   :as $.run.sym]
            [convex.run.sreq]
            [convex.sync      :as $.sync]))


;;;;;;;;;;


(comment




  )
