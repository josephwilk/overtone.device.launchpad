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

  (let [launchpad-id 0]

    (e/on-event [:Launchpad :control launchpad-id :up]
                (fn [m] (s/up-mode (:launchpad m)))
                ::up-mode)

    (e/on-event [:Launchpad :control launchpad-id :down]
                (fn [m] (s/down-mode (:launchpad m)))
                ::down-mode)

    (e/on-event [:Launchpad :control launchpad-id :left]
                (fn [m] (s/left-mode (:launchpad m)))
                ::left-mode)

    (e/on-event [:Launchpad :control launchpad-id :right]
                (fn [m] (s/right-mode (:launchpad m)))
                ::right-mode))

  (comment
    (boot!)
    (use 'overtone.live)
    (event-debug-on)
    (device/reset-launchpad (first launchpad-connected-receivers))
    (device/intromation (first launchpad-connected-receivers))
    ))
