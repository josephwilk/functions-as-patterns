(ns functions-as-patterns.core
  (:require
   [mikera.image.colours :refer [rand-colour]]
   [functions-as-patterns.color :as color]))

(defmacro view [[fn-to-view & args]]
  (let  [v (vec args)]
    `(color/example->color {:fn ~fn-to-view :args ~v})))

(defmacro view-as-colors [[fn-to-view & args]]
  (let  [v (vec args)]
    `(color/example->forced-color {:fn ~fn-to-view :args ~v})))

(defmacro render [dir [fn-to-view & args]]
  (let  [v (vec args)]
    `(color/example->color {:fn ~fn-to-view :args ~v :dir ~dir})))

(comment
  (view-as-colors (partition-all 2 [1 2 3]))

  (view (identity [[[(rand-colour) (rand-colour)]] [(rand-colour) (rand-colour)]]))
  (view (identity [[(rand-colour)] [(rand-colour) (rand-colour)]]))
  (view (identity [[(rand-colour) (rand-colour) (rand-colour)]]))
  (view (identity [[[[(rand-colour)]]]]))
  (view (identity [[(rand-colour) (rand-colour) (rand-colour)]]))
  (view (identity [(rand-colour)]))
  (view (identity [[(rand-colour)]]))
  (view (identity [[[(rand-colour)]]]))
  (view (identity [[[[(rand-colour)]]]]))
  )
