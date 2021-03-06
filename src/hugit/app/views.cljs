(ns hugit.app.views
  (:require [hugit.util :as u :refer [<sub evt> toast>]]
            [hugit.views :refer [navigable-list scrollable-list text-input]]))

(defn status []
  (let [{:keys [branch-name
                branch-status
                head-commit-summary
                staged-deleted
                unstaged-deleted
                untracked
                unstaged
                branches
                staged]}
        @(<sub [:repo])

        local-branches (:local branches)

        selected (<sub [:router/view-state :selected])]
    [:box#status
     {:top 0
      :right 0
      :width "100%"
      :style {:border {:fg :magenta}}
      :border {:type :line}
      :label " Status "}
     [:box#head
      {:top 1
       :left 1
       :right 2
       :align :left}
      [:text (str "Head: [" branch-name "] " head-commit-summary " " branch-status)]]
     [navigable-list
      {:top 3
       :bottom 1
       :left 1
       :align :left
       :items [(str "Deleted (" (+ (count staged-deleted)
                                   (count unstaged-deleted)) ")")
               (str "Untracked (" (count untracked) ")")
               (str "Unstaged (" (count unstaged) ")")
               (str "Staged (" (count staged) ")")
               (str "Local Branches (" (count local-branches) ")")
               (str "Commit Log")]
       :selected @selected
       :custom-key-handlers
       {["c"] {:f (fn [_]
                    (evt> [:router/goto :input
                           {:label "Commit Message"
                            :on-submit (fn [msg]
                                         (toast> "Commiting")
                                         (evt> [:commit msg])
                                         (evt> [:router/goto-and-forget :commits]))
                            :on-cancel #(evt> [:router/go-back])}]))
               :label "Commit"
               :type "Action"}
        ["f"] {:f (fn [_]
                    (toast> "Fetching")
                    (evt> [:fetch-branch-status branch-name]))
               :label "Fetch"
               :type "Action"}
        ["p"] {:f (fn [_]
                    (toast> "Pushing")
                    (evt> [:push]))
               :label "Push"
               :type "Action"}}
       :on-select
       (fn [x]
         (evt> [:assoc-in [:router/view-state :selected] x])
         (cond
           (zero? x)
           (evt> [:router/goto :deleted-files
                  {:label "Deleted"
                   :unstaged-files-path [:repo :unstaged-deleted]
                   :staged-files-path [:repo :staged-deleted]}])

           (< 0 x 4)
           (letfn [(get-file [type idx]
                     (nth @(<sub [:repo type])
                          idx))]
             (evt> [:router/goto :files
                    (case x
                      1 {:label "Untracked"
                         :files-path [:repo :untracked]
                         :on-select
                         (fn [file-idx]
                           (evt> [:assoc-in [:router/view-state :selected] file-idx])
                           (let [file (get-file :untracked file-idx)]
                             (evt>
                              [:router/goto :file
                               {:label file
                                :content-path [:repo :untracked-content file]}])))
                         :custom-key-handlers
                         {["s"] {:f #(let [file (get-file :untracked %)]
                                       (toast> "Staging " file)
                                       (evt> [:stage-file file]))
                                 :label "Stage"
                                 :type "Action"}}}
                      2 {:label "Unstaged"
                         :files-path [:repo :unstaged]
                         :on-select
                         (fn [file-idx]
                           (evt> [:assoc-in [:router/view-state :selected] file-idx])
                           (let [file (get-file :unstaged file-idx)]
                             (evt>
                              [:router/goto :diffs
                               {:label file
                                :file file
                                :hunks-path [:repo :unstaged-hunks file]
                                :actions ["s" "k"]}])))
                         :custom-key-handlers
                         {["s"] {:f #(let [file (get-file :unstaged %)]
                                       (toast> "Staging " file)
                                       (evt> [:stage-file file]))
                                 :label "Stage"
                                 :type "Action"}
                          ["r"] {:f #(let [file (get-file :unstaged %)]
                                       (toast> "Untracking " file)
                                       (evt> [:untrack-file file]))
                                 :label "Untrack"
                                 :type "Action"}
                          ["k"] {:f #(let [file (get-file :unstaged %)]
                                       (toast> "Checking out " file)
                                       (evt> [:checkout-file file]))
                                 :label "Checkout"
                                 :type "Action"}}}
                      3 {:label "Staged"
                         :files-path [:repo :staged]
                         :on-select
                         (fn [file-idx]
                           (evt> [:assoc-in [:router/view-state :selected] file-idx])
                           (let [file (get-file :staged file-idx)]
                             (evt>
                              [:router/goto :diffs
                               {:label file
                                :file file
                                :hunks-path [:repo :staged-hunks file]
                                :actions ["u"]}])))
                         :custom-key-handlers
                         {["u"] {:f #(let [file (get-file :staged %)]
                                       (toast> "Unstaging " file)
                                       (evt> [:unstage-file file]))
                                 :label "Unstage"
                                 :type "Action"}}})]))

           (== x 4)
           (evt> [:router/goto :branches
                  {:label "Local Branches"
                   :branches-path [:repo :branches :local]}])

           (== x 5)
           (evt> [:router/goto :commits])))}]]))

(defn deleted-files []
  (let [{:keys [staged-files-path unstaged-files-path]}
        @(<sub [:router/view-state])

        staged-files (<sub staged-files-path)
        unstaged-files (<sub unstaged-files-path)]
    [:box#deleted-files
     {:top 0
      :right 0
      :width "100%"
      :style {:border {:fg :magenta}}
      :border {:type :line}
      :label "Deleted files"}
     [navigable-list
      {:top 1
       :left 1
       :right 2
       :align :left
       :items (concat (for [item @unstaged-files]
                        (str "[unstaged] " item))
                      (for [item @staged-files]
                        (str "[staged] " item)))
       :custom-key-handlers
       {["s"] {:f (fn [idx]
                    (if (< idx (count @unstaged-files))
                      (let [file (nth @unstaged-files idx)]
                        (toast> "Staging " file)
                        (evt> [:stage-file file]))))
               :label "Stage"
               :type "Action"}
        ["u"] {:f (fn [idx]
                    (let [index (- idx (count @unstaged-files))]
                      (if (>= idx (count @unstaged-files))
                        (let [file (nth @staged-files index)]
                            (toast> "Unstaging " file)
                            (evt> [:unstage-file file])))))
               :label "Unstage"
               :type "Action"}
        ["k"] {:f (fn [idx]
                    (if (< idx (count @unstaged-files))
                      (let [file (nth @unstaged-files idx)]
                        (toast> "Checking out " file)
                        (evt> [:checkout-file file]))))
               :label "Checkout"
               :type "Action"}}
       :on-back
       #(evt> [:router/go-back])}]]))

(defn files []
  (let [{:keys [files-path label selected
                on-select custom-key-handlers]}
        @(<sub [:router/view-state])

        files (<sub files-path)]
    [:box#files
     {:top 0
      :right 0
      :width "100%"
      :style {:border {:fg :magenta}}
      :border {:type :line}
      :label (str " " label " ")}
     [navigable-list
      {:top 1
       :left 1
       :right 2
       :align :left
       :items @files
       :selected selected
       :custom-key-handlers custom-key-handlers
       :on-select on-select
       :on-back #(evt> [:router/go-back])}]]))

(defn file []
  (let [label (<sub [:router/view-state :label])
        content-path (<sub [:router/view-state :content-path])
        text (<sub @content-path)
        size (<sub [:terminal/size])
        rows (:rows @size)]
    [:box#file
     {:top 0
      :right 0
      :width "100%"
      :style {:border {:fg :magenta}}
      :border {:type :line}
      :label (str " " @label " ")}
     [scrollable-list
      {:top 0
       :left 1
       :right 2
       :align :left
       :window-size (- rows 6)
       :items (clojure.string/split @text #"\n")
       :on-back
       #(evt> [:router/go-back])}]]))

(defn diffs []
  (let [label (<sub [:router/view-state :label])
        file  (<sub [:router/view-state :file])
        hunks-path (<sub [:router/view-state :hunks-path])
        actions (<sub [:router/view-state :actions])
        hunks (<sub @hunks-path)
        separated-hunks (fn [hunks]
                          (interleave hunks
                                      (repeat (count hunks)
                                              {:dummy? true
                                               :size 1
                                               :text "====="})))
        size (<sub [:terminal/size])
        rows (:rows @size)]
    [:box#diffs
     {:top 0
      :right 0
      :width "100%"
      :style {:border {:fg :magenta}}
      :border {:type :line}
      :label (str " "
                  (or @label
                      "Diff")
                  " ")}
     [scrollable-list
      {:top 0
       :left 1
       :right 2
       :align :left
       :window-size (- rows 6)
       :items (for [{:keys [text]} (separated-hunks @hunks)
                    :let [lines (clojure.string/split text #"\n")]
                    line lines]
                line)
       :item-props-f
       (fn [line]
         (case (first line)
           \- {:style {:fg :red}}
           \+ {:style {:fg :green}}
           {:style {:fg :white}}))
       :custom-key-handlers
       (select-keys
        {["s"] {:f (fn [idx]
                     (let [hunk (u/nth-weighted-item
                                 (separated-hunks @hunks)
                                 :size
                                 idx)]
                       (when-not (:dummy? hunk)
                         (toast> "Staging hunk")
                         (evt> [:stage-hunk hunk]))))
                :label "Stage Hunk"
                :type "Action"}
         ["u"] {:f (fn [idx]
                     (let [hunk (u/nth-weighted-item
                                 (separated-hunks @hunks)
                                 :size
                                 idx)]
                       (when-not (:dummy? hunk)
                         (toast> "Unstaging hunk")
                         (evt> [:unstage-hunk hunk]))))
                :label "Unstage Hunk"
                :type "Action"}
         ["k"] {:f (fn [idx]
                     (let [hunk (u/nth-weighted-item
                                 (separated-hunks @hunks)
                                 :size
                                 idx)]
                       (when-not (:dummy? hunk)
                         (toast> "NOT IMPLEMENTED: Discarding hunk")
                         (evt> [:discard-hunk hunk]))))
                :label "Discard Hunk"
                :type "Action"}}
        (map vector @actions))
       :on-back
       #(evt> [:router/go-back])}]]))

(defn commits []
  (let [commits (<sub [:repo :commits])
        selected (<sub [:router/view-state :selected])
        size (<sub [:terminal/size])
        rows (:rows @size)
        commit-str (fn [{:keys [sha summary]}]
                     (str (->> sha (take 7) clojure.string/join)
                          " "
                          summary))]
    [:box#commits
     {:top 0
      :right 0
      :width "100%"
      :style {:border {:fg :magenta}}
      :border {:type :line}
      :label " Commit Log "}
     [scrollable-list
      {:top 0
       :left 1
       :right 2
       :align :left
       :window-size (- rows 6)
       :items (map commit-str @commits)
       :selected @selected
       :on-select
       (fn [idx]
         (let [commit (nth @commits idx)]
           (evt> [:assoc-in [:router/view-state :selected] idx])
           (evt> [:get-commit-hunks commit])
           (evt> [:router/goto :diffs
                  {:label (commit-str commit)
                   :hunks-path [:repo :commit-hunks]}])))
       :on-back
       #(evt> [:router/go-back])}]]))

(defn branches []
  (let [branches-path (<sub [:router/view-state :branches-path])
        branches (<sub @branches-path)
        current-branch (<sub [:repo :branch-name])
        label (<sub [:router/view-state :label])
        selected (<sub [:router/view-state :selected])
        size (<sub [:terminal/size])
        rows (:rows @size)]
    [:box#commits
     {:top 0
      :right 0
      :width "100%"
      :style {:border {:fg :magenta}}
      :border {:type :line}
      :label (str " " @label " ")}
     [scrollable-list
      {:top         0
       :left        1
       :right       2
       :align       :left
       :window-size (- rows 6)
       :items       @branches
       :item-props-f
       (fn [branch]
         (if (= @current-branch branch)
           {:style {:fg :green}}
           {:style {:fg :white}}))
       :selected    @selected
       :custom-key-handlers
       {["enter"] {:f (fn [idx]
                        (let [branch (nth @branches idx)]
                          (toast> "Checking out " branch)
                          (evt> [:checkout-branch branch])
                          (evt> [:router/goto :status])))
                   :label "Checkout"
                   :type  "Action"}
        ["n"] {:f (fn [_]
                    (evt> [:router/goto :input
                           {:label "New Branch"
                            :on-submit
                            (fn [branch-name]
                              (if (-> @(<sub [:repo :branches :local])
                                      set
                                      (contains? branch-name))
                                (do
                                  (toast> (str "A branch called " branch-name " already exists!"))
                                  (evt> [:router/goto :branches
                                         {:label "Local Branches"
                                          :branches-path [:repo :branches :local]}]))
                                (do
                                  (toast> "Creating new branch")
                                  (evt> [:create-branch branch-name])
                                  (evt> [:router/goto-and-forget :status]))))
                            :on-cancel #(evt> [:router/go-back])}]))
               :label "New Branch"
               :type  "Action"}
        ["d"] {:f (fn [idx]
                    (evt> [:assoc-in [:router/view-state :selected] idx])
                    (let [branch-name (nth @branches idx)]
                      (toast> "Enter 'yes' to delete " branch-name)
                      (evt> [:router/goto :input
                             {:label (str "Delete local branch " branch-name "?")
                              :on-submit
                              (fn [input]
                                (when (= "yes" input)
                                  (toast> "Deleting local branch " branch-name)
                                  (evt> [:delete-branch branch-name]))
                                (evt> [:router/go-back]))
                              :on-cancel #(evt> [:router/go-back])}])))
               :label "Delete Branch"
               :type  "Action"}}
       :on-back #(evt> [:router/go-back])}]]))

(defn input []
  (let [{:keys [label on-submit on-cancel]}
        @(<sub [:router/view-state])]
    [:box
     {:top 0
      :right 0
      :width "100%"
      :style {:border {:fg :magenta}}
      :border {:type :line}
      :label (str " " label " ")}
     [text-input
      {:top 1
       :left 1
       :height 10
       :on-submit on-submit
       :on-cancel on-cancel}]]))

(defn viewport [height]
  [:box#viewport
   {:height height}
   (let [view @(<sub [:router/view])]
     [(case view
        :status status
        :deleted-files deleted-files
        :files files
        :file file
        :branches branches
        :commits commits
        :diffs diffs
        :input input)])])

(defn toast []
  (let [text @(<sub [:toast/view-state :text])]
    [:box#toast
     {:bottom 0
      :height 3
      :style {:border {:fg :magenta}}
      :border {:type :line}}
     [:text
      {:left 1}
      text]]))

(defn home []
  (let [size (<sub [:terminal/size])
        rows (:rows @size)]
    [:box#home
     {:top 0
      :left 0
      :height "100%"
      :width "100%"}
     [viewport (- rows 3)]
     [toast]]))
