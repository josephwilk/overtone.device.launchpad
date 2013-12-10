(ns launchpad.state-maps
  (:require [launchpad.grid :as grid]
            [launchpad.side :as side]))

(defn mode       [state] (:active @state))
(defn grid-index [state] (:grid-index @state))
(defn grid-y     [state] (second (grid-index state)))

(defn active-mode? [state candidate-mode] (= candidate-mode (mode state)))
(defn session-mode? [state] (not= 0 (:session @state)))

(defn active-side [state] (-> ((mode state) @state) :side))
(defn active-grid [state] (-> ((mode state) @state) :grid))

(defn x-max [state] (grid/x-max (active-grid state)))
(defn y-max [state] (grid/y-max (active-grid state)))

(defn active-page [state]
  (map-indexed
   (fn [idx row]
     (let [side-grid (side/project (active-side state) (grid-y state))]
       (println  :---------> (concat row [(nth side-grid idx)]))
       (concat row [(nth side-grid idx)])))
   (grid/project (grid-index state) (active-grid state))))

(defn toggle! [state x y]
  (let [new-grid (grid/toggle (grid-index state) (active-grid state) x y)]
    (swap! state assoc-in [(mode state) :grid] new-grid)
    state))

(defn trigger-fn
  ([state x y]  (trigger-fn state (str x "x" y)))
  ([state name]
     (let [handle (str (mode state) "-" (grid-y state))]
       (get-in (:fn-map @state) [handle (keyword name)]))))

(defn toggle-side! [state x]
  (let [new-side (side/toggle (active-side state) x (grid-y state))]
    (println new-side)
    (swap! state assoc-in [(mode state) :side] new-side)))

(defn on?
  ([state x y] (on? state x y (grid-index state)))
  ([state x y grid-index] (grid/on? grid-index (active-grid state) x y)))

(defn set [state x y value]
  (swap! state assoc-in [(mode state) :grid] (grid/set (grid-index state) (active-grid state) x y value)))

(defn cell
  "Cell relative to the active grid"
  [state y x] (grid/cell (grid-index state) (active-grid state) y x))

(defn absolute-cell [state y x] (grid/absolute-cell (active-grid state) y x))

(defn side-cell [state x] (side/cell (active-side state) x (grid-y state)))

(defn row    [state n] (grid/row (grid-index state) (active-grid state) n))
(defn column [state n] (grid/col (grid-index state) (active-grid state) n))

(defn column-off [state col]
  (let [grid (active-grid state)
        new-grid (reduce (fn [new-grid row] (grid/set (grid-index state) new-grid row col 0)) grid (range 0 grid/grid-width))]
    (swap! state assoc-in [(mode state) :grid] new-grid)))

(defn command-right-active?
  ([state x]            (command-right-active? state x (grid-index state)))
  ([state x grid-index] (side/on? (active-side state) x (grid-y state))))

(defn absolute-command-right-active? [state x]
  (side/absolute-on? (active-side state) x))

(defn reset!         [state] (reset! state (empty)))
(defn reset-position [state] (swap! state assoc :grid-index [0 0]))
(defn set-position   [state x] (swap! state assoc :grid-index [x 0]))

(defn shift-left [state]
  (let [[x-pos y-pos] (grid-index state)]
    (when (> x-pos 0)
      (swap! state assoc :grid-index [(dec x-pos) y-pos]))))

(defn shift-right [state]
  (let [active-mode (mode state)
        [x-pos y-pos] (grid-index state)
        current-grid-size (grid/x-page-count (active-grid state))]
    (when (>= (inc x-pos) current-grid-size)
      (swap! state assoc-in [active-mode :grid] (grid/shift-left (active-grid state))))
    (swap! state assoc :grid-index [(inc x-pos) y-pos])))

(defn shift-up [state]
  (let [[x-pos y-pos] (grid-index state)]
    (when (> y-pos 0)
      (swap! state assoc :grid-index [x-pos (dec y-pos)])))
  (println (grid-index state)))

(defn shift-down [state]
  (let [active-mode (mode state)
        [x-pos y-pos] (grid-index state)
        current-grid-size (grid/y-page-count (active-grid state))]
    (when (>= (inc y-pos) current-grid-size)
      (swap! state assoc-in [active-mode :grid] (grid/shift-down (active-grid state)))
      (swap! state assoc-in [active-mode :side] (side/shift-down (active-side state))))
    (swap! state assoc :grid-index [x-pos (inc y-pos)]))

  (println (grid-index state)))

(defn complete-row
  "Return a single row y spanning all dimensions"
  [state y]
  (grid/complete-row (active-grid state) y))

(defn write-complete-grid-row!
  [state y row]
  (let [new-grid (grid/write-complete-grid-row (active-grid state) y row)]
    (swap! state assoc-in [(mode state) :grid] new-grid)))

(defn empty []
  {:active :up
   :session 0
   :up     {:grid (grid/empty) :side (side/empty)}
   :down   {:grid (grid/empty) :side (side/empty)}
   :left   {:grid (grid/empty) :side (side/empty)}
   :right  {:grid (grid/empty) :side (side/empty)}
   :user1  {:grid (grid/empty) :side (side/empty)}
   :user2  {:grid (grid/empty) :side (side/empty)}
   :fn-map (grid/fn-grid)
   :grid-index [0 0]})

(comment
  (use '[launchpad.core] :reload)
  (column-off (:state (first launchpad-kons)) 8)
  (column (:state (first launchpad-kons)) 8))
