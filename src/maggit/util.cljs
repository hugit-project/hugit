(ns maggit.util)

(defn nth-weighted-item
  [weighted-items weight-fn n]
  (let [repeated-weighted-items (map #(repeat (weight-fn %) %)
                                     weighted-items)
        items (apply concat repeated-weighted-items)]
    (nth items n)))
