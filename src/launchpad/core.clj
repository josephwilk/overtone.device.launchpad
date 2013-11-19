(ns launchpad.core
  (:use
   [overtone.live]
   [overtone.inst.drum])
  (:require
   [overtone.studio.midi :as midi]
   [overtone.libs.event  :as e]
   [launchpad.mode       :as mode]
   [launchpad.state-maps :as state-maps]
   [launchpad.device     :as device]
   [launchpad.grid       :as g]))

(defn bind
  "For a specific mode bind a grid cell key press to a function.
   If function takes an argument it will be passed a stateful launchpad device."
  ([mode cell fun] (bind (first launchpad-kons) mode cell fun))
  ([lp mode cell fun]
     (assert (some #{mode} device/modes))
     (swap! (:fn-map lp) assoc-in [mode cell] fun)))

(defn boot!
  "Setup event bindings for launchpad devices."
  []
  (defonce launchpad-connected-receivers (midi/midi-find-connected-receivers "Launchpad"))
  (defonce launchpad-connected-devices   (midi/midi-find-connected-devices "Launchpad"))
  (defonce launchpad-stateful-devices    (map device/stateful-launchpad launchpad-connected-devices))
  (defonce launchpad-kons                (device/merge-launchpad-kons launchpad-connected-receivers launchpad-stateful-devices))

  (e/on-event [:Launchpad :control :up]
              (fn [m] (mode/trigger (:launchpad m) :up))
              ::up-mode)

  (e/on-event [:Launchpad :control :down]
              (fn [m] (mode/trigger (:launchpad m) :down))
              ::down-mode)

  (e/on-event [:Launchpad :control :left]
              (fn [m] (mode/trigger (:launchpad m) :left))
              ::left-mode)

  (e/on-event [:Launchpad :control :right]
              (fn [m] (mode/trigger (:launchpad m) :right))
              ::right-mode)

  (e/on-event [:Launchpad :control :user1]
              (fn [m] (mode/trigger (:launchpad m) :user1))
              ::user1-mode)

  (e/on-event [:Launchpad :control :user2]
              (fn [m] (mode/trigger (:launchpad m) :user2))
              ::user2-mode)

  (e/on-event [:Launchpad :control :mixer]
              (fn [m] (mode/trigger (:launchpad m) :mixer))
              ::mixer-mode)

  (e/on-event [:Launchpad :control :session]
              (fn [m] (mode/trigger (:launchpad m) :session))
              ::session-mode)

  (e/on-event [:Launchpad :grid-off :user1]
              (fn [{lp :launchpad [x y] :id}]
                (state-maps/toggle! (:state lp) x y)
                (device/led-off lp [x y]))
              ::grid-user-1-off)

  (e/on-event [:Launchpad :grid-off :user2]
              (fn [{lp :launchpad [x y] :id}]
                (state-maps/toggle! (:state lp) x y)
                (device/led-off lp [x y]))
              ::grid-user-2-off)

  (comment
    (boot!)
    (use 'overtone.live)
    (event-debug-on)
    (device/reset-launchpad (first launchpad-connected-receivers))
    (device/intromation (first launchpad-connected-receivers))))
