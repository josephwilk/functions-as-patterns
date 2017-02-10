(ns functions-as-patterns.seq
  (:require
   [functions-as-patterns.color :as color]
   [functions-as-patterns.core :refer :all]))

(def doc-dir (str (clojure.string/replace (:out (clojure.java.shell/sh "pwd")) "\n" "") "/doc" ))

;;Get shorter
;;;distinct filter remove take-nth for

(render doc-dir (distinct (concat (take 2 (color/color-seq 2 color/rgb-highlight-color)) (take 2 (drop 3 (color/hues 5))) (color/color-seq 1 color/rgb-highlight-color))))

(render doc-dir (dedupe (concat (take 2 (color/color-seq 2 color/rgb-highlight-color)) (take 2 (drop 3 (color/hues 5))) (color/color-seq 1 color/rgb-highlight-color))))

;;The pattern is more the fn than the filter/remove
(render doc-dir (filter (fn [x] (= 0 (mod x 3))) (color/hues 10)))
(render doc-dir (remove (fn [x] (= 0 (mod x 3))) (color/hues 10)))

(render doc-dir (take-nth 3 (color/hues 7)))
(render doc-dir (take 5 (for [x (range 5) y (range 5) :while (< y x)] [(color/int->color x)
                                                                       (color/int->color y)])))

;;Get longer
;;;cons conj concat lazy-cat mapcat cycle interleave interpose

(render doc-dir (cons  color/rgb-highlight-color (color/color-seq 3  (last (color/hues 4)))))
(render doc-dir (conj  (color/color-seq 3 color/rgb-highlight-color) (last (color/hues 4))))
(render doc-dir (concat (color/color-seq 3) (color/color-seq 3 color/rgb-highlight-color)))

(render doc-dir (cons  color/rgb-highlight-color (color/color-seq 3  (last (color/hues 4)))))
(render doc-dir (conj  [(last (color/hues 4))] (color/color-seq 3 color/rgb-highlight-color) ))

(render-titled doc-dir "vec" (conj   [(last (color/hues 4))] (apply vector (color/color-seq 3 color/rgb-highlight-color))))
(render-titled doc-dir "list" (conj  (list (last (color/hues 4))) (apply vector (color/color-seq 3 color/rgb-highlight-color))))

(render doc-dir (concat (color/color-seq 3) (color/color-seq 3 color/rgb-highlight-color)))


;; (view (lazy-cat (color/color-seq 3) (color/color-seq 3 color/rgb-highlight-color)))

(render doc-dir (mapcat (fn [x] x) [(color/color-seq 3) (color/color-seq 3 color/rgb-highlight-color)]))
(render doc-dir (interpose color/rgb-highlight-color (color/color-seq 5)))
(render doc-dir (interleave (color/hues 30 2 color/highlight-color) (color/color-seq 5)))

;;Tail-items
;;;rest nthrest next fnext nnext drop drop-while take-last for

(render doc-dir (rest (cons (last (color/hues 4)) (color/color-seq 3 color/rgb-highlight-color))))
(render doc-dir (nthrest (color/hues 5) 2))
(render doc-dir (nthnext (color/hues 5) 2))


;;Head-items
;;;take take-while butlast drop-last for

(render doc-dir (butlast (cons (nth (color/hues 4) 2) (color/color-seq 2 color/rgb-highlight-color))))
(view (drop-last 2 (concat (color/color-seq 2) (color/color-seq 2 color/rgb-highlight-color))))

;;Change
;;;flatten group-by partition partition-all partition-by split-at split-with filter
;;;remove replace shuffle

(render doc-dir (shuffle (color/hues 7)))
(render doc-dir (replace (color/hues 5 45) [0 3 4]))
(render doc-dir (partition 3 (color/hues 7)))
(render doc-dir (partition-all 3 (color/hues 7)))
(render doc-dir (partition-by even? (color/hues 10)))
(render doc-dir (split-at 2 (color/color-seq 3 color/rgb-highlight-color)))
(render doc-dir (split-with even? (color/color-seq 3 color/rgb-highlight-color)))
(render doc-dir (flatten (partition 2 (color/hues 10))))

(render doc-dir (flatten (partition 1 (partition-all 2 (color/hues 8)))))

;;Rearrange
;;;reverse sort sort-by compare

(render doc-dir (reverse (color/hues 4 30)))
(render doc-dir (sort (shuffle (shuffle (shuffle (color/hues 7 10))))))

;;Process items
;;;map pmap map-indexed mapcat for replace seque
(conj )
