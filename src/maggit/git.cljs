(ns maggit.git
  (:require [clojure.string :as str]
            [maggit.shell :refer [exec]]
            [cljs.core.async])
  (:require-macros [cljs.core.async.macros]
                   [maggit.async :as a]))

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

;; Patch: file
;; Hunk: set of differing lines
(defn commit-diff-promise
  [repo-promise commit-sha]
  (a/async
   (with-out-str
     (let [repo (a/await repo-promise)
           commit (a/await (.getCommit repo commit-sha))
           diffs (js->clj (a/await (.getDiff commit)))]
       (a/doseq [diff diffs
                 patch (a/await (.patches diff))
                 hunk (a/await (.hunks patch))]
         (println "========")
         (println "-" (-> patch .oldFile .path))
         (println "+" (-> patch .newFile .path))
         (println "========")
         (a/doseq [line (a/await (.lines hunk))]
           (print (js/String.fromCharCode (.origin line))
                    (-> line .content)))
         (println "\n\n"))))))

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
