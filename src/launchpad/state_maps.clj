(ns launchpad.state-maps
  (:require [launchpad.device :as device]))

(defn turn-off-old-active-mode [launchpad]
  (device/led-off* launchpad (:active @(:state launchpad))))

(defn up-mode [launchpad]
  (turn-off-old-active-mode launchpad)
  (device/led-on* launchpad :up)
  (swap! (:state launchpad) assoc :active :up)
  (device/render-grid launchpad (:up @(:state launchpad))))

(defn down-mode [launchpad]
  (turn-off-old-active-mode launchpad)
  (device/led-on* launchpad :down)
  (swap! (:state launchpad) assoc :active :down)
  (device/render-grid launchpad (:down @(:state launchpad))))

(defn left-mode [launchpad]
  (turn-off-old-active-mode launchpad)
  (device/led-on* launchpad :left)
  (swap! (:state launchpad) assoc :active :left)
  (device/render-grid launchpad (:left @(:state launchpad))))

(defn right-mode [launchpad]
  (turn-off-old-active-mode launchpad)
  (device/led-on* launchpad :right)
  (swap! (:state launchpad) assoc :active :right)
  (device/render-grid launchpad (:right @(:state launchpad))))
