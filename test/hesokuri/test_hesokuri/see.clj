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

(ns hesokuri.test-hesokuri.see
  (:use clojure.test
        hesokuri.see))

(deftest test-shrink
  (are [expr expected-result]
       (is (= expected-result (shrink expr)))
       (repeat 3 (repeat 10 :x))
       [(repeat 10 :x) [:hesokuri.see/path 0] [:hesokuri.see/path 0]]

       (hesokuri.see/shrink [{} {}])
       [{} [:hesokuri.see/path 0]]

       (let [atm (atom [:foo])]
         [atm atm [:foo]])
       [[:hesokuri.see/atom [:foo]]
        [:hesokuri.see/path 0]
        [:hesokuri.see/path 0 :deref]]))
