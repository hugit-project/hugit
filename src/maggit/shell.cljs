(ns maggit.shell
  (:require [clojure.string :as str]))

(defonce shelljs
  (js/require "shelljs"))

(defn- exec*
  [command & {:keys [async silent encoding callback]
              :or {async false
                   silent false
                   encoding "utf8"}
              :as options}]
  (let [opts (dissoc options :callback)]
    (if (some? callback)
      (.exec shelljs
             command
             (clj->js opts)
             callback)
      (let [res (.exec shelljs
                       command
                       (clj->js opts))]
        {:code (.-code res)
         :stdout (.trim (.-stdout res))
         :stderr (.trim (.-stderr res))}))))

(defn exec
  [& strings]
  (exec*  (str/join strings)
          :silent true))

(defn exec-promise
  [& strings]
  (js/Promise.
   (fn [resolve]
     (letfn [(callback [code stdout stderr]
               (resolve {:code code
                         :stdout (.trim stdout)
                         :stderr (.trim stderr)}))]
       (exec* (str/join strings)
              :async true
              :silent true
              :callback callback)))))
