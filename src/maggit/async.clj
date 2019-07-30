(ns maggit.async)

(defmacro ->chan
  [promise]
  `(let [ch# (cljs.core.async/chan)]
     (.then ~promise
            #(cljs.core.async.macros/go (cljs.core.async/>! ch# %)))
     ch#))

(defmacro async
  [& body]
  `(~'js/Promise.
    (fn [resolve#]
      (cljs.core.async.macros/go
        (resolve# (let []
                    ~@body))))))

(defmacro await
  [promise]
  `(cljs.core.async/<! (->chan ~promise)))
