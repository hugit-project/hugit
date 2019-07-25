(ns maggit.events
  "Event handlers for re-frame dispatch events.
  Used to manage app db updates and side-effects.
  https://github.com/Day8/re-frame/blob/master/docs/EffectfulHandlers.md"
  (:require [re-frame.core :as rf]
            [maggit.git :as git]))

; Below are very general effect handlers. While you can use these to get
; going quickly you are likely better off creating custom handlers for actions
; specific to your application.

(rf/reg-event-db
  :init
  (fn [db [_ opts terminal-size]]
    {:opts opts
     :router/view :home
     :terminal/size terminal-size
     :repo {:path (js/process.cwd)}}))

(rf/reg-event-db
  :merge
  (fn [db [_ data]]
    (merge db data)))

(rf/reg-event-db
  :assoc-in
  (fn [db [_ path val]]
    (assoc-in db path val)))

(rf/reg-event-db
  :append-to
  (fn [db [_ path val]]
    (update-in db path concat [val])))

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
              (rf/dispatch [:assoc-in [:repo :unstaged] []])
              (rf/dispatch [:assoc-in [:repo :staged] []])
              (.forEach statuses
                        (fn [file]
                          (let [status (-> file .status js->clj set)]
                            (println status)
                            (when (contains? status "WT_MODIFIED")
                              (rf/dispatch [:append-to [:repo :unstaged] (.path file)]))
                            (when (contains? status "INDEX_MODIFIED")
                              (rf/dispatch [:append-to [:repo :staged] (.path file)]))))))))
   db))
