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

(defn- color [state row]
  (if (state-maps/absolute-command-right-active? state row)
    :green
    :amber))

(defn sample-watch-fn [{state :state :as lp} sample y mode]
  (fn [ref _ _ frame]
    (when (and (state-maps/active-mode? state mode)
               (state-maps/visible? state 0 y))
      (let [new-cell (cell-from-playtime frame sample)
            grid-y (mod y grid/grid-height)]
        (doseq [x (remove #(= % new-cell) (range 0 grid/grid-width))]
          (state-maps/set state grid-y x 0)
          (device/led-off lp [grid-y x]))
        (when-not (state-maps/on? state grid-y new-cell)
          (state-maps/set state grid-y new-cell 1)
          (device/led-on lp [grid-y new-cell] 3 (color state y)))))))

(defn setup-row [lp sample-row mode idx]
  (let [cb (control-bus)
        cb-monitor (control-bus-monitor cb)
        page (int (/ idx grid/grid-height))
        grid-y (mod idx grid/grid-height)]

    (ctl (:sequencer sample-row) :cb cb)

    (doseq [cell (range 0 grid/grid-width)]
      (bind [mode page] (keyword (str grid-y "x" cell))
            (fn [lp] (ctl (:sequencer sample-row) :start-point (start-point-for cell (:sample sample-row)) :bar-trg 1))))

    (bind [mode page] (nth device/side-controls grid-y)
          (fn [lp]
            (if (state-maps/absolute-command-right-active? (:state lp) idx)
              (ctl (:sequencer sample-row) :start-point 0 :amp 1 :bar-trig 1)
              (ctl (:sequencer sample-row) :start-point 0 :bar-trig 0 :amp 0))))

    (add-watch cb-monitor (keyword (str "sample-" idx "-" mode))
               (sample-watch-fn lp (:sample sample-row) idx mode))))

(defn sample-rows [lp mode samples]  (doseq [sample samples] (setup-row lp sample mode (:row sample))))
