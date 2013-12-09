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

(defn- color-and-intensity [state x absolute-y]
  (if (zero? x)
    (if (state-maps/absolute-command-right-active? state absolute-y)
      [:green 3]
      [:amber 3])
    (if (state-maps/absolute-command-right-active? state absolute-y)
      [:green 1]
      [:amber 1])))

(defn grid-refresh [{state :state :as lp} lp-sequencer phrase-size mode]
  (fn [beat]
    (when (state-maps/active-mode? state mode)
      (let [current-x (int (mod (dec beat) phrase-size))
            idxs (take 8 (drop current-x (cycle (range 0 phrase-size))))
            y-pos (state-maps/grid-y state)]

        (when (= current-x (dec phrase-size))
          (doseq [idx (range 0 (state-maps/y-max state))]
            (when (state-maps/absolute-command-right-active? state idx)
              (sequencer-write! lp-sequencer idx (take phrase-size (state-maps/complete-grid-row state idx))))))

        (when-not (state-maps/session-mode? state)
          (doall
           (map-indexed
            (fn [x absolute-x]
              (when (< absolute-x (state-maps/y-max state))
                (doseq [y (range 0 8)]
                  (let [y-page (state-maps/grid-y state)
                        absolute-y (+ y (* grid/grid-height y-page))
                        [color intensity] (color-and-intensity state x absolute-y)]


                    (device/render-cell lp (state-maps/absolute-grid-cell state absolute-y absolute-x) y x intensity color)))))
            idxs)))))))
