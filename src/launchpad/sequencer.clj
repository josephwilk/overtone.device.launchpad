(ns launchpad.sequencer
  (:use overtone.live))

(defsynth skipping-sequencer
  "Supports looping and jumping position"
  [buf 0 rate 1 out-bus 0 start-point 0 bar-trg [0 :tr] loop? 0 vol 1.0 pan 0]
  (let [p (scaled-play-buf 1 buf rate bar-trg start-point loop?)]
    (out [0 1] (* vol p))))

(defsynth orig-mono-sequencer
  "Plays a single channel audio buffer (with panning)"
  [buf 0 rate 1 out-bus 0 beat-num 0 pattern 0  num-steps 8 beat-cnt-bus 0 beat-trg-bus 0 rq-bus 0]
  (let [cnt      (in:kr beat-cnt-bus)
        beat-trg (in:kr beat-trg-bus)
        bar-trg  (and (buf-rd:kr 1 pattern cnt)
                      (= beat-num (mod cnt num-steps))
                      beat-trg)
        vol      (set-reset-ff bar-trg)]
    (out out-bus (* vol
                    (pan2
                     (rlpf
                      (scaled-play-buf 1 buf rate bar-trg)
                      (demand bar-trg 0 (dbrown 200 20000 50 INF))
                      (lin-lin:kr (lf-tri:kr 0.01) -1 1 0.1 0.9)))))))

(defsynth mono-sequencer
  "Plays a single channel audio buffer."
  [buf 0 rate 1 out-bus 0 beat-num 0 pattern 0  num-steps 8 beat-cnt-bus 0 beat-trg-bus 0 rq-bus 0 pan 0]
  (let [cnt      (in:kr beat-cnt-bus)
        beat-trg (in:kr beat-trg-bus)
        bar-trg  (and (buf-rd:kr 1 pattern cnt)
                      (= beat-num (mod cnt num-steps))
                      beat-trg)
        vol      (set-reset-ff bar-trg)]
    (out out-bus (* vol (pan2 (scaled-play-buf 1 buf rate bar-trg) pan)))))

(defn- start-synths [samples patterns num-steps tgt-group beat-cnt-bus beat-trg-bus out-bus]
  (doall (mapcat (fn [sample pattern out-bus]
                   (map (fn [step-idx]
                          (mono-sequencer [:tail tgt-group]
                                           :buf (to-sc-id sample)
                                           :beat-num step-idx
                                           :pattern (:pattern-buf pattern)
                                           :beat-cnt-bus beat-cnt-bus
                                           :beat-trg-bus beat-trg-bus
                                           :out-bus out-bus))
                        (range num-steps)))
                 samples
                 patterns
                 (repeat out-bus))))

(defn- mk-sequence-patterns
  "Setup our buffers"
  [samples num-steps]
  (doall (map (fn [sample]
                (with-meta {:num-steps num-steps
                            :pattern-buf (buffer num-steps)}
                  {:type ::sequence-pattern}))
              samples)))

(defn mk-sequencer [handle samples num-steps beat-cnt-bus beat-trg-bus out-bus]
  (let [patterns (mk-sequence-patterns samples num-steps)
        seq-group (group "lp-sequencer")
        synths    (start-synths samples patterns num-steps seq-group beat-cnt-bus beat-trg-bus out-bus)]
    (with-meta {:patterns patterns
                :num-steps num-steps
                :num-samples (count samples)
                :seq-group seq-group
                :beat-cnt-bus beat-cnt-bus
                :beat-trg-bus beat-trg-bus
                :out-bus out-bus
                :synths (agent synths)}
      {:type ::sequencer})))

(defn swap-samples! [sequencer samples]
  (send (:synths sequencer)
        (fn [synths]
          (kill (:seq-group sequencer))
          (start-synths (take (:num-samples sequencer) samples)
                        (:patterns sequencer)
                        (:num-steps sequencer)
                        (:seq-group sequencer)
                        (:beat-cnt-bus sequencer)
                        (:beat-trg-bus sequencer)
                        (:out-bus sequencer)))))

(defn sequencer-write!
  [sequencer idx pattern]
  (let [buf (:pattern-buf (nth (:patterns sequencer) idx))]
    (buffer-write! buf pattern)))

(defn reset-pattern! [sequencer idx]
  (let [pattern (nth (:patterns sequencer) idx)
        reset-pattern (vec (take (:num-steps pattern) (repeat 0)))]
    (sequencer-write! sequencer idx  reset-pattern)))

(defn reset-all-patterns! [sequencer]
  (doseq [c (range (count (:patterns sequencer)))]
    (reset-pattern! sequencer c)))

(defn sequencer-pattern
  "Returns the current state of the sequencer pattern with index idx"
  [sequencer idx]
  (let [buf (:pattern-buf (nth (:patterns sequencer) idx))]
    (seq (buffer-data buf))))

(defn sequencer-patterns
  "Returns a sequence of the current state of all the patterns in
   sequencer"
  [sequencer]
  (doall (map (fn [i] (sequencer-pattern sequencer i)) (range (:num-samples sequencer)))))
