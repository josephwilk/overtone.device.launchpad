;;Demos showing all the wonderful things you can do with launchpad and overtone
;;This is a space for those ideas to grow and the good ones will be extracted out
;;and provided as plugins.
(do
  (use '[launchpad.core] :reload)
  (use 'overtone.live :reload)
  (boot!))

(def lp (first launchpad-kons))

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
  (bind :user1 :4x2 #(bing))

  ;;Notice how we can bind to different y dimensions
  (bind [:user1 1] :0x0 #(bing)))

;;Map each sample to a row where each button
;;forces playback of the sample to a specific timepoint
;; LED tracks current point in sample (No beat syncing yet)
(do
  (require '[launchpad.device :as device] :reload)
  (require '[launchpad.state-maps :as state-maps] :reload)
  (require '[launchpad.sequencer :refer :all] :reload)
  (require '[overtone.at-at :as at-at])

  (def phat-s        (sample (freesound-path 48489)))
  (def groove-s      (sample (freesound-path 48488)))
  (def funky-s       (sample (freesound-path 172549)))
  (def memory-moon-s (sample (freesound-path 27567)))
  (def retweak-s     (sample (freesound-path 25921)))

  (defsynth reverb-skipping-sequencer
    [buf 0 rate 1 out-bus 0 start-point 0 bar-trg [0 :tr] loop? 0 vol 1.0 pan 0 rot 1 room 0.5 wet 0.83 damp 0.8 mix 0.83]
    (let [p (scaled-play-buf 1 buf rate bar-trg start-point loop?)]
      (out [0 1]
           (* vol (free-verb p mix room damp)))))

  (def phat        (reverb-skipping-sequencer :buf (to-sc-id phat-s) :loop? true :bar-trg 0 :out-bus 0 :vol 0))
  (def groove      (reverb-skipping-sequencer :buf (to-sc-id groove-s) :loop? true :bar-trg 0 :out-bus 0 :vol 0))
  (def funky       (skipping-sequencer :buf (to-sc-id funky-s) :loop? true :bar-trg 0 :out-bus 0 :vol 0))
  (def memory-moon (skipping-sequencer :buf (to-sc-id memory-moon-s) :loop? true :bar-trg 0 :out-bus 0 :vol 0))
  (def retweak     (skipping-sequencer :buf (to-sc-id retweak-s) :loop? true :bar-trg 0 :out-bus 0 :vol 0))

  (def phat-row        {:playtime (atom 0) :start (atom nil) :row 0 :sample phat-s        :sequencer phat})
  (def groove-row      {:playtime (atom 0) :start (atom nil) :row 1 :sample groove-s      :sequencer groove})
  (def funky-row       {:playtime (atom 0) :start (atom nil) :row 2 :sample funky-s       :sequencer funky})
  (def memory-moon-row {:playtime (atom 0) :start (atom nil) :row 3 :sample memory-moon-s :sequencer memory-moon})
  (def retweak-row     {:playtime (atom 0) :start (atom nil) :row 4 :sample retweak-s     :sequencer retweak})

  (use 'launchpad.plugin.sample-rows :reload)
  (sample-rows lp :left [phat-row groove-row funky-row memory-moon-row retweak-row])

  ;;Playing

  (comment
    (ctl groove :out-bus 0 :vol 1.0)
    (ctl phat   :out-bus 0 :vol 1.0 :rate 1)
    (ctl groove :pan -1)
    (ctl groove :pan 1)

    (ctl groove :rate 1)

    (ctl phat :mix 1 :room 0.8 :damp 0.8)
    (ctl phat :mix 0 :room 0 :damp 0)
    (ctl groove :mix 0 :room 0 :damp 0)

    (ctl phat :room 0.8)
    (ctl phat :wet 0.0 :room 0)
    (ctl phat :rot 1))

  ;;(stop)
  ;;(stop-all)
  )

;;Metromone
;; Use :mixer cell to display beat
(do
  (require '[launchpad.plugin.metronome :as metronome])
  (require '[overtone.synth.timing :as timing])
  (use '[overtone.helpers.lib :only [uuid]])

  (defonce count-trig-id (trig-id))
  (defonce root-trg-bus (control-bus)) ;; global metronome pulse
  (defonce root-cnt-bus (control-bus)) ;; global metronome count
  (defonce beat-trg-bus (control-bus)) ;; beat pulse (fraction of root)
  (defonce beat-cnt-bus (control-bus)) ;; beat count
  (def BEAT-FRACTION "Number of global pulses per beat" 30)
  (def current-beat (atom BEAT-FRACTION))
  (defonce r-cnt (timing/counter :in-bus root-trg-bus :out-bus root-cnt-bus))
  (defonce r-trg (timing/trigger :rate 100 :in-bus root-trg-bus))
  (defonce b-cnt (timing/counter :in-bus beat-trg-bus :out-bus beat-cnt-bus))
  (defonce b-trg (timing/divider :div BEAT-FRACTION :in-bus root-trg-bus :out-bus beat-trg-bus))

  ;;Sending out beat event
  (defsynth get-beat [] (send-trig (in:kr beat-trg-bus) count-trig-id (+ (in:kr beat-cnt-bus) 1)))

  (defonce beat-rep-key (uuid))
  (defonce get-beat-s (get-beat))

  (metronome/start lp :mixer count-trig-id beat-rep-key))

;;Use LED row sequences to indicate when beats strike
(do
  (require '[launchpad.plugin.beat :as beat])
  (require '[launchpad.plugin.beat-scroll :as beat-scroll])
  (require '[overtone.synth.timing :as timing])
  (use '[overtone.helpers.lib :only [uuid]])
  (use 'launchpad.sequencer)

  (def phrase-size 16)

  (defonce count-trig-id (trig-id))
  (defonce root-trg-bus (control-bus)) ;; global metronome pulse
  (defonce root-cnt-bus (control-bus)) ;; global metronome count
  (defonce beat-trg-bus (control-bus)) ;; beat pulse (fraction of root)
  (defonce beat-cnt-bus (control-bus)) ;; beat count
  (def BEAT-FRACTION "Number of global pulses per beat" 30)
  (def current-beat (atom BEAT-FRACTION))
  (defonce r-cnt (timing/counter :in-bus root-trg-bus :out-bus root-cnt-bus))
  (defonce r-trg (timing/trigger :rate 100 :in-bus root-trg-bus))
  (defonce b-cnt (timing/counter :in-bus beat-trg-bus :out-bus beat-cnt-bus))
  (defonce b-trg (timing/divider :div BEAT-FRACTION :in-bus root-trg-bus :out-bus beat-trg-bus))
  (defsynth get-beat [] (send-trig (in:kr beat-trg-bus) count-trig-id (+ (in:kr beat-cnt-bus) 1)))

  (def tom-electro-s       (sample (freesound-path 108001)))
  (def sizzling-high-hat-s (sample (freesound-path 44859)))
  (def kick-s              (sample (freesound-path 777)))
  (def hip-hop-kick-s      (sample (freesound-path 131336)))
  (def clap-s              (sample (freesound-path 24786)))
  (def bell-s              (sample (freesound-path 173000)))
  (def snare-s             (sample (freesound-path 100397)))

  (def click-s    (sample (freesound-path 406)))
  (def boom-s     (sample (freesound-path 33637)))
  (def subby-s    (sample (freesound-path 25649)))
  (def choir-s    (sample (freesound-path 172323)))
  (def godzilla-s (sample (freesound-path 206078)))
  (def outiuty-s  (sample (freesound-path 55086)))

  (def samples-set-2 [kick-s click-s boom-s subby-s choir-s godzilla-s outiuty-s
                      tom-electro-s sizzling-high-hat-s hip-hop-kick-s clap-s bell-s snare-s])

  (def big-lp-sequencer (mk-sequencer "big-launchpad-sequencer" samples-set-2 phrase-size beat-cnt-bus beat-trg-bus 0))

  (defonce big-refresh-beat-key (uuid))

  ;; Think of this as the event loop for the grid, triggered on a beat
  (on-trigger count-trig-id (beat-scroll/grid-refresh lp big-lp-sequencer phrase-size :up) big-refresh-beat-key)
  ;;(on-trigger count-trig-id (beat/grid-refresh lp lp-sequencer phrase-size) refresh-beat-key)

  (beat/setup-side-controls :up big-lp-sequencer)

  ;;Adjust bpm
  (bind :up :7x7 (fn [] (ctl b-trg :div (swap! current-beat inc))))
  (bind :up :7x6 (fn [] (ctl b-trg :div (swap! current-beat dec))))

  ;;Swap samples bound to rows
  (bind :up :7x5 (fn [] (swap-samples! big-lp-sequencer samples-set-2)))

  ;;Shutdown
  (bind :up :arm  (fn [lp] (beat/off lp big-lp-sequencer))))

(do
  (require '[launchpad.plugin.beat :as beat])
  (require '[launchpad.plugin.beat-scroll :as beat-scroll])
  (require '[overtone.synth.timing :as timing])
  (use '[overtone.helpers.lib :only [uuid]])
  (use 'launchpad.sequencer)

  (def phrase-size 16)

  (defonce count-trig-id (trig-id))
  (defonce root-trg-bus (control-bus)) ;; global metronome pulse
  (defonce root-cnt-bus (control-bus)) ;; global metronome count
  (defonce beat-trg-bus (control-bus)) ;; beat pulse (fraction of root)
  (defonce beat-cnt-bus (control-bus)) ;; beat count
  (def BEAT-FRACTION "Number of global pulses per beat" 30)
  (def current-beat (atom BEAT-FRACTION))
  (defonce r-cnt (timing/counter :in-bus root-trg-bus :out-bus root-cnt-bus))
  (defonce r-trg (timing/trigger :rate 100 :in-bus root-trg-bus))
  (defonce b-cnt (timing/counter :in-bus beat-trg-bus :out-bus beat-cnt-bus))
  (defonce b-trg (timing/divider :div BEAT-FRACTION :in-bus root-trg-bus :out-bus beat-trg-bus))
  (defsynth get-beat [] (send-trig (in:kr beat-trg-bus) count-trig-id (+ (in:kr beat-cnt-bus) 1)))

  (def tom-electro-s       (sample (freesound-path 108001)))
  (def sizzling-high-hat-s (sample (freesound-path 44859)))
  (def kick-s              (sample (freesound-path 777)))
  (def hip-hop-kick-s      (sample (freesound-path 131336)))
  (def clap-s              (sample (freesound-path 24786)))
  (def bell-s              (sample (freesound-path 173000)))
  (def snare-s             (sample (freesound-path 100397)))

  (def click-s    (sample (freesound-path 406)))
  (def boom-s     (sample (freesound-path 33637)))
  (def subby-s    (sample (freesound-path 25649)))
  (def choir-s    (sample (freesound-path 172323)))
  (def godzilla-s (sample (freesound-path 206078)))
  (def outiuty-s  (sample (freesound-path 55086)))

  (def samples-set-1 [kick-s click-s boom-s subby-s choir-s godzilla-s outiuty-s])
  (def samples-set-2 [tom-electro-s sizzling-high-hat-s hip-hop-kick-s clap-s bell-s snare-s])

  (def lp-sequencer (mk-sequencer "launchpad-sequencer" samples-set-2 phrase-size beat-cnt-bus beat-trg-bus 0))

  (defonce refresh-beat-key (uuid))

  ;; Think of this as the event loop for the grid, triggered on a beat
  ;;(on-trigger count-trig-id (beat-scroll/grid-refresh lp lp-sequencer phrase-size) refresh-beat-key)
  (on-trigger count-trig-id (beat/grid-refresh-fn lp lp-sequencer phrase-size :right) refresh-beat-key)

  (beat/setup-side-controls :right lp-sequencer)

  ;;Adjust bpm
  (bind :right :7x7 (fn [] (ctl b-trg :div (swap! current-beat inc))))
  (bind :right :7x6 (fn [] (ctl b-trg :div (swap! current-beat dec))))

  ;;Swap samples bound to rows
  (bind :right :7x5 (fn [] (swap-samples! lp-sequencer samples-set-1)))
  (bind :right :7x4 (fn [] (swap-samples! lp-sequencer samples-set-2)))

  ;;Shutdown
  (bind :right :arm  (fn [lp] (beat/off lp lp-sequencer))))

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
