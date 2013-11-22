(ns launchpad.state-maps
  (:require [launchpad.grid :as grid]))

(defn mode [state] (:active @state))

(defn active-mode? [state candidate-mode] (= candidate-mode (mode state)))

(defn active-grid [state] ((mode state) @state))

(defn toggle! [state x y]
  (let [new-grid (grid/toggle (active-grid state) x y)]
    (swap! state assoc (mode state) new-grid)
    state))

(defn trigger-fn
  ([state x y]  (trigger-fn state (str x "x" y)))
  ([state name] (get-in (:fn-map @state) [(mode state) (keyword name)])))

(defn toggle-side! [state x] (toggle! state x grid/side-btns))
(defn on? [state x y] (grid/on? (active-grid state) x y))

(defn grid-row [state n] (grid/grid-row (active-grid state) n))

(defn set [state x y value]
  (swap! state assoc (mode state) (grid/set (active-grid state) x y value)))

(defn cell [state x y] (grid/cell (active-grid state) x y))

(defn row [state n] (grid/row (active-grid state) n))
(defn column [state n] (grid/col (active-grid state) n))

(defn column-off [state col]
  (let [grid (active-grid state)
        new-grid (reduce (fn [new-grid row] (grid/set new-grid row col 0)) grid (range 0 8))]
    (swap! state assoc (mode state) new-grid)))

(defn command-right-active? [state x] (on? state x grid/side-btns))

(defn reset! [state] (reset! state (empty)))

(defn empty []
  {:active :up
   :up     (grid/empty)
   :down   (grid/empty)
   :left   (grid/empty)
   :right  (grid/empty)
   :user1  (grid/empty)
   :user2  (grid/empty)
   :fn-map (grid/fn-grid)})

(comment
  (use '[launchpad.core] :reload)
  (column-off (:state (first launchpad-kons)) 8)
  (column (:state (first launchpad-kons)) 8))
