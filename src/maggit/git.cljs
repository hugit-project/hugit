(ns maggit.git
  #_(:require-macros [cljs.core.async.macros :refer [go go-loop]])
  #_(:require [cljs.core.async :refer [<! >!] :as a])
  (:require [clojure.string :as str]))

(defonce Git
  (js/require "nodegit"))

(defn repo-promise [path]
  (-> Git
      .-Repository
      (.open path)))

(defn current-branch-promise [repo]
  (.getCurrentBranch repo))

(defn head-commit-promise [repo]
  (.getHeadCommit repo))

(defn current-branch-name-promise
  [repo-promise]
  (-> repo-promise
      (.then current-branch-promise)
      (.then (fn [branch]
               (last (str/split (.name branch) #"/"))))))

(defn current-head-commit-message-promise
  [repo-promise]
  (-> repo-promise
      (.then head-commit-promise)
      (.then (fn [commit]
               (.message commit)))))
