(ns launchpad.state-maps
  (:require [launchpad.device :as device]))

(def modes [:up :down :left :right])

;;FIXME: This is wasting precious refresh events.
;; only turn off whats on.
(defn reset-direction-modes [launchpad]
  (doseq [mode modes] (device/led-off* launchpad mode)))

(defn up-mode [launchpad]
  (reset-direction-modes launchpad)
  (device/led-on* launchpad :up)
  (device/render-grid launchpad (:up @(:state launchpad))))

(defn down-mode [launchpad]
  (reset-direction-modes launchpad)
  (device/led-on* launchpad :down)
  (device/render-grid launchpad (:down @(:state launchpad))))

(defn left-mode [launchpad]
  (reset-direction-modes launchpad)
  (device/led-on* launchpad :left)
  (device/render-grid launchpad (:left @(:state launchpad))))

(defn right-mode [launchpad]
  (reset-direction-modes launchpad)
  (device/led-on* launchpad :right)
  (device/render-grid launchpad (:right @(:state launchpad))))
