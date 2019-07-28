(ns maggit.keys
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defonce global-bindings
  {["q"] {:f #(.exit js/process 0)
          :label "Quit"}})

(defonce current-bindings
  (r/atom {}))

(defn register-bindings
  [key-bindings]
  (doseq [[hotkeys {:keys [label]}] key-bindings]
    (swap! current-bindings
           assoc hotkeys label))
  @current-bindings)

(defn unregister-bindings
  [key-bindings]
  (doseq [[hotkeys] key-bindings]
    (swap! current-bindings
           dissoc hotkeys)))

(defn bind-keys
  "Set key bindings mapping keys to functions.
  Takes a blessed screen and a map of keybindings.
  Returns nil.
  See global-bindings for example input."
  [screen key-bindings]
  (doseq [[hotkeys {:keys [f]}] key-bindings]
    (.key screen (clj->js hotkeys) f)))

(defn unbind-keys
  "Remove key bindings from blessed screen instance.
  Takes a blessed screen instance and a map of keybindings.
  Returns nil."
  [screen key-bindings]
  (doseq [[hotkeys {:keys [f]}] key-bindings]
    (.unkey screen (clj->js hotkeys) f)))

(defn setup
  "Bind global-bindings to blssed screen instance.
  Takes blessed screen instance.
  Returns nil."
  [screen]
  (bind-keys screen global-bindings)
  (register-bindings global-bindings))

(defn with-keys
  "Wrap a hiccup element with key-bindings. The bindings are created when
  the component is mounted and removed when the component is removed.
  Takes a blessed screen instance, map of key bindings, and a hiccup element:

  screen       blessed/screen - A blessed screen instance
  key-bindings hash-map       - Map of keybindings to handler functions
  content      vector         - A hiccup element vector to wrap

  Returns a wrapped hiccup reagent element.

  Example:
  (with-keys screen {[\"q\" \"esc\"] #(rf/dispatch [:app/quit])}
    [:box \"Quit me.\"])"
  [screen key-bindings content]
  (r/with-let [_ (bind-keys screen key-bindings)]
    (register-bindings key-bindings)
    content
    (finally
      (unbind-keys screen key-bindings)
      (unregister-bindings key-bindings))))

(defn keymap-component
  []
  [:box#keys
   {:top 0
    :left 0
    :width "20%"
    :label "Keys"
    :style {:border {:fg :magenta}}
    :border {:type :line}}
   (for [[idx [hotkeys label]]
         (map-indexed vector @current-bindings)]
     ^{:key idx}
     [:box
      {:top (inc idx)}
      [:text
       {:left 1
        :content (clojure.string/join ", " hotkeys)}]
      [:text
       {:left "50%"
        :content label}]])])
