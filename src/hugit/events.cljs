(ns hugit.events
  "Event handlers for re-frame dispatch events.
  Used to manage app db updates and side-effects.
  https://github.com/Day8/re-frame/blob/master/docs/EffectfulHandlers.md"
  (:require [re-frame.core :as rf]
            [hugit.git :as git]
            [hugit.logs :refer [log] :as logs]
            [clojure.string :as str]))

(defonce watch
  (js/require "watch"))

(defonce fs
  (js/require "fs"))

(rf/reg-event-db
  :init
  (fn [db [_ opts terminal-size]]
    (rf/dispatch [:get-status])
    (let [cwd (js/process.cwd)]
      (.unwatchTree watch cwd)
      (.watchTree watch cwd (fn [& args]
                              (rf/dispatch [:get-status])))
      (logs/setup)
      {:opts opts
       :router/view :status
       :router/view-state {}
       :router/nav-stack []
       :terminal/size terminal-size
       :repo {:path cwd}})))

(rf/reg-event-db
  :merge
  (fn [db [_ data]]
    (merge db data)))

(rf/reg-event-db
  :assoc-in
  (fn [db [_ path val]]
    (assoc-in db path val)))

(rf/reg-event-db
  :update-in
  (fn [db [_ path f & args]]
    (update-in db path #(apply f % args))))

(rf/reg-event-db
 :router/goto
 (fn [db [_ view & [state]]]
   (-> db
       (update-in [:router/nav-stack]
                  concat [{:view (:router/view db)
                           :state (:router/view-state db)}])
       (assoc-in [:router/view] view)
       (assoc-in [:router/view-state] state))))

(rf/reg-event-db
 :router/goto-and-forget
 (fn [db [_ view & [state]]]
   (-> db
       (assoc-in [:router/view] view)
       (assoc-in [:router/view-state] state))))

(rf/reg-event-db
 :router/go-back
 (fn [db _]
   (let [{:keys [view state]} (last (:router/nav-stack db))]
     (-> db
         (update-in [:router/nav-stack] butlast)
         (assoc-in [:router/view] view)
         (assoc-in [:router/view-state] state)))))

(rf/reg-event-db
 :get-status
 (fn [db _]
   (let [repo-path (get-in db [:repo :path])
         repo* (git/repo-promise repo-path)
         branch-name* (git/current-branch-name-promise repo*)
         file-statuses* (git/statuses-promise repo*)
         head-commit* (git/head-commit-promise repo*)
         commits* (git/commits-promise head-commit*)
         unstaged-hunks* (git/unstaged-hunks-promise repo*)
         staged-hunks* (git/staged-hunks-promise repo*)
         local-branches* (git/local-branches-promise)]
     (.then branch-name*
            (fn [branch-name]
              (rf/dispatch
               [:assoc-in [:repo :branch-name] branch-name])))
     (.then head-commit*
            (fn [head-commit]
              (rf/dispatch [:assoc-in [:repo :head-commit-summary]
                            (.summary head-commit)])))
     (.then file-statuses*
            (fn [statuses]
              (rf/dispatch-sync [:assoc-in [:repo :untracked] []])
              (rf/dispatch-sync [:assoc-in [:repo :unstaged] []])
              (rf/dispatch-sync [:assoc-in [:repo :staged] []])
              (rf/dispatch-sync [:assoc-in [:repo :untracked-content] {}])
              (.forEach statuses
                        (fn [file]
                          (let [status (-> file .status js->clj set)
                                path (.path file)]
                            (when (contains? status "WT_NEW")
                              (rf/dispatch
                               [:update-in [:repo :untracked] conj
                                path])
                              (.readFile
                               fs path
                               (fn [_ text]
                                 (rf/dispatch
                                  [:assoc-in [:repo :untracked-content path]
                                   (str text)]))))
                            (when (contains? status "WT_MODIFIED")
                              (rf/dispatch
                               [:update-in [:repo :unstaged] conj
                                path]))
                            (when (contains? status "INDEX_NEW")
                              (rf/dispatch
                               [:update-in [:repo :staged] conj
                                path]))
                            (when (contains? status "INDEX_MODIFIED")
                              (rf/dispatch
                               [:update-in [:repo :staged] conj
                                path])))))))
     (.then unstaged-hunks*
            (fn [unstaged-hunks]
              (rf/dispatch-sync [:assoc-in [:repo :unstaged-hunks] {}])
              (doseq [{:keys [path] :as hunk} unstaged-hunks]
                (rf/dispatch-sync
                 [:update-in [:repo :unstaged-hunks path]
                  concat [hunk]]))))
     (.then staged-hunks*
            (fn [staged-hunks]
              (rf/dispatch-sync [:assoc-in [:repo :staged-hunks] {}])
              (doseq [{:keys [path] :as hunk} staged-hunks]
                (rf/dispatch-sync
                 [:update-in [:repo :staged-hunks path]
                  concat [hunk]]))))
     (.then commits*
            (fn [commits]
              (rf/dispatch [:assoc-in [:repo :commits] commits])))
     (.then local-branches*
            (fn [branches]
              (rf/dispatch [:assoc-in [:repo :branches :local] branches]))))
     db))

(rf/reg-event-db
 :stage-file
 (fn [db [_ file]]
   (git/stage-file file)
   db))

(rf/reg-event-db
 :untrack-file
 (fn [db [_ file]]
   (git/untrack-file file)
   db))

(rf/reg-event-db
 :unstage-file
 (fn [db [_ file]]
   (git/unstage-file file)
   db))

(rf/reg-event-db
 :checkout-file
 (fn [db [_ file]]
   (git/checkout-file file)
   db))

(rf/reg-event-db
 :checkout-branch
 (fn [db [_ branch]]
   (let [{:keys [command stdout stderr]}
         (git/checkout-branch branch)]
     (println "\n$" command)
     (println stdout)
     (println stderr)
     (println))
   db))

(rf/reg-event-db
 :commit
 (fn [db [_ msg]]
   (git/commit msg)
   db))

(rf/reg-event-db
 :push
 (fn [db _]
   (.then (git/push-promise)
          (fn [{:keys [command stdout stderr]}]
            (println "\n$" command)
            (println stdout)
            (println stderr)
            (println)))
   db))

(rf/reg-event-db
 :toast
 (fn [db [_ & strings]]
   (js/setTimeout
    #(rf/dispatch [:assoc-in [:toast/view-state :text] ""])
    3000)
   (assoc-in db [:toast/view-state :text] (str/join strings))))

(rf/reg-event-db
 :get-commit-hunks
 (fn [db [_ commit]]
   (let [repo-path (get-in db [:repo :path])
         repo* (git/repo-promise repo-path)]
     (.then (git/commit-hunks-promise repo* (:sha commit))
            #(rf/dispatch [:assoc-in [:repo :commit-hunks] %])))
   db))

(rf/reg-event-db
 :stage-hunk
 (fn [db [_ hunk]]
   (let [repo-path (get-in db [:repo :path])
         repo* (git/repo-promise repo-path)]
     (git/stage-hunk-promise repo* hunk))
   db))

(rf/reg-event-db
 :unstage-hunk
 (fn [db [_ hunk]]
   (let [repo-path (get-in db [:repo :path])
         repo* (git/repo-promise repo-path)]
     (git/unstage-hunk-promise repo* hunk))
   db))

(rf/reg-event-db
 :discard-hunk
 (fn [db [_ hunk]]
   (let [repo-path (get-in db [:repo :path])
         repo* (git/repo-promise repo-path)]
     ;; TODO: implement
     )
   db))

(rf/reg-event-db
 :create-branch
 (fn [db [_ branch-name]]
   (let [{:keys [command stdout stderr]}
          (git/create-branch branch-name)]
      (do
        (println "\n$" command)
        (println stdout)
        (print stderr)))
   db))
