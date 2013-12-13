(ns launchpad.t-state-maps
  (:use midje.sweet)
  (:require [launchpad.state-maps :as state-maps]))

(facts "visible?"
  (fact "indicates if an absolute x y cell is current visible"
    (state-maps/visible? (atom {:grid-index [0 1]}) 0 0) => false
    (state-maps/visible? (atom {:grid-index [0 1]}) 0 8) => true

    (state-maps/visible? (atom {:grid-index [1 0]}) 1 0) => false
    (state-maps/visible? (atom {:grid-index [1 0]}) 9 0) => true))
