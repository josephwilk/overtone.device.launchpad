(ns launchpad.state-maps
  (:require [launchpad.grid :as grid]))

(defn mode [state] (:active @state))

(defn active-mode? [state candidate-mode] (= candidate-mode (mode state)))

(defn active-grid [state] ((mode state) @state))

(defn grid-index [state] (:grid-index @state))

(defn toggle! [state x y]
  (let [new-grid (grid/toggle (grid-index state) (active-grid state) x y)]
    (swap! state assoc (mode state) new-grid)
    state))

(defn trigger-fn
  ([state x y]  (trigger-fn state (str x "x" y)))
  ([state name] (get-in (:fn-map @state) [(mode state) (keyword name)])))

(defn toggle-side! [state x] (toggle! state x grid/side-btns))

(defn on? [state x y] (grid/on? (grid-index state) (active-grid state) x y))

(defn grid-row [state n] (grid/grid-row (grid-index state) (active-grid state) n))

(defn set [state x y value]
  (swap! state assoc (mode state) (grid/set (grid-index state) (active-grid state) x y value)))

(defn cell [state y x] (grid/cell (grid-index state) (active-grid state) y x))

(defn row [state n] (grid/row (grid-index state) (active-grid state) n))
(defn column [state n] (grid/col (grid-index state) (active-grid state) n))

(defn column-off [state col]
  (let [grid (active-grid state)
        new-grid (reduce (fn [new-grid row] (grid/set (grid-index state) new-grid row col 0)) grid (range 0 8))]
    (swap! state assoc (mode state) new-grid)))

(defn command-right-active? [state x] (on? state x grid/side-btns))

(defn session-mode? [state] (not= 0 (:session @state)))

(defn reset! [state] (reset! state (empty)))

(defn reset-position [state] (swap! state assoc :grid-index [0 0]))

(defn shift-left [state]
  (let [active-mode (mode state)
        index (grid-index state)]
    (swap! state assoc active-mode (grid/shift-left (active-grid state)))
    (swap! state assoc :grid-index [(dec (first index)) 0])
    (println :index (grid-index state))))

(defn shift-right [state]
  (swap! state assoc :grid-index [(inc (first (grid-index state))) 0])
  (println :index (grid-index state)))

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
