(defproject fbiwantedclj "0.1.0-SNAPSHOT"
  :description "The backend service to retrieve, search and paginate values from the FBI wanted  endpoint"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.1"]
                 [korma "0.4.3"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [com.taoensso/timbre "5.2.1"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/tools.logging "1.2.4"]
                 [org.postgresql/postgresql "42.5.0"]
                 [clj-http "3.12.3"]
                 [clj-time "0.15.2"]
                 [ring-cors "0.1.13"]
                 ]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler fbiwantedclj.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
