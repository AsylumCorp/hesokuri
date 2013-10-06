; Copyright (C) 2013 Google Inc.
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;    http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns hesokuri.see
  "A module that helps transform a data structure so that redundant sections are
  abbreviated. For instance, (repeat 3 (repeat 10 :x)) will be transformed to:
  ((:x :x :x :x :x :x :x :x :x :x)
   {:hesokuri.see/p [0]}
   {:hesokuri.see/p [0]})")

(defn- shrink-with-paths
  [expr curr-path path-map]
  (let [previous-path (delay (path-map expr))
        new-path-map (delay (assoc path-map expr (cons ::path curr-path)))
        box-kind (delay ({clojure.lang.Atom ::atom
                          clojure.lang.Ref ::ref
                          clojure.lang.Agent ::agent
                          clojure.lang.Var ::var}
                         (class expr)))]
    (cond
     @previous-path [@previous-path path-map]

     @box-kind
     (let [[shrunk-value path-map]
           (shrink-with-paths @expr (conj curr-path :deref) @new-path-map)]
       [(list @box-kind shrunk-value) path-map])

     (or (vector? expr) (seq? expr) (list? expr) (set? expr))
     (loop [i 0
            [e :as els] (seq expr)
            path-map @new-path-map
            shrunk []]
       (if els
         (let [[shrunk-e path-map]
               (shrink-with-paths e (conj curr-path i) path-map)]
           (recur (inc i) (next els) path-map (conj shrunk shrunk-e)))
        [shrunk path-map]))


     (map? expr)
     (loop [[[k v] :as els] (seq expr)
            path-map @new-path-map
            shrunk {}]
       (cond
        els (let [[shrunk-v path-map]
                  (shrink-with-paths v (conj curr-path k) path-map)]
              (recur (next els) path-map (assoc shrunk k shrunk-v)))
        :else [shrunk path-map]))

     :else [expr path-map])))


(defn shrink
  [expr]
  (first (shrink-with-paths expr [] {})))
