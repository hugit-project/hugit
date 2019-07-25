(ns maggit.git
  #_(:require-macros [cljs.core.async.macros :refer [go go-loop]])
  #_(:require [cljs.core.async :refer [<! >!] :as a])
  (:require [clojure.string :as str]))

;; Basic Classes
;; =============
(defonce Git
  (js/require "nodegit"))

(defonce Repository
  (.-Repository Git))


;; Get Basic Objects
;; =================
(defn repo-promise [path]
  (.open Repository path))


;; Utils
;; =====
(defn current-branch-name-promise
  [repo-promise]
  (-> repo-promise
      (.then #(.getCurrentBranch %))
      (.then (fn [branch]
               (-> (.name branch)
                   (str/split #"/")
                   last)))))

(defn current-head-commit-message-promise
  [repo-promise]
  (-> repo-promise
      (.then #(.getHeadCommit %))
      (.then (fn [commit]
               (.message commit)))))

(defn statuses-promise
  [repo-promise]
  (.then repo-promise
         #(.getStatus %)))
