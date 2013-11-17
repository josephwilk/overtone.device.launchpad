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

(bind :up   :0x0 #(kick))
(bind :down :0x0 #(hat3))
```

### Plugins (Much more interesting stuff)

* Expressing drum/sample positions using LEDS (automatically synching to the beat). 
* Adding a metronome cell which flashes with beat
* Mapping sample to a row, each button forces a jump to a timepoint (like MLR)
* Turning a grid into an instruments

Explore the code here: https://github.com/josephwilk/overtone.device.launchpad/blob/master/docs/demo.clj

Or if you want to try from the repl

```
lein repl
(load-file "docs/demo.clj")
```

## See Overtone and Launchpad in actions:

* http://youtu.be/BFZ0vo4QUA0

## Launchpad internal documentation

* http://d19ulaff0trnck.cloudfront.net/sites/default/files/novation/downloads/4700/launchpad-s-prm.pdf

## License

Copyright © 2013 Joseph Wilk

Distributed under the Eclipse Public License, the same as Clojure.
