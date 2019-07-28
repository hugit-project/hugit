(ns maggit.app
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [maggit.core :refer [render screen]]
   [maggit.debug.views :as debug]
   [maggit.demo.views :refer [home]]
   [maggit.main :as main]))

(defn ui []
  (let [{:keys [rows]} @(rf/subscribe [:size])]
    [:box
     {:top 0
      :left 0
      :height "100%"
      :width "100%"}
     [:box
      {:top 0
       :left 0
       :height "70%"}
      [main/ui]]
     [:box
      {:top "70%"
       :left 0
       :height "30%"}
      [debug/log-box rows]]]))

(defn main!
  "Main development entrypoint.
  Takes list of cli args, parses into opts map, inserts opts into re-frame db
  then initializes the ui.

  Returns nil but mostly invokes side-effects."
  [& args]
  (main/init! ui :opts (main/args->opts args)))

(defn reload!
  "Called when dev env is reloaded. Re-renders the ui and logs a message."
  [_]
  (-> (r/reactify-component ui)
      (r/create-element #js {})
      (render @screen))
  (println "Reloading app"))



(defn log-fn
  "A log handler function to append messages to our debug/logger atom.
  Takes variadic arguments, typically strings or stringifiable args.
  Appends string of all args to log.

  Source for this came from:
  https://gist.github.com/polymeris/5e117676b79a505fe777df17f181ca2e#file-core-cljs-L105"
  [& args]
  (swap! debug/logger conj (clojure.string/join " " args)))

;; Override the console.* messages to use our log-fn. Later if we call
;; console.log or println the message shows up in our debug box.

(set! (.-log js/console) log-fn)
(set! (.-error js/console) log-fn)
(set! (.-info js/console) log-fn)
(set! (.-debug js/console) log-fn)
(re-frame.loggers/set-loggers! {:log log-fn
                                :error log-fn
                                :warn log-fn})

(set! *main-cli-fn* main!)
