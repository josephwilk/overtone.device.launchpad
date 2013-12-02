(ns launchpad.mode
  (:require
   [launchpad.device :as device]
   [launchpad.state-maps :as state-maps]))

(defn- turn-off-old-active-mode [launchpad]
  (device/led-off launchpad (state-maps/mode (:state launchpad))))

(defn- activate-mode [launchpad mode]
  (turn-off-old-active-mode launchpad)
  (device/led-on launchpad mode 3 :amber)
  (state-maps/reset-position (:state launchpad))
  (swap! (:state launchpad) assoc :active mode)
  (device/render-grid launchpad))

(defn trigger [launchpad mode] (activate-mode launchpad mode))

(defn trigger-binary-mode [launchpad mode]
  (if (state-maps/session-mode? (:state launchpad))
    (do
      (device/led-off launchpad mode)
      (swap! (:state launchpad) assoc mode 0))
    (do
      (device/led-on launchpad mode 3 :yellow)
      (swap! (:state launchpad) assoc mode 1))))

(defn session? [lp]
  (state-maps/session-mode? (:state lp)))
