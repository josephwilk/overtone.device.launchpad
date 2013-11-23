# Launchpad

[![Build Status](https://travis-ci.org/josephwilk/overtone.device.launchpad.png)](https://travis-ci.org/josephwilk/overtone.device.launchpad)

A framework for interacting a Launchpad with Overtone through Clojure.

![Launchpad S](http://s24.postimg.org/tn4w9d7j5/687474703a2f2f7331302e706f7374696d672e6f72672f6d.jpg)

## Clojars

Adding Launchpad as a dependency in your project:

https://clojars.org/launchpad

## Usage

Launchpad works through an evented system. While you can do anything with those events `launchpad.core` setups some
useful defaults:

* All top control buttons except user1/2
  * Enter mode where each grid remembers keys pressed.
  * Grid buttons simply toggle state/light on/off.
* user1 & user2:
  * Enter mode where there is no persistent grid state.
  * Grid buttons trigger led/fn key-down and off with key-up

### Simple example

```clojure
(use 'overtone.live)
(use 'overtone.inst.drum)
(use 'launchpad.core)

(boot!)

(bind :up :0x0 #(kick))
(bind :up :0x1 #(hat3))
```

## Plugins

### Mapping samples to a row

Map each sample to a row where each button forces playback of the sample to a specific timepoint. LED tracks current point in sample.

```clojure
(use 'overtone.live)
(use 'launchpad.core)
(use 'launchpad.sequencer)
(use 'launchpad.plugin.sample-rows)

(boot!)

(def lp (first launchpad-kons))
(def phat-s (sample (freesound-path 48489)))
(def phat (skipping-sequencer :buf (to-sc-id phat-s)
                              :loop? true
                              :bar-trg 0
                              :out-bus 0
                              :vol 0))

(def phat-row {:playtime (atom 0)
               :start (atom nil)
               :row 0
               :sample phat-s
               :sequencer phat})

(sample-rows lp :left [phat-row])
```

### Demo Examples

* Expressing drum/sample positions using LEDS (automatically synching to the beat).
* Adding a metronome cell which flashes with beat
* Turning a grid into an instruments

Explore the code here: https://github.com/josephwilk/overtone.device.launchpad/blob/master/docs/demo.clj

Or if you want to try from the repl

```
lein repl
(load-file "docs/demo.clj")
```

## Overtone and Launchpad in action

* http://youtu.be/0On-UHcK4s0
* http://youtu.be/BFZ0vo4QUA0

## Launchpad internal documentation

* http://d19ulaff0trnck.cloudfront.net/sites/default/files/novation/downloads/4700/launchpad-s-prm.pdf

## License

Copyright © 2013 Joseph Wilk

Distributed under the Eclipse Public License, the same as Clojure.
