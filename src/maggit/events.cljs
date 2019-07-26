(ns maggit.events
  "Event handlers for re-frame dispatch events.
  Used to manage app db updates and side-effects.
  https://github.com/Day8/re-frame/blob/master/docs/EffectfulHandlers.md"
  (:require [re-frame.core :as rf]
            [maggit.git :as git]))

(defonce watch
  (js/require "watch"))

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
 :get-status
 (fn [db _]
   (let [repo-path (get-in db [:repo :path])
         repo* (git/repo-promise repo-path)
         branch-name* (git/current-branch-name-promise repo*)
         commit-message* (git/current-head-commit-message-promise repo*)
         file-statuses* (git/statuses-promise repo*)]
     (.then branch-name*
            (fn [branch-name]
              (rf/dispatch
               [:assoc-in [:repo :branch-name] branch-name])))
     (.then commit-message*
            (fn [msg]
              (rf/dispatch
               [:assoc-in [:repo :head-commit-message] msg])))
     (.then file-statuses*
            (fn [statuses]
              (rf/dispatch [:assoc-in [:repo :untracked] []])
              (rf/dispatch [:assoc-in [:repo :unstaged] []])
              (rf/dispatch [:assoc-in [:repo :staged] []])
              (.forEach statuses
                        (fn [file]
                          (let [status (-> file .status js->clj set)]
                            (when (contains? status "WT_NEW")
                              (rf/dispatch
                               [:update-in [:repo :untracked] conj (.path file)]))
                            (when (contains? status "WT_MODIFIED")
                              (rf/dispatch
                               [:update-in [:repo :unstaged] conj (.path file)]))
                            (when (contains? status "INDEX_MODIFIED")
                              (rf/dispatch
                               [:update-in [:repo :staged] conj (.path file)]))))))))
   db))
