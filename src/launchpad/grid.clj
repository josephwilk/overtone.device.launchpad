(ns launchpad.grid)

(defn new []
  ;;Random for visual testing of changing grid
  (map
   (fn [row] (map (fn [_] (rand-int 2)) row))
   [[0 0 0 0 0 0 0 0]
    [0 0 0 0 0 0 0 0]
    [0 0 0 0 0 0 0 0]
    [0 0 0 0 0 0 0 0]
    [0 0 0 0 0 0 0 0]
    [0 0 0 0 0 0 0 0]
    [0 0 0 0 0 0 0 0]
    [0 0 0 0 0 0 0 0]]))
