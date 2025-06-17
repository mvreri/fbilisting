(ns fbiwantedclj.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.cors :refer [wrap-cors]]
            [fbiwantedclj.query :as query]
            [ring.middleware.json :as middleware]
            [taoensso.timbre :as timbre]
            [clojure.data.json :as json]
            [clojure.walk :as walk]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
           (GET "/" [] "Hello World")

           (POST "/api/v1/fbi/list" request
             (let [body (walk/keywordize-keys (:body request))
                   listnum (query/make-fbi-list-request body)
                   limit (if (number? (:limit (:usersparams body))) (:limit (:usersparams body)) (Double/parseDouble (:limit (:usersparams body))))
                   pages (/ listnum limit)
                   ]
               ;post the response expected back to the UI
               (with-out-str (json/pprint {:data {
                                                  :status 200
                                                  :title (str "FBI List" )
                                                  :detail (query/make-fbi-list-request (:refno body))
                                                  :currentpage (if (number? (:page (:usersparams body))) (:page (:usersparams body)) (Double/parseDouble (:page (:usersparams body))))
                                                  :totalpages (Math/ceil pages)
                                                  }
                                           })
                             )
               )
             )
           (route/not-found "Not Found"))

(defn wrap-fallback-exception
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        {:status 500 :body (str "Something isn't quite right..." e)}))))

(def app
  (-> app-routes
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)
      (wrap-defaults app-routes)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:post :get]
                 :access-control-allow-header ["Access-Control-Allow-Origin" "*"
                                               "Origin" "X-Requested-With"
                                               "Content-Type" "Accept"]
                 )
      (wrap-fallback-exception)
      )
  )
