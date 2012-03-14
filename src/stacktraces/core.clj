(ns stacktraces.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [stacktraces.helpers :as helper]))

(def request-quota-left (atom 0))

;; I'm not letting it throw exceptions, which I know I should, but I
;; don't know how to wrap it properly (yet).  This is a TODO
(def default-http-params {:throw-exceptions false :ignore-unknown-host? true :as :json})

(def url "http://api.stackexchange.com/2.0")

(defn request
  "make an HTTP GET request agains the API, passing in any optional query params"
  
  ([api-method] 
    {:post [(swap! request-quota-left (:quota_remaining %))]}
    (http/get (str url api-method) default-http-params))
  ([api-method params]
    {:post [(swap! request-quota-left (:quota_remaining %))]}
     (http/get (str url api-method "?") (merge default-http-params {:query-params params}))))

(defn get-errors
  "List all of the error types in the StackExchange world.  If passed an err-code, look it up and return the right value for the error explaining what it means."
  ([] (request "/errors")) 
  ([err-code] (request (str url "/errors/" err-code))))

(defn get-sxch-sites
  "Get a list of the StackExchange sites."
  ([] (request "/sites"))
  ([opts] (request "/sites" opts))
  ([page per-page] (request "/sites" {:page page :pagesize per-page})))

(defn site-info [site-name]
  "Get the Site info"
  (request "/info" {:site site-name}))

(defn get-sxch-names []
  "Gets a seq of StackExchange site names"
  (map :name (:items (:body (get-sxch-sites)))))

