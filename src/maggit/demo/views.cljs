(ns maggit.demo.views
  (:require [re-frame.core :as rf]))

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
       [:box#unstaged
        {:top 4
         :left 1
         :right 2
         :align :left
         :label "Unstaged"}
        (for [[idx file] (map-indexed vector unstaged)]
          ^{:key idx}
          [:text
           {:top (inc idx)}
           file])])
     (when (seq staged)
       [:box#staged
        {:top (+ 4 (if (seq unstaged) 2 0) (count unstaged))
         :left 1
         :right 2
         :align :left
         :label "Staged"}
        (for [[idx file] (map-indexed vector staged)]
          ^{:key idx}
          [:text
           {:top (inc idx)}
           file])])]))
