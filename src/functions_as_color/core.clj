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

(defn hues
  ([steps] (hues steps 30 10))
  ([steps base] (hues steps base 100))
  ([steps factor base]
   (map
    (fn [hue-adjust] (colors/rgba-int
                     (colors/adjust-hue base hue-adjust)))
    (range 0 (* steps factor) steps))))

(defn paint-rectangle! [img color posx posy rect-size stroke-size]
  (let [x (+ posx stroke-size)
        y (+ posy stroke-size)
        w (- rect-size stroke-size)
        h (- rect-size (* 2 stroke-size))]
    (fill-rect! img
                posx posy
                (+ w (* 2 stroke-size)) (+ (* 2 stroke-size) h)
                (colors/rgba-int stroke-color))
    (fill-rect! img
                x y
                w h color)))

(defn leaf? [node] (not (sequential? node)))

(defn render [data title]
  (let [rect-size 100
        total-cells (count (flatten data))
        stroke-size 1
        bi (new-image (+ (* total-cells rect-size) stroke-size) rect-size)]

    (fill! bi (colors/rgba-int stroke-color))
    (doall
     (map-indexed (fn [idx color]
                    (if (leaf? color)
                      (paint-rectangle! bi color (* rect-size idx) 0 rect-size stroke-size)))
                    
                    (let [previous-cells (count data)
                          cells (count color)
                          new-rect-size (/ rect-size 2)
                          parent-indent (* idx cells rect-size)
                          middle-position (/ (- (* rect-size cells) (* new-rect-size cells)) 2)]
                        (paint-rectangle! bi
                                          rgb-darker-highlight-color
                                          (+ (* cells rect-size idx))
                                          0
                                          (* cells rect-size)
                                          stroke-size)
                        (doall
                         (map-indexed (fn [idx2 color2]
                                        (paint-rectangle! bi color2
                                                          (+
                                                            middle-position
                                                            parent-indent
                                                            (* idx2 new-rect-size))
                                                            (/ new-rect-size 2)
                                                            new-rect-size
                                                            stroke-size))
                                        )
                                      color))

                  data))
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
       (render (nth args i)  (str name "_arg" i)))
     (render out (str name "_post")))))

(defn example->color [{fn-to-doc :fn args :args}]
  (let [args (vec args)]
    (apply render-fn
           fn-to-doc
           (apply fn-to-doc args)
           args)))

(example->color
 {:fn clojure.core/interpose
  :args [rgb-highlight-color
         (take 8 (cycle [rgb-blank-color]))]})

(example->color
 {:fn clojure.core/interleave
  :args [(hues 30 2 highlight-color)
         (take 8 (cycle [blank-color]))]})

(example->color
 {:fn clojure.core/nthrest
  :args [(hues 25 10 highlight-color)
         4]})

(example->color
 {:fn clojure.core/shuffle
  :args [(hues 25 10 highlight-color)]})

(example->color
 {:fn  clojure.core/replace
  :args [(vec (hues 25 10 highlight-color))
         [0 3 4 5]]})

;;nested lists patterns
(example->color
 {:fn clojure.core/partition
  :args [4
         (hues 25 10 highlight-color)]})


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
