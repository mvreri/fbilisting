(ns fbiwantedclj.config
  (:require [taoensso.timbre :as timbre])
  (:import  [java.util Properties]
            [java.io FileInputStream]
            )
  (:use [clojure.walk])
  (:use korma.core)
  (:use korma.db)
  )

(def app-configs (atom {}))

(defn- config-file []
  (let [result (let [result (System/getenv "fbiservice")]
                 (when result (java.io.File. result)))]
    (if (and result (.isFile result))
      result
      (do (timbre/fatal (format "serverConfig(%s) = nil" result))
          (throw (Exception. (format "Server configuration file (%s) not fo6" result)))))))

(defn load-config [config]
  (let [properties (Properties.)
        fis (FileInputStream. config)]
    ; populate the properties hashmap with values from the output stream
    (.. properties (load fis))
    (keywordize-keys (into {} properties))))

(defn config-value [name & args]
  (let [value (@app-configs name)]
    (when value
      (let [args (when args (apply assoc {} args))
            {type :type} args
            args (dissoc args :type)
            value (if (vector? value)
                    (loop [x (first value)
                           xs (next value)]
                      (let [properties (dissoc x :value)]
                        (if (or (and (empty? args)
                                     (empty? properties))
                                (and (not (empty? args))
                                     (every? (fn [[k v]]
                                               (= (properties k) v))
                                             args)))
                          (x :value)
                          (when xs
                            (recur (first xs) (next xs))))))
                    value)]
        (when value
          (let [value #^String value]
            (cond (or (nil? type) (= type :string)) value
                  ;; ---
                  (= type :int) (Integer/valueOf value)
                  (= type :long) (Long/valueOf value)
                  (= type :bool) (contains? #{"yes" "true" "y" "t" "1"}
                                            (.toLowerCase value))
                  (= type :path) (java.io.File. value)
                  (= type :url) (java.net.URL. value)
                  )))))))

(defn initialize-config []
  (timbre/info "initializing configurations..")
  (reset! app-configs (load-config (config-file)))
  (def config-path (config-value :config-path))

  (if (not (.exists (clojure.java.io/file config-path)))
    (do
      (timbre/info "Stopping Application. Config File not Found")
      (java.lang.System/exit 0)
      )
    (timbre/info "config-path ----> " config-path)
    )
  )
