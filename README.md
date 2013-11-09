# Launchpad

[![Build Status](https://travis-ci.org/josephwilk/overtone.device.launchpad.png)](https://travis-ci.org/josephwilk/overtone.device.launchpad)

Experimenting with ways of interacting a Launchpad with Overtone and Clojure.

![Launchpad S](http://s10.postimg.org/mj3szi1i1/launchpad_s.jpg)

## Goal

Provide defaults to get you moving quickly and start exploring while providing the scope later to completely customising the launchpad to your musical desires.

Support for interacting with intelligent machines. One day.

## Usage

* left, right, up and down
  * Enter mode where each grid remembers keys pressed.
  * Grid buttons simply toggle state/light on/off.
* user1, user1:
  * Enter mode where there is no persistent grid state.
  * Grid buttons trigger led/fn key-down and off with key-up

```clojure
(use '[launchpad.core] :reload)
(use 'overtone.live)

(boot!)

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

;;Use LED row sequences to indicate when beats should strike
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

;;Use LED row sequences to indicate when beats strike (uses samples + supercollider counter)
(do
  (require '[launchpad.grid :as grid])
  (require '[launchpad.state-maps :as state-maps])
  (require '[launchpad.device :as device])
  (require '[launchpad.core :as c])
  (require '[overtone.synth.timing :as timing])
  (use '[overtone.helpers.lib :only [uuid]])

  (defonce count-trig-id (trig-id))

  (defonce buf-0 (buffer 8))
  (defonce buf-1 (buffer 8))
  (defonce buf-2 (buffer 8))
  (defonce buf-3 (buffer 8))

  (buffer-write! buf-0 [0 0 0 0 0 0 0 0])
  (buffer-write! buf-1 [0 0 0 0 0 0 0 0])
  (buffer-write! buf-2 [0 0 0 0 0 0 0 0])
  (buffer-write! buf-3 [0 0 0 0 0 0 0 0])

  (defonce root-trg-bus (control-bus)) ;; global metronome pulse
  (defonce root-cnt-bus (control-bus)) ;; global metronome count
  (defonce beat-trg-bus (control-bus)) ;; beat pulse (fraction of root)
  (defonce beat-cnt-bus (control-bus)) ;; beat count

  (def BEAT-FRACTION "Number of global pulses per beat" 30)

  (def r-cnt (timing/counter :in-bus root-trg-bus :out-bus root-cnt-bus))
  (def r-trg (timing/trigger :rate 100 :in-bus root-trg-bus))
  (def b-cnt (timing/counter :in-bus beat-trg-bus :out-bus beat-cnt-bus))
  (def b-trg (timing/divider :div BEAT-FRACTION :in-bus root-trg-bus :out-bus beat-trg-bus))

  ;;Sending out beat event
  (defsynth get-beat [] (send-trig (in:kr beat-trg-bus) count-trig-id (+ (in:kr beat-cnt-bus) 1)))

  (def kick-s  (sample (freesound-path 777)))
  (def click-s (sample (freesound-path 406)))
  (def boom-s  (sample (freesound-path 33637)))
  (def subby-s (sample (freesound-path 25649)))

  (defsynth mono-sequencer
    "Plays a single channel audio buffer."
    [buf 0 rate 1 out-bus 0 beat-num 0 sequencer 0 amp 1]
    (let [cnt      (in:kr beat-cnt-bus)
          beat-trg (in:kr beat-trg-bus)
          bar-trg  (and (buf-rd:kr 1 sequencer cnt)
                        (= beat-num (mod cnt 8))
                        beat-trg)
          vol      (set-reset-ff bar-trg)]
      (out
       out-bus (* vol
                  amp
                  (pan2
                   (rlpf
                    (scaled-play-buf 1 buf rate bar-trg)
                    (demand bar-trg 0 (dbrown 200 20000 50 INF))
                    (lin-lin:kr (lf-tri:kr 0.01) -1 1 0.1 0.9)))))))

  (def kicks (doall
              (for [x (range 8)]
                (mono-sequencer :buf kick-s :beat-num x :sequencer buf-0))))

  (def clicks (doall
               (for [x (range 8)]
                 (mono-sequencer :buf click-s :beat-num x :sequencer buf-1))))

  (def booms (doall
              (for [x (range 8)]
                (mono-sequencer :buf boom-s :beat-num x :sequencer buf-2))))

  (def subbies (doall
                (for [x (range 8)]
                  (mono-sequencer :buf subby-s :beat-num x :sequencer buf-3))))

  (defn fire-buffer-sequence [lp buf row] (buffer-write! buf (state-maps/row (:state lp) row)))

  (def key3 (uuid))

  (defonce get-beat-s (get-beat))

  (on-trigger count-trig-id
    (fn [beat]
      (let [current-row (mod (dec beat) 8)
            last-row (mod (- beat 2) 8)
            lp (first c/launchpad-kons)
            col (state-maps/column (:state lp) current-row)
            last-col (state-maps/column (:state lp) last-row)]

        (device/led-off lp [7 last-row])
        (device/led-on  lp [7 current-row] 1 :yellow)

        (doseq [r (range 0 3)]
          (if (= 1 (nth last-col r))
            (device/led-on lp [r last-row] 1 :green)
            (device/led-off lp [r last-row]))

          (if (= (nth col r) 1)
            (device/led-on  lp  [r current-row] 3 :green)
            (device/led-on  lp  [r current-row] 1 :yellow)))))
    key3)

  (bind :up :vol  (fn [lp] (fire-buffer-sequence lp buf-0 0)))
  (bind :up :pan  (fn [lp] (fire-buffer-sequence lp buf-1 1)))
  (bind :up :snda (fn [lp] (fire-buffer-sequence lp buf-2 2)))
  (bind :up :arm  (fn [lp] (buffer-write! buf-0 [0 0 0 0 0 0 0 0])
                          (buffer-write! buf-1 [0 0 0 0 0 0 0 0])
                          (buffer-write! buf-2 [0 0 0 0 0 0 0 0])
                          (buffer-write! buf-3 [0 0 0 0 0 0 0 0]))))

```

## Todos

So many things to do. Contribute, ideas and code.

https://github.com/josephwilk/overtone.device.launchpad/issues?labels=enhancement&page=1&state=open

## License

Copyright © 2013 Joseph Wilk

Distributed under the Eclipse Public License, the same as Clojure.
