(defproject org.clj-grenada/darkestperu "0.1.0-SNAPSHOT"
  :description "Currently a tiny library for assembling JAR files"
  :url "https://github.com/clj-grenada/darkestperu"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [prismatic/schema "0.4.3"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.10"]]
                   :source-paths ["dev"]}})
