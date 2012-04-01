(ns ^{:doc "Utilities"}
  apache-camel-clojure.core)

(def log-file
  "/home/denis/tmp/camel/camel.log")

(defn log
  [msg] (spit log-file (str (java.util.Date.) " - " msg "\n") :append true))

