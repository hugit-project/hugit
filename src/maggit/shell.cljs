(ns maggit.shell
  (:require [clojure.string :as str]))

(defonce shelljs
  (js/require "shelljs"))

(defn exec*
  [command & {:keys [async silent encoding callback]
              :or {async false
                   silent false
                   encoding "utf8"
                   callback (fn [_])}
              :as options}]
  (let [opts (dissoc options :callback)
        res (.exec shelljs
                   command
                   (clj->js opts)
                   callback)]
    {:code (.-code res)
     :stdout (.-stdout res)
     :stderr (.-stderr res)}))

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
                         :stdout stdout
                         :stderr stderr}))]
       (exec* (str/join strings)
              :async true
              :silent true
              :callback callback)))))
