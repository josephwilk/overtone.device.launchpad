(ns launchpad.grid)

(defn new []
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
