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


;; Utils
;; =====
(defmacro doseq*
  [[item items] & body]
  `(loop [remaining# ~items]
     (when-let [remaining# (seq remaining#)]
       (let [[~item & others#] remaining#]
         ~@body
         (recur others#)))))

(defmacro doseq
  [bindings & body]
  (let [[b & bs] (->> bindings
                      (partition-all 2)
                      reverse)]
    (loop [body `(doseq* ~(vec b)
                   ~@body)
           bs bs]
      (if-let [bs (seq bs)]
        (recur `(doseq* ~(vec (first bs))
                        ~body)
               (rest bs))
        body))))
