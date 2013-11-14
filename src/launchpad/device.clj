(ns launchpad.device
  (:use
   launchpad.util)
  (:require
   [overtone.studio.midi :refer :all]
   [overtone.libs.event :refer :all]

   [launchpad.state-maps :as state-maps]
   [launchpad.grid :as grid]))

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
  [(range 0 9)
   (range 16 25)
   (range 32 41)
   (range 48 57)
   (range 64 73)
   (range 80 89)
   (range 96 105)
   (range 112 121)])

(defn- coordinate->note [x y] (grid/cell grid-notes x y))

(def launchpad-config
  {:name "Launchpad S"
   :interfaces {:grid-controls
                {:controls
                 {:up      {:note 104 :type :control-change}
                  :down    {:note 105 :type :control-change}
                  :left    {:note 106 :type :control-change}
                  :right   {:note 107 :type :control-change}
                  :session {:note 108 :type :control-change}
                  :user1   {:note 109 :type :control-change}
                  :user2   {:note 110 :type :control-change}
                  :mixer   {:note 111 :type :control-change}}

                 :side-controls
                 {:vol     {:note 8   :type :note-on :row 0}
                  :pan     {:note 24  :type :note-on :row 1}
                  :snda    {:note 40  :type :note-on :row 2}
                  :sndb    {:note 56  :type :note-on :row 3}
                  :stop    {:note 72  :type :note-on :row 4}
                  :trkon   {:note 88  :type :note-on :row 5}
                  :solo    {:note 104 :type :note-on :row 6}
                  :arm     {:note 120 :type :note-on :row 7}}}

                :leds {:name "LEDs"
                       :type :midi-out
                       :midi-handle "Launchpad S"
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

(defn side->row [name] (-> launchpad-config :interfaces :grid-controls :side-controls name :row))

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
    {:note (apply coordinate->note id) :fn (-> launchpad-config :interfaces :leds :grid :fn)}
    (-> launchpad-config :interfaces :leds :controls id)))

(defn- led-off*
  [rcvr id]
  (when-let [{led-id :note midi-fn :fn} (led-details id)]
    (midi-fn rcvr led-id off)))

(defn led-off
  [launchpad id]
  (let [rcvr (-> launchpad :rcv)]
    (led-off* rcvr id)))

(defn- led-on*
  ([rcvr id brightness color]
     (when-let [{led-id :note midi-fn :fn} (led-details id)]
       (midi-fn rcvr led-id (velocity {:color color
                                       :intensity brightness})))))

(defn led-on
  ([launchpad id] (led-on launchpad id full-brightness :red))
  ([launchpad id brightness color]
      (let [rcvr (-> launchpad :rcv)]
        (led-on* rcvr id brightness color))))

(defn render-grid [launchpad grid]
  (doseq [[x row] (map vector (iterate inc 0) grid)]
    (doseq [[y col] (map vector (iterate inc 0) row)]
      (if (= 1 col)
        (led-on launchpad [x y])
        (led-off launchpad [x y])))))

(defn reset-launchpad [rcvr] (midi-control rcvr 0 0))

(defn intromation [rcvr]
  (reset-launchpad rcvr)
  (doall
   (pmap (fn [col]
           (let [refresh (+ 50 (rand-int 50))
                 start-lag (rand-int 1000)]
             (Thread/sleep start-lag)
             (doseq [intensity (range 1 4)]
               (doseq [row (range 0 8)]
                 (led-on* rcvr [row col] intensity :red)
                 (Thread/sleep (- refresh row))))))
         (range 0 8)))
  (midi-control rcvr all-lights 127)
  (Thread/sleep 400)
  (doseq [row (reverse (range 0 8))]
    (doseq [col (reverse (range 0 8))]
      (led-off* rcvr [row col]))
    (Thread/sleep 50))
  (reset-launchpad rcvr))

(defn stateful-launchpad
  [device]
  (let [interfaces (-> launchpad-config :interfaces)
        state      (atom (state-maps/empty))
        device-key (midi-full-device-key device)]
    {:dev        device
     :interfaces interfaces
     :state      state
     :type       ::stateful-launchpad}))

(defn- grid-event-handler [launchpad x y]
  (let [state (:state launchpad)]
    (fn [{:keys [data2-f]}]
      (when-let [trigger-fn (state-maps/trigger-fn state x y)]
        (if (= 0 (arg-count trigger-fn))
          (trigger-fn)
          (trigger-fn launchpad)))
      (if (some #{(state-maps/mode state)} [:user1 :user2])
        (led-on launchpad [x y])
        (let [new-state (state-maps/toggle! state x y)]
          (if (state-maps/on? new-state x y)
            (led-on launchpad [x y])
            (led-off launchpad [x y])))))))

(defn- side-event-handler [launchpad name]
  (let [state (:state launchpad)]
    (fn [{:keys [data2-f]}]
      (state-maps/toggle-side! state (side->row name))
      (when-let [trigger-fn (state-maps/trigger-fn state name)]
        (if (= 0 (arg-count trigger-fn))
          (trigger-fn)
          (trigger-fn launchpad))))))

(defn- register-event-handlers-for-launchpad
  [device rcv idx]
  (let [launchpad  (map->Launchpad (assoc device :rcv rcv))
        interfaces (:interfaces device)
        device-key (midi-full-device-key (:dev device))
        device-num (midi-device-num      (:dev device))
        state      (:state device)]

    ;;Grid events
    (doseq [[x row] (map vector (iterate inc 0) (grid/project-8x8 grid-notes))
            [y note] (map vector (iterate inc 0) row)]
      (let [type      :note-on
            note      note
            handle    (concat device-key [type note])
            update-fn (grid-event-handler launchpad x y)]
        (println :handle handle)
        (on-event handle update-fn (str "update-state-for" handle))

        (on-event (concat device-key [:note-off note])
                  (fn [_] (when (some #{(state-maps/mode state)} [:user1 :user2])
                           (led-off launchpad [x y])))
                  (str "update-led-for" [:note-off note]))))

    ;;Side events
    (doseq [[k v] (-> launchpad-config :interfaces :grid-controls :side-controls)]
      (let [type      (:type v)
            note      (:note v)
            row       (:row v)
            handle    (concat device-key [type note])
            update-fn (side-event-handler launchpad k)]
        (println :handle handle)
        (on-event handle update-fn (str "update-state-for" handle))))

    ;;Control events
    (doseq [[k v] (-> launchpad-config :interfaces :grid-controls :controls)]
      (let [type      (:type v)
            note      (:note v)
            handle    (concat device-key [type note])
            update-fn (fn [{:keys [data2-f]}]
                        (event [:Launchpad :control idx k]
                               :val data2-f
                               :id k
                               :launchpad launchpad
                               :idx idx))]
        (println :handle handle)
        (on-event handle update-fn (str "update-state-for" handle))))
    launchpad))

(defn merge-launchpad-kons
  [rcvs stateful-devs]
  (doseq [rcv rcvs]
    (intromation rcv)
    (led-on* rcv :up 1 :yellow))
  (map (fn [[stateful-dev rcv id]] (register-event-handlers-for-launchpad stateful-dev rcv id))
    (map vector stateful-devs rcvs (range))))
