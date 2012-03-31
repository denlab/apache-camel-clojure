(ns apache-camel-clojure.c01
  (use [clojure.repl :only [doc]])
  (import [org.apache.camel         CamelContext]
          [org.apache.camel.builder RouteBuilder]
          [org.apache.camel.impl    DefaultCamelContext]))

(def route (proxy [RouteBuilder] []
             (configure []
               (.. this
                   (from "file:/home/denis/tmp/camel/c01/in")
                   (to   "file:/home/denis/tmp/camel/c01/out")))))

(def ctx (DefaultCamelContext.))

(doto ctx
  (.addRoutes route)
  (.start))

;; play with it ...

;; then stop it:

(.stop ctx)





