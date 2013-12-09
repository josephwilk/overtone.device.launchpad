(ns launchpad.grid
  "We refer to the 8x8 buttons as the Grid
   and the full 9x9 (including side buttons as the Page"
  (:use [slingshot.slingshot :only [throw+]]))

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

(defn- cord-offset [cord position] )

(defn x-offset [x x-pos] (+ x (* x-pos page-width)))
(defn y-offset [y y-pos] (+ y (* y-pos grid-height)))

(defn project-8x8
  ([full-grid] (project-8x8 [0 0] full-grid))
  ([[x-pos y-pos] full-grid]
     (map (fn [row]
            (->> row
                 (drop (x-offset 0 x-pos))
                 (take grid-width)))
          (take grid-height (drop (y-offset 0 y-pos) full-grid)))))

(defn project-page
  ([full-grid] (project-page [0 0] full-grid))
  ([[x-pos y-pos] full-grid]
     (remove nil?
             (map (fn [row]
                    (->> row
                         (drop (x-offset 0 x-pos))
                         (take page-width)
                         seq))
                  (take grid-height (drop (y-offset 0 y-pos) full-grid))))))

(defn complete-grid
  "Grid and hence no side buttons"
  [grid]
  (map #(mapcat drop-last (split-at page-width %)) grid))

(defn x-page-count [grid] (int (/ (count (first grid)) page-width)))
(defn y-page-count [grid] (int (/ (count grid) grid-height)))
(defn x-max [grid] (count (first grid)))
(defn y-max [grid] (count grid))

(defn side? [x] (not= grid-width (mod x (inc grid-width))))

(defn side [full-grid] (map (fn [row] (nth row side-btns)) full-grid) )

(defn grid-column
  "Direct access into the grid irrelevant of x grid-index"
  [[_ y-pos] grid x]
  (let [grid-x (cond
                (>= x grid-width) (int (+ (/ x grid-width) x))
                true x)]
    (when (< grid-x (count (first grid)))
      (map #(nth % grid-x) (take grid-height (drop (y-offset 0 y-pos) grid))))))

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

(defn absolute-grid-cell [grid y x]
  (-> grid
      (nth y)
      (nth (+ x  (int (/ x 8))))))

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
      (take (x-offset page-width x-pos))
      (nth grid y-offset))))

(defn complete-grid-row
  "Direct access into the entire grid, ignores any grid position"
  [grid y]
  (mapcat
   drop-last
   (split-at page-width (nth grid y))))

(defn write-complete-grid-row [grid y row]
  (when-not (= (count row) (- (x-max grid) (x-page-count grid)))
    (throw+ {:type ::DifferingRowSize :hint (str "row must match grid row size. The grid has rows: " (- (x-max grid) (x-page-count grid)) " passed row has: " (count row))}))
  (assoc (vec grid) y (map int (mapcat #(concat % [1]) (split-at grid-width row)))))

(defn grid-row
  ([grid n] (grid-row [0 0] grid n))
  ([[x-pos y-pos] grid n]
     (->> (nth grid (y-offset y-pos n))
          (drop (x-offset 0 x-pos))
          (take (x-offset grid-width x-pos)))))

(defn col
  ([grid x] (col [0 0] grid x))
  ([[x-pos y-pos] grid x]
     (let [x-offset (x-offset x x-pos)]
       (when (< x-offset (count (first grid)))
         (map #(nth % x-offset) grid)))))

(defn shift-left [grid] (map #(concat % (take page-width (repeat 0))) grid))

(defn shift-down [grid] (concat grid (repeat grid-height (take page-width (repeat 0)))))
