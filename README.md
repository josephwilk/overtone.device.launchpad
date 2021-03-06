# Launchpad

[![Build Status](https://travis-ci.org/josephwilk/overtone.device.launchpad.png)](https://travis-ci.org/josephwilk/overtone.device.launchpad)

A framework for interacting a Launchpad with Overtone through Clojure.

![Launchpad S](http://s24.postimg.org/tn4w9d7j5/687474703a2f2f7331302e706f7374696d672e6f72672f6d.jpg)

## Overtone & Launchpad in Action

Watch me: http://youtu.be/tZrhaTQA9Qk

## Clojars

Adding Launchpad as a dependency in your project:

https://clojars.org/launchpad

## Usage

Launchpad works through an evented system. While you can do anything with those events `launchpad.core` setups some
useful defaults:

* When :session mode is lit allows infinite scrolling left or right (for greater than 8 phrases)
* When :session mode is lit allows infinite scrolling up or down (for binding more than 8 beats/samples)
* All top control buttons except :user1 / :user2 & :session
  * Enter mode where each grid remembers keys pressed.
  * Grid buttons simply toggle state/light on/off.
* :user1 & :user2
  * Enter mode where there is no persistent grid state.
  * Grid buttons trigger led/fn key-down and off with key-up

### Simple example

```clojure
(use 'overtone.live)
(use 'overtone.inst.drum)
(use 'launchpad.core)

(boot!)

;;Bind to the main grid
(bind :up :0x0 #(kick))
(bind :up :0x1 #(hat3))

;;Bind to the virtual grid @ y=1 (i.e session mode on and down btn once)
(bind [:up 1] :0x1 #(hat3))
```

## Plugins

For usage see examples in https://github.com/josephwilk/overtone.device.launchpad/blob/master/docs/demo.clj

* Metronome 
* Mapping samples to a row
* Beat sequencing
* Beat sequencing with scroll update

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

## Launchpad internal documentation

* http://d19ulaff0trnck.cloudfront.net/sites/default/files/novation/downloads/4700/launchpad-s-prm.pdf

## License

Copyright © 2014 Joseph Wilk

Distributed under the Eclipse Public License, the same as Clojure.
