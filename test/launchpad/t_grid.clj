(ns launchpad.t-grid
  (:require [midje.sweet :refer :all]
            [launchpad.grid :as grid]))

(fact "it toggles the state of a cell"
  (grid/toggle [[0 0 0]
                [0 0 0]
                [0 0 0]] 1 1) => [[0 0 0]
                                  [0 1 0]
                                  [0 0 0]])
