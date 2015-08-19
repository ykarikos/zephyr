(ns passwordless.core)

(def url-safe-chars
  (let [chars-between (fn [x y] (map char (range (int x) (int y))))]
    (concat (chars-between \0 \9)
            (chars-between \a \z)
            (chars-between \A \Z))))

(defn fixed-length-password [n]
  (let [password (take n (repeatedly #(rand-nth url-safe-chars)))]
    (reduce str password)))

(fixed-length-password 10)
