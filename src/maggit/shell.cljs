(ns maggit.shell)

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
                   (clj->js options)
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
               (println stdout)
               (resolve {:code code
                         :stdout stdout
                         :stderr stderr}))]
       (exec* (str/join strings)
              :silent true
              :async true
              :callback callback)))))
