(ns maggit.views
  (:require [reagent.core :as r]
            [maggit.core :refer [screen]]
            [maggit.keys :refer [with-keys]]))

(defn- enhance-handler-map [handlers arg-atom]
  (into {} (for [[keys f] handlers]
             [keys #(f @arg-atom)])))


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
   - custom-key-handlers: {[\"left\" \"right\"] (fn [idx] (println idx))}"
    [{:keys [items item-props selected
             on-select on-back custom-key-handlers]
      :or {on-select (fn [_])
           on-back (fn [])}
      :as props}]
    (r/with-let [selected (r/atom (or selected 0))]
      (with-keys @screen
        (merge {["down"]  #(swap! selected cycle-next-item items)
                ["up"]    #(swap! selected cycle-prev-item items)
                ["right"] #(on-select @selected)
                ["left"]  on-back}
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
   - custom-key-handlers: {[\"left\" \"right\"] (fn [idx] (println idx))}"
    [{:keys [items item-props
             window-start window-size
             on-select on-back custom-key-handlers]
      :or {on-select (fn [_])
           on-back (fn [])}
      :as props}]
    (r/with-let [window-start (r/atom (or window-start 0))
                 window-size (or window-size 5)
                 window (r/atom (take window-size items))]
      (with-keys @screen
        (merge
         {["down"]  #(do (swap! window-start next-item items)
                         (reset! window (get-window items
                                                    @window-start
                                                    window-size)))
          ["up"]    #(do (swap! window-start prev-item items)
                         (reset! window (get-window items
                                                    @window-start
                                                    window-size)))
          ["right"] #(on-select @window-start)
          ["left"]  on-back}
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
                  item)]))]))))
