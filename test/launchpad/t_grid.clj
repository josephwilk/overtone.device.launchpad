(ns launchpad.t-grid
  (:require [midje.sweet :refer :all]
            [launchpad.grid :as grid]))

(def empty-8x8-grid
  [[0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]])

(facts "changing a grid with a index position"
  (fact "toogle respects current index position"
    (grid/toggle [0 0] empty-8x8-grid 1 1) => [[0 0 0 0 0 0 0 0]
                                               [0 1 0 0 0 0 0 0]
                                               [0 0 0 0 0 0 0 0]
                                               [0 0 0 0 0 0 0 0]
                                               [0 0 0 0 0 0 0 0]
                                               [0 0 0 0 0 0 0 0]
                                               [0 0 0 0 0 0 0 0]
                                               [0 0 0 0 0 0 0 0]]

    (grid/toggle [1 0] [[0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                        [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                        [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                        [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                        [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                        [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                        [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                        [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                        [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]] 1 1) =>

                        [[0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                         [0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0]
                         [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                         [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                         [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                         [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                         [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                         [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
                         [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]]))

(fact "it toggles the state of a cell"
  (grid/toggle [0 0] [[0 0 0]
                      [0 0 0]
                      [0 0 0]] 1 1) => [[0 0 0]
                                        [0 1 0]
                                        [0 0 0]]

  (grid/toggle [0 0] [[0 0 0]
                      [0 1 0]
                      [0 0 0]] 1 1) => [[0 0 0]
                                        [0 0 0]
                                        [0 0 0]])

(fact "set"
  (grid/set [[0 0 0]
             [0 0 0]
             [0 0 0]] 2 2 1) => [[0 0 0]
                                 [0 0 0]
                                 [0 0 1]])

(facts "out of bound cordinates"
  (fact "absolute-column returns all 0s"
        (grid/absolute-column [0 0]
                              [[0 0 0 0 0 0 0 0 0]
                               [0 0 0 0 0 0 0 0 0]]
                               9) => [0 0 0 0 0 0 0 0])

  (fact "col returns all 0s"
    (grid/col [[0 0 1]
               [0 0 0]
               [0 0 1]] 2) => [1 0 1]

    (grid/col [[0 0 0]
               [0 0 0
               [0 0 0]]] 4) => [0 0 0 0 0 0 0 0]))


(facts "shift-left"
  (fact "adds 8 new cells to every row"
    (grid/shift-left empty-8x8-grid) =>  (n-of [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0] 8)))

(facts "shift-down"
  (fact "adds 8 new rows"
    (grid/shift-down empty-8x8-grid) => (n-of [0 0 0 0 0 0 0 0] 16)))
