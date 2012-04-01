(ns apache-camel-clojure.c02.c02
  (use [clojure.repl         :only [doc]]
       [clojure.java.javadoc :only [javadoc add-remote-javadoc]]
       [clojure.pprint       :only [pprint]]
       [clojure.reflect      :only [reflect]]
       [apache-camel-clojure.core])
  (import [org.apache.camel         CamelContext Processor]
          [org.apache.camel.builder RouteBuilder]
          [org.apache.camel.impl    DefaultCamelContext]))

(comment 
  (def proc (proxy [Processor] []
              (process [exchange]
                (log (str "ex:" exchange)))))


  (def route (proxy [RouteBuilder] []
               (configure []
                 (.. this
                     (from "file:/home/denis/tmp/camel/c01/in")
                     (process proc)
                     (to   "file:/home/denis/tmp/camel/c01/out")))))

  (def ctx (DefaultCamelContext.))

  (doto ctx
    (.addRoutes route)
    (.start))

  ;; play with it ...

  ;; for example dynamically change the processor ;)

  (update-proxy proc {"process" (fn [this exchange]
                                  (log (str "We just downloaded: "
                                            (.. exchange
                                                getIn
                                                (getHeader "CamelFileName")))))})

  ;; then stop it:

  (.stop ctx))





