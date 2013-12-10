(ns launchpad.side)

(def side-btn-height 8)

(defn cell-idx [x x-pos] [x-pos x])

(defn toggle [side x x-pos]
  (let [old-cell (get-in side (cell-idx x x-pos))
        new-cell (if (= 1 old-cell) 0 1)]
    (assoc-in side (cell-idx x x-pos) new-cell)))

(defn cell [side x x-pos] (get-in side (cell-idx x x-pos) 0))

(defn absolute-cell [side x] (cell side (mod x side-btn-height) (int (/ x side-btn-height))))

(defn absolute-on? [side x]
  (when-let [cell (not= 0 (absolute-cell side x))]
    cell))

(defn on? [side x x-pos]
  (when-let [cell (not= 0 (cell side x x-pos))]
    cell))

(defn empty [] [[0 0 0 0 0 0 0 0]])

(defn shift-down [side] (conj side [0 0 0 0 0 0 0 0]))

(defn project [side x-pos] (nth side x-pos))
