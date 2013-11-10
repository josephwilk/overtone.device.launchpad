(ns launchpad.state-maps
  (:require [launchpad.grid :as grid]))

(defn mode [state] (:active @state))

(defn active-grid [state] ((mode state) @state))

(defn toggle! [state x y]
  (let [new-grid (grid/toggle (active-grid state) x y)]
    (swap! state assoc (mode state) new-grid)
    state))

(defn toggle-side! [state row]
  (let [old-row (:command-right @state)
        cell (nth old-row row )
        new-row (assoc old-row row (mod (inc cell) 2))]
    (swap! state assoc :command-right new-row)))

(defn on? [state x y] (grid/on? (active-grid state) x y))

(defn row [state n] (grid/row (active-grid state) n))

(defn column [state n] (grid/col (active-grid state) n))

(defn command-right-active? [state row]
  (= 1 (nth (:command-right @state) row)))

(defn empty []
  {:active :up
   :up    (grid/empty)
   :down  (grid/empty)
   :left  (grid/empty)
   :right (grid/empty)
   :user1 (grid/empty)
   :user2 (grid/empty)
   :command-right [0 0 0 0 0 0 0 0]
   :command-top   [0 0 0 0 0 0 0 0]})
