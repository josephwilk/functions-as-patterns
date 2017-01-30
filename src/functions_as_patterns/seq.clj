(ns functions-as-patterns.seq
  (:require
   [functions-as-patterns.color :as color]
   [functions-as-patterns.core :refer :all]))

(def doc-dir (str (clojure.string/replace (:out (clojure.java.shell/sh "pwd")) "\n" "") "/doc/" ))


(view-as-colors (partition 3 (range 10)))
;;Get shorter
;;;distinct filter remove take-nth for

(view (distinct (sort (concat (color/hues 5) (color/hues 5)))))

;;The pattern is more the fn than the filter/remove
(view (filter (fn [x] (= 0 (mod x 3))) (color/hues 10)))
(view (remove (fn [x] (= 0 (mod x 3))) (color/hues 10)))

(view (take-nth 3 (color/hues 10)))
(view (take 5 (for [x (range 5) y (range 5) :while (< y x)] [(color/int->color x)
                                                             (color/int->color y)])))

;;Get longer
;;;cons conj concat lazy-cat mapcat cycle interleave interpose

(view (cons   color/rgb-highlight-color (color/color-seq 3)))
(view (conj   (color/color-seq 3)        color/rgb-highlight-color))
(view (concat (color/color-seq 3) (color/color-seq 3 color/rgb-highlight-color)))

;;fails (view (conj (color/hues 5) (color/hues 5)))
;;fails (view (lazy-cat (color-seq 3) (color-seq 3 rgb-highlight-color)))

(view (mapcat (fn [x] x) [(color/color-seq 3) (color/color-seq 3 color/rgb-highlight-color)]))
(view (interpose color/rgb-highlight-color (color/color-seq 8)))
(view (interleave (color/hues 30 2 color/highlight-color) (color/color-seq 8)))

;;Tail-items
;;;rest nthrest next fnext nnext drop drop-while take-last for

(view (rest (color/hues 4)))
(view (nthrest (color/hues 10) 4))

;;Head-items
;;;take take-while butlast drop-last for

;;Change
;;;flatten group-by partition partition-all partition-by split-at split-with filter
;;;remove replace shuffle

(view (shuffle (interpose color/rgb-highlight-color (color/color-seq 5))))
(view (replace (color/hues 10) [0 3 4 5]))
(view (partition 3 (color/hues 10)))
(view (flatten (partition 2 (color/hues 10))))
(view (flatten (partition 1 (partition 2 (color/hues 10)))))

;;(view (group-by  (color/hues 10)))

;;Rendering fun :)
(render doc-dir (partition-all 3 (color/hues 10)))

;;Rearrange
;;;reverse sort sort-by compare

(view (reverse (color/hues 4 30)))
(view (sort (shuffle (color/hues 6 15))))
;; rearrange? (view (compare [1 2] [2 3 4]))

;;Process items
;;;map pmap map-indexed mapcat for replace seque
