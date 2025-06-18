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

;convert query strings to keywords
(defn request-to-keywords [req]
  (into {} (for [[_ k v] (re-seq #"([^&=]+)=([^&]+)" req)]
             [(keyword k) v])))

(defroutes app-routes
           (GET "/" [] "Hello World")

           (GET "/api/v1/fbi/list" request
             ;(timbre/info (get-in request [:query-string]))
             (timbre/info (request-to-keywords (get-in request [:query-string])) )
             ;(timbre/info (get-in request [:params]))
             ;(timbre/info (query/make-fbi-list-request (request-to-keywords (get-in request [:query-string]))))
             (let [defaultparams {:page "1", :limit 20}
                   body (request-to-keywords (get-in request [:query-string]))
                   _ (timbre/info (:limit body))
                   limit (if (= (:limit body) nil)
                           20
                           (if (number? (:limit body)) (:limit body) (Double/parseDouble (:limit body)))
                           )
                   fbireq (query/make-fbi-list-request body)
                   listnum (:total fbireq)
                   listpg (:page fbireq)
                   allpages (/ listnum (:defaultparams limit))
                   ]
               ;post the response expected back to the UI
               (json/write-str {:data {
                                    :status 200
                                    :title (str "FBI List" )
                                    :details fbireq
                                       :page listpg
                                       :total_records listnum
                                    }
                             })
               )
             )

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
