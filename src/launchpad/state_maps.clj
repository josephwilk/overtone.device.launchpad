(ns launchpad.state-maps
  (:require [launchpad.grid :as grid]))

(defn mode [state] (:active @state))

(defn active-grid [state] ((mode state) @state))

(defn toggle! [state x y]
  (let [new-grid (grid/toggle (active-grid state) x y)]
    (swap! state assoc (mode state) new-grid)
    state))

(defn on? [state x y] (grid/on? (active-grid state) x y))

(defn empty []
  {:active :up
   :up    (grid/empty)
   :down  (grid/empty)
   :left  (grid/empty)
   :right (grid/empty)
   :user1 (grid/empty)
   :user2 (grid/empty)})
