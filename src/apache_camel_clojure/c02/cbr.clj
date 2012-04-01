(ns ^{:doc "CBR (Content Based Router) example"}
  apache-camel-clojure.c02.cbr
  (use [clojure.repl         :only [doc]]
       [clojure.java.javadoc :only [javadoc add-remote-javadoc]]
       [clojure.pprint       :only [pprint]]
       [clojure.reflect      :only [reflect]]
       [apache-camel-clojure.core])
  (import [javax.jms                      ConnectionFactory]
          [org.apache.activemq            ActiveMQConnectionFactory]
          [org.apache.camel.component.jms JmsComponent]
          [org.apache.camel               CamelContext Processor]
          [org.apache.camel.builder       RouteBuilder]
          [org.apache.camel.impl          DefaultCamelContext]))

;;
;; Usage:
;;
;; in emacs clojure-mode:
;; 
;; - compile this file: C-c C-k
;; - Go to the end of the first form (def proc) and execute it: C-x C-e
;; - Go to the following form: C-M-f
;; - and execute it

(defn make-log-proc
  [msg] (proxy [Processor] []
              (process [exchange]
                (log (str msg ":" exchange)))))

(comment
  (def route (proxy [RouteBuilder] []
               (configure []
                 (.. this
                     (from "file:/home/denis/tmp/camel/c02/in")
                     (process (make-log-proc "before jms queue: "))
                     (to "jms:incomingOrders"))
                 (.. this
                     (from "jms:incomingOrders")
                     choice
                     (when (-> this
                               (.header "CamelFileName")
                               (.endsWith ".xml")))
                     (to "file:/home/denis/tmp/camel/c02/out-xml")
                     (when (-> this
                               (.header "CamelFileName")
                               (.endsWith ".csv")))
                     (to "file:/home/denis/tmp/camel/c02/out-csv")))))

  (def connFact (ActiveMQConnectionFactory. "vm://localhost"))
  
  (def ctx (DefaultCamelContext.))

  (doto ctx
    (.addRoutes route)
    (.addComponent "jms" (JmsComponent/jmsComponentAutoAcknowledge connFact))
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
