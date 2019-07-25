(ns maggit.demo.views
  (:require
   [clojure.string :refer [join]]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [maggit.views :refer [router vertical-menu]]))

(defn navbar
  "Displays a blessed js box with a vertical-menu used for navigation.
  User can use up/down keys to navigate items and view a page.
  Dispatches re-frame :merge to set :router/view in app db.
  Returns a hiccup :box vector."
  [_]
  [:box#home
   {:top    0
    :left   0
    :width  "30%"
    :height "50%"
    :style  {:border {:fg :cyan}}
    :border {:type :line}
    :label  " Menu "}
   [vertical-menu {:options {:home "Status"
                             :about "About"
                             :resources "Resources"
                             :credits "Credits"}
                   :bg :magenta
                   :fg :black
                   :on-select
                   (fn [selected]
                     (rf/dispatch [:merge {:router/view selected}])

                     (case selected
                       :home (rf/dispatch [:get-status])

                       ;; default
                       nil))}]])

(defn home
  "Display welcome message and general usage info to user.
  Returns hiccup :box element."
  [_]
  (let [{:keys [branch-name
                head-commit-message
                unstaged
                staged]
         :as repo}
        @(rf/subscribe [:repo])]
    [:box#status
     {:top 0
      :right 0
      :width "70%"
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
           {:top (-> idx (* 2) inc)}
           file])])
     (when (seq staged)
       [:box#staged
        {:top (+ 4 (inc (* 2 (count unstaged))))
         :left 1
         :right 2
         :align :left
         :label "Staged"}
        (for [[idx file] (map-indexed vector staged)]
          ^{:key idx}
          [:text
           {:top (-> idx (* 2) inc)}
           file])])]))

(defn about
  "Display link to the template project and share features.
  Returns hiccup :box vector."
  [_]
  [:box#about
   {:top 0
    :right 0
    :width "70%"
    :height "50%"
    :style {:border {:fg :blue}}
    :border {:type :line}
    :label " About "}
   [:box#content
    {:top 1
     :left 1
     :right 1
     :bottom 1}
    [:text {:content "Demo ClojureScript Terminal-User-Interface (TUI) app generated from the leiningen cljs-tui template."}]
    [:box {:top 3
           :align :center
           :style {:fg :green}
           :content "https://github.com/eccentric-j/cljs-tui-template"}]
    [:text {:top 5
            :align :center
            :content  (join "\n  - "
                        ["Features:\n"
                         "Use ClojureScript and functional programming\n    to deliver rich CLIs quickly"
                         "Manage your state and side-effects with re-frame"
                         "Compose simple view functions into a rich UI\n    with Reagent React views"
                         "Use web technologies you are already familiar with"
                         "Faster start up time with node"
                         "Supports shadow, figwheel-main, or lein-figwheel"])}]]])

(defn resources
  "Share links to libraries this project is built with.
  Returns hiccup :box vector."
  [_]
  [:box#about
   {:top 0
    :right 0
    :width "70%"
    :height "50%"
    :style {:border {:fg :red}}
    :border {:type :line}
    :label " Resources "}
   [:box#content
    {:top 1
     :left 1
     :right 1
     :bottom 1}
    [:text (join "\n  - "
                 ["Learn more about the technology behind this powerful ClojureScript template:\n"
                  "https://clojurescript.org/"
                  "https://github.com/chjj/blessed"
                  "https://github.com/Yomguithereal/react-blessed"
                  "https://reagent-project.github.io/"
                  "https://shadow-cljs.org/"
                  "https://figwheel.org/"
                  "https://github.com/bhauman/lein-figwheel"])]]])

(defn credits
  "Give respect and credit to the Denis for inspiring for this project.
  Returns hiccup :box vector."
  [_]
  [:box#about
   {:top 0
    :right 0
    :width "70%"
    :height "50%"
    :style {:border {:fg :yellow}}
    :border {:type :line}
    :label " Credits "}
   [:box#content
    {:top 1
     :left 1
     :right 1
     :bottom 1}
    [:box
     {:top 0
      :align :center
      :content "https://github.com/denisidoro/floki"}]
    [:box
     {:top 2
      :content (join "\n  - "
                 ["This project was deeply inspired by Floki, a ClojureScript TUI created by Denis Isidoro."])}]
    [:box
     {:top 5
      :align :center
      :content "https://git.io/fhhOf"}]
    [:box
     {:top 7
      :content "Special thanks to Camilo Polymeris whose gist inspired Floki and this template."}]
    [:text
     {:top 10}
     (join "\n  - "
           ["Templated created by Eccentric J and is open sourced on github."])]
    [:box
     {:top 12
      :left 0
      :align :left
      :content "- https://github.com/eccentric-j/cljs-tui-template\n- https://eccentric-j.com/"}]]])

(defn demo
  "Main demo UI wrapper.

  Takes a hash-map and a hiccup child vector:

  hash-map:
  :view keyword - Current view keyword that maps to one of the views below.

  child:
  Typically something like a hiccup [:box ...] vector

  Returns hiccup :box vector."
  [{:keys [view]} child]
  [:box#base {:left   0
              :right  0
              :width  "100%"
              :height "100%"}
   [navbar]
   [router {:views {:home home
                    :about about
                    :resources resources
                    :credits credits}
            :view view}]
   child])
