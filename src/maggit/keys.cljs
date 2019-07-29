(ns maggit.keys
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defonce global-bindings
  {["q"] {:f #(.exit js/process 0)
          :label "Quit"
          :type "Global"}})

(defonce current-bindings
  (r/atom {}))

(defn register-bindings
  [key-bindings]
  (doseq [[hotkeys {:keys [type label]}] key-bindings
          :let [type (or type "Misc")]]
    (swap! current-bindings assoc-in
           [type hotkeys] label))
  @current-bindings)

(defn unregister-bindings
  [key-bindings]
  (doseq [[hotkeys {:keys [type]}] key-bindings
          :let [type (or type "Misc")]]
    (swap! current-bindings update
           type dissoc hotkeys)))

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

(defn binding-component
  [top hotkeys label]
  [:box
   {:top top}
   [:box
    {:width "45%"}
    [:text
     {:right 0
      :content label}]]
   [:text
    {:left "50%"
     :width "45%"
     :content (clojure.string/join ", " hotkeys)}]])

(defn binding-group-component
  [top items label]
  (when (seq items)
    [:box
     {:top top
      :label label
      :height (+ 2 (count items))
      :style {:border {:fg :magenta}}
      :border {:type :line}}
     (for [[idx [hotkeys item-label]] (map-indexed vector items)
           :let [item-top idx]]
       ^{:key idx}
       [binding-component item-top hotkeys item-label])]))

(defn keymap-component
  []
  (let [top (atom 0)]
    [:box#keys
     {:top 0
      :left 0
      :width "20%"
      :label "Keys"
      :style {:border {:fg :magenta}}
      :border {:type :line}}
     (doall
      (for [[idx [label items]] (map-indexed vector @current-bindings)
            :let [group-top @top
                  _ (swap! top + (count items) 2)]]
        ^{:key idx}
        [binding-group-component group-top items label]))]))
