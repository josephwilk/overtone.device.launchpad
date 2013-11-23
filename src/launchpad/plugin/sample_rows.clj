(ns launchpad.plugin.sample-rows
  "Map each sample to a row where each button
   forces playback of the sample to a specific timepoint
   LED tracks current point in sample"
  (:use [overtone.live])
  (:require
   [overtone.at-at :as at-at]
   [launchpad.device :as device]
   [launchpad.core :refer :all]
   [launchpad.state-maps :as state-maps]))

(defn frames->ms
  "Convert frames to ms for a sample"
  [frame sample] (/ frame (/ (:size sample) (* 1000 (:duration sample)))))

(defn play-position
  "return current play position in seconds"
  [start-time sample]
  (let [elapsed-ms (- (now) @start-time)
        elapsed-s (/ elapsed-ms 1000)]
    (mod elapsed-s (:duration sample))))

(defn start-at
  "Start player at specified start point expressed in frames"
  [player start-frame sample start-timestamp]
  (reset! start-timestamp (- (now) (frames->ms start-frame sample)))
  (ctl player :start-point start-frame :bar-trg 1))

(defn cell-from-playtime
  "return active cell based on play position in seconds"
  [play-pos sample]
  (if (= (int play-pos) 0)
    0
    (int (/ play-pos (/ (:duration sample) 8)))))

(defn start-point-for [row sample] (* row (/ (:size sample) 8)))

(defn sample-watch-fn [lp sample row]
  (fn [_ _ _ ns]
    (when (state-maps/active-mode? (:state lp) :left)
      (let [new-cell (cell-from-playtime (int ns) sample)]
        (doseq [col (remove #(= % new-cell) (range 0 8))]
          (state-maps/set (:state lp) row col 0)
          (device/led-off lp [row col]))
        (when-not (state-maps/on? (:state lp) row new-cell)
          (state-maps/set (:state lp) row new-cell 1)
          (device/led-on lp [row new-cell] 3 :green))))))

(defonce time-pool (at-at/mk-pool))

(defn setup-event-loop [lp samples mode]
  (at-at/every 100
               #(when (state-maps/active-mode? (:state lp) mode)
                  (doseq [sample-row samples]
                    (when (state-maps/command-right-active? (:state lp) (:row sample-row))
                      (reset! (:playtime sample-row) (play-position (:start sample-row) (:sample sample-row))))))
    time-pool))

(defn setup-row [lp sample-row mode idx]
  (doseq [cell (range 0 8)]
    (bind mode (keyword (str idx "x" cell))
          (fn [lp] (start-at (:sequencer sample-row)
                            (start-point-for cell (:sample sample-row))
                            (:sample sample-row)
                            (:start sample-row)))))

  (bind mode (nth device/side-controls idx)
        (fn [lp]
          (if (state-maps/command-right-active? (:state lp) (:row sample-row))
            (do
              (ctl (:sequencer sample-row) :start-point 0 :vol 1 :bar-trig 1)
              (reset! (:playtime sample-row) 0.0)
              (reset! (:start sample-row) (now)))
            (ctl (:sequencer sample-row) :start-point 0 :bar-trig 0 :vol 0))))

  (add-watch (:playtime sample-row)
             (keyword (str "sample-" idx "-" mode))
             (sample-watch-fn lp (:sample sample-row) (:row sample-row))))

(defn sample-rows [lp mode samples]
  (doseq [[idx sample]
          (map vector (iterate inc 0) samples)] (setup-row lp sample mode idx))
  (setup-event-loop lp samples mode))
