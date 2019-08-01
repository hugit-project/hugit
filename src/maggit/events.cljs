(ns maggit.events
  "Event handlers for re-frame dispatch events.
  Used to manage app db updates and side-effects.
  https://github.com/Day8/re-frame/blob/master/docs/EffectfulHandlers.md"
  (:require [re-frame.core :as rf]
            [maggit.git :as git]
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
         unstaged-hunks* (git/unstaged-hunks-promise repo*)]
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
              (rf/dispatch [:assoc-in [:repo :untracked] []])
              (rf/dispatch [:assoc-in [:repo :unstaged] []])
              (rf/dispatch [:assoc-in [:repo :staged] []])
              (.forEach statuses
                        (fn [file]
                          (let [status (-> file .status js->clj set)
                                path (.path file)]
                            (when (contains? status "WT_NEW")
                              (rf/dispatch
                               [:update-in [:repo :untracked] conj
                                path]))
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
              (doseq [{:keys [path text]} unstaged-hunks]
                (rf/dispatch
                 [:update-in [:repo :unstaged-hunks path]
                  concat
                  [{:text text
                    :line-start 0
                    :line-end 0}]]))))
     (.then commits*
            (fn [commits]
              (rf/dispatch [:assoc-in [:repo :commits] commits]))))
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
 :commit
 (fn [db [_ msg]]
   (git/commit msg)
   db))

(rf/reg-event-db
 :push
 (fn [db _]
   (git/push)
   db))

(rf/reg-event-db
 :toast
 (fn [db [_ & strings]]
   (js/setTimeout
    #(rf/dispatch [:assoc-in [:toast/view-state :text] ""])
    3000)
   (assoc-in db [:toast/view-state :text] (str/join strings))))

(rf/reg-event-db
 :show-commit
 (fn [db [_ commit]]
   (let [repo-path (get-in db [:repo :path])
         repo* (git/repo-promise repo-path)]
     (.then (git/commit-diff-promise repo* (:sha commit))
            #(rf/dispatch [:router/goto :diffs
                           {:label (:sha commit)
                            :text %}])))
   db))

(rf/reg-event-db
 :show-file
 (fn [db [_ path]]
   (let [contents (.readFileSync fs path)]
     (rf/dispatch [:router/goto :diffs
                   {:label path
                    :text contents}]))
   db))

(rf/reg-event-db
 :show-staged-file-diffs
 (fn [db [_ path]]
   (let [repo-path (get-in db [:repo :path])
         repo* (git/repo-promise repo-path)
         text* (git/staged-file-diff-promise repo* path)]
     (.then text*
            #(rf/dispatch [:router/goto :diffs
                           {:label path
                            :text %}])))
   db))

(rf/reg-event-db
 :show-unstaged-file-diffs
 (fn [db [_ path]]
   (let [hunks (get-in db [:repo :unstaged-hunks path])
         text (str/join "\n=======\n\n" (map :text hunks))]
     (rf/dispatch [:router/goto :diffs
                   {:label path
                    :text text}]))
   db))
