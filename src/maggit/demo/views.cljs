(ns maggit.demo.views
  (:require [re-frame.core :as rf]
            [maggit.views :refer [navigable-list scrollable-list]]))

(defn status []
  (let [{:keys [branch-name
                head-commit-message
                untracked
                unstaged
                staged]}
        @(rf/subscribe [:repo])

        {:keys [selected]}
        @(rf/subscribe [:status-view])]
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
     [navigable-list
      {:top 4
       :left 1
       :align :left
       :items [(str "Untracked (" (count untracked) ")")
               (str "Unstaged (" (count unstaged) ")")
               (str "Staged (" (count staged) ")")
               (str "Commit Log")]
       :selected selected
       :on-select
       (fn [x]
         (rf/dispatch [:assoc-in [:status-view :selected] x])
         (if (< x 3)
           (do
             (rf/dispatch [:assoc-in [:files-view]
                           (case x
                             0 {:label "Untracked"
                                :files-path [:repo :untracked]}
                             1 {:label "Unstaged"
                                :files-path [:repo :unstaged]}
                             2 {:label "Staged"
                                :files-path [:repo :staged]})])
             (rf/dispatch [:assoc-in [:router/view] :files]))
           (rf/dispatch [:assoc-in [:router/view] :commits])))}]]))

(defn files []
  (let [{:keys [files-path label]}
        @(rf/subscribe [:files-view])

        files @(rf/subscribe [:get-in files-path])]
    [:box#files
     {:top 0
      :right 0
      :width "100%"
      :height "50%"
      :style {:border {:fg :magenta}}
      :border {:type :line}
      :label (str " " label " ")}
     [navigable-list
      {:top 1
       :left 1
       :right 2
       :align :left
       :items files
       :custom-key-handlers
       {["s"] (fn [x]
                (rf/dispatch [:stage-file (nth files x)]))}
       :on-back
       #(do
          (rf/dispatch [:assoc-in [:files-view] {}])
          (rf/dispatch [:assoc-in [:router/view] :status]))}]]))

(defn commits []
  (let [commits @(rf/subscribe [:get-in [:repo :commits]])
        screen-size @(rf/subscribe [:size])]
    [:box#commits
     {:top 0
      :right 0
      :width "100%"
      :height "50%"
      :style {:border {:fg :magenta}}
      :border {:type :line}
      :label " Commit Log "}
     [scrollable-list
      {:top 1
       :left 1
       :right 2
       :align :left
       :window-size (-> screen-size :rows (* 0.5) (- 6))
       :items (for [{:keys [sha summary]} commits]
                (str (->> sha (take 7) clojure.string/join)
                     " "
                     summary))
       :on-back
       #(rf/dispatch [:assoc-in [:router/view] :status])}]]))

(defn home []
  (let [view @(rf/subscribe [:view])]
    [(case view
       :status status
       :files files
       :commits commits)]))
