;;Demos showing all the wonderful things you can do with launchpad and overtone
;;This is a space for those ideas to grow and the good ones will be extracted out
;;and provided as plugins.
(do
  (use '[launchpad.core] :reload)
  (use 'overtone.live :reload)
  (boot!))

;;Drum kit
(do
  (use 'overtone.inst.drum)

  (bind :user1 :0x0 #(kick))
  (bind :user1 :1x0 #(kick2))
  (bind :user1 :2x0 #(kick3))
  (bind :user1 :3x0 #(kick4))
  (bind :user1 :4x0 #(dub-kick))
  (bind :user1 :5x0 #(dance-kick))
  (bind :user1 :6x0 #(dry-kick))
  (bind :user1 :7x0 #(quick-kick))
  (bind :user1 :0x1 #(open-hat))
  (bind :user1 :1x1 #(closed-hat))
  (bind :user1 :2x1 #(closed-hat2))
  (bind :user1 :3x1 #(hat3))
  (bind :user1 :4x1 #(soft-hat))
  (bind :user1 :5x1 #(snare))
  (bind :user1 :6x1 #(snare2))
  (bind :user1 :7x1 #(noise-snare))
  (bind :user1 :0x2 #(tone-snare))
  (bind :user1 :1x2 #(tom))
  (bind :user1 :2x2 #(clap))
  (bind :user1 :3x2 #(haziti-clap))
  (bind :user1 :4x2 #(bing)))

;;Map each sample to a row where each button
;;forces playback of the sample to a specific timepoint
;; LED tracks current point in sample (No beat syncing yet)
(do
  (require '[launchpad.device :as device] :reload)
  (require '[launchpad.state-maps :as state-maps] :reload)
  (require '[overtone.at-at :as at-at])

  (def lp (first launchpad-kons))

  (def phat-s (sample (freesound-path 48489)))
  (def groove-s (sample (freesound-path 48488)))

  (defsynth skipping-sequencer
    "Supports looping and jumping position"
    [buf 0 rate 1 out-bus 0 start-point 0 bar-trg [0 :tr] loop? 0 vol 1.0]
    (out out-bus (* vol (scaled-play-buf 1 buf rate bar-trg start-point loop?))))

  (def phat   (skipping-sequencer :buf (to-sc-id phat-s) :loop? true :bar-trg 0 :out-bus 0 :vol 0))
  (def groove (skipping-sequencer :buf (to-sc-id groove-s) :loop? true :bar-trg 0 :out-bus 0 :vol 0))

  (def phat-start-timestamp (atom nil))
  (def phat-playtime        (atom 0))

  (def groove-start-timestamp (atom nil))
  (def groove-playtime        (atom 0))

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

  (defn sample-watch-fn [sample row]
    (fn [_ _ _ ns]
      (when (state-maps/active-mode? (:state lp) :left)
        (let [new-cell (cell-from-playtime (int ns) sample)]
          (doseq [col (remove #(= % new-cell) (range 0 8))]
            (state-maps/set (:state lp) row col 0)
            (device/led-off lp [row col]))
          (when-not (state-maps/on? (:state lp) row new-cell)
            (state-maps/set (:state lp) row new-cell 1)
            (device/led-on lp [row new-cell] 3 :green))))))

  (add-watch phat-playtime   :phat-key   (sample-watch-fn phat-s 0))
  (add-watch groove-playtime :groove-key (sample-watch-fn groove-s 1))

  (comment (remove-watch phat-playtime :phat-key)
           (remove-watch phat-playtime :groove-key))

  (bind :left :0x0 (fn [lp] (start-at phat (start-point-for 0 phat-s) phat-s phat-start-timestamp)))
  (bind :left :0x1 (fn [lp] (start-at phat (start-point-for 1 phat-s) phat-s phat-start-timestamp)))
  (bind :left :0x2 (fn [lp] (start-at phat (start-point-for 2 phat-s) phat-s phat-start-timestamp)))
  (bind :left :0x3 (fn [lp] (start-at phat (start-point-for 3 phat-s) phat-s phat-start-timestamp)))
  (bind :left :0x4 (fn [lp] (start-at phat (start-point-for 4 phat-s) phat-s phat-start-timestamp)))
  (bind :left :0x5 (fn [lp] (start-at phat (start-point-for 5 phat-s) phat-s phat-start-timestamp)))
  (bind :left :0x6 (fn [lp] (start-at phat (start-point-for 6 phat-s) phat-s phat-start-timestamp)))
  (bind :left :0x7 (fn [lp] (start-at phat (start-point-for 7 phat-s) phat-s phat-start-timestamp)))

  (bind :left :vol (fn [lp]
                     (if (state-maps/command-right-active? (:state lp) 0)
                       (do
                         (ctl phat :start-point 0 :vol 1 :bar-trig 1)
                         (reset! phat-playtime 0.0)
                         (reset! phat-start-timestamp (now)))
                       (ctl phat :start-point 0 :bar-trig 0 :vol 0 :loop?))))

  (bind :left :1x0 (fn [lp] (start-at groove (start-point-for 0 groove-s) groove-s groove-start-timestamp)))
  (bind :left :1x1 (fn [lp] (start-at groove (start-point-for 1 groove-s) groove-s groove-start-timestamp)))
  (bind :left :1x2 (fn [lp] (start-at groove (start-point-for 2 groove-s) groove-s groove-start-timestamp)))
  (bind :left :1x3 (fn [lp] (start-at groove (start-point-for 3 groove-s) groove-s groove-start-timestamp)))
  (bind :left :1x4 (fn [lp] (start-at groove (start-point-for 4 groove-s) groove-s groove-start-timestamp)))
  (bind :left :1x5 (fn [lp] (start-at groove (start-point-for 5 groove-s) groove-s groove-start-timestamp)))
  (bind :left :1x6 (fn [lp] (start-at groove (start-point-for 6 groove-s) groove-s groove-start-timestamp)))
  (bind :left :1x7 (fn [lp] (start-at groove (start-point-for 7 groove-s) groove-s groove-start-timestamp)))

  (bind :left :pan (fn [lp]
                     (if (state-maps/command-right-active? (:state lp) 1)
                       (do
                         (ctl groove :start-point 0 :vol 1 :bar-trig 1)
                         (reset! groove-playtime 0.0)
                         (reset! groove-start-timestamp (now)))
                       (ctl groove :start-point 0 :bar-trig 0 :vol 0))))

  (defonce time-pool (at-at/mk-pool))
  (def event-loop (at-at/every 100
                               #(when (state-maps/active-mode? (:state lp) :left)

                                  (println :ON)

                                  (when (state-maps/command-right-active? (:state lp) 0)
                                    (reset! phat-playtime (play-position phat-start-timestamp phat-s)))

                                  (when (state-maps/command-right-active? (:state lp) 1)
                                    (reset! groove-playtime (play-position groove-start-timestamp groove-s))))
                               time-pool))
  ;;(kill event-loop)
  ;;(kill x)
  ;;(stop)
  )

;;Use LED row sequences to indicate when beats strike
(do
  (require '[launchpad.state-maps :as state-maps])
  (require '[launchpad.device :as device])
  (require '[launchpad.core :as c])
  (require '[overtone.synth.timing :as timing])
  (use '[overtone.helpers.lib :only [uuid]])
  (use 'launchpad.sequencer)

  (defonce count-trig-id (trig-id))

  (defonce root-trg-bus (control-bus)) ;; global metronome pulse
  (defonce root-cnt-bus (control-bus)) ;; global metronome count
  (defonce beat-trg-bus (control-bus)) ;; beat pulse (fraction of root)
  (defonce beat-cnt-bus (control-bus)) ;; beat count

  (def BEAT-FRACTION "Number of global pulses per beat" 30)
  (def current-beat (atom BEAT-FRACTION))

  (def r-cnt (timing/counter :in-bus root-trg-bus :out-bus root-cnt-bus))
  (def r-trg (timing/trigger :rate 100 :in-bus root-trg-bus))
  (def b-cnt (timing/counter :in-bus beat-trg-bus :out-bus beat-cnt-bus))
  (def b-trg (timing/divider :div BEAT-FRACTION :in-bus root-trg-bus :out-bus beat-trg-bus))

  ;;Sending out beat event
  (defsynth get-beat [] (send-trig (in:kr beat-trg-bus) count-trig-id (+ (in:kr beat-cnt-bus) 1)))

  (def kick-s     (sample (freesound-path 777)))
  (def click-s    (sample (freesound-path 406)))
  (def boom-s     (sample (freesound-path 33637)))
  (def subby-s    (sample (freesound-path 25649)))
  (def choir-s    (sample (freesound-path 172323)))
  (def godzilla-s (sample (freesound-path 206078)))
  (def snow-running-s (sample (freesound-path 160605)))
  2086
  (def all-samples [kick-s click-s boom-s subby-s choir-s godzilla-s snow-running-s])

  (def lp-sequencer (mk-sequencer "launchpad-sequencer" all-samples 8 beat-cnt-bus beat-trg-bus 0))

  (def refresh-beat-key (uuid))
  (def beat-rep-key (uuid))

  (defonce get-beat-s (get-beat))

  ;; Use 7x7 cell to display beat
  (on-trigger count-trig-id
    (fn [beat]
      (let [lp (first c/launchpad-kons)
            brightness (mod beat 4)]
        (device/led-on  lp [7 7] brightness :yellow)))
    beat-rep-key)

  ;; Think of this as the event loop for the grid, triggered on a beat
  (on-trigger count-trig-id
    (fn [beat]
      (when (state-maps/active-mode? (:state lp) :up)

        (let [current-row (mod (dec beat) 8)
              last-row (mod (- beat 2) 8)
              lp (first c/launchpad-kons)
              col (state-maps/column (:state lp) current-row)
              last-col (state-maps/column (:state lp) last-row)]

          ;;Refresh new patterns just before beat 0
          ;;Ensures new patterns start on beat
          (when (= current-row 7.0)
            (doseq [idx (range 6)]
              (when (state-maps/command-right-active? (:state lp) idx)
                (sequencer-write! lp-sequencer idx (state-maps/grid-row (:state lp) idx)))))

          (doseq [r (range 0 8)]
            (when (state-maps/command-right-active? (:state lp) r)
              (if (= 1 (nth last-col r))
                (device/led-on lp [r last-row] 1 :green)
                (device/led-off lp [r last-row]))

              (if (= (nth col r) 1)
                (device/led-on lp  [r current-row] 3 :green)
                (device/led-on lp  [r current-row] 1 :yellow)))))))
    refresh-beat-key)

  (defn toggle-row [lp idx]
    (when-not (state-maps/command-right-active? (:state lp) idx)
      (reset-pattern! lp-sequencer idx)
      (device/render-row lp idx)))

  (bind :up :vol   (fn [lp] (toggle-row lp 0)))
  (bind :up :pan   (fn [lp] (toggle-row lp 1)))
  (bind :up :snda  (fn [lp] (toggle-row lp 2)))
  (bind :up :sndb  (fn [lp] (toggle-row lp 3)))
  (bind :up :stop  (fn [lp] (toggle-row lp 4)))
  (bind :up :trkon (fn [lp] (toggle-row lp 5)))
  (bind :up :solo  (fn [lp] (toggle-row lp 6)))

  ;;Adjust bpm
  (bind :up :7x6 (fn [] (ctl b-trg :div (swap! current-beat inc))))
  (bind :up :7x5 (fn [] (ctl b-trg :div (swap! current-beat dec))))

  ;;Shutdown
  (bind :up :arm  (fn [lp]
                    (reset-all-patterns! lp-sequencer)
                    (state-maps/column-off (:state lp) 8)
                    (device/command-right-leds-all-off lp)
                    (device/render-grid lp))))

;;Use LED row sequences to indicate when beats should strike (without forced beat sync)
(do
  (require '[launchpad.grid :as grid])
  (require '[launchpad.state-maps :as state-maps])
  (use 'overtone.inst.drum)

  (def m (metronome 240))

  (defn looper
    ([nome sound state] (looper nome sound state 0))
    ([nome sound state p]
       (let [p (if (> p 7) 0 p)
             beat (nome)]
         (at (nome beat) (do (when (nth state p) (sound))))
         (apply-at (nome (inc beat)) looper [nome sound state (inc p)]))))

  (defn fire-sequence [lp fun row]
    (looper m fun (map #(if (= % 1) true false) (state-maps/row (:state lp) row))))

  (bind :down :vol  (fn [lp] (fire-sequence lp kick 0)))
  (bind :down :pan  (fn [lp] (fire-sequence lp snare 1)))
  (bind :down :snda (fn [lp] (fire-sequence lp clap 2)))
  (bind :down :arm  (fn [lp] (stop))))

(comment
  (do
    (use '[launchpad.core] :reload)
    (require '[launchpad.device :as device])
    (stop)
    (device/reset-launchpad (first launchpad-connected-receivers))))
