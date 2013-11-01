(ns launchpad.core
  (:require
   [overtone.studio.midi :refer :all]
   [overtone.libs.event :refer :all]))

(defonce launchpad-connected-receivers (midi-find-connected-receivers "Launchpad"))
(defonce launchpad-connected-devices   (midi-find-connected-devices "Launchpad"))

(defrecord Launchpad [rcv dev interfaces])

(def note-on  0x80)
(def note-off 0x90)
(def control  0xB0)

(def all-lights 0)

(def off               0)
(def low-brightness    1)
(def medium-brightness 2)
(def full-brightness   3)

(def led-colors [:red :green :yellow])

(def flags {:normal 12
            :flash 8
            :double-buffering 0})

(def grid-notes
  [(range 0 8)
   (range 16 24)
   (range 32 40)
   (range 48 56)
   (range 64 72)
   (range 80 88)
   (range 96 104)
   (range 112 120)])

(defn cordinate-to-note [x y] (nth (nth grid-notes x) y))

(def launchpad-config
  {:name "Launchpad S"
   :interfaces
   {:leds {:name "LEDs"
           :type :midi-out
           :midi-handle "Launchpad S"
           :control-defaults {:type :led}
           :controls {:up      {:note 104 :fn midi-control}
                      :down    {:note 105 :fn midi-control}
                      :left    {:note 106 :fn midi-control}
                      :right   {:note 107 :fn midi-control}
                      :session {:note 108 :fn midi-control}
                      :user1   {:note 109 :fn midi-control}
                      :user2   {:note 110 :fn midi-control}
                      :mixer   {:note 111 :fn midi-control}

                      :vol     {:note 8   :fn midi-note-on}
                      :pan     {:note 24  :fn midi-note-on}
                      :snda    {:note 40  :fn midi-note-on}
                      :sndb    {:note 56  :fn midi-note-on}
                      :stop    {:note 72  :fn midi-note-on}
                      :trkon   {:note 88  :fn midi-note-on}
                      :solo    {:note 104 :fn midi-note-on}
                      :arm     {:note 120 :fn midi-note-on}}
           :grid {:fn midi-note-on}}}})

(defn reset-launchpad [rcvr] (midi-control rcvr 0 0))

(defn velocity [{color :color intensity :intensity}]
  (if (some #{color} led-colors)
    (let [intensity (if (> intensity 3) 3 intensity)
          green (case color
                  :green intensity
                  :yellow intensity
                  0)
          red (case color
                :red intensity
                :yellow intensity
                0)
          mode :normal]
      (+ (* 16 green)
         red
         (mode flags)))
    0))

(defn- led-details [id]
  (if (vector? id)
    {:note (apply cordinate-to-note id) :fn (-> launchpad-config :interfaces :leds :grid :fn)}
    (-> launchpad-config :interfaces :leds :controls id)))

(defn- led-off
  [rcvr id]
  (when-let [{led-id :note midi-fn :fn} (led-details id)]
    (midi-fn rcvr led-id off)))

(defn- led-on
  ([rcvr id] (led-on rcvr id full-brightness :red))
  ([rcvr id brightness color]
     (when-let [{led-id :note midi-fn :fn} (led-details id)]
       (midi-fn rcvr led-id (velocity {:color color
                                       :intensity brightness})))))

(defn intromation [rcvr]
  (doseq [row (range 0 8)]
    (doseq [intensity (range 1 4)]
      (doseq [col (range 0 8)] (led-on rcvr [col row] intensity :red))
      (Thread/sleep 50))
    (Thread/sleep (/ 50 (+ 1 row))))
  (midi-control rcvr all-lights 127)
  (Thread/sleep 400)
  (doseq [row (reverse (range 0 8))]
    (doseq [col (reverse (range 0 8))]
      (led-off rcvr [col row]))
    (Thread/sleep 50))
  (reset-launchpad rcvr))

(comment
  (intromation (first launchpad-connected-receivers))
  (led-on  (first launchpad-connected-receivers) :snda)
  (led-off (first launchpad-connected-receivers) :snda)
  (reset-launchpad (first launchpad-connected-receivers)))
