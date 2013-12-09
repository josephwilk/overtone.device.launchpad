(ns launchpad.plugin.beat
  "Use LEDS in a row to express points beat should strike.
   When playing scroll through > 8 phrase beats. Either
   edit beats live as they play or switch to session mode and
   edit each grid."
  (:use [launchpad.sequencer])
  (:require
   [launchpad.core :refer :all]
   [launchpad.state-maps :as state-maps]
   [launchpad.device :as device]
   [launchpad.grid :as grid]))

(defn- toggle-row [lp lp-sequencer idx]
  (when-not (state-maps/absolute-command-right-active? (:state lp) idx)
    (reset-pattern! lp-sequencer idx)
    (device/render-row lp (mod idx 8))))

(defn- render-beats [{state :state :as lp} lp-sequencer last-col col previous-x current-x]
  (doseq [r (range 0 grid/grid-width)]
    (when (state-maps/command-right-active? state r [0 0])
      (when (seq last-col)
        (if (= 1 (nth last-col r))
          (device/led-on lp [r (mod previous-x grid/grid-width)] 2 :green)
          (device/led-off lp [r (mod previous-x grid/grid-width)])))

      (when (seq col)
        (if (= 1 (nth col r))
          (when (= 1 (int (nth (sequencer-pattern lp-sequencer r) current-x)))
            (device/led-on lp  [r (mod current-x grid/grid-width)] 3 :green))
          (device/led-on lp  [r (mod current-x grid/grid-width)] 2 :amber))))))

(defn grid-pull [{state :state :as lp} lp-sequencer]
  (let [all-patterns (sequencer-patterns lp-sequencer)]
    (doall (map-indexed
            (fn [idx row]
              (state-maps/write-complete-grid-row! state idx row))
            all-patterns))

    (device/render-grid lp)))

(defn grid-refresh [{state :state :as lp} lp-sequencer phrase-size mode]
  (fn [beat]
    (when (state-maps/active-mode? state mode)
      (let [current-x (int (mod (dec beat) phrase-size))
            previous-x (int (mod (- beat 2) phrase-size))
            col (state-maps/grid-column state current-x)
            last-col (state-maps/grid-column state previous-x)]

        (when-not (state-maps/session-mode? state)
          (let [[x _] (state-maps/grid-index state)
                active-page (int (/ current-x 8))]
            (when (not= x active-page)
              (state-maps/set-position state active-page)
              (doseq [r (range 0 grid/grid-width)]
                (when (state-maps/command-right-active? state r [0 0])
                  (device/render-row lp r 1 :green))))))

        ;;Refresh new patterns just before beat 0
        ;;Ensures new patterns start on beat
        (when (= current-x (dec phrase-size))
          (doseq [idx (range grid/grid-height)]
            (when (state-maps/command-right-active? state idx [0 0])
              (sequencer-write! lp-sequencer idx (take phrase-size (state-maps/complete-grid-row state idx))))))

        (if (state-maps/session-mode? state)
          (let [[x _] (state-maps/grid-index state)
                current-page (int (/ current-x 8))]
            (when (= x current-page)
              (render-beats lp lp-sequencer last-col col previous-x current-x)))
          (render-beats lp lp-sequencer last-col col previous-x current-x))))))

(defn setup-side-controls
  "Side controls toggle on or off a sequence row"
  ([mode lp-sequencer]
     (let [row-count (:num-samples lp-sequencer)]
       (doall
        (map
         (fn [row]
           (let [y (int (/ row grid/grid-height))
                 btn (nth device/side-controls (mod row (count device/side-controls)))]
             (bind [mode y] btn (fn [lp] (toggle-row lp lp-sequencer row)))))
         (range row-count))))))

(defn off
  "Disable all sequences"
  [lp lp-sequencer]
  (reset-all-patterns! lp-sequencer)
  (state-maps/column-off (:state lp) 8)
  (device/command-right-leds-all-off lp)
  (device/render-grid lp))
