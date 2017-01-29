(ns functions-as-patterns.core
  (:require [functions-as-patterns.color :as color]))

(defmacro view [[fn-to-view & args]]
  (let  [v (vec args)]
    `(example->color
      {:fn ~fn-to-view :args ~v})))

(defmacro render [[dir fn-to-view & args]]
  (let  [v (vec args)]
    `(example->color
      {:fn ~fn-to-view :args ~v :dir dir})))
