(ns launchpad.state-maps
  (:require [launchpad.device :as device]))

(def modes [:up :down :left :right])

;;FIXME: This is wasting precious refresh events.
;; only turn off whats on.
(defn reset-direction-modes [launchpad]
  (doseq [mode modes] (device/led-off* launchpad mode)))

(defn up-mode [launchpad]
  (reset-direction-modes launchpad)
  (device/led-on* launchpad :up))

(defn down-mode [launchpad]
  (reset-direction-modes launchpad)
  (device/led-on* launchpad :down))

(defn left-mode [launchpad]
  (reset-direction-modes launchpad)
  (device/led-on* launchpad :left))

(defn right-mode [launchpad]
  (reset-direction-modes launchpad)
  (device/led-on* launchpad :right))
