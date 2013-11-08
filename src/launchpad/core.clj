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
  "For a specific mode bind a grid cell key press to a function. 
   If function takes an argument it will be passed a stateful launchpad device."
  [mode cell fun]
  (swap! g/fn-grid assoc-in [mode cell] fun))

(defn boot! 
  "Setup event bindings for launchpad devices."
  []
  (defonce launchpad-connected-receivers (midi/midi-find-connected-receivers "Launchpad"))
  (defonce launchpad-connected-devices   (midi/midi-find-connected-devices "Launchpad"))
  (defonce launchpad-stateful-devices    (map device/stateful-launchpad launchpad-connected-devices))
  (defonce launchpad-kons                (device/merge-launchpad-kons launchpad-connected-receivers launchpad-stateful-devices))

  (let [launchpad-id 0]

    (e/on-event [:Launchpad :control launchpad-id :up]
                (fn [m] (mode/trigger (:launchpad m) :up))
                ::up-mode)

    (e/on-event [:Launchpad :control launchpad-id :down]
                (fn [m] (mode/trigger (:launchpad m) :down))
                ::down-mode)

    (e/on-event [:Launchpad :control launchpad-id :left]
                (fn [m] (mode/trigger (:launchpad m) :left))
                ::left-mode)

    (e/on-event [:Launchpad :control launchpad-id :right]
                (fn [m] (mode/trigger (:launchpad m) :right))
                ::right-mode)

    (e/on-event [:Launchpad :control launchpad-id :user1]
                (fn [m] (mode/trigger (:launchpad m) :user1))
                ::user1-mode)

    (e/on-event [:Launchpad :control launchpad-id :user2]
                (fn [m] (mode/trigger (:launchpad m) :user2))
                ::user2-mode))

  (comment
    (boot!)
    (use 'overtone.live)
    (event-debug-on)
    (device/reset-launchpad (first launchpad-connected-receivers))
    (device/intromation (first launchpad-connected-receivers))))
