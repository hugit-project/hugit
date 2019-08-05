(ns hugit.logs
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [hugit.keys :as keys]
            [hugit.views :refer [scrollable-list]]))

(defonce log-lines
  (r/atom []))

(defonce max-lines
  1000)

(defn log
  [& strings]
  (let [blob (str/join strings)
        lines (str/split blob #"\n")]
    (swap! log-lines concat lines)
    (swap! log-lines #(take-last max-lines %))))

(rf/reg-event-db
 :logs/log
 (fn [db [_ & strings]]
   (apply log strings)
   db))

(rf/reg-event-db
 :logs/show-logs
 (fn [db _]
   (assoc db :logs/show-logs? true)))

(rf/reg-event-db
 :logs/hide-logs
 (fn [db _]
   (assoc db :logs/show-logs? false)))

(set! (.-log js/console) log)
(set! (.-error js/console) log)
(set! (.-info js/console) log)
(set! (.-debug js/console) log)
(re-frame.loggers/set-loggers! {:log log
                                :error log
                                :warn log})

(defn log-box
  "Display a box that shows the last several lines of logged output based on
  screen height.
  Can be thrown off by multi-line lines of text.
  Returns hiccup vector.

  Source inspired by:
  https://gist.github.com/polymeris/5e117676b79a505fe777df17f181ca2e"
  [screen]
  (let [size (rf/subscribe [:get-in [:terminal/size]])
        rows (:rows @size)]
    (keys/with-keys @screen
      {["left"] {:f #(rf/dispatch [:logs/hide-logs])
                 :label "Hide Logs"
                 :type "Navigation"}}
      [:box#log
       {:top          0
        :bottom       0
        :right        0
        :width        "100%"
        :height       "100%"
        :style {:border {:fg :magenta}}
        :border {:type :line}}
       [scrollable-list
        {:top 0
         :left 1
         :right 2
         :align :left
         :items @log-lines
         :window-size (- rows 6)
         :selected (- (count @log-lines)
                      (- rows 6))}]])))
