(ns maggit.shell)

(defonce shelljs
  (js/require "shelljs"))

(defn exec [& strings]
  (.setTimeout
   #(.exec shelljs
           (clojure.string/join strings)
           #js {:silent true})
   0))
