(defproject launchpad "0.1.0-SNAPSHOT"
  :description "Use Launchpad with Overtone"
  :url "http://github.com/josephwilk/launchpad"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [overtone "0.9.0-SNAPSHOT"]
                 [overtone/at-at "1.2.0"]]

  :profiles {:dev {:dependencies [[midje "1.5.1"]]
                   :plugins      [[lein-midje "3.0.1"]
                                  [lein-kibit "0.0.8"]]}})
