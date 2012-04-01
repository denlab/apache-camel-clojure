(defproject apache-camel-clojure "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.apache.camel/camel-core       "2.9.0"]
                 [org.apache.camel/camel-jms        "2.9.0"]
                 [org.apache.activemq/activemq-core "5.5.1"]]
  :dev-dependencies [[midje "1.3.1"]
                     [com.intelie/lazytest "1.0.0-SNAPSHOT" :exclusions [swank-clojure]]])
