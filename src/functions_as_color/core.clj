(ns functions-as-color.core
  (require [mikera.image.core :refer :all]
           [mikera.image.colours :refer :all]
           [com.evocomputing.colors :as colors]))

(def blank-color     (colors/create-color "#3A396C"))
(def highlight-color (colors/create-color "#EE5C96"))
(def stroke-color    (colors/create-color "#111111"))

(def darker-highlight-color (colors/adjust-hue (colors/create-color "#3A396C") -35))

(def rgb-blank-color     (colors/rgba-int blank-color))
(def rgb-highlight-color (colors/rgba-int highlight-color))
(def rgb-darker-highlight-color (colors/rgba-int darker-highlight-color))
(def rgb-stroke-color    (colors/rgba-int stroke-color))

(def working-dir "/Users/josephwilk/Desktop/clojure_functions")

(defn container-color [depth]
  (-> blank-color
      (colors/adjust-hue (* depth -50))
      colors/rgba-int))

(defn no-of-leaf-nodes [[head & tail]]
  (cond
    (and (not (seq? tail)) (nil? head))              0
    (and (not (seq? tail)) (not (sequential? head))) 1
    (and (seq? tail)       (not (sequential? head))) (+ 1 (no-of-leaf-nodes tail))
    :else                                            (+ (no-of-leaf-nodes head) (no-of-leaf-nodes tail))))

(defn hues
  ([steps] (hues 25 steps highlight-color))
  ([steps factor base]
   (map
    (fn [hue-adjust] (colors/rgba-int
                     (colors/adjust-hue base hue-adjust)))
    (range 0 (* steps factor) steps))))

(defn paint-stroked-rectangle! [img color posx posy rect-w rect-h stroke-size]
  (let [x (+ posx stroke-size)
        y (+ posy stroke-size)
        w (- rect-w stroke-size)
        h (- rect-h (* 2 stroke-size))]
    (fill-rect! img
                posx posy
                (+ w (* 2 stroke-size)) (+ (* 2 stroke-size) h)
                (colors/rgba-int stroke-color))
    (fill-rect! img
                x y
                w h color)))

(defn leaf?     [node] (not (sequential? node)))
(defn children? [node] (sequential? node))

(defn paint-rectangle! [img color rect-size stroke-size x-pos depth x-offset y-offset]
  (if (leaf? color)
    (paint-stroked-rectangle! img color
                              (+ x-offset (* rect-size x-pos)) (* depth y-offset)
                              rect-size                         rect-size
                              stroke-size)
    (let [width (* (no-of-leaf-nodes color) rect-size) ]
      (paint-stroked-rectangle! img (container-color depth)
                                (+ x-offset (* width x-pos)) (* depth y-offset)
                                width rect-size
                                stroke-size))))

(defn paint-all! [img rect-size stroke-size x-offset y-offset depth]
  (fn [idx color]
    (let [rect-new-size (if (= 0 depth) rect-size (/ rect-size (* depth 2)))]
      (paint-rectangle! img color rect-new-size stroke-size idx depth x-offset y-offset)

      (when (children? color)
        (let [no-children     (no-of-leaf-nodes color)
              new-rect-size   (/ rect-size (* (inc depth) 2))
              parent-indent   (* idx no-children rect-new-size)
              middle-position (/ (-
                                  (+ (* 2 x-offset) (* rect-new-size no-children))
                                  (* new-rect-size no-children))
                                 2)]
          (doall
           (map-indexed
            (paint-all! img rect-size stroke-size
                        (+ parent-indent middle-position)
                        (- new-rect-size (/ new-rect-size (* (inc depth) 2)))
                        (inc depth))
            color)))))))

(defn render [data title]
  (let [rect-size 100
        total-cells  (no-of-leaf-nodes data)
        stroke-size 1
        bi (new-image (+ (* total-cells rect-size) stroke-size) rect-size)]

    (fill! bi (colors/rgba-int stroke-color))
    (doall (map-indexed (paint-all! bi rect-size stroke-size 0 0 0) data))
    (show bi :zoom 1.0 :title title)
    (save bi (str working-dir "/" title ".png"))))

(defn fn->str [fn-to-convert] (-> (str fn-to-convert) (clojure.string/split #"@") first))

(defn- color->rgba [c]
  (if (= (type c)
         :com.evocomputing.colors/color)
    (colors/rgba-int c)
    c))

(defn render-fn
  ([fn-to-doc out & args]
   (let [name (fn->str fn-to-doc)
         args (->>
               args
               (map (fn [a] (if (sequential? a) a [a])))
               (map (fn [args] (map color->rgba args))))
         out (map color->rgba out)
         ]
     (dotimes [i (count args)]
       (try
         (render (nth args i)  (str name "_arg" i))
         (catch Exception e (println "Unable to render:" (nth args i)))))
     (render out (str name "_post")))))

(defn example->color [{fn-to-doc :fn args :args}]
  (let [args (vec args)]
    (apply render-fn
           fn-to-doc
           (apply fn-to-doc args)
           args)))

(defmacro view [[fn-to-view & args]]
  (let  [v (vec args)]
    `(example->color
      {:fn ~fn-to-view :args ~v})))

(view
 (interpose rgb-highlight-color (take 8 (cycle [rgb-blank-color]))))

(view
 (interleave (hues 30 2 highlight-color) (take 8 (cycle [blank-color]))))

(view
 (nthrest (hues 10) 4))

(view
 (shuffle (hues 10)))

(view
 (replace (vec (hues 10)) [0 3 4 5]))

(view
 (partition 2 (partition 3 (hues 25 10 highlight-color))))



  ;;Get shorter
;;;distinct filter remove take-nth for

  ;;Get longer
;;;cons conj concat lazy-cat mapcat cycle interleave interpose

  ;;Tail-items
;;;rest nthrest next fnext nnext drop drop-while take-last for

  ;;Head-items
;;;take take-while butlast drop-last for

  ;;'Change'
;;;conj concat distinct flatten group-by partition partition-all partition-by split-at split-with filter
;;;remove replace shuffle

  ;;Rearrange
;;;reverse sort sort-by compare

  ;;Process items
;;;map pmap map-indexed mapcat for replace seque
