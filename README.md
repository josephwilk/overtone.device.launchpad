# Launchpad

[![Build Status](https://travis-ci.org/josephwilk/overtone.device.launchpad.png)](https://travis-ci.org/josephwilk/overtone.device.launchpad)

Experimenting with ways of interacting a Launchpad with Overtone and Clojure.

![Launchpad S](http://s10.postimg.org/mj3szi1i1/launchpad_s.jpg)

## Goal

Provide defaults to get you moving quickly and start exploring while providing the scope later to completely customising the launchpad to your musical desires.

Support for interacting with intelligent machines. One day.

## Usage

The default (setup by launchpad.core):

* left, right, up and down
  * Enter mode where each grid remembers keys pressed.
  * Grid buttons simply toggle state/light on/off.
* user1, user1:
  * Enter mode where there is no persistent grid state.
  * Grid buttons trigger led/fn key-down and off with key-up

Binding demos: https://github.com/josephwilk/overtone.device.launchpad/blob/master/docs/demo.cl

## Todos

So many things to do. Contribute, ideas and code.

https://github.com/josephwilk/overtone.device.launchpad/issues?labels=enhancement&page=1&state=open

## Demos

* http://www.youtube.com/watch?v=xTf2pTbjU-Y

## Launchpad documentation

* http://d19ulaff0trnck.cloudfront.net/sites/default/files/novation/downloads/4700/launchpad-s-prm.pdf

## License

Copyright © 2013 Joseph Wilk

Distributed under the Eclipse Public License, the same as Clojure.
