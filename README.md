# Launchpad

[![Build Status](https://travis-ci.org/josephwilk/overtone.device.launchpad.png)](https://travis-ci.org/josephwilk/overtone.device.launchpad)

Experimenting with ways of interacting a Launchpad with Overtone and Clojure.

![Launchpad S](http://s10.postimg.org/mj3szi1i1/launchpad_s.jpg)

## Usage

Currently left, right, up and down select different states each of which has its own grid.
Grid buttons simply toggle on/off.

```clojure
(use 'launchpad.core)
(use 'overtone.live)
(use 'overtone.inst.drum)

(boot!)

(bind :up :0x0 kick3)
(bind :up :1x0 kick2)
(bind :up :2x0 kick1)
```

## Todos

Sooo many things to do. Contribute, ideas and code.

https://github.com/josephwilk/overtone.device.launchpad/issues?labels=enhancement&page=1&state=open


## License

Copyright © 2013 Joseph Wilk

Distributed under the Eclipse Public License, the same as Clojure.
