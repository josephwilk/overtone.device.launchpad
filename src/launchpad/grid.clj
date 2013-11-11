(ns launchpad.grid)

(def fn-grid (atom {}))

(def side-btns 8)

(defn empty []
  [[0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]])

(defn project-8x8 [full-grid] (map #(butlast) full-grid))

(defn side [full-grid] (map #(last) full-grid) )

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
