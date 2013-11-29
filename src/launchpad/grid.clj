(ns launchpad.grid)

(defn fn-grid [] {})

(def side-btns 8)

(def grid-width 8)

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
  (if (zero? x-pos)
    (+ x (* x-pos grid-width))
    (+ x 1 (* x-pos grid-width)) ;; + 1 for the right btns
    ))

(defn project-8x8
  ([full-grid] (project-8x8 [0 0] full-grid))
  ([[x-pos y-pos] full-grid]
     (map (fn [row]
            (->> row
                 (drop (x-offset 0 x-pos))
                 (take 8))) full-grid)))

(defn side [full-grid] (map (fn [row] (nth row 8)) full-grid) )

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
  ([[x-pos y-pos] grid y x] (-> grid (nth y) (nth (x-offset x x-pos)))))

(defn on?
  ([grid y x] (on? [0 0] grid y x))
  ([position-cords grid y x] (not= 0 (cell position-cords grid y x))))

(defn row
  ([grid n] (row [0 0] grid n))
  ([[x-pos y-pos] grid n]
     (->
      (drop (x-offset 0 x-pos))
      (take (x-offset 8 x-pos))
      (nth grid n))))

(defn grid-row
  ([grid n] (grid-row [0 0] grid n))
  ([[x-pos y-pos] grid n]
     (->> (nth grid n)
          (drop (x-offset 0 x-pos))
          (take (x-offset 8 x-pos)))))

(defn col [grid n] (map #(nth % n) grid))

(defn shift-left [grid]
  (map #(concat % [0 0 0 0 0 0 0 0]) grid))
