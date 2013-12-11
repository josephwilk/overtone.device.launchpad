(ns launchpad.plugin.sample-rows
  "Map each sample to a row where each button
   forces playback of the sample to a specific timepoint
   LED tracks current point in sample"
  (:use [overtone.live])
  (:require
   [overtone.at-at :as at-at]
   [launchpad.device :as device]
   [launchpad.core :refer :all]
   [launchpad.grid :as grid]
   [launchpad.state-maps :as state-maps]))

(defn- cell-from-playtime
  "return active cell based on play position in seconds"
  [frame-pos sample]
  (if (= (int frame-pos) 0)
    0
    (int (/ frame-pos (/ (:size sample) grid/grid-width)))))

(defn- start-point-for
  "frame position for cell x "
  [x sample]
  (* x (/ (:size sample) grid/grid-width)))

(defn- color [{state :state :as lp} row]
  (if (state-maps/absolute-command-right-active? state row)
    :green
    :amber))

(defn sample-watch-fn [lp sample row mode]
  (fn [ref _ _ frame]
    (when (state-maps/active-mode? (:state lp) mode)
      (let [new-cell (cell-from-playtime frame sample)]
        (doseq [col (remove #(= % new-cell) (range 0 grid/grid-width))]
          (state-maps/set (:state lp) row col 0)
          (device/led-off lp [row col]))
        (when-not (state-maps/on? (:state lp) row new-cell)
          (state-maps/set (:state lp) row new-cell 1)
          (device/led-on lp [row new-cell] 3 (color lp row)))))))

(defn setup-row [lp sample-row mode idx]
  (let [cb (control-bus)
        cb-monitor (control-bus-monitor cb)]
    (ctl (:sequencer sample-row) :cb cb)

    (doseq [cell (range 0 grid/grid-width)]
      (bind mode (keyword (str idx "x" cell))
            (fn [lp] (ctl (:sequencer sample-row) :start-point (start-point-for cell (:sample sample-row)) :bar-trg 1))))

    (bind mode (nth device/side-controls idx)
          (fn [lp]
            (if (state-maps/absolute-command-right-active? (:state lp) (:row sample-row))
              (ctl (:sequencer sample-row) :start-point 0 :amp 1 :bar-trig 1)
              (ctl (:sequencer sample-row) :start-point 0 :bar-trig 0 :amp 0))))

    (add-watch cb-monitor (keyword (str "sample-" idx "-" mode))
               (sample-watch-fn lp (:sample sample-row) (:row sample-row) mode))))

(defn sample-rows [lp mode samples]
  (doseq [[idx sample] (map vector (iterate inc 0) samples)]
    (setup-row lp sample mode idx)))
