(ns maggit.debug.views
  (:require [clojure.string :refer [join]]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(defonce logger
  (r/atom []))

(defn log-height
  "Calculates half the height of the screen minus 3 for padding and margin.
  Takes the number of rows that make up the terminal's character height.
  Returns an integer."
  [rows]
  (- (/ rows 2)
     3))

(defn log-box
  "Display a box that shows the last several lines of logged output based on
  screen height.
  Can be thrown off by multi-line lines of text.
  Returns hiccup vector.

  Source inspired by:
  https://gist.github.com/polymeris/5e117676b79a505fe777df17f181ca2e"
  [rows]
  [:box#log
   {:top          0
    :bottom       0
    :right        0
    :style        {:fg :yellow
                   :bg :grey}
    :scrollable   true
    :scrollbar    true
    :alwaysScroll true}
   [:text {:left    1
           :top     0
           :bottom  0
           :right   1
           :style   {:fg :yellow
                     :bg :grey}
           :content (->> (take-last (log-height rows) @logger)
                         (join "\n"))}]])

(defn clear-log!
  "Util function to clear the log if needed. This should likely be called
  from the REPL during development."
  []
  (reset! logger {}))
