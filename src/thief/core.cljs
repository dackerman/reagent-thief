(ns thief.core
  (:require [reagent.core :as reagent :refer [atom]]))


(def block-height 100)
(def start-y 350)
(def fps 20.0)
(def time-speed 0.3)
(def jump-force 10)
(def gravity 9.81)

(def frametime (/ 1.0 fps))

(defn make-gap [x y width color]
  {:x x :y y :width width :color color})


(def gaps
  [(make-gap 700 300 200 "red")
   (make-gap 1000 400 300 "green")])


(defonce app-state
  (atom {:player {:x 100 :y start-y :vy 0 :jumps 0}
         :ground (merge (make-gap 0 start-y 500 "blue") {:start true})
         :gaps gaps
         :camera 0
         :t 0
         :dt frametime}))

(defn draw-player [player]
  [:img {:src "img/running.gif"
         :style {:position "absolute"
                 :left (- (:x player) 20)
                 :top (- (:y player) 33)
                 :width 40}}])


(defn draw-block [{:keys [x width color] :as block} extra-styles]
  (let [block-style {:position "absolute"
                     :left x
                     :width width
                     :backgroundColor color }]
    [:div {:style (merge block-style extra-styles)}]))


(defn draw-bottom-block [{:keys [x y width color] :as block}]
  (draw-block block {:height 500 :top y }))


(defn draw-top-block [{:keys [x y width color] :as block}]
  (draw-block block {:height (- y block-height) :top 0 }))


(defn draw-gap [{:keys [x y width color start] :as block}]
  [:div
   (if start [:div] [draw-top-block block])
   [draw-bottom-block block]])

(defn within-ground [ground player]
  (let [px (:x player)
        py (:y player)
        within (and ground
                    (<= (:x ground) px)
                    (>= (+ (:width ground) (:x ground)) px))]
    within))

(defn is-on-ground [state]
  (let [player (:player state)
        ground (:ground state)]
    (if-let [within (within-ground ground player)]
      (if (>= (:y player) (:y ground))
        ground))))


(defn apply-gravity [state dt]
  (let [player (:player state)
        vy (:vy player)
        on-ground (is-on-ground state)
        newvy (if on-ground 0 (- vy (* dt 10 gravity)))]
    (assoc-in state [:player :vy] newvy)))

(defn jump [state]
  (if (or (is-on-ground state) (<= (get-in state [:player :jumps]) 1))
    (-> state
        (assoc-in [:player :vy] jump-force)
        (update-in [:player :jumps] inc))
    state))

(defn physics [state]
  (let [player (:player state)
        vy (:vy player)
        y (:y player)
        dt (:dt state)
        newy (- y (* vy dt 10 gravity))]
    (assoc-in state [:player :y] newy)))

(defn snap-to-ground [state]
  (let [ground (is-on-ground state)]
    (if ground
      (-> state
          (assoc-in [:player :y] (:y ground))
          (assoc-in [:player :jumps] 0))
      state)))


(defn update-ground [state]
  (let [within (within-ground (:ground state) (:player state))
        gaps (:gaps state)]
    (if within
      state      
      (loop [gaps gaps]
        (if-let [gap (first gaps)]
          (if (within-ground gap (:player state))
            (assoc-in state [:ground] gap)
            (recur (next gaps)))
          state)))))

(defn tick-state [state]
  (let [dt (:dt state)]
    (-> state
        (physics)
        (apply-gravity dt)
        (snap-to-ground)
        (update-ground))))

(defn tick-time []
  (swap! app-state #(-> %
                        (assoc-in [:t] (+ (* time-speed frametime) (:t %)))
                        (assoc-in [:dt] (* time-speed frametime))
                        (tick-state))))

(defn draw-state [state]
  [:div
   (map (fn [tup] [:pre (str (first tup)) " -> " (str (second tup))]) state)])

(defonce key-already-down (atom false))

(defn page []
  (let [state @app-state]
    [:div
     {:tabIndex "1"
      :on-key-down #(let [is-d (= 68 (.-which %))]
                      (if (and is-d (not @key-already-down))
                        (do
                          (swap! app-state jump)
                          (reset! key-already-down true))))
      :on-key-up #(reset! key-already-down false)
      :style {:position "absolute"
              :width (.-availWidth js/screen)
              :height (.-availHeight js/screen)
              :padding 0 :margin 0}}
     [draw-state state]
     [draw-gap (:ground state)]
     (map draw-gap (:gaps state))
     [draw-player (:player state)]]))


(defonce timer (js/setInterval tick-time (* 1000 frametime)))

(defn main []
  (reagent/render-component [page] (.getElementById js/document "app")))
