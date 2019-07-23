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
     :repo {:path "/Users/divyansh/repos/maggit"}}))

(rf/reg-event-db
  :update
  (fn [db [_ data]]
    (merge db data)))

(rf/reg-event-db
  :assoc-in
  (fn [db [_ path val]]
    (assoc-in db path val)))

(rf/reg-event-db
  :set
  (fn [db [_ data]]
    data))

(rf/reg-event-db
 :get-status
 (fn [db _]
   (let [repo-path (get-in db [:repo :path])
         repo* (git/repo-promise repo-path)
         current-branch-name* (git/current-branch-name-promise repo*) 
         current-head-commit-message* (git/current-head-commit-message-promise repo*)]
     (.then current-branch-name*
            (fn [branch-name]
              (rf/dispatch
               [:assoc-in [:repo :branch-name] branch-name])))
     (.then current-head-commit-message*
            (fn [msg]
              (rf/dispatch
               [:assoc-in [:repo :head-commit-message] msg]))))
   db))
