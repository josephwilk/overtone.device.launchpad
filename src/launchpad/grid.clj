(ns launchpad.grid)

(defn fn-grid [] {})

(def side-btns 8)

(def grid-width 8)
(def grid-height 8)

;;A page includes side btn
(def page-width 9)

(defn empty []
  [[0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0 0]])

(defn x-offset [x x-pos]
  (+ x (* x-pos page-width)))

(defn project-8x8
  ([full-grid] (project-8x8 [0 0] full-grid))
  ([[x-pos y-pos] full-grid]
     (map (fn [row]
            (->> row
                 (drop (x-offset 0 x-pos))
                 (take grid-width)))
          full-grid)))

(defn project-page
  ([full-grid] (project-page [0 0] full-grid))
  ([[x-pos y-pos] full-grid]
     (map (fn [row]
            (->> row
                 (drop (x-offset 0 x-pos))
                 (take page-width)))
      full-grid
      )))

(defn side? [x] (not= grid-width (mod x (inc grid-width))))
(defn side [full-grid] (map (fn [row] (nth row side-btns)) full-grid) )

(defn toggle
  ([grid y x] (toggle [0 0] grid y x))
  ([[x-pos y-pos] grid y x]
      (let [x-offset (x-offset x x-pos)
            old-row (-> grid (nth y) (vec))
            old-cell (nth old-row x-offset)
            new-row (assoc old-row x-offset (if (= 1 old-cell) 0 1))
            new-grid (assoc (vec grid) y new-row)]
        new-grid)))

(defn set
  ([grid y x value] (set [0 0] grid y x value))
  ([[x-pos y-pos]  grid y x value]
      (let [old-row (-> grid (nth y) (vec))
            new-row (assoc old-row (x-offset x x-pos) value)]
        (assoc (vec grid) y new-row))))

(defn cell
  ([grid y x] (cell [0 0] grid y x))
  ([[x-pos y-pos] grid y x]
     (let [x-offset (x-offset x x-pos)]
       (-> grid
           (nth y)
           (nth x-offset)))))

(defn on?
  ([grid y x] (on? [0 0] grid y x))
  ([position-cords grid y x] (not= 0 (cell position-cords grid y x))))

(defn row
  ([grid n] (row [0 0] grid n))
  ([[x-pos y-pos] grid n]
     (->
      (drop (x-offset 0 x-pos))
      (take (x-offset page-width x-pos))
      (nth grid n))))

(defn complete-grid-row [grid n]
  (mapcat
   drop-last
   (split-at page-width (nth grid n))))

(defn grid-row
  ([grid n] (grid-row [0 0] grid n))
  ([[x-pos y-pos] grid n]
     (->> (nth grid n)
          (drop (x-offset 0 x-pos))
          (take (x-offset grid-width x-pos)))))

(defn col
  ([grid x] (col [0 0] grid x))
  ([[x-pos y-pos] grid x]
     (when (< x (count (first grid)))
       (map #(nth % (x-offset x x-pos)) grid))))

(defn shift-left [grid]
  (map #(concat % (take page-width (repeat 0))) grid))
