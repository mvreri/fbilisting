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
             (let [defaultparams {:page 1, :limit 20}
                   body (request-to-keywords (get-in request [:query-string]))
                   body (if (= body nil) defaultparams body)
                   limit (if (= (:limit body) nil)
                           (:limit defaultparams)
                           (if (number? (:limit body)) (:limit body) (Double/parseDouble (:limit body)))
                           )
                   fbireq (query/make-fbi-list-request body)
                   ;after the listing is called
                   allpages (double (/ (:total fbireq) 20))
                   ]
               ;post the response expected back to the UI
               (if (not= (:status fbireq) 200)
                 (json/write-str {:errors {
                                         :status (:status fbireq)
                                         :title (str "FBI List" )
                                         :details (:message fbireq)
                                         }
                                  })
                 (json/write-str {:data {
                                         :status (:status fbireq)
                                         :title (str "FBI List" )
                                         :details fbireq
                                         :page (:page fbireq)
                                         :total_records (:total fbireq)
                                         :number_of_pages allpages
                                         }
                                  })
                 )

               )
             )


           (GET "/api/v1/fbi/search" request
             (let [defaultparams {:page 1, :limit 20}
                   body (request-to-keywords (get-in request [:query-string]))
                   ;fbireq (query/search-fbi-list body)
                   fbireq (query/search-fbi-list-request body)
                   ;after the listing is called
                   allpages (double (/ (:total fbireq) 20))
                   ]
               ;post the response expected back to the UI
               (if (not= (:status fbireq) 200)
                 (json/write-str {:errors {
                                         :status (:status fbireq)
                                         :title (str "FBI List Search" )
                                         :details (:message fbireq)
                                         }
                                  })
                 (json/write-str {:data {
                                         :status (:status fbireq)
                                         :title (str "FBI List Search" )
                                         :details fbireq
                                         :page (:page fbireq)
                                         :total_records (:total fbireq)
                                         :number_of_pages allpages
                                         }
                                  })
                 )
               )
             )

           ;add records from the endpoints to the database
           (POST "/api/v1/fbi/list/update" request
             (let [body (walk/keywordize-keys (:body request))
                   updatelist (query/make-fbi-list-request body)
                   ]
               ;post the response expected back to the UI
               (with-out-str (json/pprint {:data {
                                                  :status 201
                                                  :title (str "FBI List Updated" )
                                                  :detail (str "Most wanted list updated successfully")
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
      (wrap-cors :access-control-allow-credentials "true"
                 :access-control-allow-origin      [#".*"]
                 :access-control-allow-headers #{"accept"
                                                 "accept-encoding"
                                                 "accept-language"
                                                 "authorization"
                                                 "content-type"
                                                 "origin"}
                 :access-control-allow-methods     [:get :put :post :delete :options]
                 )
      (wrap-fallback-exception)
      )
  )
