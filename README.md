# Launchpad

[![Build Status](https://travis-ci.org/josephwilk/overtone.device.launchpad.png)](https://travis-ci.org/josephwilk/overtone.device.launchpad)

Experimenting with ways of interacting a Launchpad with Overtone and Clojure.

![Launchpad S](http://s10.postimg.org/mj3szi1i1/launchpad_s.jpg)

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

  (bind :up :vol (fn [launchpad]
                   (looper m dance-kick (map #(if (= % 1) true false)
                                        (grid/row (state-maps/active-grid (:state launchpad)) 0)))))

  (bind :up :arm (fn [launchpad] (stop))))

```

## Todos

So many things to do. Contribute, ideas and code.

https://github.com/josephwilk/overtone.device.launchpad/issues?labels=enhancement&page=1&state=open

## License

Copyright © 2013 Joseph Wilk

Distributed under the Eclipse Public License, the same as Clojure.
