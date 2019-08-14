(ns hugit.util
  (:require [re-frame.core :as rf]))

(defn <sub [query]
  (rf/subscribe [:get-in query]))

(defn evt> [event]
  (rf/dispatch event))

(defn toast> [& msgs]
  (rf/dispatch-sync (vec (cons :toast msgs))))

(defn timeout [ms]
  (js/Promise.
   (fn [resolve]
     (js/setTimeout #(resolve ms)
                    ms))))

(defn nth-weighted-item
  [weighted-items weight-fn n]
  (let [repeated-weighted-items (map #(repeat (weight-fn %) %)
                                     weighted-items)
        items (apply concat repeated-weighted-items)]
    (nth items n)))
