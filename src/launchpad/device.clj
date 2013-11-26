(ns launchpad.device
  (:use
   launchpad.util)
  (:require
   [overtone.studio.midi :refer :all]
   [overtone.libs.event :refer :all]

   [launchpad.state-maps :as state-maps]
   [launchpad.grid :as grid]))

(defrecord Launchpad [rcv dev interfaces state])

(def note-on  0x80)
(def note-off 0x90)
(def control  0xB0)

(def all-lights 0)

(def off               0)
(def low-brightness    1)
(def medium-brightness 2)
(def full-brightness   3)

(def led-colors [:red :green :yellow :orange :amber])

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

(def modes (-> launchpad-config :interfaces :grid-controls :controls keys))
(def side-controls [:vol :pan :snda :sndb :stop :trkon :solo :arm])

(defn side->row [name] (-> launchpad-config :interfaces :grid-controls :side-controls name :row))

(defn- velocity [{color :color intensity :intensity}]
  (if (some #{color} led-colors)
    (let [intensity (if (> intensity 3) 3 intensity)
          green (case color
                  :green intensity
                  :yellow intensity
                  :orange 2
                  :amber intensity
                  0)
          red (case color
                :red intensity
                :yellow 2
                :orange intensity
                :amber intensity
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
  ([launchpad id] (led-on launchpad id full-brightness :yellow))
  ([launchpad id brightness color]
      (let [rcvr (-> launchpad :rcv)]
        (led-on* rcvr id brightness color))))

(defn toggle-led [launchpad id cell]
  (if-not (= 0 cell)
    (led-on launchpad id)
    (led-off launchpad id)))

(defn command-right-leds-all-off [lp]
  (doseq [row (range 0 8)] (led-off lp [row 8])))

(defn render-row [launchpad row]
  (let [grid (state-maps/active-grid (:state launchpad))]
    (doseq [y (range 0 8)]
      (toggle-led launchpad [row y] (grid/cell grid row y)))))

(defn render-grid [launchpad]
  (let [grid (state-maps/active-grid (:state launchpad))]
    (doseq [[x row] (map vector (iterate inc 0) grid)
            [y cell] (map vector (iterate inc 0) row)]
      (toggle-led launchpad [x y] cell))))

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

(defn- side-event-handler [launchpad name state]
  (fn [_]
    (state-maps/toggle-side! state (side->row name))
    (toggle-led launchpad name (state-maps/cell state (side->row name) 8))
    (when-let [trigger-fn (state-maps/trigger-fn state name)]
      (if (= 0 (arg-count trigger-fn))
        (trigger-fn)
        (trigger-fn launchpad)))))

(defn- bind-grid-events [launchpad device-key idx state]
  (doseq [[x row] (map vector (iterate inc 0) (grid/project-8x8 grid-notes))
          [y note] (map vector (iterate inc 0) row)]
    (let [type      :note-on
          note      note
          on-handle (concat device-key [type note])
          on-fn (fn [_]
                  (state-maps/toggle! state x y)
                  (toggle-led launchpad [x y] (state-maps/cell state x y))
                  (when-let [trigger-fn (state-maps/trigger-fn state x y)]
                    (if (= 0 (arg-count trigger-fn))
                      (trigger-fn)
                      (trigger-fn launchpad)))
                  (let [active-mode (state-maps/mode state)]
                    (event [:Launchpad :grid-on idx active-mode]
                           :id [x y]
                           :note note
                           :launchpad launchpad
                           :idx idx)))
          off-handle (concat device-key [:note-off note])
          off-fn (fn [_]
                   (event [:Launchpad :grid-off idx (state-maps/mode state)]
                          :id [x y]
                          :note note
                          :launchpad launchpad
                          :idx idx))]

      (println :handle on-handle)
      (println :handle off-handle)

      (on-event on-handle  on-fn  (str "grid-on-event-for" on-handle))
      (on-event off-handle off-fn (str "grid-off-event-for" off-handle)))))

(defn- bind-side-events [launchpad device-key interfaces state]
  (doseq [[k v] (-> interfaces :grid-controls :side-controls)]
    (let [type      (:type v)
          note      (:note v)
          row       (:row v)
          on-handle (concat device-key [type note])
          on-fn (side-event-handler launchpad k state)]
      (println :handle on-handle)
      (on-event on-handle on-fn (str "side-on-event-for" on-handle)))))

(defn- bind-control-events [launchpad device-key idx interfaces]
   (doseq [[k v] (-> interfaces :grid-controls :controls)]
      (let [type      (:type v)
            note      (:note v)
            on-handle    (concat device-key [type note])
            on-fn (fn [{:keys [data2-f]}]
                    (event [:Launchpad :control k]
                           :val data2-f
                           :id k
                           :launchpad launchpad
                           :idx idx)

                    (event [:Launchpad :control idx k]
                           :val data2-f
                           :id k
                           :launchpad launchpad
                           :idx idx))]
        (println :handle on-handle)
        (on-event on-handle on-fn (str "top-on-event-for" on-handle)))))

(defn- register-event-handlers-for-launchpad
  [device rcv idx]
  (let [launchpad  (map->Launchpad (assoc device :rcv rcv))
        interfaces (:interfaces device)
        device-key (midi-full-device-key (:dev device))
        state      (:state device)]
    (bind-grid-events    launchpad device-key idx state)
    (bind-side-events    launchpad device-key interfaces state)
    (bind-control-events launchpad device-key idx interfaces)
    launchpad))

(defn stateful-launchpad
  [device]
  (let [interfaces (-> launchpad-config :interfaces)
        state      (atom (state-maps/empty))
        device-key (midi-full-device-key device)]
    {:dev        device
     :interfaces interfaces
     :state      state
     :type       ::stateful-launchpad}))

(defn merge-launchpad-kons
  [rcvs stateful-devs]
  (doseq [rcv rcvs]
    (intromation rcv)
    (led-on* rcv :up 3 :yellow))
  (doall
   (map (fn [[stateful-dev rcv id]]
          (register-event-handlers-for-launchpad stateful-dev rcv id))
        (map vector stateful-devs rcvs (range)))))

(comment
  (require '[launchpad.core :as core])
  (command-right-leds-all-off (first core/launchpad-kons)))
