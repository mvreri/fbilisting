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

;utility fns
(defn convert-json-to-map [req]
  (walk/keywordize-keys (walk/keywordize-keys (json/read-str req)))
  )

;ueries that dont need results
(defn connect-db-run-query [fq]
  (try
    (with-db db-connection-fbisrvc (exec-raw fq))
    (catch Exception e
      {:status 500 :body {
                          :title "Database Error"
                          :description  e}}))

  )

;queries that expect results
(defn connect-db-run-query-with-result [fq]
  (try
    (with-db db-connection-fbisrvc (exec-raw fq :results))
    (catch Exception e
      {:status 500 :body {
                          :title "Database Error"
                          :description  e}}))

  )


(defn get-fbi-list [susref]
  (connect-db-run-query-with-result
    (format "SELECT refno refno, details::text details
             FROM tbl_wanted_suspects
             WHERE refno = '%s';"
            susref
            )
    )
  )

(defn make-fbi-list-request [req]
  "this is where we fetch the data from"
  ;(timbre/info req)
  ;(timbre/info fbi-base-url)
  (let [fbilst (httpclient/get (str fbi-base-url "?page=" (:page req))
                               {
                                :headers       {}
                                :insecure? true
                                :max-redirects 5
                                :redirect-strategy :graceful
                                :socket-timeout 1000
                                :connection-timeout 1000
                                }
                               )
        ]

    ;validate response using the http status code
    (if (= (:status fbilst) 200)
      (let [
            fbilist (:body fbilst)
            fbilist_map (convert-json-to-map fbilist)
            ]

        (if (= (count (:items fbilist_map)) 0)
          (do
            {
             :total 0, :items [], :page 0, :message (str "No more records found")
             }
            )
          (let [fbilist_map (assoc fbilist_map :message  (str "Records retrieved successfully"))]
            fbilist_map
            )
          )
        )
      (if (= (:status fbilst) 404)
        {
         :total 0, :items [], :page 0, :message (str "Error: " (:status fbilst) " - Response Not Found")
         }
        (if (= (:status fbilst) 403)
          {
           :total 0, :items [], :page 0, :message (str "Error: " (:status fbilst) " - URL Access Forbidden")
           }
          (if (= (:status fbilst) 500)
            {
             :total 0, :items [], :page 0, :message (str "Error: " (:status fbilst) " - Internal Server error")
             }
            (if (= (:status fbilst) 503)
              {
               :total 0, :items [], :page 0, :message (str "Error: " (:status fbilst) " - Service Unavailable")
               }
              )
            )
          )
        )
      )
    )
  )