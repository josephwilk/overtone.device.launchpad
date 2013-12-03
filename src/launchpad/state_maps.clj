(ns launchpad.state-maps
  (:require [launchpad.grid :as grid]))

(defn mode       [state] (:active @state))
(defn grid-index [state] (:grid-index @state))

(defn active-mode? [state candidate-mode] (= candidate-mode (mode state)))

(defn active-grid [state] ((mode state) @state))

(defn active-page [state]
  (grid/project-page (grid-index state) (active-grid state)))

(defn toggle! [state x y]
  (let [new-grid (grid/toggle (grid-index state) (active-grid state) x y)]
    (swap! state assoc (mode state) new-grid)
    state))

(defn trigger-fn
  ([state x y]  (trigger-fn state (str x "x" y)))
  ([state name] (get-in (:fn-map @state) [(mode state) (keyword name)])))

(defn toggle-side! [state x] (toggle! state x grid/side-btns))

(defn on?
  ([state x y] (on? state x y (grid-index state)))
  ([state x y grid-index] (grid/on? grid-index (active-grid state) x y)))

(defn grid-row [state n] (grid/grid-row (grid-index state) (active-grid state) n))

(defn set [state x y value]
  (swap! state assoc (mode state) (grid/set (grid-index state) (active-grid state) x y value)))

(defn cell [state y x] (grid/cell (grid-index state) (active-grid state) y x))

(defn row [state n] (grid/row (grid-index state) (active-grid state) n))
(defn column [state n] (grid/col (grid-index state) (active-grid state) n))

(defn grid-column [state n] (grid/grid-column (active-grid state) n))

(defn column-off [state col]
  (let [grid (active-grid state)
        new-grid (reduce (fn [new-grid row] (grid/set (grid-index state) new-grid row col 0)) grid (range 0 8))]
    (swap! state assoc (mode state) new-grid)))

(defn command-right-active?
  ([state x] (command-right-active? state x (grid-index state)))
  ([state x grid-index] (on? state x grid/side-btns grid-index)))

(defn session-mode? [state] (not= 0 (:session @state)))

(defn reset! [state] (reset! state (empty)))

(defn reset-position [state] (swap! state assoc :grid-index [0 0]))
(defn set-position [state x] (swap! state assoc :grid-index [x 0]))

(defn shift-left [state]
  (let [[x-pos y-pos] (grid-index state)]
    (when (> x-pos 0)
      (swap! state assoc :grid-index [(dec x-pos) 0]))))

(defn shift-right [state]
  (let [active-mode (mode state)
        [x-pos y-pos] (grid-index state)
        current-grid-size (int (/ (count (first (active-grid state))) grid/page-width))]
    (when (>= (inc x-pos) current-grid-size)
      (swap! state assoc active-mode (grid/shift-left (active-grid state)))))
  (swap! state assoc :grid-index [(inc (first (grid-index state))) 0]))

(defn complete-grid-row
  "Return a single row y spanning all dimensions"
  [state y]
  (grid/complete-grid-row (active-grid state) y))

(defn empty []
  {:active :up
   :session 0
   :up     (grid/empty)
   :down   (grid/empty)
   :left   (grid/empty)
   :right  (grid/empty)
   :user1  (grid/empty)
   :user2  (grid/empty)
   :fn-map (grid/fn-grid)
   :grid-index [0 0]})

(comment
  (use '[launchpad.core] :reload)
  (column-off (:state (first launchpad-kons)) 8)
  (column (:state (first launchpad-kons)) 8))
