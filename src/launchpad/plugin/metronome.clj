(ns launchpad.plugin.metronome
  "Pulse brightness of an LED in time with the
   beat (as sent as a trigger to count-trig-id"
  (:use overtone.live)
  (:require [launchpad.device :as device]))

(defn start [lp mode count-trig-id trigger-key]
  (on-trigger count-trig-id
              (fn [beat]
                (let [brightness (mod beat 4)]
                  (device/led-on  lp :mixer brightness :amber)))
              trigger-key))
