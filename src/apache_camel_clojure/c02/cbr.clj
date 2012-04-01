(ns ^{:doc "CBR (Content Based Router) example"}
  apache-camel-clojure.c02.cbr
  (use [clojure.repl         :only [doc]]
       [clojure.java.javadoc :only [javadoc add-remote-javadoc]]
       [clojure.pprint       :only [pprint]]
       [clojure.reflect      :only [reflect]]
       [clojure.xml          :only [emit parse]]
       [clojure.java.shell   :only [sh]]
       [apache-camel-clojure.core])
  (import [javax.jms                      ConnectionFactory]
          [org.apache.activemq            ActiveMQConnectionFactory]
          [org.apache.camel.component.jms JmsComponent]
          [org.apache.camel               CamelContext Processor]
          [org.apache.camel.builder       RouteBuilder]
          [org.apache.camel.impl          DefaultCamelContext]
          [org.apache.camel.model         MulticastDefinition]))

;;
;; Usage:
;;
;; in emacs clojure-mode:
;; 
;; - compile this file: C-c C-k
;; - Go to the end of the first form (def proc) and execute it: C-x C-e
;; - Go to the following form: C-M-f
;; - and execute it

(def camel-path "/home/denis/tmp/camel")

(defn path
  [name] (str "file:" camel-path "/c02/" name))

(def paths
  (let [n [:in :out-xml :out-csv :out-bad :out-nex :out-pro :out-acc]]
    (zipmap n
            (map-indexed #(path (.replace (str % %2) \: \-))
                         n))))

(defn new-input-msg-str
  [test?] (with-out-str
            (emit {:tag     :order
                   :attrs   (conj {:name "motor", :amount "1", :customer "foo"}
                                  (if test? [:test "true"]))
                   :content nil})))

(defn new-input-msg
  ([]      (new-input-msg false))
  ([test?] (spit (str (:in paths) "/" (System/currentTimeMillis) ".xml")
                 (new-input-msg-str test?))))

(defn make-log-proc
  [msg] (proxy [Processor] []
              (process [exchange]
                (log (str msg ":" exchange)))))

(comment
  (def route (proxy [RouteBuilder] []
               (configure []
                 (.. this
                     (from (:in paths))
                     (process (make-log-proc "\nbefore jms queue: "))
                     (to "jms:incomingOrders"))
                 (.. this
                     (from "jms:incomingOrders")
                     choice
                     (when (-> this
                               (.header "CamelFileName")
                               (.endsWith ".xml")))       (to (:out-xml paths))
                     (when (-> this
                               (.header "CamelFileName")
                               (.regex "^.*(csv|csl)$"))) (to (:out-csv paths))
                     otherwise (to (:out-bad paths)) stop
                     end
                     (to (:out-nex paths)))
                 (.. this
                     (from (:out-xml paths))
                     (filter (.xpath this "/order[not(@test)]"))
                     multicast (to (to-array [(:out-pro paths) (:out-acc paths)])))
                 (.. this
                     (from (:out-pro paths))
                     (process (make-log-proc "production: received XML")))
                 (.. this
                     (from (:out-acc paths))
                     (process (make-log-proc "accounting: received XML"))))))

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
