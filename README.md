# Launchpad

[![Build Status](https://travis-ci.org/josephwilk/overtone.device.launchpad.png)](https://travis-ci.org/josephwilk/overtone.device.launchpad)

Experimenting with ways of interacting a Launchpad with Overtone and Clojure.

![Launchpad S](http://s10.postimg.org/mj3szi1i1/launchpad_s.jpg)

## Goal

Provide defaults to get you moving quickly and start exploring while providing the scope later to completely customising the launchpad to your musical desires.

Support for interacting with intelligent machines. One day.

## Clojars

Adding Launchpad as a dependency in your project:

https://clojars.org/launchpad

## Usage

launchpad.core sets up some useful defaults to make your device useful:

* All top control buttons except user1/2
  * Enter mode where each grid remembers keys pressed.
  * Grid buttons simply toggle state/light on/off.
* user1 & user2:
  * Enter mode where there is no persistent grid state.
  * Grid buttons trigger led/fn key-down and off with key-up

Some examples of how you can use the grid to bind interesting things:

Binding demos: https://github.com/josephwilk/overtone.device.launchpad/blob/master/docs/demo.clj

If you want to try from the repl

```
lein repl
(load-file "docs/demo.clj")
```

## Todos

So many things to do. Contribute, ideas and code.

https://github.com/josephwilk/overtone.device.launchpad/issues?labels=enhancement&page=1&state=open

## Demos

* http://youtu.be/j9eq9KKof9k
* http://www.youtube.com/watch?v=xTf2pTbjU-Y

## Launchpad documentation

* http://d19ulaff0trnck.cloudfront.net/sites/default/files/novation/downloads/4700/launchpad-s-prm.pdf

## License

Copyright © 2013 Joseph Wilk

Distributed under the Eclipse Public License, the same as Clojure.
