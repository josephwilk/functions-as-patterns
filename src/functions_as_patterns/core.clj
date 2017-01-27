(ns functions-as-patterns.core
  (require [mikera.image.core :refer :all]
           [mikera.image.colours :refer :all]
           [com.evocomputing.colors :as colors]))

(def blank-color     (colors/create-color "#663477"))
(def highlight-color (colors/create-color "#EE5C96"))
(def stroke-color    (colors/create-color "#111111"))
(def text-color (colors/create-color "#ffffff"))

(def darker-highlight-color (colors/adjust-hue (colors/create-color "#3A396C") -35))

(def rgb-blank-color     (colors/rgba-int blank-color))
(def rgb-highlight-color (colors/rgba-int highlight-color))
(def rgb-darker-highlight-color (colors/rgba-int darker-highlight-color))
(def rgb-stroke-color    (colors/rgba-int stroke-color))
(def rgb-text-color (colors/rgba-int text-color))

(def working-dir "/Users/josephwilk/Desktop/clojure_functions")

(defn int->color [i]
  (-> highlight-color
      (colors/adjust-hue (* i -50))
      colors/rgba-int))

(defn container-color [depth]
  (-> blank-color
      (colors/adjust-hue (* depth -30))
      colors/rgba-int))

(defn flatten-all [coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (if (coll? (first s))
       (concat (flatten (first s)) (flatten-all (rest s)))
       (cons (first s) (flatten-all (rest s)))))))

(defn no-of-leaf-nodes [col]
  (if (sequential? col)
    (count (flatten-all col))
    0))

(defn hues
  ([steps] (hues 25 steps highlight-color))
  ([steps factor] (hues factor steps highlight-color))
  ([steps factor base]
   (-> (map
        (fn [hue-adjust] (colors/rgba-int
                         (colors/adjust-hue base hue-adjust)))
        (range 0 (* steps factor) steps))
       vec)))

(defn color-seq
  ([n] (color-seq n rgb-blank-color))
  ([n color] (take n (cycle [color]))))

(defn fill-round-rect!
  ([image x y w h colour]
   (let [g (graphics image)
         ^Color colour (to-java-color colour)]
     (.setColor g colour)
     (.fillRect g (int x) (int y) (int w) (int h))
     image)))

(defn draw-chars! [image text x y w h colour]
  (let [g (graphics image)
        ^Color colour (to-java-color colour)]
    (.setColor g colour)
    (.drawChars g (char-array text) 0 (count (char-array text)) (int (+ (/ (- w (* 5 (count (char-array text))) ) 2) x)) (int (+ (/ h 2) y)))
    image))

(defn paint-stroked-rectangle! [img color posx posy rect-w rect-h stroke-size]
  (let [x (+ posx stroke-size)
        y (+ posy stroke-size)
        w (- rect-w stroke-size)
        h (- rect-h (* 2 stroke-size))]
    (fill-round-rect! img
                posx posy
                (+ w (* 2 stroke-size)) (+ (* 2 stroke-size) h)
                rgb-stroke-color)
    (fill-round-rect! img
                x y
                w h
                color)
    (draw-chars! img (str posx) x y w h rgb-text-color)))

(defn leaf?     [node] (not (sequential? node)))
(defn children? [node] (sequential? node))

(defn paint-rectangle! [img color rect-size stroke-size x-pos depth x-offset y-offset offset]
  (if (leaf? color)
    (paint-stroked-rectangle! img color
                              (+ x-offset (* rect-size x-pos)) (* depth y-offset)
                              rect-size                         rect-size
                              stroke-size)
    (let [width (* (no-of-leaf-nodes color) rect-size)]
      (paint-stroked-rectangle! img (container-color (* 2 depth))
                                (+ x-offset offset)
                                (* depth y-offset)
                                width (+ rect-size)
                                stroke-size))))

(defn paint-all! [img rect-size stroke-size x-offset y-offset depth]
  (fn [parent-indent [idx color]]

    (let [previous-rect (if (<= (dec depth) 0) rect-size (/ rect-size (* (dec depth) 2)))
          rect-new-size (if (= 0 depth) rect-size (/ rect-size (* depth 2)))]
      (println "[paint-all!]: " :acc parent-indent :idx idx :depth depth :color color  :new-rec rect-new-size)
      (paint-rectangle! img color rect-new-size stroke-size
                        idx depth
                        x-offset y-offset parent-indent)

      (if (children? color)
        (let [no-children     (no-of-leaf-nodes color)
              new-rect-size   (/ rect-size (* (inc depth) 2))
              middle-position  (/ (-
                                   (+ (* 2 x-offset) (* rect-new-size no-children))
                                   (* new-rect-size no-children))
                                  2)]
          (reduce
           (paint-all!
            img rect-size stroke-size
            (+ parent-indent middle-position)
            (- new-rect-size (/ new-rect-size (* (inc depth) 2)))
            (inc depth))
           parent-indent
           (map vector (range) color)))
        (+ parent-indent rect-size)
        ))))

(defn render [data title]
  (let [rect-size 100
        total-cells  (no-of-leaf-nodes data)
        stroke-size 1
        bi (new-image (+ (* total-cells rect-size) stroke-size) rect-size)]

    (fill! bi (colors/rgba-int stroke-color))
    (reduce (paint-all! bi rect-size stroke-size 0 0 0)
            0
            (map vector (range) data))
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
     (render out (str name "_post"))
     )))

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

(comment
  ;Troublesome Examples

  ;;;Misses second box!
  (view (identity [[[(rand-colour)]]  [[(rand-colour)]]]))

  ;;Behaving
  (view (partition-all 3 (partition 2 (hues 10))))
  (view (identity [[[(rand-colour)]]  [(rand-colour) (rand-colour)]]))
)
