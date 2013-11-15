(ns launchpad.t-grid
  (:require [midje.sweet :refer :all]
            [launchpad.grid :as grid]))

(fact "it toggles the state of a cell"
  (grid/toggle [[0 0 0]
                [0 0 0]
                [0 0 0]] 1 1) => [[0 0 0]
                                  [0 1 0]
                                  [0 0 0]])

(fact "set"
      (grid/set [[0 0 0]
                 [0 0 0]
                 [0 0 0]] 2 2 1) => [[0 0 0]
                                     [0 0 0]
                                     [0 0 1]])
