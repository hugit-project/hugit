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

(defn head-commit-promise
  [repo-promise]
  (.then repo-promise
         #(.getHeadCommit %)))

(defn current-head-commit-message-promise
  [repo-promise]
  (-> repo-promise
      head-commit-promise
      (.then (fn [commit]
               (.message commit)))))

(defn statuses-promise
  [repo-promise]
  (.then repo-promise
         #(.getStatus %)))

(defn commits-promise
  [repo-promise]
  (js/Promise.
   (fn [resolve]
     (-> repo-promise
         head-commit-promise
         (.then (fn [head-commit]
                  (let [history (.history head-commit)]
                    (.on history "end"
                         (fn [commits]
                           (resolve
                            (for [commit (js->clj commits)]
                              {:sha (.sha commit)
                               :author {:name (-> commit .author .name)
                                        :email (-> commit .author .email)}
                               :date (.date commit)
                               :summary (.summary commit)
                               :message (.message commit)}))))
                    (.start history))))))))

(defn stage-file
  [repo-promise file]
  (-> repo-promise
      (.then #(.refreshIndex %))
      (.then (fn [index]
               (.addByPath index file)
               (.write index)))
      (.then (fn [code]
               (when-not (zero? code)
                 (stage-file repo-promise file))))))

(defn unstage-file
  [repo-promise file]
  (-> repo-promise
      (.then #(.refreshIndex %))
      (.then (fn [index]
               (.write index)))))

(defn untrack-file
  [repo-promise file]
  (println :untracking)
  (-> repo-promise
      (.then #(.refreshIndex %))
      (.then (fn [index]
               (.removeByPath index file)
               (.write index)))
      (.then (fn [code]
               (if-not (zero? code)
                 (untrack-file repo-promise file)
                 (println :untracked))))))
