(ns launchpad.t-device
  (:use midje.sweet)
  (:require [launchpad.device :as device]))

(fact "velocity maps color and intensity to a decimal signal"
  (#'device/velocity {:color :red    :intensity 3}) => 15
  (#'device/velocity {:color :yellow :intensity 3}) => 63
  (#'device/velocity {:color :green  :intensity 3}) => 60)

(fact "invalid colors return 0"
  (#'device/velocity {:color :brown :intensity 0}) => 0)

(fact "intensity is capped at 3"
  (#'device/velocity {:color :red :intensity 100000}) => 15)
