(ns launchpad.sequencer
  (:use overtone.live))

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
  [buf 0 rate 1 out-bus 0 beat-num 0 pattern 0  num-steps 8 beat-cnt-bus 0 beat-trg-bus 0 rq-bus 0]
  (let [cnt      (in:kr beat-cnt-bus)
        beat-trg (in:kr beat-trg-bus)
        bar-trg  (and (buf-rd:kr 1 pattern cnt)
                      (= beat-num (mod cnt num-steps))
                      beat-trg)
        vol      (set-reset-ff bar-trg)]
    (out out-bus (* vol (scaled-play-buf 1 buf rate bar-trg)))))

(defn- start-synths [samples patterns num-steps beat-cnt-bus beat-trg-bus out-bus]
  (doall (mapcat (fn [sample pattern out-bus]
                   (map (fn [step-idx]
                          (println (to-sc-id sample))
                          (mono-sequencer :buf (to-sc-id sample)
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
        synths   (start-synths samples patterns num-steps beat-cnt-bus beat-trg-bus out-bus)]
    (with-meta {:patterns patterns
                :num-steps num-steps
                :num-samples (count samples)
                :synths (agent synths)}
      {:type ::sequencer})))

(defn sequencer-write!
  [sequencer idx pattern]
  (let [buf (:pattern-buf (nth (:patterns sequencer) idx))]
    (buffer-write! buf pattern)))

(defn reset-all-patterns! [sequencer]
  (doseq [c (range (count (:patterns sequencer)))]
    (sequencer-write! sequencer c [0 0 0 0 0 0 0 0 0])))

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
