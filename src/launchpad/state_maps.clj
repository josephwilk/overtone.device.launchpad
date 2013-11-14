(ns launchpad.state-maps
  (:require [launchpad.grid :as grid]))

(defn mode [state] (:active @state))

(defn active-grid [state] ((mode state) @state))

(defn toggle! [state x y]
  (let [new-grid (grid/toggle (active-grid state) x y)]
    (swap! state assoc (mode state) new-grid)
    state))

(defn trigger-fn
  ([state x y]  (trigger-fn state (str x "x" y)))
  ([state name] (get-in @grid/fn-grid [(mode state) (keyword name)])))

(defn toggle-side! [state x] (toggle! state x grid/side-btns))
(defn on? [state x y] (grid/on? (active-grid state) x y))
(defn row [state n] (grid/row (active-grid state) n))
(defn column [state n] (grid/col (active-grid state) n))
(defn command-right-active? [state x] (on? state x grid/side-btns))

(defn empty []
  {:active :up
   :up    (grid/empty)
   :down  (grid/empty)
   :left  (grid/empty)
   :right (grid/empty)
   :user1 (grid/empty)
   :user2 (grid/empty)})
