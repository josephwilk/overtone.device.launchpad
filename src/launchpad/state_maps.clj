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
  (swap! (:state launchpad) assoc :active :up)
  (device/render-grid launchpad (:up @(:state launchpad))))

(defn down-mode [launchpad]
  (reset-direction-modes launchpad)
  (device/led-on* launchpad :down)
  (swap! (:state launchpad) assoc :active :down)
  (device/render-grid launchpad (:down @(:state launchpad))))

(defn left-mode [launchpad]
  (reset-direction-modes launchpad)
  (device/led-on* launchpad :left)
  (swap! (:state launchpad) assoc :active :left)
  (device/render-grid launchpad (:left @(:state launchpad))))

(defn right-mode [launchpad]
  (reset-direction-modes launchpad)
  (device/led-on* launchpad :right)
  (swap! (:state launchpad) assoc :active :right)
  (device/render-grid launchpad (:right @(:state launchpad))))
