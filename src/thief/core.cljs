(ns thief.core
  (:require [reagent.core :as reagent :refer [atom]]))


(def block-height 200)
(def start-y 350)
(def fps 30.0)
(def time-speed 0.3)
(def jump-force 15)
(def gravity 15.81)
(def scroll-speed 10)

(def frametime (/ 1.0 fps))

(defn make-gap [x y width color]
  {:x x :y y :width width :color color})

(defn rand-between [low high]
  (+ low (rand (- high low))))

(defn make-rand-gap [start-x]
  (let [rand-gap (rand-between 100 300)
        rand-y (rand-between 200 400)
        rand-width (rand-between 200 500)
        rand-col #(rand-int 255)
        rand-color (str "rgb(" (rand-col) "," (rand-col) "," (rand-col) ")")]
    (make-gap (+ rand-gap start-x) rand-y rand-width rand-color)))


(defn make-rand-gaps [total]
  (loop [gaps '()
         num-gaps 0
         last-x 500]
    (if (> num-gaps total)
      gaps
      (let [gap (make-rand-gap last-x)]
        (recur (conj gaps gap) (inc num-gaps) (+ (:x gap) (:width gap)))))))

(def gaps
  (into []
        (conj
            (make-rand-gaps 20)
            (merge (make-gap 0 start-y 500 "blue") {:start true}))))


(defonce app-state
  (atom {:player {:x 100 :y start-y :vy 0 :jumps 0}
         :ground (first gaps)
         :gaps gaps
         :camera 0
         :t 0
         :dt frametime}))

(defn draw-player [state]
  (let [player (:player state)
        camera (:camera state)]
    [:img {:src "img/running.gif"
           :style {:position "absolute"
                   :left (- (:x player) 20 camera)
                   :top (- (:y player) 33)
                   :width 40}}]))


(defn draw-block [{:keys [x width color] :as block} extra-styles camera]
  (let [block-style {:position "absolute"
                     :left (- x camera)
                     :width width
                     :backgroundColor color }]
    ^{:key color} [:div {:style (merge block-style extra-styles)}]))


(defn draw-bottom-block [{:keys [x y width color] :as block} camera]
  (draw-block block {:height 500 :top y } camera))


(defn draw-top-block [{:keys [x y width color] :as block} camera]
  (draw-block block {:height (- y block-height) :top 0 } camera))


(defn draw-gap [{:keys [x y width color start] :as block} camera]
  [:div
   (if start [:div] [draw-top-block block camera])
   [draw-bottom-block block camera]])

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
  (let [ground (is-on-ground state)
        diff (- (:y (:player state)) (:y ground))]
    (if (and ground (< diff 30)) 
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
          (assoc-in state [:ground] nil))))))

(defn move-left [state]
  (let [dt (:dt state)
        camera (:camera state)
        newcamera (+ camera dt scroll-speed)]
    (-> state
        (assoc-in [:player :x] (+ 100 newcamera))
        (assoc-in [:camera] newcamera))))

(defn inside-axis [val min max]
  (and (>= val min) (< val max)))

(defn colliding-with-ground [ground player]
  (if (and ground (not (:start ground)))
    (let [fudge 1
          px (:x player)
          py (:y player)
          gx (:x ground)
          gymax (+ fudge (:y ground))
          gy (- (:y ground) block-height fudge)
          gxmax (+ (:width ground) (:x ground))
          inside-x (inside-axis px gx gxmax)
          inside-y (inside-axis py gy gymax)
          is-colliding (and inside-x (not inside-y))]
      is-colliding)
    false))

(defn check-if-dead [state]
  (let [player (:player state)
        ground (:ground state)
        px (:x player)
        py (:y player)
        offscreen (> py 1000)
        colliding (colliding-with-ground ground player)]
    (if (or offscreen colliding)
      (assoc-in state [:dead] true)
      state)))

(defn tick-state [state]
  (let [dt (:dt state)
        dead (:dead state)]
    (if dead
      (println "died.")
      (-> state
          (physics)
          (apply-gravity dt)
          (snap-to-ground)
          (check-if-dead)
          (update-ground)
          (move-left)))))

(defn tick-time []
  (swap! app-state #(-> %
                        (assoc-in [:t] (+ (* time-speed frametime) (:t %)))
                        (assoc-in [:dt] (* time-speed frametime))
                        (tick-state))))

(defn draw-state [state]
  [:div
   (map (fn [tup] [:pre (str (first tup)) " -> " (str (second tup))]) (select-keys state [:player :ground]))])

(defonce key-already-down (atom false))

(defonce timer (js/setInterval tick-time (* 1000 frametime)))

(defn page []
  (let [state @app-state]
    (if (:dead state)
      (do
        (js/clearInterval timer)
        [:h1 "dead."])
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
  ;    [draw-state state]
       [draw-player state]
       [draw-gap (:ground state) (:camera state)]
       (map #(draw-gap % (:camera state)) (:gaps state))])))


(defn main []
  (reagent/render-component [page] (.getElementById js/document "app")))
