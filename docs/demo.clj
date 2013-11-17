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

;;For a single sample map it to a row where each button
;;forces playback of the sample to a specific timepoint
(do
  (def harp-s (sample (freesound-path 27130)))
  (def harp-duration (* 46.8  (* 1000 (:duration harp-s))))

  (defsynth skipping-sequencer
    "Supports looping and jumping position"
    [buf 0 rate 1 out-bus 0 start-point 0 bar-trg [0 :tr] loop? 0 vol 1.0]
    (out out-bus (* vol (scaled-play-buf 1 buf rate bar-trg start-point loop?))))

  (def harp (skipping-sequencer :buf (to-sc-id harp-s)
                                 :loop? false
                                 :bar-trg 0
                                 :out-bus 0))


(defn start-at [player time]
  (ctl player :start-point time)
  (ctl player :bar-trg 1))

(bind :left :0x0 (fn [lp] (start-at harp (* 0 (/ harp-duration 8)))))
(bind :left :0x1 (fn [lp] (start-at harp (* 1 (/ harp-duration 8)))))
(bind :left :0x2 (fn [lp] (start-at harp (* 2 (/ harp-duration 8)))))
(bind :left :0x3 (fn [lp] (start-at harp (* 3 (/ harp-duration 8)))))
(bind :left :0x4 (fn [lp] (start-at harp (* 4 (/ harp-duration 8)))))
(bind :left :0x5 (fn [lp] (start-at harp (* 5 (/ harp-duration 8)))))
(bind :left :0x6 (fn [lp] (start-at harp (* 6 (/ harp-duration 8)))))
(bind :left :0x7 (fn [lp] (start-at harp (* 7 (/ harp-duration 8)))))
(bind :left :vol (fn [lp] (start-at harp (* 8 (/ harp-duration 8)))))

;(kill x)
;(stop)
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
              (device/led-on lp  [r current-row] 1 :yellow))))))
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
