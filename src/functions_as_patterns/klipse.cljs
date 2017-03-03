(ns functions-as-patterns.klipse)

(def rect-size 70)
(def rect-start 70)
(def stroke-size 2)

(def canvas-id (atom "canvas-1"))
(def all-hues ["#EE5C96" "#EE5F5C" "#EE9C5C" "#EED85C" "#C7EE5C" "#8AEE5C"
               "#5CEE6B" "#5CEEA8" "#5CEEE5" "#5CBAEE" "#5C7EEE" "#775CEE" "#B45CEE"
               "#EE5CEB" "#EE5CAE" "#EE5C71" "#EE835C" "#EEC05C" "#DFEE5C" "#A2EE5C"
               "#65EE5C" "#5CEE8F" "#5CEECC" "#5CD3EE" "#5C96EE" "#5F5CEE" "#9C5CEE"
               "#D95CEE" "#EE5CC7" "#EE5C8A" "#EE6B5C" "#EEA85C" "#EEE55C" "#BAEE5C"])

(def blank-color             "#663477")
(def highlight-color         "#EE5C96")
(def stroke-color            "#111111")
(def text-color              "#ffffff")
(def darker-highlight-color  "#3A396C")

(defn container-color [depth] "#663477")

(defn- no-of-leaf-nodes [col]
 (if (sequential? col)
   (count (flatten col))
   0))

(defn- fill-round-rect!
 ([image x y w h colour]
    (set! (.-fillStyle image) colour)
    (doto image
      (.beginPath)
      (.rect x y w h)
      (.fill) )))

(defn- find-color [color-lookup v] (get color-lookup v v))

(defn- paint-stroked-rectangle! [img color-lookup seq-value posx posy rect-w rect-h]
 (let [x (+ posx stroke-size)
       y (+ posy stroke-size)
       w (- rect-w stroke-size)
       h (- rect-h (* 2 stroke-size))
       color (find-color color-lookup seq-value)]

   (fill-round-rect! img
               posx posy
               (+ w (* 2 stroke-size)) (+ (* 2 stroke-size) h)
               stroke-color)
   (fill-round-rect! img
               x y
               w h
               color)
   (comment
     ;; debug
     (draw-chars! img (str posx) x y w h text-color))))

(defn- leaf?     [node] (not (sequential? node)))
(defn- children? [node] (sequential? node))

(defn- paint-rectangle! [img color-lookup color rect-size depth x-offset]
 (let [y-offset (/ (- rect-start rect-size) 2)]
   (if (leaf? color)
     (paint-stroked-rectangle! img color-lookup color x-offset y-offset rect-size rect-size)
     (let [width (* (no-of-leaf-nodes color) (/ rect-size 2) )]
       (paint-stroked-rectangle! img color-lookup (container-color depth) x-offset y-offset width rect-size)))))

(defn- paint-all! [img color-lookup rect-size x-offset depth]
 (fn [parent-indent [idx color]]
   (paint-rectangle! img color-lookup color rect-size depth parent-indent)

   (if (children? color)
     (let [new-rect-size (/ rect-size 2)
           indent (/ new-rect-size 2)]
       (+
        indent
        (reduce
         (paint-all! img color-lookup new-rect-size parent-indent (inc depth))
         (+ indent parent-indent)
         (map vector (range) color))))
     (+ parent-indent rect-size)
     )))


(defn color-map [args]
 (let [all-args (->> args flatten (remove (fn [a] (sequential? a))))
       colors (reduce (fn [acc [idx v]]
                        (assoc acc v (get acc v (nth all-hues idx))))
                      {}
                      (map vector (range) all-args))]
   colors))

(defn view [data]
  (let [color-lookup (color-map data)
        canvas (js/document.getElementById @canvas-id)
        ctx (.getContext canvas "2d")
        canvas-width (.-width canvas)
        canvas-height (.-height canvas)
        total-cells  (no-of-leaf-nodes data)
        new-width (+ (* total-cells rect-size) stroke-size)
        new-height  rect-size]
        (.clearRect ctx 0 0 canvas-width canvas-height)
        (set! (.. canvas -width) new-width)
        (set! (.. canvas -style -width) new-width)
        (set! (.. canvas -height) new-height)
        (set! (.. canvas -style -height) new-height)
      (reduce (paint-all! ctx color-lookup rect-size 0 0)
      0
      (map vector (range) data)))
  data)
