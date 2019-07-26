(ns maggit.views
  (:require [reagent.core :as r]
            [maggit.core :refer [screen]]
            [maggit.keys :refer [with-keys]]))

(letfn [(next-item [curr items]
          (if (== (count items) (inc curr))
            0
            (inc curr)))
        (prev-item [curr items]
          (if (== -1 (dec curr))
            (dec (count items))
            (dec curr)))]
  (defn navigable-list
    "Returns  a vertical list of items that can be navigated and selected from
   - items: list of option strings, can be navigated using <up>/<down>
   - item-props: properties that will be applied to each item
   - selected: index of the currently selected item
   - on-select: function that will be called with the selected index when <right> is pressed
   - on-back: function that will be called when <left> is pressed"
    [{:keys [items item-props selected
             on-select on-back]
      :or {on-select (fn [_])
           on-back (fn [])}
      :as props}]
    (r/with-let [selected (r/atom (or selected 0))]
      (with-keys @screen
        {["down"]  #(swap! selected next-item items)
         ["up"]    #(swap! selected prev-item items)
         ["right"] #(on-select @selected)
         ["left"]  on-back}
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
