(ns fbiwantedclj.query
  (:require [fbiwantedclj.config :as config]
            [clojure.string :as s]
            [taoensso.timbre :as timbre]
            [ring.middleware.json :as middleware]
            [clojure.data.json :as json]
            [korma.db :refer :all]
            [clj-http.client :as httpclient]
            [clj-time.coerce :as tc]
            [clojure.string :as string]
            [clojure.walk :as walk]
            [korma.core :refer :all]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as tc]
            [clj-time.local :as l]
            [clj-time.jdbc :as jd]
    )
  )

;initializing the config values in this namespace
(config/initialize-config)

;converting the values in the JSON file to a map
(def conffig-atom (atom (walk/keywordize-keys (walk/keywordize-keys (json/read-str (slurp config/config-path))))))
;(timbre/info (instance? clojure.lang.Atom conffig-atom))
(def conffig @conffig-atom)

;this is the connection to the postgres database
(def db-connection-fbisrvc
  {:classname "org.postgresql.Driver"
   :subprotocol (:db-subprotocol conffig)
   :user (:db-user conffig)
   :password (:db-password conffig)
   :subname (:db-subname conffig)
   })

;variables that we will need in the backend from the config file
(def application (:application conffig))
(def fbi-base-url (:fbi-base-url conffig))

;testing whether we receive config values here
;(timbre/info fbi-base-url)

(defn make-fbi-list-request [req]
  "this is where we fetch the data from"
  (let [
        sim (:body (httpclient/post (str fbi-base-url )
                                    {
                                     :headers       {}
                                     :body          (json/write-str req)
                                     :body-encoding "UTF-8"
                                     :content-type  :json
                                     :accept        :json
                                     }
                                    )
              )
        _ (timbre/info "sim >>>>> " sim)
        ]
    sim
    )

  )
