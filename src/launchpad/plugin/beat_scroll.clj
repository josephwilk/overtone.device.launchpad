(ns launchpad.plugin.beat-scroll
  "Provides a scroll mode for watching beats move in.
   Not really useful for live editing (only use session mode for that)
   but useful for getting  a better sense on upcoming beats"
  (:use [launchpad.sequencer])
  (:require
   [launchpad.core :refer :all]
   [launchpad.state-maps :as state-maps]
   [launchpad.device :as device]
   [launchpad.grid :as grid]))

(defn- color-and-intensity [state y]
  (if (zero? y)
    [:green 3]
    [:green 1]))

(defn grid-refresh [{state :state :as lp} lp-sequencer phrase-size]
  (fn [beat]
    (when (state-maps/active-mode? state :up)

      (let [current-x (int (mod (dec beat) phrase-size))
            idxs (take 8 (drop current-x (cycle (range 0 phrase-size))))
            y-pos (state-maps/grid-y state)]

        (doseq [idx (range 0 (state-maps/y-max state))]
          (when (state-maps/command-right-active? state idx [0 0])
            (sequencer-write! lp-sequencer idx (take phrase-size (state-maps/complete-grid-row state idx)))))

        (when-not (state-maps/session-mode? state)
          (doall (map-indexed
                  (fn [idx col-idx]
                    (when (< col-idx (state-maps/x-max state))
                      (let [[color intensity] (color-and-intensity state idx)]
                        (device/render-column-at lp (state-maps/grid-column state col-idx) idx intensity color))))
                  idxs)))))))
