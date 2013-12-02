(ns launchpad.plugin.beat
  "Use LEDS in a row to express points beat should strike.
   When playing while scroll through > 8 phrase beats. Either
   edit beats live as they play or switch to session mode and
    edit each grid."
  (:use [launchpad.sequencer])
  (:require
   [launchpad.state-maps :as state-maps]
   [launchpad.device :as device]
   [launchpad.grid :as grid]))

(defn toggle-row [lp lp-sequencer idx]
  (when-not (state-maps/command-right-active? (:state lp) idx [0 0])
    (reset-pattern! lp-sequencer idx)
    (device/render-row lp idx)))

(defn grid-refresh [lp lp-sequencer phrase-size]
  (fn [beat]
    (when (state-maps/active-mode? (:state lp) :up)
      (let [current-x (int (mod (dec beat) phrase-size))
            previous-x (int (mod (- beat 2) phrase-size))
            col (state-maps/grid-column (:state lp) current-x)
            last-col (state-maps/grid-column (:state lp) previous-x)]

        (when-not (state-maps/session-mode? (:state lp))
          (let [[x _] (state-maps/grid-index (:state lp))
                active-page (int (/ current-x 8))]
            (when (not= x active-page)
              (state-maps/set-position (:state lp) active-page)
              (doseq [r (range 0 grid/grid-width)]
                (when (state-maps/command-right-active? (:state lp) r [0 0])
                  (device/render-row lp r 1 :green))))))

        ;;Refresh new patterns just before beat 0
        ;;Ensures new patterns start on beat
        (when (= current-x (dec phrase-size))
          (doseq [idx (range grid/grid-height)]
            (when (state-maps/command-right-active? (:state lp) idx [0 0])
              (sequencer-write! lp-sequencer idx (take phrase-size (state-maps/complete-grid-row (:state lp) idx))))))

        (doseq [r (range 0 grid/grid-width)]
          (when (state-maps/command-right-active? (:state lp) r [0 0])
            (when (seq last-col)
              (if (= 1 (nth last-col r))
                (device/led-on lp [r (mod previous-x 8)] 2 :green)
                (device/led-off lp [r (mod previous-x 8)])))

            (when (seq col)
              (if (= 1 (nth col r))
                (when (= 1 (int (nth (sequencer-pattern lp-sequencer r) current-x)))
                  (device/led-on lp  [r (mod current-x 8)] 3 :green))
                (device/led-on lp  [r (mod current-x 8)] 1 :amber)))))))))
