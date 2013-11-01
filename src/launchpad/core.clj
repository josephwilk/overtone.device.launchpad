(ns launchpad.core
  (:require [overtone.studio.midi :as midi]
            [overtone.libs.event :as e]
            [launchpad.state-maps :as s]
            [launchpad.device :as device]))

(defn boot! []
  (defonce launchpad-connected-receivers (midi/midi-find-connected-receivers "Launchpad"))
  (defonce launchpad-connected-devices   (midi/midi-find-connected-devices "Launchpad"))
  (defonce launchpad-stateful-devices    (map device/stateful-launchpad launchpad-connected-devices))
  (defonce launchpad-kons                (device/merge-launchpad-kons launchpad-connected-receivers launchpad-stateful-devices))

  (e/on-event [:Launchpad :control 0 :up]
              (fn [m] (s/up-mode (:launchpad m)))
              ::up-mode)

  (e/on-event [:Launchpad :control 0 :down]
              (fn [m] (s/down-mode (:launchpad m)))
              ::down-mode)

  (e/on-event [:Launchpad :control 0 :left]
              (fn [m] (s/left-mode (:launchpad m)))
              ::left-mode)

  (e/on-event [:Launchpad :control 0 :right]
              (fn [m] (s/right-mode (:launchpad m)))
              ::right-mode))


(comment
  (boot!)
  (use 'overtone.live)
  (event-debug-on))
