(ns launchpad.t-core
  (:use midje.sweet
        launchpad.device))

(fact "velocity maps color and intensity to a decimal signal"
  (velocity {:color :red    :intensity 3}) => 15
  (velocity {:color :yellow :intensity 3}) => 63
  (velocity {:color :green  :intensity 3}) => 60)

(fact "invalid colors return 0"
  (velocity {:color :brown :intensity 0}) => 0)

(fact "intensity is capped at 3"
  (velocity {:color :red :intensity 100000}) => 15)
