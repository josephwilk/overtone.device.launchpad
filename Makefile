all:
	lein midje launchpad.t-device launchpad.t-grid

ci:
	sudo apt-get update
	sudo apt-get install supercollider

travis:
	lein2 midje launchpad.t-device launchpad.t-grid

