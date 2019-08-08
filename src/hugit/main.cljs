(ns hugit.main
  "Main application entrypoint. Defines root UI view, cli-options,
  arg parsing logic, and initialization routine"
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [mount.core :refer [defstate] :as mount]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [hugit.core :refer [render screen]]
   [hugit.app.views :refer [home]]
   [hugit.events]
   [hugit.keys :refer [keymap-component]]
   [hugit.logs :refer [log-box]]
   [hugit.resize :refer [size]]
   [hugit.subs]
   [hugit.views :as views]))

(def cli-options
  [["-p" "--port PORT" "port number"
    :default 80
    :parse-fn #(js/Number %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :update-fn inc]
   ["-h" "--help"]])

(defn args->opts
  "Takes a list of arguments.
  Returns a map of parsed CLI args."
  [args]
  (parse-opts args cli-options))

(defn init!
  "Initialize the application.
  Takes a root UI view function that returns a hiccup element and optionally
  a map of parsed CLI args.
  Returns rendered reagent view."
  [view & {:keys [opts]}]
  (mount/start)
  (rf/dispatch-sync [:init (:options opts) (size @screen)])
  (-> (r/reactify-component view)
      (r/create-element #js {})
      (render @screen)))

(defn ui []
  (let [show-logs? (rf/subscribe [:get-in [:logs/show-logs?]])
        size (rf/subscribe [:get-in [:terminal/size]])
        rows (:rows @size)]
    [:box
     {:top 0
      :left 0
      :width "100%"
      :height "100%"}
     [:box
      {:top 0
       :left "20%"
       :width "80%"}
      (if @show-logs?
        [log-box screen]
        [home])]
     [keymap-component]]))

(defn main!
  "Main application entrypoint function. Initializes app, renders root UI view
  and initializes the re-frame app db.
  Takes list of CLI args.
  Returns rendered reagent view."
  [& args]
  (init! ui :opts (args->opts args)))

(set! *main-cli-fn* main!)
