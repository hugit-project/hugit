(ns maggit.views
  (:require [reagent.core :as r]
            [maggit.core :refer [screen]]
            [maggit.keys :refer [with-keys]]))

(defn- enhance-handler-map
  [handlers arg-atom]
  (into {} (for [[keys {:keys [f label]}] handlers]
             [keys {:f #(f @arg-atom)
                    :label label}])))


(letfn [(cycle-next-item [curr items]
          (if (== (dec (count items)) curr)
            0
            (inc curr)))
        (cycle-prev-item [curr items]
          (if (== -1 (dec curr))
            (dec (count items))
            (dec curr)))]
  (defn navigable-list
    "Returns  a vertical list of items that can be navigated and selected from
   - items: list of option strings, can be navigated using <up>/<down>
   - item-props: properties that will be applied to each item
   - selected: index of the currently selected item
   - on-select: function that will be called with the selected index when <right> is pressed
   - on-back: function that will be called when <left> is pressed
   - custom-key-handlers: {[\"left\" \"right\"] {:f (fn [idx] (println idx)) :label \"Print\"}}"
    [{:keys [items item-props selected
             on-select on-back custom-key-handlers]
      :or {on-select (fn [_])
           on-back (fn [])}
      :as props}]
    (r/with-let [selected (r/atom (or selected 0))]
      (with-keys @screen
        (merge {["down"]  {:f #(swap! selected cycle-next-item items)
                           :label "Next Item"}
                ["up"]    {:f #(swap! selected cycle-prev-item items)
                           :label "Prev Item"}
                ["right"] {:f #(on-select @selected)
                           :label "Select"}
                ["left"]  {:f on-back
                           :label "Back"}}
               (enhance-handler-map custom-key-handlers selected))
        [:box (dissoc props
                      :items :item-props :selected
                      :on-select :on-back)
         (doall
          (for [[idx item] (map-indexed vector items)]
            ^{:key idx}
            [:text (merge {:top (inc idx)}
                          item-props)
             (str (if (== @selected idx) "> " "  ")
                  item)]))]))))

(letfn [(next-item [curr items]
          (if (== (dec (count items)) curr)
            curr
            (inc curr)))
        (prev-item [curr items]
          (if (zero? curr)
            curr
            (dec curr)))
        (get-window [items curr window-size]
          (->> items
               (drop curr)
               (take window-size)))]
  (defn scrollable-list
    "Returns  a vertical list of items that can be scrolled and selected from
   - items: list of option strings, can be navigated using <up>/<down>
   - item-props: properties that will be applied to each item
   - selected: index of the currently selected item
   - on-select: function that will be called with the selected index when <right> is pressed
   - on-back: function that will be called when <left> is pressed
   - custom-key-handlers: {[\"left\" \"right\"] {:f (fn [idx] (println idx)) :label \"Print\"}}"
    [{:keys [items item-props
             window-start window-size
             on-select on-back custom-key-handlers]
      :or {on-select (fn [_])
           on-back (fn [])}
      :as props}]
    (r/with-let [window-start (r/atom (or window-start 0))
                 window-size (or window-size 5)]
      (let [window (r/atom (get-window items
                                       @window-start
                                       window-size))]
        (with-keys @screen
          (merge
           {["down"]  {:f #(do (swap! window-start next-item items)
                               (reset! window (get-window items
                                                          @window-start
                                                          window-size)))
                       :label "Next Item"}
            ["up"]    {:f #(do (swap! window-start prev-item items)
                               (reset! window (get-window items
                                                          @window-start
                                                          window-size)))
                       :label "Prev Item"}
            ["right"] {:f #(on-select @window-start)
                       :label "Select"}
            ["left"]  {:f on-back
                       :label "Back"}}
           (enhance-handler-map custom-key-handlers window-start))
          [:box (dissoc props
                        :items :item-props :selected
                        :on-select :on-back)
           (doall
            (for [[idx item] (map-indexed vector @window)]
              ^{:key idx}
              [:text (merge {:top (inc idx)}
                            item-props)
               (str (if (zero? idx) "> " "  ")
                    item)]))])))))

(defn text-input
  "Text input from user
   - on-submit: function that will be called with the current text on <enter>
   - on-cancel: function that will be called on <escape>"
  [{:keys [on-submit on-cancel]
    :or {on-submit (fn [_])
         on-cancel (fn [])}
    :as props}]
  [:textbox
   (merge {:mouse true
           :keys true
           :vi true
           :inputOnFocus true
           :onSubmit on-submit
           :on-cancel on-cancel}
          (dissoc props :on-submit :on-cancel))])
