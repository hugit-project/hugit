(ns maggit.subs
  "Re-frame app db subscriptions. Essentially maps a keyword describing a
  result to a function that retrieves the current value from the app db."
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  :db
  (fn [db _]
    db))

(rf/reg-sub
  :get-in
  (fn [db [_ path]]
    (get-in db path)))

(rf/reg-sub
  :view
  (fn [db _]
    (:router/view db)))

(rf/reg-sub
  :size
  (fn [db _]
    (:terminal/size db)))

(rf/reg-sub
  :repo
  (fn [db _]
    (:repo db)))

(rf/reg-sub
  :status-view
  (fn [db _]
    (:status-view db)))

(rf/reg-sub
  :files-view
  (fn [db _]
    (:files-view db)))

(rf/reg-sub
  :input-view
  (fn [db _]
    (:input-view db)))

(rf/reg-sub
  :diffs-view
  (fn [db _]
    (:diffs-view db)))
