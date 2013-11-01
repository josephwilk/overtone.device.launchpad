(ns launchpad.core
  (:require [overtone.studio.midi :as midi]
            [launchpad.core :as core]))

(defn boot! []
  (defonce launchpad-connected-receivers (midi/midi-find-connected-receivers "Launchpad"))
  (defonce launchpad-connected-devices   (midi/midi-find-connected-devices "Launchpad"))
  (defonce launchpad-stateful-devices    (map core/stateful-launchpad launchpad-connected-devices))
  (defonce launchpad-kons                (core/merge-launchpad-kons launchpad-connected-receivers launchpad-stateful-devices)))

(comment
  (boot!))
