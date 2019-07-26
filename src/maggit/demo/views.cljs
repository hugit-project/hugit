(ns maggit.demo.views
  (:require [re-frame.core :as rf]
            [maggit.views :refer [navigable-list]]))

(defn status []
  (let [{:keys [branch-name
                head-commit-message
                unstaged
                staged]
         :as repo}
        @(rf/subscribe [:repo])]
    [:box#status
     {:top 0
      :right 0
      :width "100%"
      :height "50%"
      :style {:border {:fg :magenta}}
      :border {:type :line}
      :label " Status "}
     [:box#head
      {:top 1
       :left 1
       :right 2
       :align :left}
      [:text (str "Head: [" branch-name "] " head-commit-message)]]
     (when (seq unstaged)
       [navigable-list
        {:top 4
         :left 1
         :right 2
         :align :left
         :label "Unstaged"
         :items unstaged}])
     (when (seq staged)
       [navigable-list
        {:top (+ 4 (if (seq unstaged) 2 0) (count unstaged))
         :left 1
         :right 2
         :align :left
         :label "Staged"
         :items staged}])]))
