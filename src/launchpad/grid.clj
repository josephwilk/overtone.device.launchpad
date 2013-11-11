(ns launchpad.grid)

(def fn-grid (atom {}))

(defn empty []
  [[0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]])

(defn toggle [grid x y]
  (let [old-row (-> grid (nth x) (vec))
        old-cell (nth old-row y)
        new-row (assoc old-row y (if (= 1 old-cell) 0 1))
        new-grid (assoc (vec grid) x new-row)]
    new-grid))

(defn cell [grid x y] (-> grid (nth x) (nth y)))

(defn on? [grid x y] (not= 0 (cell grid x y)))

(defn row [grid n] (nth grid n))

(defn col [grid n] (map #(nth % n) grid))
