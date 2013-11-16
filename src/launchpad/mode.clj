(ns launchpad.mode
  (:require
   [launchpad.device :as device]
   [launchpad.state-maps :as state-maps]))

(defn- turn-off-old-active-mode [launchpad]
  (device/led-off launchpad (state-maps/mode (:state launchpad))))

(defn- activate-mode [launchpad mode]
  (turn-off-old-active-mode launchpad)
  (device/led-on launchpad mode 3 :yellow)
  (swap! (:state launchpad) assoc :active mode)
  (device/render-grid launchpad))

(defn trigger [launchpad mode] (activate-mode launchpad mode))
