(ns launchpad.core
  (:use
   [overtone.live]
   [overtone.inst.drum])
  (:require
   [overtone.studio.midi :as midi]
   [overtone.libs.event  :as e]
   [launchpad.mode       :as mode]
   [launchpad.device     :as device]
   [launchpad.grid       :as g]))

(defn bind
  "For a specific active mode bind a grid cell press to a function"
  [mode cell fun]
  (swap! g/fn-grid assoc-in [mode cell] fun))

(defn boot! []
  (defonce launchpad-connected-receivers (midi/midi-find-connected-receivers "Launchpad"))
  (defonce launchpad-connected-devices   (midi/midi-find-connected-devices "Launchpad"))
  (defonce launchpad-stateful-devices    (map device/stateful-launchpad launchpad-connected-devices))
  (defonce launchpad-kons                (device/merge-launchpad-kons launchpad-connected-receivers launchpad-stateful-devices))

  (let [launchpad-id 0]

    (e/on-event [:Launchpad :control launchpad-id :up]
                (fn [m] (mode/up-mode (:launchpad m)))
                ::up-mode)

    (e/on-event [:Launchpad :control launchpad-id :down]
                (fn [m] (mode/down-mode (:launchpad m)))
                ::down-mode)

    (e/on-event [:Launchpad :control launchpad-id :left]
                (fn [m] (mode/left-mode (:launchpad m)))
                ::left-mode)

    (e/on-event [:Launchpad :control launchpad-id :right]
                (fn [m] (mode/right-mode (:launchpad m)))
                ::right-mode))

  (comment
    (boot!)
    (use 'overtone.live)
    (event-debug-on)
    (device/reset-launchpad (first launchpad-connected-receivers))
    (device/intromation (first launchpad-connected-receivers))))
