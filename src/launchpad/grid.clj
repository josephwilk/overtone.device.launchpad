(ns launchpad.grid
  (:use [slingshot.slingshot :only [throw+]]))

(defn fn-grid [] {})

(def grid-width  8)
(def grid-height 8)

(defn empty []
  [[0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]])

(defn x-offset [x x-pos] (+ x (* x-pos grid-width)))
(defn y-offset [y y-pos] (+ y (* y-pos grid-height)))

(defn project
  ([full-grid] (project [0 0] full-grid))
  ([[x-pos y-pos] full-grid]
     (map (fn [row]
            (let [new-row (->> row
                               (drop (x-offset 0 x-pos))
                               (take grid-width)
                               seq)]
              (or new-row (take grid-width (repeat 0)))))
          (take grid-height (drop (y-offset 0 y-pos) full-grid)))))

(defn x-page-count [grid] (int (/ (count (first grid)) grid-width)))
(defn y-page-count [grid] (int (/ (count grid) grid-height)))
(defn x-max [grid] (count (first grid)))
(defn y-max [grid] (count grid))

(defn toggle
  ([grid y x] (toggle [0 0] grid y x))
  ([[x-pos y-pos] grid y x]
     (let [x-offset (x-offset x x-pos)
           y-offset (y-offset y y-pos)
           old-row (-> grid (nth y-offset) (vec))
           old-cell (nth old-row x-offset)
           new-row (assoc old-row x-offset (if (= 1 old-cell) 0 1))
           new-grid (assoc (vec grid) y-offset new-row)]
       new-grid)))

(defn set
  "Set a cell within a grid to a value"
  ([grid y x value] (set [0 0] grid y x value))
  ([[x-pos y-pos]  grid y x value]
     (let [y-offset (y-offset y y-pos)
           old-row (-> grid (nth y-offset) (vec))
           new-row (assoc old-row (x-offset x x-pos) value)]
        (assoc (vec grid) y-offset new-row))))

(defn cell
  "Find the value of a cell"
  ([grid y x] (cell [0 0] grid y x))
  ([[x-pos y-pos] grid y x]
     (let [x-offset (x-offset x x-pos)
           y-offset (y-offset y y-pos)]
       (-> grid
           (nth y-offset)
           (nth x-offset)))))

(defn absolute-cell [grid y x]
  (if (and (< y (y-max grid)) (< x (x-max grid)))
    (-> grid (nth y) (nth x))
    0))

(defn on?
  ([grid y x] (on? [0 0] grid y x))
  ([position-cords grid y x] (not= 0 (cell position-cords grid y x))))

(defn row
  ([grid y] (row [0 0] grid y))
  ([[x-pos y-pos] grid y]
     (let [y-offset (y-offset y y-pos)])
     (->
      grid
      (drop (x-offset 0 x-pos))
      (take (x-offset grid-width x-pos))
      (nth grid y-offset))))

(defn col
  ([grid x] (col [0 0] grid x))
  ([[x-pos y-pos] grid x]
     (let [x-offset (x-offset x x-pos)]
       (if (< x-offset (x-max grid))
         (map #(nth % x-offset) (drop (y-offset 0 y-pos) grid))
         (take grid-height (repeat 0))))))

(defn absolute-column
  "Direct access into the grid irrelevant of x grid-index"
  [[_ y-pos] grid x]
  (let [grid-x (cond
                (> x grid-width) (int (dec (+ (/ x grid-width) x)))
                true x)]
    (if (< grid-x (x-max grid))
      (map #(nth % grid-x) (take grid-height (drop (y-offset 0 y-pos) grid)))
      (take grid-height (repeat 0)))))

(defn complete-row
  "Direct access into the entire grid, ignores any grid position"
  [grid y] (nth grid y))

(defn write-complete-grid-row [grid y row]
  (when-not (= (count row) (x-max grid))
    (throw+ {:type ::DifferingRowSize :hint (str "row must match grid row size. The grid has rows: " (x-max grid) " passed row has: " (count row))}))
  (assoc (vec grid) y (map int (mapcat #(concat % [1]) (split-at grid-width row)))))

(defn shift-left [grid]
  (map #(concat % (take grid-width (repeat 0))) grid))

(defn shift-down [grid]
  (let [x (x-page-count  grid)]
    (concat grid (repeat grid-height (take (* x grid-width) (repeat 0))))))
