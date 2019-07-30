(ns maggit.git
  (:require [clojure.string :as str]
            [maggit.shell :refer [exec]]
            [cljs.core.async])
  (:require-macros [cljs.core.async.macros]
                   [maggit.async :refer [async await doseq]]))

;; Basic Classes
;; =============
(defonce Git
  (js/require "nodegit"))

(defonce Repository
  (.-Repository Git))

(defonce Diff
  (.-Diff Git))


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

(defn print-commit-diffs
  [repo-promise commit-sha]
  (async
   (let [repo (await repo-promise)
         commit (await (.getCommit repo commit-sha))
         diffs (js->clj (await (.getDiff commit)))]
     (doseq [diff diffs
             patch (await (.patches diff))
             hunk (await (.hunks patch))]
       (println "diff"
                (-> patch .oldFile .path)
                (-> patch .newFile .path))
       (doseq [line (await (.lines hunk))]
         (println (js/String.fromCharCode (.origin line))
                  (-> line .content .trim)))))))

;; Git commancds
;; =============
;; To use in bootstrap phase
(defn stage-file
  [file]
  (exec "git add " file))

(defn unstage-file
  [file]
  (exec "git reset " file))

(defn untrack-file
  [file]
  (exec "git rm --cached " file))

(defn checkout-file
  [file]
  (exec "git checkout " file))

(defn commit
  [msg]
  (exec "git commit -m \"" msg "\""))
