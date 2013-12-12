(ns launchpad.side)

(def side-btn-height 8)

(defn empty [] [[0 0 0 0 0 0 0 0]])

(defn toggle [side x x-pos]
  (let [old-cell (get-in side [x-pos x])
        new-cell (if (= 1 old-cell) 0 1)]
    (assoc-in side [x-pos x] new-cell)))

(defn cell [side x x-pos] (get-in side [x-pos x] 0))

(defn absolute-cell [side x] (cell side (mod x side-btn-height) (int (/ x side-btn-height))))

(defn absolute-on? [side x] (not= 0 (absolute-cell side x)))

(defn on? [side x x-pos] (not= 0 (cell side x x-pos)))

(defn shift-down [side] (conj side [0 0 0 0 0 0 0 0]))

(defn project [side x-pos] (nth side x-pos))
