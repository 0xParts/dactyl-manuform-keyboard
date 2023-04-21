(ns dactyl-keyboard.dactyl
    (:refer-clojure :exclude [use import])
    (:require [clojure.core.matrix :refer [array matrix mmul]]
              [scad-clj.scad :refer :all]
              [scad-clj.model :refer :all]
              [unicode-math.core :refer :all])
    ;(use (incanter core stats charts io)))
)

;;;;;;;;;;;;;;;;;;
;; Added method ;;
;;;;;;;;;;;;;;;;;;

(defmethod write-expr :fill [depth [form & block]]
  (concat
    (list (indent depth) "fill () {\n")
    (mapcat #(write-expr (inc depth) %1) block)
    (list (indent depth) "}\n")))

(defn fill [ & block]
  `(:fill ~block))


;(defn load-var [rowsy colsy thumby]
;  (let [arg1 (+ rowsy colsy)]
;  (println "arg1: " arg1)
;  )
;)
;(param 1 2 3)

;(println "Whaaat: " arg1)

;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;

(defn deg2rad [degrees]
  (* (/ degrees 180) pi))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Wrist rest parameters ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def hand-rest-width 70)
(def hand-rest-length 100)
(def hand-rest-height 40)
(def hand-rest-top 0.9) ; side angles of hand rest, smaller number, smaller top
(def hand-rest-radius 3)
(def hand-rest-x 15) ;front-back tilt
(def hand-rest-y 8) ;left-right tilt
(def hand-rest-lip-width 1)
(def hand-rest-lip-height 5)
(def hand-rest-position-x 15)
(def hand-rest-position-y -114)

(def pad-r 5.25)
(def pad-z 1.5)
(def pad-off 6.5) ; pad offset from edge on hand-rest

; placement of hooks on case --->>> changed to magnet
(def hook1-placement-x 3.5)
(def hook1-placement-y (+ -59.54 1))
(def hook2-placement-x 25)
(def hook2-placement-y (+ -50.65 1))

;;;;;;;;;;;;;;;;;;;;;;
;; Shape parameters ;;
;;;;;;;;;;;;;;;;;;;;;;

;(def nrows 6)
;(def ncols 6)

(def α (/ π 12))                        ; curvature of the columns
(def β (/ π 36))                        ; curvature of the rows
(def centerrow (- nrows 3))             ; controls front-back tilt
(def centercol 3)                       ; controls left-right tilt / tenting (higher number is more tenting)
(def tenting-angle (/ π 12))            ; or, change this for more precise tenting control

(def pinky-15u false)                   ; controls whether the outer column uses 1.5u keys
(def first-15u-row 0)                   ; controls which should be the first row to have 1.5u keys on the outer column
(def last-15u-row 3)                    ; controls which should be the last row to have 1.5u keys on the outer column
(def extra-row false)                   ; adds an extra bottom row to the outer columns
(def inner-column false)                ; adds an extra inner column (two less rows than nrows)
;(def thumb-style "tightly")             ; toggles between "default", "mini", "cf" and "tightly" thumb cluster

(def column-style :standard)

(defn column-offset [column]
  (if inner-column
    (cond (<= column 1) [0 -2 0]
          (= column 3) [0 2.82 -4.5]
          (>= column 5) [0 -12 5.64]    ; original [0 -5.8 5.64]
          :else [0 0 0])
    (cond (= column 2) [0 2.82 -4.5]
          (>= column 4) [0 -12 5.64]    ; original [0 -5.8 5.64]
          :else [0 0 0])))

(def thumb-offsets
  (case thumb-style
    "default" [6 -3 7]
    "mini" [6 -3 7]
    "cf" [6 -3 7]
    "tightly" [8 -5 1])); 6 -3 7

(def keyboard-z-offset 14.5);9               ; controls overall height; original=9 with centercol=3; use 16 for centercol=2

(def extra-width 2.5)                   ; extra space between the base of keys; original= 2
(def extra-height 1)                  ; original= 0.5

(def wall-z-offset -8);-8                 ; length of the first downward-sloping part of the wall (negative)
(def wall-xy-offset 5);5                  ; offset in the x and/or y direction for the first downward-sloping part of the wall (negative)
(def wall-thickness 2)                  ; wall thickness parameter; originally 5

;; Settings for column-style == :fixed
;; The defaults roughly match Maltron settings
;; http://patentimages.storage.googleapis.com/EP0219944A2/imgf0002.png
;; Fixed-z overrides the z portion of the column ofsets above.
;; NOTE: THIS DOESN'T WORK QUITE LIKE I'D HOPED.
(def fixed-angles [(deg2rad 10) (deg2rad 10) 0 0 0 (deg2rad -15) (deg2rad -15)])
(def fixed-x [-41.5 -22.5 0 20.3 41.4 65.5 89.6])  ; relative to the middle finger
(def fixed-z [12.1    8.3 0  5   10.7 14.5 17.5])
(def fixed-tenting (deg2rad 0))

; If you use Cherry MX or Gateron switches, this can be turned on.
; If you use other switches such as Kailh, you should set this as false
(def create-side-nubs? false)

; If you want hot swap sockets enable this
(def hot-swap true)

; Show keycaps on the keyboard
(def show-caps false)

;;;;;;;;;;;;;;;;;;;;;;;
;; General variables ;;
;;;;;;;;;;;;;;;;;;;;;;;

(def lastrow (dec nrows))
(def cornerrow (dec lastrow))
(def lastcol (dec ncols))
(def extra-cornerrow (if extra-row lastrow cornerrow))
(def innercol-offset (if inner-column 1 0))

;;;;;;;;;;;;;;;;;
;; Switch Hole ;;
;;;;;;;;;;;;;;;;;

(def keyswitch-height 14.15)
(def keyswitch-width 14.15)

(def sa-profile-key-height 12.7)

(def plate-thickness 3.5)
(def side-nub-thickness 4)
(def retention-tab-thickness 1.5)
(def retention-tab-hole-thickness (- (+ plate-thickness 0.5) retention-tab-thickness))
(def mount-width (+ keyswitch-width 3.2))
(def mount-height (+ keyswitch-height 2.7))

(def socket-height-adjust 1.2)

(def hot-socket

  (difference
   
    (difference
      (translate [0 0 (- -2.05 (/ socket-height-adjust 2))]
        (cube (+ keyswitch-height 3.6) (+ keyswitch-width 3) (+ 3.1 socket-height-adjust))
      )
      (translate [0 0 (- (/ socket-height-adjust -2) 0.5)]
        (cube keyswitch-height keyswitch-width socket-height-adjust)
      )
    )

    ; corner hot-fix for tightly
    (rotate [0 (deg2rad 25) (deg2rad 40)]
      (translate [16.5 3 5]
        (cube 10 10 10)
      )
    )

    ; hot-swap socket hole
    (scale [1 1 1]
    (translate [0.075 4.815 (- -2.75 socket-height-adjust)]
      (union
        ; cube1
        (cube 119.6 114.1 2)

        ; circle1
        (translate [-4.8 0.55 0]
          (binding [*fn* 100] (cylinder 1.5 2))
        )

        (translate [-3.35 -1.75 0]
          (difference
            ;cube2
            (cube 5.9 4.6 2)
            ;circle2
            (translate [2.95 -2.55 0]
              (binding [*fn* 100] (cylinder 2.25 2))
            )
          )
        )
        (translate [6 0.325 0]
          (cube 6 1.8 2)
        )
        (translate [-6 -2.215 0]
          (cube 6 1.8 2)
        )
        (translate [2.475 0.325 0]
          (binding [*fn* 200] (cylinder 1.7 20))
        )
        (translate [-3.875 -2.215 0]
          (binding [*fn* 200] (cylinder 1.7 20))
        )
      )
    )
    (binding [*fn* 100] (cylinder 2.3 20))
    (translate [-5.08 0 0]
      (binding [*fn* 100] (cylinder 1.1 20))
      (translate [2 -0.4 0]
        (cube 4 4 10)
      )
    )
    (translate [5.08 0 0]
      (binding [*fn* 100] (cylinder 1.1 20))
    )
    )
    (translate [0 (/ (+ keyswitch-width 3) -4) (- -2.05 socket-height-adjust) ]
      (cube (+ keyswitch-height 3.6) (/ (+ keyswitch-width 3) 2) 3.1)
    )
    ;(binding [*fn* 50] (cylinder 2 2))
  )
)

(def fill-caps
  (hull
  (union
    (translate [0 0 (- 9.75 0)]
      (cube 18.3 18.3 0.1)
    )
    (translate [0 0 (- 17.25 0)]
      (cube 12.2 12.2 0.1)
    )
  )
  )
)

(def single-plate
  (let [top-wall (->> (cube (+ keyswitch-width 3) 1.5 (+ plate-thickness 0.5))
                      (translate [0
                                  (+ (/ 1.5 2) (/ keyswitch-height 2))
                                  (- (/ plate-thickness 2) 0.25)]))
        left-wall (->> (cube 1.8 (+ keyswitch-height 3) (+ plate-thickness 0.5))
                       (translate [(+ (/ 1.8 2) (/ keyswitch-width 2))
                                   0
                                   (- (/ plate-thickness 2) 0.25)]))
        side-nub (->> (binding [*fn* 30] (cylinder 1 2.75))
                      (rotate (/ π 2) [1 0 0])
                      (translate [(+ (/ keyswitch-width 2)) 0 1])
                      (hull (->> (cube 1.5 2.75 side-nub-thickness)
                                 (translate [(+ (/ 1.5 2) (/ keyswitch-width 2))
                                             0
                                             (/ side-nub-thickness 2)])))
                      (translate [0 0 (- plate-thickness side-nub-thickness)]))
        plate-half (union top-wall left-wall (if create-side-nubs? (with-fn 100 side-nub)))
        top-nub (->> (cube 5 5 retention-tab-hole-thickness)
                     (translate [(+ (/ keyswitch-width 2.5)) 0 (- (/ retention-tab-hole-thickness 2) 0.5)]))
        top-nub-pair (union top-nub
                            (->> top-nub
                                 (mirror [1 0 0])
                                 (mirror [0 1 0])))]
    (difference
     (union plate-half
            (->> plate-half
                 (mirror [1 0 0])
                 (mirror [0 1 0]))
            (if hot-swap (mirror [0 0 0] hot-socket))
            (if show-caps fill-caps)
            )
     (->>
      top-nub-pair
      (rotate (/ π 2) [0 0 1])))))

;(spit "things/cap_test.scad"
;      (write-scad
;        (union
;          single-plate
;        )
;      )
;)


;(spit "things/socket_test.scad"
;      (write-scad
;        (union
;          hot-socket
;        )
;      )
;)

;;;;;;;;;;;;;;;;
;; SA Keycaps ;;
;;;;;;;;;;;;;;;;

(def sa-length 18.415)
(def sa-double-length 37.5)
(def sa-cap {1 (let [bl2 (/ sa-length 2)
                     m (/ 17 2)
                     key-cap (hull (->> (polygon [[bl2 bl2] [bl2 (- bl2)] [(- bl2) (- bl2)] [(- bl2) bl2]])
                                        (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                        (translate [0 0 0.05]))
                                   (->> (polygon [[m m] [m (- m)] [(- m) (- m)] [(- m) m]])
                                        (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                        (translate [0 0 3.7]))
                                   (->> (polygon [[6 6] [6 -6] [-6 -6] [-6 6]])
                                        (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                        (translate [0 0 7.4])))]
                 (->> key-cap
                      (translate [0 0 (+ 6 plate-thickness)])
                      (color [220/255 163/255 163/255 1])))
             2 (let [bl2 sa-length
                     bw2 (/ sa-length 2)
                     key-cap (hull (->> (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
                                        (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                        (translate [0 0 0.05]))
                                   (->> (polygon [[6 16] [6 -16] [-6 -16] [-6 16]])
                                        (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                        (translate [0 0 12])))]
                 (->> key-cap
                      (translate [0 0 (+ 5 plate-thickness)])
                      (color [127/255 159/255 127/255 1])))
             1.5 (let [bl2 (/ sa-length 2)
                       bw2 (/ (* sa-length 1.5) 2)
                       key-cap (hull (->> (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
                                          (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                          (translate [0 0 0.05]))
                                     (->> (polygon [[11 6] [-11 6] [-11 -6] [11 -6]])
                                          (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                          (translate [0 0 7.4])))]
                   (->> key-cap
                        (translate [0 0 (+ 5 plate-thickness)])
                        (color [240/255 223/255 175/255 1])))})

;; Fill the keyholes instead of placing a a keycap over them
(def keyhole-fill (->> (cube keyswitch-height keyswitch-width plate-thickness)
                       (translate [0 0 (/ plate-thickness 2)])))

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Placement Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(def columns (range (+ innercol-offset 0) ncols))
(def rows (range 0 nrows))

(def innercolumn 0)
(def innerrows (range 0 (- nrows 2)))

(def cap-top-height (+ plate-thickness sa-profile-key-height))
(def row-radius (+ (/ (/ (+ mount-height extra-height) 2)
                      (Math/sin (/ α 2)))
                   cap-top-height))
(def column-radius (+ (/ (/ (+ mount-width extra-width) 2)
                         (Math/sin (/ β 2)))
                      cap-top-height))
(def column-x-delta (+ -1 (- (* column-radius (Math/sin β)))))

(defn offset-for-column [col, row]
  (if (and pinky-15u
           (= col lastcol)
           (<= row last-15u-row)
           (>= row first-15u-row))
    4.7625
    0))

(defn apply-key-geometry [translate-fn rotate-x-fn rotate-y-fn column row shape]
  (let [column-angle (* β (- centercol column))
        placed-shape (->> shape
                          (translate-fn [(offset-for-column column, row) 0 (- row-radius)])
                          (rotate-x-fn  (* α (- centerrow row)))
                          (translate-fn [0 0 row-radius])
                          (translate-fn [0 0 (- column-radius)])
                          (rotate-y-fn  column-angle)
                          (translate-fn [0 0 column-radius])
                          (translate-fn (column-offset column)))
        column-z-delta (* column-radius (- 1 (Math/cos column-angle)))
        placed-shape-ortho (->> shape
                                (translate-fn [0 0 (- row-radius)])
                                (rotate-x-fn  (* α (- centerrow row)))
                                (translate-fn [0 0 row-radius])
                                (rotate-y-fn  column-angle)
                                (translate-fn [(- (* (- column centercol) column-x-delta)) 0 column-z-delta])
                                (translate-fn (column-offset column)))
        placed-shape-fixed (->> shape
                                (rotate-y-fn  (nth fixed-angles column))
                                (translate-fn [(nth fixed-x column) 0 (nth fixed-z column)])
                                (translate-fn [0 0 (- (+ row-radius (nth fixed-z column)))])
                                (rotate-x-fn  (* α (- centerrow row)))
                                (translate-fn [0 0 (+ row-radius (nth fixed-z column))])
                                (rotate-y-fn  fixed-tenting)
                                (translate-fn [0 (second (column-offset column)) 0]))]
    (->> (case column-style
               :orthographic placed-shape-ortho
               :fixed        placed-shape-fixed
               placed-shape)
         (rotate-y-fn  tenting-angle)
         (translate-fn [0 0 keyboard-z-offset]))))

(defn key-place [column row shape]
  (apply-key-geometry translate
                      (fn [angle obj] (rotate angle [1 0 0] obj))
                      (fn [angle obj] (rotate angle [0 1 0] obj))
                      column row shape))

(defn rotate-around-x [angle position]
  (mmul
   [[1 0 0]
    [0 (Math/cos angle) (- (Math/sin angle))]
    [0 (Math/sin angle)    (Math/cos angle)]]
   position))

(defn rotate-around-y [angle position]
  (mmul
   [[(Math/cos angle)     0 (Math/sin angle)]
    [0                    1 0]
    [(- (Math/sin angle)) 0 (Math/cos angle)]]
   position))

(defn key-position [column row position]
  (apply-key-geometry (partial map +) rotate-around-x rotate-around-y column row position))

(def key-holes
  (apply union
         (for [column columns
               row rows
               :when (or (.contains [(+ innercol-offset 2) (+ innercol-offset 3)] column)
                         (and (.contains [(+ innercol-offset 4) (+ innercol-offset 5)] column) extra-row (= ncols (+ innercol-offset 6)))
                         (and (.contains [(+ innercol-offset 4)] column) extra-row (= ncols (+ innercol-offset 5)))
                         (and inner-column (not= row cornerrow)(= column 0))
                         (not= row lastrow))]
           (->> single-plate
                ;                (rotate (/ π 2) [0 0 1])
                (key-place column row)))))
(def key-holes-left
  (apply union
         (for [column columns
               row rows
               :when (or (.contains [(+ innercol-offset 2) (+ innercol-offset 3)] column)
                         (and (.contains [(+ innercol-offset 4) (+ innercol-offset 5)] column) extra-row (= ncols (+ innercol-offset 6)))
                         (and (.contains [(+ innercol-offset 4)] column) extra-row (= ncols (+ innercol-offset 5)))
                         (and inner-column (not= row cornerrow)(= column 0))
                         (not= row lastrow))]
           (->> (mirror [1 0 0] single-plate)
                ;                (rotate (/ π 2) [0 0 1])
                (key-place column row)))))
(def caps
  (apply union
         (conj (for [column columns
               row rows
               :when (or (and (= column 0) (< row 3))
                         (and (.contains [1 2] column) (< row 4))
                         (.contains [3 4 5 6] column))]
               (->> (sa-cap (if (and pinky-15u (= column lastcol) (not= row lastrow)) 1.5 1))
                    (key-place column row)))
               (list (key-place 0 0 (sa-cap 1))
                 (key-place 0 1 (sa-cap 1))
                 (key-place 0 2 (sa-cap 1))))))

(def caps-fill
  (apply union
         (conj (for [column columns
               row rows
               :when (or (.contains [(+ innercol-offset 2) (+ innercol-offset 3)] column)
                         (and (.contains [(+ innercol-offset 4) (+ innercol-offset 5)] column) extra-row (= ncols (+ innercol-offset 6)))
                         (and (.contains [(+ innercol-offset 4)] column) extra-row (= ncols (+ innercol-offset 5)))
                         (and inner-column (not= row cornerrow)(= column 0))
                         (not= row lastrow))]
                 (key-place column row keyhole-fill))
               (list (key-place 0 0 keyhole-fill)
                 (key-place 0 1 keyhole-fill)
                 (key-place 0 2 keyhole-fill)))))

;placement for the innermost column
(def key-holes-inner
  (if inner-column
    (apply union
           (for [row innerrows]
             (->> single-plate
                  ;               (rotate (/ π 2) [0 0 1])
                  (key-place 0 row))))))

;;;;;;;;;;;;;;;;;;;;
;; Web Connectors ;;
;;;;;;;;;;;;;;;;;;;;

(def web-thickness 4)
(def post-size 0.1)
(def web-post (->> (cube post-size post-size web-thickness)
                   (translate [0 0 (+ (/ web-thickness -2)
                                      plate-thickness)])))

(def post-adj (/ post-size 2))
(def web-post-tr (translate [(- (/ mount-width 1.95) post-adj) (- (/ mount-height 1.95) post-adj) 0] web-post))
(def web-post-tl (translate [(+ (/ mount-width -1.95) post-adj) (- (/ mount-height 1.95) post-adj) 0] web-post))
(def web-post-bl (translate [(+ (/ mount-width -1.95) post-adj) (+ (/ mount-height -1.95) post-adj) 0] web-post))
(def web-post-br (translate [(- (/ mount-width 1.95) post-adj) (+ (/ mount-height -1.95) post-adj) 0] web-post))

; wide posts for 1.5u keys in the main cluster
(if pinky-15u
  (do (def wide-post-tr (translate [(- (/ mount-width 1.2) post-adj)  (- (/ mount-height  2) post-adj) 0] web-post))
    (def wide-post-tl (translate [(+ (/ mount-width -1.2) post-adj) (- (/ mount-height  2) post-adj) 0] web-post))
    (def wide-post-bl (translate [(+ (/ mount-width -1.2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))
    (def wide-post-br (translate [(- (/ mount-width 1.2) post-adj)  (+ (/ mount-height -2) post-adj) 0] web-post)))
  (do (def wide-post-tr web-post-tr)
    (def wide-post-tl web-post-tl)
    (def wide-post-bl web-post-bl)
    (def wide-post-br web-post-br)))

(defn triangle-hulls [& shapes]
  (apply union
         (map (partial apply hull)
              (partition 3 1 shapes))))

(def connectors
  (apply union
         (concat
          ;; Row connections
          (for [column (range (+ innercol-offset 0) (dec ncols))
                row (range 0 lastrow)]
            (triangle-hulls
             (key-place (inc column) row web-post-tl)
             (key-place column row web-post-tr)
             (key-place (inc column) row web-post-bl)
             (key-place column row web-post-br)))

          ;; Column connections
          (for [column columns
                row (range 0 cornerrow)]
            (triangle-hulls
             (key-place column row web-post-bl)
             (key-place column row web-post-br)
             (key-place column (inc row) web-post-tl)
             (key-place column (inc row) web-post-tr)))

          ;; Diagonal connections
          (for [column (range 0 (dec ncols))
                row (range 0 cornerrow)]
            (triangle-hulls
             (key-place column row web-post-br)
             (key-place column (inc row) web-post-tr)
             (key-place (inc column) row web-post-bl)
             (key-place (inc column) (inc row) web-post-tl))))))

(def inner-connectors
  (if inner-column
    (apply union
           (concat
            ;; Row connections
            (for [column (range 0 1)
                  row (range 0 (- nrows 2))]
              (triangle-hulls
               (key-place (inc column) row web-post-tl)
               (key-place column row web-post-tr)
               (key-place (inc column) row web-post-bl)
               (key-place column row web-post-br)))

            ;; Column connections
            (for [row (range 0 (dec cornerrow))]
              (triangle-hulls
               (key-place innercolumn row web-post-bl)
               (key-place innercolumn row web-post-br)
               (key-place innercolumn (inc row) web-post-tl)
               (key-place innercolumn (inc row) web-post-tr)))

            ;; Diagonal connections
            (for [column (range 0 (dec ncols))
                  row (range 0 2)]
              (triangle-hulls
               (key-place column row web-post-br)
               (key-place column (inc row) web-post-tr)
               (key-place (inc column) row web-post-bl)
               (key-place (inc column) (inc row) web-post-tl)))))))

(def extra-connectors
  (if extra-row
    (apply union
           (concat
            (for [column (range 3 ncols)
                  row (range cornerrow lastrow)]
              (triangle-hulls
               (key-place column row web-post-bl)
               (key-place column row web-post-br)
               (key-place column (inc row) web-post-tl)
               (key-place column (inc row) web-post-tr)))

            (for [column (range 3 (dec ncols))
                  row (range cornerrow lastrow)]
              (triangle-hulls
               (key-place column row web-post-br)
               (key-place column (inc row) web-post-tr)
               (key-place (inc column) row web-post-bl)
               (key-place (inc column) (inc row) web-post-tl)))

            (for [column (range 4 (dec ncols))
                  row (range lastrow nrows)]
              (triangle-hulls
               (key-place (inc column) row web-post-tl)
               (key-place column row web-post-tr)
               (key-place (inc column) row web-post-bl)
               (key-place column row web-post-br)))))))

;;;;;;;;;;;;;;;;;;;
;; Default Thumb ;;
;;;;;;;;;;;;;;;;;;;

(def thumborigin
  (map + (key-position (+ innercol-offset 1) cornerrow [(/ mount-width 2) (- (/ mount-height 2)) 0])
       thumb-offsets))

(defn thumb-tr-place [shape]
  (->> shape
       (rotate (deg2rad  10) [1 0 0])
       (rotate (deg2rad -23) [0 1 0])
       (rotate (deg2rad  10) [0 0 1])
       (translate thumborigin)
       (translate [-12 -16 3])
       ))
(defn thumb-tl-place [shape]
  (->> shape
       (rotate (deg2rad  10) [1 0 0])
       (rotate (deg2rad -23) [0 1 0])
       (rotate (deg2rad  10) [0 0 1])
       (translate thumborigin)
       (translate [-32 -15 -2])))
(defn thumb-mr-place [shape]
  (->> shape
       (rotate (deg2rad  -6) [1 0 0])
       (rotate (deg2rad -34) [0 1 0])
       (rotate (deg2rad  48) [0 0 1])
       (translate thumborigin)
       (translate [-29 -40 -13])
       ))
(defn thumb-ml-place [shape]
  (->> shape
       (rotate (deg2rad   6) [1 0 0])
       (rotate (deg2rad -34) [0 1 0])
       (rotate (deg2rad  40) [0 0 1])
       (translate thumborigin)
       (translate [-51 -25 -12])))
(defn thumb-br-place [shape]
  (->> shape
       (rotate (deg2rad -16) [1 0 0])
       (rotate (deg2rad -33) [0 1 0])
       (rotate (deg2rad  54) [0 0 1])
       (translate thumborigin)
       (translate [-37.8 -55.3 -25.3])
       ))
(defn thumb-bl-place [shape]
  (->> shape
       (rotate (deg2rad  -4) [1 0 0])
       (rotate (deg2rad -35) [0 1 0])
       (rotate (deg2rad  52) [0 0 1])
       (translate thumborigin)
       (translate [-56.3 -43.3 -23.5])
       ))

(defn thumb-1x-layout [shape]
  (union
   (thumb-mr-place shape)
   (thumb-ml-place shape)
   (thumb-br-place shape)
   (thumb-bl-place shape)))

(defn thumb-15x-layout [shape]
  (union
   (thumb-tr-place shape)
   (thumb-tl-place shape)))

(def larger-plate
  (let [plate-height (/ (- sa-double-length mount-height) 3)
        top-plate (->> (cube mount-width plate-height web-thickness)
                       (translate [0 (/ (+ plate-height mount-height) 2)
                                   (- plate-thickness (/ web-thickness 2))]))
        ]
    (union top-plate (mirror [0 1 0] top-plate))))

(def larger-plate-half
  (let [plate-height (/ (- sa-double-length mount-height) 3)
        top-plate (->> (cube mount-width plate-height web-thickness)
                       (translate [0 (/ (+ plate-height mount-height) 2)
                                   (- plate-thickness (/ web-thickness 2))]))
        ]
    (union top-plate (mirror [0 0 0] top-plate))))

(def thumbcaps
  (union
   (thumb-1x-layout (sa-cap 1))
   (thumb-15x-layout (rotate (/ π 2) [0 0 1] (sa-cap 1.5)))))

(def thumbcaps-fill
  (union
   (thumb-1x-layout keyhole-fill)
   (thumb-15x-layout (rotate (/ π 2) [0 0 1] keyhole-fill))))

(def thumb
  (union
   (thumb-1x-layout (rotate (/ π 2) [0 0 0] single-plate))
   (thumb-tr-place (rotate (/ π 2) [0 0 1] single-plate))
   (thumb-tr-place larger-plate)
   (thumb-tl-place (rotate (/ π 2) [0 0 1] single-plate))
   (thumb-tl-place larger-plate-half)))

(def thumb-left
  (union
   (thumb-1x-layout (rotate (/ π 2) [0 0 0] (mirror [1 0 0] single-plate)))
   (thumb-tr-place (rotate (/ π 2) [0 0 1] (mirror [1 0 0] single-plate)))
   (thumb-tr-place larger-plate)
   (thumb-tl-place (rotate (/ π 2) [0 0 1] (mirror [1 0 0] single-plate)))
   (thumb-tl-place larger-plate-half)))

(def thumb-post-tr (translate [(- (/ mount-width 2) post-adj)  (- (/ mount-height  1.1) post-adj) 0] web-post))
(def thumb-post-tl (translate [(+ (/ mount-width -2) post-adj) (- (/ mount-height  1.1) post-adj) 0] web-post))
(def thumb-post-bl (translate [(+ (/ mount-width -2) post-adj) (+ (/ mount-height -1.1) post-adj) 0] web-post))
(def thumb-post-br (translate [(- (/ mount-width 2) post-adj)  (+ (/ mount-height -1.1) post-adj) 0] web-post))

(def thumb-connectors
  (union
   (triangle-hulls    ; top two
    (thumb-tl-place thumb-post-tr)
    (thumb-tl-place (translate [-0.33 -0.25 0] web-post-br))
    (thumb-tr-place thumb-post-tl)
    (thumb-tr-place thumb-post-bl))
   (triangle-hulls    ; bottom two on the right
    (thumb-br-place web-post-tr)
    (thumb-br-place web-post-br)
    (thumb-mr-place web-post-tl)
    (thumb-mr-place web-post-bl))
   (triangle-hulls    ; bottom two on the left
    (thumb-bl-place web-post-tr)
    (thumb-bl-place web-post-br)
    (thumb-ml-place web-post-tl)
    (thumb-ml-place web-post-bl))
   (triangle-hulls    ; centers of the bottom four
    (thumb-br-place web-post-tl)
    (thumb-bl-place web-post-bl)
    (thumb-br-place web-post-tr)
    (thumb-bl-place web-post-br)
    (thumb-mr-place web-post-tl)
    (thumb-ml-place web-post-bl)
    (thumb-mr-place web-post-tr)
    (thumb-ml-place web-post-br))
   (triangle-hulls    ; top two to the middle two, starting on the left
    (thumb-tl-place thumb-post-tl)
    (thumb-ml-place web-post-tr)
    (thumb-tl-place (translate [0.25 0.1 0] web-post-bl))
    (thumb-ml-place web-post-br)
    (thumb-tl-place (translate [-0.33 -0.25 0] web-post-br))
    (thumb-mr-place web-post-tr)
    (thumb-tr-place thumb-post-bl)
    (thumb-mr-place web-post-br)
    (thumb-tr-place thumb-post-br))
   (triangle-hulls    ; top two to the main keyboard, starting on the left
    (thumb-tl-place thumb-post-tl)
    (key-place (+ innercol-offset 0) cornerrow web-post-bl)
    (thumb-tl-place thumb-post-tr)
    (key-place (+ innercol-offset 0) cornerrow web-post-br)
    (thumb-tr-place thumb-post-tl)
    (key-place (+ innercol-offset 1) cornerrow web-post-bl)
    (thumb-tr-place thumb-post-tr)
    (key-place (+ innercol-offset 1) cornerrow web-post-br)
    (key-place (+ innercol-offset 2) lastrow web-post-tl)
    (key-place (+ innercol-offset 2) lastrow web-post-bl)
    (thumb-tr-place thumb-post-tr)
    (key-place (+ innercol-offset 2) lastrow web-post-bl)
    (thumb-tr-place thumb-post-br)
    (key-place (+ innercol-offset 2) lastrow web-post-br)
    (key-place (+ innercol-offset 3) lastrow web-post-bl)
    (key-place (+ innercol-offset 2) lastrow web-post-tr)
    (key-place (+ innercol-offset 3) lastrow web-post-tl)
    (key-place (+ innercol-offset 3) cornerrow web-post-bl)
    (key-place (+ innercol-offset 3) lastrow web-post-tr)
    (key-place (+ innercol-offset 3) cornerrow web-post-br))
   (triangle-hulls
    (key-place (+ innercol-offset 1) cornerrow web-post-br)
    (key-place (+ innercol-offset 2) lastrow web-post-tl)
    (key-place (+ innercol-offset 2) cornerrow web-post-bl)
    (key-place (+ innercol-offset 2) lastrow web-post-tr)
    (key-place (+ innercol-offset 2) cornerrow web-post-br)
    (key-place (+ innercol-offset 3) cornerrow web-post-bl))
   (if extra-row
     (union
      (triangle-hulls
       (key-place (+ innercol-offset 3) lastrow web-post-tr)
       (key-place (+ innercol-offset 3) lastrow web-post-br)
       (key-place (+ innercol-offset 4) lastrow web-post-tl)
       (key-place (+ innercol-offset 4) lastrow web-post-bl))
      (triangle-hulls
       (key-place (+ innercol-offset 3) lastrow web-post-tr)
       (key-place (+ innercol-offset 3) cornerrow web-post-br)
       (key-place (+ innercol-offset 4) lastrow web-post-tl)
       (key-place (+ innercol-offset 4) cornerrow web-post-bl)))
     (union
      (triangle-hulls
       (key-place (+ innercol-offset 3) lastrow web-post-tr)
       (key-place (+ innercol-offset 3) lastrow web-post-br)
       (key-place (+ innercol-offset 4) cornerrow web-post-bl))
      (triangle-hulls
       (key-place (+ innercol-offset 3) lastrow web-post-tr)
       (key-place (+ innercol-offset 3) cornerrow web-post-br)
       (key-place (+ innercol-offset 4) cornerrow web-post-bl))))))

;;;;;;;;;;;;;;;;
;; Mini Thumb ;;
;;;;;;;;;;;;;;;;

(defn minithumb-tr-place [shape]
  (->> shape
       (rotate (deg2rad  14) [1 0 0])
       (rotate (deg2rad -15) [0 1 0])
       (rotate (deg2rad  10) [0 0 1]) ; original 10
       (translate thumborigin)
       (translate [-15 -10 5]))) ; original 1.5u  (translate [-12 -16 3])
(defn minithumb-tl-place [shape]
  (->> shape
       (rotate (deg2rad  10) [1 0 0])
       (rotate (deg2rad -23) [0 1 0])
       (rotate (deg2rad  25) [0 0 1]) ; original 10
       (translate thumborigin)
       (translate [-35 -16 -2]))) ; original 1.5u (translate [-32 -15 -2])))
(defn minithumb-mr-place [shape]
  (->> shape
       (rotate (deg2rad  10) [1 0 0])
       (rotate (deg2rad -23) [0 1 0])
       (rotate (deg2rad  25) [0 0 1])
       (translate thumborigin)
       (translate [-23 -34 -6])))
(defn minithumb-br-place [shape]
  (->> shape
       (rotate (deg2rad   6) [1 0 0])
       (rotate (deg2rad -34) [0 1 0])
       (rotate (deg2rad  35) [0 0 1])
       (translate thumborigin)
       (translate [-39 -43 -16])))
(defn minithumb-bl-place [shape]
  (->> shape
       (rotate (deg2rad   6) [1 0 0])
       (rotate (deg2rad -32) [0 1 0])
       (rotate (deg2rad  35) [0 0 1])
       (translate thumborigin)
       (translate [-51 -25 -11.5]))) ;        (translate [-51 -25 -12])))

(defn minithumb-1x-layout [shape]
  (union
   (minithumb-mr-place shape)
   (minithumb-br-place shape)
   (minithumb-tl-place shape)
   (minithumb-bl-place shape)))

(defn minithumb-15x-layout [shape]
  (union
   (minithumb-tr-place shape)))

(def minithumbcaps
  (union
   (minithumb-1x-layout (sa-cap 1))
   (minithumb-15x-layout (rotate (/ π 2) [0 0 1] (sa-cap 1)))))

(def minithumbcaps-fill
  (union
   (minithumb-1x-layout keyhole-fill)
   (minithumb-15x-layout (rotate (/ π 2) [0 0 1] keyhole-fill))))

(def minithumb
  (union
   (minithumb-1x-layout single-plate)
   (minithumb-15x-layout single-plate)))

(def minithumb-left
  (union
   (minithumb-1x-layout (mirror [1 0 0] single-plate))
   (minithumb-15x-layout (mirror [1 0 0] single-plate))))


(def minithumb-post-tr (translate [(- (/ mount-width 2) post-adj)  (- (/ mount-height  2) post-adj) 0] web-post))
(def minithumb-post-tl (translate [(+ (/ mount-width -2) post-adj) (- (/ mount-height  2) post-adj) 0] web-post))
(def minithumb-post-bl (translate [(+ (/ mount-width -2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))
(def minithumb-post-br (translate [(- (/ mount-width 2) post-adj)  (+ (/ mount-height -2) post-adj) 0] web-post))

(def minithumb-connectors
  (union
   (triangle-hulls    ; top two
    (minithumb-tl-place web-post-tr)
    (minithumb-tl-place web-post-br)
    (minithumb-tr-place minithumb-post-tl)
    (minithumb-tr-place minithumb-post-bl))
   (triangle-hulls    ; bottom two
    (minithumb-br-place web-post-tr)
    (minithumb-br-place web-post-br)
    (minithumb-mr-place web-post-tl)
    (minithumb-mr-place web-post-bl))
   (triangle-hulls
    (minithumb-mr-place web-post-tr)
    (minithumb-mr-place web-post-br)
    (minithumb-tr-place minithumb-post-br))
   (triangle-hulls    ; between top row and bottom row
    (minithumb-br-place web-post-tl)
    (minithumb-bl-place web-post-bl)
    (minithumb-br-place web-post-tr)
    (minithumb-bl-place web-post-br)
    (minithumb-mr-place web-post-tl)
    (minithumb-tl-place web-post-bl)
    (minithumb-mr-place web-post-tr)
    (minithumb-tl-place web-post-br)
    (minithumb-tr-place web-post-bl)
    (minithumb-mr-place web-post-tr)
    (minithumb-tr-place web-post-br))
   (triangle-hulls    ; top two to the middle two, starting on the left
    (minithumb-tl-place web-post-tl)
    (minithumb-bl-place web-post-tr)
    (minithumb-tl-place web-post-bl)
    (minithumb-bl-place web-post-br)
    (minithumb-mr-place web-post-tr)
    (minithumb-tl-place web-post-bl)
    (minithumb-tl-place web-post-br)
    (minithumb-mr-place web-post-tr))
   (triangle-hulls    ; top two to the main keyboard, starting on the left
    (minithumb-tl-place web-post-tl)
    (key-place (+ innercol-offset 0) cornerrow web-post-bl)
    (minithumb-tl-place web-post-tr)
    (key-place (+ innercol-offset 0) cornerrow web-post-br)
    (minithumb-tr-place minithumb-post-tl)
    (key-place (+ innercol-offset 1) cornerrow web-post-bl)
    (minithumb-tr-place minithumb-post-tr)
    (key-place (+ innercol-offset 1) cornerrow web-post-br)
    (key-place (+ innercol-offset 2) lastrow web-post-tl)
    (key-place (+ innercol-offset 2) lastrow web-post-bl)
    (minithumb-tr-place minithumb-post-tr)
    (key-place (+ innercol-offset 2) lastrow web-post-bl)
    (minithumb-tr-place minithumb-post-br)
    (key-place (+ innercol-offset 2) lastrow web-post-br)
    (key-place (+ innercol-offset 3) lastrow web-post-bl)
    (key-place (+ innercol-offset 2) lastrow web-post-tr)
    (key-place (+ innercol-offset 3) lastrow web-post-tl)
    (key-place (+ innercol-offset 3) cornerrow web-post-bl)
    (key-place (+ innercol-offset 3) lastrow web-post-tr)
    (key-place (+ innercol-offset 3) cornerrow web-post-br)
    )
   (triangle-hulls
    (key-place (+ innercol-offset 1) cornerrow web-post-br)
    (key-place (+ innercol-offset 2) lastrow web-post-tl)
    (key-place (+ innercol-offset 2) cornerrow web-post-bl)
    (key-place (+ innercol-offset 2) lastrow web-post-tr)
    (key-place (+ innercol-offset 2) cornerrow web-post-br)
    (key-place (+ innercol-offset 3) cornerrow web-post-bl))
   (if extra-row
     (union
      (triangle-hulls
       (key-place (+ innercol-offset 3) lastrow web-post-tr)
       (key-place (+ innercol-offset 3) lastrow web-post-br)
       (key-place (+ innercol-offset 4) lastrow web-post-tl)
       (key-place (+ innercol-offset 4) lastrow web-post-bl))
      (triangle-hulls
       (key-place (+ innercol-offset 3) lastrow web-post-tr)
       (key-place (+ innercol-offset 3) cornerrow web-post-br)
       (key-place (+ innercol-offset 4) lastrow web-post-tl)
       (key-place (+ innercol-offset 4) cornerrow web-post-bl)))
     (union
      (triangle-hulls
       (key-place (+ innercol-offset 3) lastrow web-post-tr)
       (key-place (+ innercol-offset 3) lastrow web-post-br)
       (key-place (+ innercol-offset 4) cornerrow web-post-bl))
      (triangle-hulls
       (key-place (+ innercol-offset 3) lastrow web-post-tr)
       (key-place (+ innercol-offset 3) cornerrow web-post-br)
       (key-place (+ innercol-offset 4) cornerrow web-post-bl))))))

;;;;;;;;;;;;;;;;
;; cf Thumb ;;
;;;;;;;;;;;;;;;;

(defn cfthumb-tl-place [shape]
  (->> shape
       (rotate (deg2rad  10) [1 0 0])
       (rotate (deg2rad -24) [0 1 0])
       (rotate (deg2rad  10) [0 0 1])
       (translate thumborigin)
       (translate [-13 -9.8 4])))
(defn cfthumb-tr-place [shape]
  (->> shape
       (rotate (deg2rad  6) [1 0 0])
       (rotate (deg2rad -24) [0 1 0])
       (rotate (deg2rad  10) [0 0 1])
       (translate thumborigin)
       (translate [-7.5 -29.5 0])))
(defn cfthumb-ml-place [shape]
  (->> shape
       (rotate (deg2rad  8) [1 0 0])
       (rotate (deg2rad -31) [0 1 0])
       (rotate (deg2rad  14) [0 0 1])
       (translate thumborigin)
       (translate [-30.5 -17 -6])))
(defn cfthumb-mr-place [shape]
  (->> shape
       (rotate (deg2rad  4) [1 0 0])
       (rotate (deg2rad -31) [0 1 0])
       (rotate (deg2rad  14) [0 0 1])
       (translate thumborigin)
       (translate [-22.2 -41 -10.3])))
(defn cfthumb-br-place [shape]
  (->> shape
       (rotate (deg2rad   2) [1 0 0])
       (rotate (deg2rad -37) [0 1 0])
       (rotate (deg2rad  18) [0 0 1])
       (translate thumborigin)
       (translate [-37 -46.4 -22])))
(defn cfthumb-bl-place [shape]
  (->> shape
       (rotate (deg2rad   6) [1 0 0])
       (rotate (deg2rad -37) [0 1 0])
       (rotate (deg2rad  18) [0 0 1])
       (translate thumborigin)
       (translate [-47 -23 -19])))

(defn cfthumb-1x-layout [shape]
  (union
   (cfthumb-tr-place (rotate (/ π 2) [0 0 0] shape))
   (cfthumb-mr-place shape)
   (cfthumb-br-place shape)
   (cfthumb-tl-place (rotate (/ π 2) [0 0 0] shape))))

(defn cfthumb-15x-layout [shape]
  (union
   (cfthumb-bl-place shape)
   (cfthumb-ml-place shape)))

(def cfthumbcaps
  (union
   (cfthumb-1x-layout (sa-cap 1))
   (cfthumb-15x-layout (rotate (/ π 2) [0 0 1] (sa-cap 1.5)))))

(def cfthumbcaps-fill
  (union
   (cfthumb-1x-layout keyhole-fill)
   (cfthumb-15x-layout (rotate (/ π 2) [0 0 1] keyhole-fill))))

(def cfthumb
  (union
   (cfthumb-1x-layout single-plate)
   (cfthumb-15x-layout larger-plate-half)
   (cfthumb-15x-layout single-plate)))

(def cfthumb-left
  (union
   (cfthumb-1x-layout (mirror [1 0 0] single-plate))
   (cfthumb-15x-layout larger-plate-half)
   (cfthumb-15x-layout (mirror [1 0 0] single-plate))))


(def cfthumb-connectors
  (union
   (triangle-hulls    ; top two
    (cfthumb-tl-place web-post-tl)
    (cfthumb-tl-place web-post-bl)
    (cfthumb-ml-place thumb-post-tr)
    (cfthumb-ml-place web-post-br))
   (triangle-hulls
    (cfthumb-ml-place thumb-post-tl)
    (cfthumb-ml-place web-post-bl)
    (cfthumb-bl-place thumb-post-tr)
    (cfthumb-bl-place web-post-br))
   (triangle-hulls    ; bottom two
    (cfthumb-br-place web-post-tr)
    (cfthumb-br-place web-post-br)
    (cfthumb-mr-place web-post-tl)
    (cfthumb-mr-place web-post-bl))
   (triangle-hulls
    (cfthumb-mr-place web-post-tr)
    (cfthumb-mr-place web-post-br)
    (cfthumb-tr-place web-post-tl)
    (cfthumb-tr-place web-post-bl))
   (triangle-hulls
    (cfthumb-tr-place web-post-br)
    (cfthumb-tr-place web-post-bl)
    (cfthumb-mr-place web-post-br))
   (triangle-hulls    ; between top row and bottom row
    (cfthumb-br-place web-post-tl)
    (cfthumb-bl-place web-post-bl)
    (cfthumb-br-place web-post-tr)
    (cfthumb-bl-place web-post-br)
    (cfthumb-mr-place web-post-tl)
    (cfthumb-ml-place web-post-bl)
    (cfthumb-mr-place web-post-tr)
    (cfthumb-ml-place web-post-br)
    (cfthumb-tr-place web-post-tl)
    (cfthumb-tl-place web-post-bl)
    (cfthumb-tr-place web-post-tr)
    (cfthumb-tl-place web-post-br))
   (triangle-hulls    ; top two to the main keyboard, starting on the left
    (cfthumb-ml-place thumb-post-tl)
    (key-place (+ innercol-offset 0) cornerrow web-post-bl)
    (cfthumb-ml-place thumb-post-tr)
    (key-place (+ innercol-offset 0) cornerrow web-post-br)
    (cfthumb-tl-place web-post-tl)
    (key-place (+ innercol-offset 1) cornerrow web-post-bl)
    (cfthumb-tl-place web-post-tr)
    (key-place (+ innercol-offset 1) cornerrow web-post-br)
    (key-place (+ innercol-offset 2) lastrow web-post-tl)
    (key-place (+ innercol-offset 2) lastrow web-post-bl)
    (cfthumb-tl-place web-post-tr)
    (key-place (+ innercol-offset 2) lastrow web-post-bl)
    (cfthumb-tl-place web-post-br)
    (key-place (+ innercol-offset 2) lastrow web-post-br)
    (key-place (+ innercol-offset 3) lastrow web-post-bl)
    (cfthumb-tl-place web-post-br)
    (cfthumb-tr-place web-post-tr))
   (triangle-hulls
    (key-place (+ innercol-offset 3) lastrow web-post-tr)
    (key-place (+ innercol-offset 3) cornerrow web-post-br)
    (key-place (+ innercol-offset 3) lastrow web-post-tl)
    (key-place (+ innercol-offset 3) cornerrow web-post-bl))
   (triangle-hulls
    (key-place (+ innercol-offset 2) lastrow web-post-tr)
    (key-place (+ innercol-offset 2) lastrow web-post-br)
    (key-place (+ innercol-offset 3) lastrow web-post-tl)
    (key-place (+ innercol-offset 3) lastrow web-post-bl))
   (triangle-hulls
    (cfthumb-tr-place web-post-br)
    (cfthumb-tr-place web-post-tr)
    (key-place (+ innercol-offset 3) lastrow web-post-bl))
   (triangle-hulls
    (key-place (+ innercol-offset 1) cornerrow web-post-br)
    (key-place (+ innercol-offset 2) lastrow web-post-tl)
    (key-place (+ innercol-offset 2) cornerrow web-post-bl)
    (key-place (+ innercol-offset 2) lastrow web-post-tr)
    (key-place (+ innercol-offset 2) cornerrow web-post-br)
    (key-place (+ innercol-offset 3) lastrow web-post-tl)
    (key-place (+ innercol-offset 3) cornerrow web-post-bl))
   (if extra-row
     (union
      (triangle-hulls
       (key-place (+ innercol-offset 3) lastrow web-post-tr)
       (key-place (+ innercol-offset 3) lastrow web-post-br)
       (key-place (+ innercol-offset 4) lastrow web-post-tl)
       (key-place (+ innercol-offset 4) lastrow web-post-bl))
      (triangle-hulls
       (key-place (+ innercol-offset 3) lastrow web-post-tr)
       (key-place (+ innercol-offset 3) cornerrow web-post-br)
       (key-place (+ innercol-offset 4) lastrow web-post-tl)
       (key-place (+ innercol-offset 4) cornerrow web-post-bl)))
     (union
      (triangle-hulls
       (key-place (+ innercol-offset 3) lastrow web-post-tr)
       (key-place (+ innercol-offset 3) lastrow web-post-br)
       (key-place (+ innercol-offset 4) cornerrow web-post-bl))
      (triangle-hulls
       (key-place (+ innercol-offset 3) lastrow web-post-tr)
       (key-place (+ innercol-offset 3) cornerrow web-post-br)
       (key-place (+ innercol-offset 4) cornerrow web-post-bl))))))


;;;;;;;;;;;;;;;;;;;;;;;
;;;; Tightly thumb ;;;;
;;;;;;;;;;;;;;;;;;;;;;;

(def tightly-thumborigin
   (map + (key-position 1 cornerrow [(/ mount-width 2) (- (/ mount-height 2)) 0])
        thumb-offsets))

(defn tightly-thumb-place [rot move shape]
  (->> shape
       (rotate (deg2rad (nth rot 0)) [1 0 0])
       (rotate (deg2rad (nth rot 1)) [0 1 0])
       (rotate (deg2rad (nth rot 2)) [0 0 1])               ; original 10
       (translate tightly-thumborigin)
       (translate move)))

; convexer
(defn thumb-r-place [shape] (tightly-thumb-place [14 -40 10] [-15 -10 5] shape)) ; right
(defn thumb-m-place [shape] (tightly-thumb-place [10 -23 20] [-35.5 -16 -7] shape)) ; middle
(defn thumb-l-place [shape] (tightly-thumb-place [6 -5 35] [-57.5 -27.5 -13] shape)) ; left

(defn tightly-thumb-layout [shape]
  (union
    (thumb-r-place shape)
    (thumb-m-place shape)
    (thumb-l-place shape)
  ))

(defn debug [shape]
  (color [0.5 0.5 0.5 0.5] shape))

(def tightly-thumbcaps (tightly-thumb-layout (sa-cap 1)))
(def tightly (tightly-thumb-layout single-plate))
(def tightly-left (tightly-thumb-layout (mirror [1 0 0] single-plate)))
;(def tightly-thumb-fill (tightly-thumb-layout filled-plate))
;(def thumb-space-below (tightly-thumb-layout switch-bottom))

(def tightly-thumb-connectors
  (union
    (triangle-hulls   ; top two
      (thumb-m-place web-post-tr)
      (thumb-m-place web-post-br)
      (thumb-r-place web-post-tl)
      (thumb-r-place web-post-bl))
    (triangle-hulls   ; top two
      (thumb-m-place web-post-tl)
      (thumb-l-place web-post-tr)
      (thumb-m-place web-post-bl)
      (thumb-l-place web-post-br))
    (triangle-hulls   ; top two to the main keyboard, starting on the left
      (key-place 2 lastrow web-post-br)
      (key-place 3 lastrow web-post-bl)
      (key-place 2 lastrow web-post-tr)
      (key-place 3 lastrow web-post-tl)
      (key-place 3 cornerrow web-post-bl)
      (key-place 3 lastrow web-post-tr)
      (key-place 3 cornerrow web-post-br)
      (key-place 4 cornerrow web-post-bl))
    (triangle-hulls   ; good
      (key-place 1 cornerrow web-post-br)
      (key-place 2 lastrow web-post-tl)
      (key-place 2 cornerrow web-post-bl)
      (key-place 2 lastrow web-post-tr)
      (key-place 2 cornerrow web-post-br)
      (key-place 3 cornerrow web-post-bl))
    (triangle-hulls
      (key-place 3 lastrow web-post-tr)
      (key-place 3 lastrow web-post-br)
      (key-place 3 lastrow web-post-tr)
      (key-place 4 cornerrow web-post-bl))
    ;above thumb-r the biggest left triangle
    (triangle-hulls
      (key-place 0 cornerrow web-post-br)
      (key-place 0 cornerrow web-post-bl)
      (thumb-r-place web-post-tl)
    )
    ;above thumb-r, the middle small triangle
    (triangle-hulls
      (key-place 0 cornerrow web-post-br)
      (key-place 1 cornerrow web-post-bl)
      (thumb-r-place web-post-tl)
    )
    ;above thumb-r, the right triangle
    (triangle-hulls
      (thumb-r-place web-post-tr)
      (thumb-r-place web-post-tl)
      (key-place 1 cornerrow web-post-bl)
    )
    ;thumb-r connected to cornerrow 1
    (triangle-hulls
      (key-place 1 cornerrow web-post-br)
      (key-place 1 cornerrow web-post-bl)
      (thumb-r-place web-post-tr)
    )
    ;to the right of thumb-r connected with 1 point to thumb-r
    (triangle-hulls
      (key-place 1 cornerrow web-post-br)
      (key-place 2 lastrow web-post-bl)
      (thumb-r-place web-post-tr)
    )
    ;to the right of thumb-r connected with 2 points to thumb-r
    (triangle-hulls 
      (key-place 2 lastrow web-post-bl)
      (thumb-r-place web-post-br)
      (thumb-r-place web-post-tr)
    )
    ; between lastrow and lastrow-1
    (triangle-hulls
      (key-place 2 lastrow web-post-bl)
      (key-place 2 lastrow web-post-tl)
      (key-place 1 cornerrow web-post-br)
    )
    ; between top-r and below lastrow
    (triangle-hulls
      (thumb-r-place web-post-br)
      (key-place 2 lastrow web-post-bl)
      (key-place 3 lastrow web-post-bl)
      (key-place 2 lastrow web-post-br)
    )
  )
)

;switching connectors, switchplates, etc. depending on thumb-style used
(when (= thumb-style "default")
  (def thumb-type thumb)
  (def thumb-type-left thumb-left)
  (def thumb-connector-type thumb-connectors)
  (def thumbcaps-type thumbcaps)
  (def thumbcaps-fill-type thumbcaps-fill))

(when (= thumb-style "cf")
  (def thumb-type cfthumb)
  (def thumb-type-left cfthumb-left)
  (def thumb-connector-type cfthumb-connectors)
  (def thumbcaps-type cfthumbcaps)
  (def thumbcaps-fill-type cfthumbcaps-fill))

(when (= thumb-style "mini")
  (def thumb-type minithumb)
  (def thumb-type-left minithumb-left)
  (def thumb-connector-type minithumb-connectors)
  (def thumbcaps-type minithumbcaps)
  (def thumbcaps-fill-type minithumbcaps-fill))

(when (= thumb-style "tightly")
  (def thumb-type tightly)
  (def thumb-type-left tightly-left)
  (def thumb-connector-type tightly-thumb-connectors)
  (def thumbcaps-type tightly-thumbcaps)
  ;(def thumbcaps-fill-type tightly-thumb-fill))
)
;;;;;;;;;;
;; Case ;;
;;;;;;;;;;

(defn bottom [height p]
  (->> (project p)
       (extrude-linear {:height height :twist 0 :convexity 0})
       (translate [0 0 (- (/ height 2) 10)])))

(defn bottom-hull [& p]
  (hull p (bottom 0.001 p)))

(def left-wall-x-offset 4)
(def left-wall-z-offset 1)

(defn left-key-position [row direction]
  (map - (key-position 0 row [(* mount-width -0.5) (* direction mount-height 0.5) 0]) [left-wall-x-offset 0 left-wall-z-offset]) )

(defn left-key-place [row direction shape]
  (translate (left-key-position row direction) shape))

(defn wall-locate1 [dx dy] [(* dx wall-thickness) (* dy wall-thickness) -1])
(defn wall-locate2 [dx dy] [(* dx wall-xy-offset) (* dy wall-xy-offset) wall-z-offset])
(defn wall-locate3 [dx dy] [(* dx (+ wall-xy-offset wall-thickness)) (* dy (+ wall-xy-offset wall-thickness)) wall-z-offset])

(defn wall-brace [place1 dx1 dy1 post1 place2 dx2 dy2 post2]
  (union
   (hull
    (place1 post1)
    (place1 (translate (wall-locate1 dx1 dy1) post1))
    (place1 (translate (wall-locate2 dx1 dy1) post1))
    (place1 (translate (wall-locate3 dx1 dy1) post1))
    (place2 post2)
    (place2 (translate (wall-locate1 dx2 dy2) post2))
    (place2 (translate (wall-locate2 dx2 dy2) post2))
    (place2 (translate (wall-locate3 dx2 dy2) post2)))
   (bottom-hull
    (place1 (translate (wall-locate2 dx1 dy1) post1))
    (place1 (translate (wall-locate3 dx1 dy1) post1))
    (place2 (translate (wall-locate2 dx2 dy2) post2))
    (place2 (translate (wall-locate3 dx2 dy2) post2)))))

(defn key-wall-brace [x1 y1 dx1 dy1 post1 x2 y2 dx2 dy2 post2]
  (wall-brace (partial key-place x1 y1) dx1 dy1 post1
              (partial key-place x2 y2) dx2 dy2 post2))

(def right-wall
  (if pinky-15u
    (union
     ; corner between the right wall and back wall
     (if (> first-15u-row 0)
       (key-wall-brace lastcol 0 0 1 web-post-tr lastcol 0 1 0 web-post-tr)
       (union (key-wall-brace lastcol 0 0 1 web-post-tr lastcol 0 0 1 wide-post-tr)
              (key-wall-brace lastcol 0 0 1 wide-post-tr lastcol 0 1 0 wide-post-tr)))
     ; corner between the right wall and front wall
     (if (= last-15u-row extra-cornerrow)
       (union (key-wall-brace lastcol extra-cornerrow 0 -1 web-post-br lastcol extra-cornerrow 0 -1 wide-post-br)
              (key-wall-brace lastcol extra-cornerrow 0 -1 wide-post-br lastcol extra-cornerrow 1 0 wide-post-br))
       (key-wall-brace lastcol extra-cornerrow 0 -1 web-post-br lastcol extra-cornerrow 1 0 web-post-br))

     (if (>= first-15u-row 2)
       (for [y (range 0 (dec first-15u-row))]
         (union (key-wall-brace lastcol y 1 0 web-post-tr lastcol y 1 0 web-post-br)
                (key-wall-brace lastcol y 1 0 web-post-br lastcol (inc y) 1 0 web-post-tr))))

     (if (>= first-15u-row 1)
       (for [y (range (dec first-15u-row) first-15u-row)] (key-wall-brace lastcol y 1 0 web-post-tr lastcol (inc y) 1 0 wide-post-tr)))

     (for [y (range first-15u-row (inc last-15u-row))] (key-wall-brace lastcol y 1 0 wide-post-tr lastcol y 1 0 wide-post-br))
     (for [y (range first-15u-row last-15u-row)] (key-wall-brace lastcol (inc y) 1 0 wide-post-tr lastcol y 1 0 wide-post-br))

     (if (<= last-15u-row (- extra-cornerrow 1))
       (for [y (range last-15u-row (inc last-15u-row))] (key-wall-brace lastcol y 1 0 wide-post-br lastcol (inc y) 1 0 web-post-br)))

     (if (<= last-15u-row (- extra-cornerrow 2))
       (for [y (range (inc last-15u-row) extra-cornerrow)]
         (union (key-wall-brace lastcol y 1 0 web-post-br lastcol (inc y) 1 0 web-post-tr)
                (key-wall-brace lastcol (inc y) 1 0 web-post-tr lastcol (inc y) 1 0 web-post-br))))
     )
    (union (key-wall-brace lastcol 0 0 1 web-post-tr lastcol 0 1 0 web-post-tr)
           (if extra-row
             (union (for [y (range 0 (inc lastrow))] (key-wall-brace lastcol y 1 0 web-post-tr lastcol y 1 0 web-post-br))
                    (for [y (range 1 (inc lastrow))] (key-wall-brace lastcol (dec y) 1 0 web-post-br lastcol y 1 0 web-post-tr)))
             (union (for [y (range 0 lastrow)] (key-wall-brace lastcol y 1 0 web-post-tr lastcol y 1 0 web-post-br))
                    (for [y (range 1 lastrow)] (key-wall-brace lastcol (dec y) 1 0 web-post-br lastcol y 1 0 web-post-tr)))
             )
           (key-wall-brace lastcol extra-cornerrow 0 -1 web-post-br lastcol extra-cornerrow 1 0 web-post-br)
           )))

(def cf-thumb-wall
  (union
   ; thumb walls
   (wall-brace cfthumb-mr-place  0 -1 web-post-br cfthumb-tr-place  0 -1 web-post-br)
   (wall-brace cfthumb-mr-place  0 -1 web-post-br cfthumb-mr-place  0 -1.15 web-post-bl)
   (wall-brace cfthumb-br-place  0 -1 web-post-br cfthumb-br-place  0 -1 web-post-bl)
   (wall-brace cfthumb-bl-place -0.3  1 thumb-post-tr cfthumb-bl-place  0  1 thumb-post-tl)
   (wall-brace cfthumb-br-place -1  0 web-post-tl cfthumb-br-place -1  0 web-post-bl)
   (wall-brace cfthumb-bl-place -1  0 thumb-post-tl cfthumb-bl-place -1  0 web-post-bl)
   ; cfthumb corners
   (wall-brace cfthumb-br-place -1  0 web-post-bl cfthumb-br-place  0 -1 web-post-bl)
   (wall-brace cfthumb-bl-place -1  0 thumb-post-tl cfthumb-bl-place  0  1 thumb-post-tl)
   ; cfthumb tweeners
   (wall-brace cfthumb-mr-place  0 -1.15 web-post-bl cfthumb-br-place  0 -1 web-post-br)
   (wall-brace cfthumb-bl-place -1  0 web-post-bl cfthumb-br-place -1  0 web-post-tl)
   (wall-brace cfthumb-tr-place  0 -1 web-post-br (partial key-place (+ innercol-offset 3) lastrow)  0 -1 web-post-bl)
   ; clunky bit on the top left cfthumb connection  (normal connectors don't work well)
   (bottom-hull
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate2 -1 0) web-post))
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate3 -1 0) web-post))
    (cfthumb-bl-place (translate (wall-locate2 -0.3 1) thumb-post-tr))
    (cfthumb-bl-place (translate (wall-locate3 -0.3 1) thumb-post-tr)))
   (hull
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate2 -1 0) web-post))
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate3 -1 0) web-post))
    (cfthumb-bl-place (translate (wall-locate2 -0.3 1) thumb-post-tr))
    (cfthumb-bl-place (translate (wall-locate3 -0.3 1) thumb-post-tr))
    (cfthumb-ml-place thumb-post-tl))
   (hull
    (left-key-place (- cornerrow innercol-offset) -1 web-post)
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate1 -1 0) web-post))
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate2 -1 0) web-post))
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate3 -1 0) web-post))
    (cfthumb-ml-place thumb-post-tl))
   (hull
    (left-key-place (- cornerrow innercol-offset) -1 web-post)
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate1 -1 0) web-post))
    (key-place 0 (- cornerrow innercol-offset) web-post-bl)
    (cfthumb-ml-place thumb-post-tl))
   (hull
    (cfthumb-bl-place thumb-post-tr)
    (cfthumb-bl-place (translate (wall-locate1 -0.3 1) thumb-post-tr))
    (cfthumb-bl-place (translate (wall-locate2 -0.3 1) thumb-post-tr))
    (cfthumb-bl-place (translate (wall-locate3 -0.3 1) thumb-post-tr))
    (cfthumb-ml-place thumb-post-tl))
   ; connectors below the inner column to the thumb & second column
   (if inner-column
     (union
      (hull
       (key-place 0 (dec cornerrow) web-post-bl)
       (key-place 0 (dec cornerrow) web-post-br)
       (key-place 0 cornerrow web-post-tr))
      (hull
       (key-place 0 cornerrow web-post-tr)
       (key-place 1 cornerrow web-post-tl)
       (key-place 1 cornerrow web-post-bl))
      (hull
       (key-place 0 (dec cornerrow) web-post-bl)
       (key-place 0 cornerrow web-post-tr)
       (key-place 1 cornerrow web-post-bl))
      (hull
       (key-place 0 (dec cornerrow) web-post-bl)
       (key-place 1 cornerrow web-post-bl)
       (cfthumb-ml-place thumb-post-tl))))))

(def mini-thumb-wall
  (union
   ; thumb walls
   (wall-brace minithumb-mr-place  0 -1 web-post-br minithumb-tr-place  0 -1 minithumb-post-br)
   (wall-brace minithumb-mr-place  0 -1 web-post-br minithumb-mr-place  0 -1 web-post-bl)
   (wall-brace minithumb-br-place  0 -1 web-post-br minithumb-br-place  0 -1 web-post-bl)
   (wall-brace minithumb-bl-place  0  1 web-post-tr minithumb-bl-place  0  1 web-post-tl)
   (wall-brace minithumb-br-place -1  0 web-post-tl minithumb-br-place -1  0 web-post-bl)
   (wall-brace minithumb-bl-place -1  0 web-post-tl minithumb-bl-place -1  0 web-post-bl)
   ; minithumb corners
   (wall-brace minithumb-br-place -1  0 web-post-bl minithumb-br-place  0 -1 web-post-bl)
   (wall-brace minithumb-bl-place -1  0 web-post-tl minithumb-bl-place  0  1 web-post-tl)
   ; minithumb tweeners
   (wall-brace minithumb-mr-place  0 -1 web-post-bl minithumb-br-place  0 -1 web-post-br)
   (wall-brace minithumb-bl-place -1  0 web-post-bl minithumb-br-place -1  0 web-post-tl)
   (wall-brace minithumb-tr-place  0 -1 minithumb-post-br (partial key-place (+ innercol-offset 3) lastrow)  0 -1 web-post-bl)
   ; clunky bit on the top left minithumb connection  (normal connectors don't work well)
   (bottom-hull
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate2 -1 0) web-post))
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate3 -1 0) web-post))
    (minithumb-bl-place (translate (wall-locate2 -0.3 1) web-post-tr))
    (minithumb-bl-place (translate (wall-locate3 -0.3 1) web-post-tr)))
   (hull
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate2 -1 0) web-post))
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate3 -1 0) web-post))
    (minithumb-bl-place (translate (wall-locate2 -0.3 1) web-post-tr))
    (minithumb-bl-place (translate (wall-locate3 -0.3 1) web-post-tr))
    (minithumb-tl-place web-post-tl))
   (hull
    (left-key-place (- cornerrow innercol-offset) -1 web-post)
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate1 -1 0) web-post))
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate2 -1 0) web-post))
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate3 -1 0) web-post))
    (minithumb-tl-place web-post-tl))
   (hull
    (left-key-place (- cornerrow innercol-offset) -1 web-post)
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate1 -1 0) web-post))
    (key-place 0 (- cornerrow innercol-offset) web-post-bl)
    (minithumb-tl-place web-post-tl))
   (hull
    (minithumb-bl-place web-post-tr)
    (minithumb-bl-place (translate (wall-locate1 -0.3 1) web-post-tr))
    (minithumb-bl-place (translate (wall-locate2 -0.3 1) web-post-tr))
    (minithumb-bl-place (translate (wall-locate3 -0.3 1) web-post-tr))
    (minithumb-tl-place web-post-tl))
   ; connectors below the inner column to the thumb & second column
   (if inner-column
     (union
      (hull
       (key-place 0 (dec cornerrow) web-post-bl)
       (key-place 0 (dec cornerrow) web-post-br)
       (key-place 0 cornerrow web-post-tr))
      (hull
       (key-place 0 cornerrow web-post-tr)
       (key-place 1 cornerrow web-post-tl)
       (key-place 1 cornerrow web-post-bl))
      (hull
       (key-place 0 (dec cornerrow) web-post-bl)
       (key-place 0 cornerrow web-post-tr)
       (key-place 1 cornerrow web-post-bl))
      (hull
       (key-place 0 (dec cornerrow) web-post-bl)
       (key-place 1 cornerrow web-post-bl)
       (minithumb-tl-place minithumb-post-tl))))))

(def default-thumb-wall
  (union
   ; thumb walls
   (wall-brace thumb-mr-place  0 -1 web-post-br thumb-tr-place  0 -1 thumb-post-br)
   (wall-brace thumb-mr-place  0 -1 web-post-br thumb-mr-place  0 -1 web-post-bl)
   (wall-brace thumb-br-place  0 -1 web-post-br thumb-br-place  0 -1 web-post-bl)
   (wall-brace thumb-ml-place -0.3  1 web-post-tr thumb-ml-place  0  1 web-post-tl)
   (wall-brace thumb-bl-place  0  1 web-post-tr thumb-bl-place  0  1 web-post-tl)
   (wall-brace thumb-br-place -1  0 web-post-tl thumb-br-place -1  0 web-post-bl)
   (wall-brace thumb-bl-place -1  0 web-post-tl thumb-bl-place -1  0 web-post-bl)
   ; thumb corners
   (wall-brace thumb-br-place -1  0 web-post-bl thumb-br-place  0 -1 web-post-bl)
   (wall-brace thumb-bl-place -1  0 web-post-tl thumb-bl-place  0  1 web-post-tl)
   ; thumb tweeners
   (wall-brace thumb-mr-place  0 -1 web-post-bl thumb-br-place  0 -1 web-post-br)
   (wall-brace thumb-ml-place  0  1 web-post-tl thumb-bl-place  0  1 web-post-tr)
   (wall-brace thumb-bl-place -1  0 web-post-bl thumb-br-place -1  0 web-post-tl)
   (wall-brace thumb-tr-place  0 -1 thumb-post-br (partial key-place (+ innercol-offset 3) lastrow)  0 -1 web-post-bl)
   ; clunky bit on the top left thumb connection  (normal connectors don't work well)
   (bottom-hull
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate2 -1 0) web-post))
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate3 -1 0) web-post))
    (thumb-ml-place (translate (wall-locate2 -0.3 1) web-post-tr))
    (thumb-ml-place (translate (wall-locate3 -0.3 1) web-post-tr)))
   (hull
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate2 -1 0) web-post))
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate3 -1 0) web-post))
    (thumb-ml-place (translate (wall-locate2 -0.3 1) web-post-tr))
    (thumb-ml-place (translate (wall-locate3 -0.3 1) web-post-tr))
    (thumb-tl-place thumb-post-tl))
   (hull
    (left-key-place (- cornerrow innercol-offset) -1 web-post)
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate1 -1 0) web-post))
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate2 -1 0) web-post))
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate3 -1 0) web-post))
    (thumb-tl-place thumb-post-tl))
   (hull
    (left-key-place (- cornerrow innercol-offset) -1 web-post)
    (left-key-place (- cornerrow innercol-offset) -1 (translate (wall-locate1 -1 0) web-post))
    (key-place 0 (- cornerrow innercol-offset) web-post-bl)
    (key-place 0 (- cornerrow innercol-offset) (translate (wall-locate1 0 0) web-post-bl))
    (thumb-tl-place thumb-post-tl))
   ; connectors below the inner column to the thumb & second column
   (if inner-column
     (union
      (hull
       (key-place 0 (dec cornerrow) web-post-bl)
       (key-place 0 (dec cornerrow) web-post-br)
       (key-place 0 cornerrow web-post-tr))
      (hull
       (key-place 0 cornerrow web-post-tr)
       (key-place 1 cornerrow web-post-tl)
       (key-place 1 cornerrow web-post-bl))
      (hull
       (key-place 0 (dec cornerrow) web-post-bl)
       (key-place 0 cornerrow web-post-tr)
       (key-place 1 cornerrow web-post-bl))
      (hull
       (key-place 0 (dec cornerrow) web-post-bl)
       (key-place 1 cornerrow web-post-bl)
       (thumb-tl-place thumb-post-tl))))
   (hull
    (thumb-ml-place web-post-tr)
    (thumb-ml-place (translate (wall-locate1 -0.3 1) web-post-tr))
    (thumb-ml-place (translate (wall-locate2 -0.3 1) web-post-tr))
    (thumb-ml-place (translate (wall-locate3 -0.3 1) web-post-tr))
    (thumb-tl-place thumb-post-tl))))

(def tightly-thumb-wall
  (union
    (wall-brace thumb-r-place 0 -1 web-post-br (partial key-place 3 lastrow) 0 -1 web-post-bl)
    (wall-brace thumb-r-place 0 -1 web-post-br thumb-r-place 0 -1 web-post-bl)
    (wall-brace thumb-m-place 0 -1 web-post-br thumb-m-place 0 -1 web-post-bl)
    (wall-brace thumb-l-place 0 -1 web-post-br thumb-l-place 0 -1 web-post-bl)
    (wall-brace thumb-l-place 0 1 web-post-tr thumb-l-place 0 1 web-post-tl)
    (wall-brace thumb-l-place -1 0 web-post-tl thumb-l-place -1 0 web-post-bl)
    ; thumb corners
    (wall-brace thumb-l-place -1 0 web-post-bl thumb-l-place 0 -1 web-post-bl)
    (wall-brace thumb-l-place -1 0 web-post-tl thumb-l-place 0 1 web-post-tl)
    ; thumb tweeners
    (wall-brace thumb-r-place 0 -1 web-post-bl thumb-m-place 0 -1 web-post-br)
    (wall-brace thumb-m-place 0 -1 web-post-bl thumb-l-place 0 -1 web-post-br)
    ;(wall-brace thumb-m-place 0 1 web-post-tl thumb-l-place 0 1 web-post-tr)
    (wall-brace thumb-l-place -1 0 web-post-bl thumb-l-place -1 0 web-post-tl)

    (wall-brace (partial left-key-place cornerrow -1) -1 0 web-post thumb-l-place 0 1 web-post-tr) 
    
    (hull
      (left-key-place cornerrow -1 web-post)
      (thumb-m-place web-post-tr)
      (thumb-m-place web-post-tl)
    )
    (hull
      (left-key-place cornerrow -1 web-post)
      (key-place 0 cornerrow web-post-bl)
      (thumb-m-place web-post-tr)
      (thumb-r-place web-post-tl)
    )
    (hull
      (thumb-m-place web-post-tl)
      (thumb-l-place web-post-tr)
      (left-key-place cornerrow -1 web-post)
    )
  )
)

;switching walls depending on thumb-style used
(def thumb-wall-type
  (case thumb-style
    "default" default-thumb-wall
    "cf" cf-thumb-wall
    "mini" mini-thumb-wall
    "tightly" tightly-thumb-wall
    ))

(def case-walls
  (union
   thumb-wall-type
   right-wall
   ; back wall
   (for [x (range 0 ncols)] (key-wall-brace x 0 0 1 web-post-tl x       0 0 1 web-post-tr))
   (for [x (range 1 ncols)] (key-wall-brace x 0 0 1 web-post-tl (dec x) 0 0 1 web-post-tr))
   ; left wall
   (for [y (range 0 (- lastrow innercol-offset))] (union
    (wall-brace (partial left-key-place y 1) -1 0 web-post (partial left-key-place y -1) -1 0 web-post)
                                                         (hull (key-place 0 y web-post-tl)
                                                               (key-place 0 y web-post-bl)
                                                               (left-key-place y  1 web-post)
                                                               (left-key-place y -1 web-post))))
   (for [y (range 1 (- lastrow innercol-offset))] (union
                                                   (wall-brace (partial left-key-place (dec y) -1) -1 0 web-post (partial left-key-place y  1) -1 0 web-post)
                                                   (hull (key-place 0 y       web-post-tl)
                                                         (key-place 0 (dec y) web-post-bl)
                                                         (left-key-place y        1 web-post)
                                                         (left-key-place (dec y) -1 web-post))))
   (wall-brace (partial key-place 0 0) 0 1 web-post-tl (partial left-key-place 0 1) -0.6 1 web-post)
   (wall-brace (partial left-key-place 0 1) -0.6 1 web-post (partial left-key-place 0 1) -1 0 web-post)
   ; front wall
   (key-wall-brace (+ innercol-offset 3) lastrow  0 -1 web-post-bl (+ innercol-offset 3) lastrow   0 -1 web-post-br)
   (key-wall-brace (+ innercol-offset 3) lastrow  0 -1 web-post-br (+ innercol-offset 4) extra-cornerrow 0 -1 web-post-bl)
   (for [x (range (+ innercol-offset 4) ncols)] (key-wall-brace x extra-cornerrow 0 -1 web-post-bl x       extra-cornerrow 0 -1 web-post-br))
   (for [x (range (+ innercol-offset 5) ncols)] (key-wall-brace x extra-cornerrow 0 -1 web-post-bl (dec x) extra-cornerrow 0 -1 web-post-br))
   ))

; Offsets for the controller/trrs holder cutout
(def holder-offset
  (case nrows
    4 -3.5
    5 0
    6 (if inner-column
          3.2
          2.2)))

(def notch-offset
  (case nrows
    4 3.35
    5 0.15
    6 -5.07))

; Cutout for controller/trrs jack holder
(def usb-holder-ref (key-position 0 0 (map - (wall-locate2  0  -1) [0 (/ mount-height 2) 0])))
(def usb-holder-position (map + [(+ 18.8 holder-offset) 18.7 1.3] [(first usb-holder-ref) (second usb-holder-ref) 2]))
(def usb-holder-space  (translate (map + usb-holder-position [-1.5 (* -1 wall-thickness) 4.4]) (cube 28.666 30 15.4)))
(def usb-holder-notch  (translate (map + usb-holder-position [-1.5 (+ 4.75 notch-offset) 4.4]) (cube 31.366 1.3 15.4)))
(def trrs-notch        (translate (map + usb-holder-position [-10.33 (+ 3.6 notch-offset) 6.6]) (cube 8.4 2.4 19.8)))

; Screw insert definition & position
(defn screw-insert-shape [bottom-radius top-radius height]
  (union
   (->> (binding [*fn* 30]
                 (cylinder [bottom-radius top-radius] height)))))

(defn screw-insert [column row bottom-radius top-radius height offset]
  (let [shift-right   (= column lastcol)
        shift-left    (= column 0)
        shift-up      (and (not (or shift-right shift-left)) (= row 0))
        shift-down    (and (not (or shift-right shift-left)) (>= row lastrow))
        position      (if shift-up     (key-position column row (map + (wall-locate2  0  1) [0 (/ mount-height 2) 0]))
                        (if shift-down  (key-position column row (map - (wall-locate2  0 -2.5) [0 (/ mount-height 2) 0]))
                          (if shift-left (map + (left-key-position row 0) (wall-locate3 -1 0))
                            (key-position column row (map + (wall-locate2  1  0) [(/ mount-width 2) 0 0])))))]
    (->> (screw-insert-shape bottom-radius top-radius height)
         (translate (map + offset [(first position) (second position) (/ height 2)])))))

; Offsets for the screw inserts dependent on extra-row & pinky-15u
(when (and pinky-15u extra-row)
    (def screw-offset-tr [1 7 0])
    (def screw-offset-br [7 14 0]))
(when (and pinky-15u (false? extra-row))
    (def screw-offset-tr [1 7 0])
    (def screw-offset-br [6.5 15.5 0]))
(when (and (false? pinky-15u) extra-row)
    (def screw-offset-tr [-3.5 6.5 0])
    (def screw-offset-br [-3.5 -6.5 0]))
(when (and (false? pinky-15u) (false? extra-row))
    (def screw-offset-tr [-7 6.5 0])
    (def screw-offset-br [-5 19.5 0]))
    
; Offsets for the screw inserts dependent on thumb-style & inner-column
(when (and (= thumb-style "cf") inner-column)
    (def screw-offset-bl [9 4 0])
    (def screw-offset-tm [9.5 -4.5 0])
    (def screw-offset-bm [13 -7 0]))
(when (and (= thumb-style "cf") (false? inner-column))
    (def screw-offset-bl [-7.7 2 0])
    (def screw-offset-tm [9.5 -4.5 0])
    (def screw-offset-bm [13 -7 0]))
(when (and (= thumb-style "mini") inner-column)
    (def screw-offset-bl [14 8 0])
    (def screw-offset-tm [9.5 -4.5 0])
    (def screw-offset-bm [-1 -7 0]))
(when (and (= thumb-style "mini") (false? inner-column))
    (def screw-offset-bl [1 4.2 0])
    ;(def screw-offset-tm [8 -4.5 0])
    (def screw-offset-bm [-1.2 -6 0]))
(when (and (= thumb-style "default") inner-column)
    (def screw-offset-bl [5 -6 0])
    (def screw-offset-tm [9.5 -4.5 0])
    (def screw-offset-bm [8 -1 0]))
(when (and (= thumb-style "default") (false? inner-column))
    (def screw-offset-bl [-10.15 -8.7 0])
    ;(def screw-offset-tm [9.5 -4.5 0])
    (def screw-offset-bm [8 0.5 0]))
(when (and (= thumb-style "tightly") inner-column)
    (def screw-offset-bl [5 -2 0])
    (def screw-offset-tm [9.5 20 0])
    (def screw-offset-bm [8 -1 0]))
(when (and (= thumb-style "tightly") (false? inner-column))
    (def screw-offset-bl [-6 -6.5 0])
    ;(def screw-offset-tm [9.5 20 0])
    (def screw-offset-bm [8 13.8 0]))


(when (and (= nrows 4) (= ncols 5))
  (def screw-offset-tr [-5.18 8.55 0])
  (def screw-offset-tm [9 -4.8 0])
  (def screw-offset-tl [7.6 9.5 0]))
(when (and (= nrows 4) (= ncols 6))
  (def screw-offset-tr [-5.7 8.55 0])
  (def screw-offset-tm [9 -4.8 0])
  (def screw-offset-tl [7.6 9.5 0]))
(when (and (= nrows 5) (= ncols 6))
  (def screw-offset-tr [-6 6.2 0])
  (def screw-offset-tm [7.9 -5.3 0])
  (def screw-offset-tl [8.8 9 0]))
(when (and (= nrows 6) (= ncols 6))
  (def screw-offset-tr [-6 3.5 0])
  (def screw-offset-tm [7 -6.05 0])
  (def screw-offset-tl [9.8 7.8 0]))

(defn screw-insert-all-shapes [bottom-radius top-radius height]
  (union (screw-insert 0 0         bottom-radius top-radius height screw-offset-tl)
         (screw-insert 0 lastrow   bottom-radius top-radius height screw-offset-bl)
         (screw-insert lastcol lastrow  bottom-radius top-radius height screw-offset-br)
         (screw-insert lastcol 0         bottom-radius top-radius height screw-offset-tr)
         (screw-insert (+ 2 innercol-offset) 0         bottom-radius top-radius height screw-offset-tm)
         (screw-insert (+ 1 innercol-offset) lastrow         bottom-radius top-radius height screw-offset-bm)))

; Hole Depth Y: 4.4
(def screw-insert-height 5)

; Hole Diameter C: 4.1-4.4
(def screw-insert-bottom-radius (/ 4.1 2)) ;4.1  - 7.2 for position seeking
(def screw-insert-top-radius (/ 4.1 2)) ;4.1
(def screw-insert-holes (screw-insert-all-shapes screw-insert-bottom-radius screw-insert-top-radius screw-insert-height))

; Wall Thickness W:\t1.65
(def screw-insert-outers (screw-insert-all-shapes (+ screw-insert-bottom-radius 2.2) (+ screw-insert-top-radius 2.2) (+ screw-insert-height 1)))
(def screw-insert-screw-holes  (screw-insert-all-shapes 1.75 1.75 350))

; Connectors between outer column and right wall when 1.5u keys are used
(def pinky-connectors
  (if pinky-15u
    (apply union
           (concat
            ;; Row connections
            (for [row (range first-15u-row (inc last-15u-row))]
              (triangle-hulls
               (key-place lastcol row web-post-tr)
               (key-place lastcol row wide-post-tr)
               (key-place lastcol row web-post-br)
               (key-place lastcol row wide-post-br)))
            (if-not (= last-15u-row extra-cornerrow) (for [row (range last-15u-row (inc last-15u-row))]
              (triangle-hulls
               (key-place lastcol (inc row) web-post-tr)
               (key-place lastcol row wide-post-br)
               (key-place lastcol (inc row) web-post-br))))
            (if-not (= first-15u-row 0) (for [row (range (dec first-15u-row) first-15u-row)]
              (triangle-hulls
               (key-place lastcol row web-post-tr)
               (key-place lastcol (inc row) wide-post-tr)
               (key-place lastcol row web-post-br))))

            ;; Column connections
            (for [row (range first-15u-row last-15u-row)]
              (triangle-hulls
               (key-place lastcol row web-post-br)
               (key-place lastcol row wide-post-br)
               (key-place lastcol (inc row) web-post-tr)
               (key-place lastcol (inc row) wide-post-tr)))
            (if-not (= last-15u-row extra-cornerrow) (for [row (range last-15u-row (inc last-15u-row))]
              (triangle-hulls
               (key-place lastcol row web-post-br)
               (key-place lastcol row wide-post-br)
               (key-place lastcol (inc row) web-post-tr))))
            (if-not (= first-15u-row 0) (for [row (range (dec first-15u-row) first-15u-row)]
              (triangle-hulls
               (key-place lastcol row web-post-br)
               (key-place lastcol (inc row) wide-post-tr)
               (key-place lastcol (inc row) web-post-tr))))
))))

;;;;;;;;;;;;;;;;;;
;;; IC fixture ;;;
;;;;;;;;;;;;;;;;;;

(def usb-hole
  (union
    (translate [0 0 10]
    (minkowski
      (binding [*fn* 100] (cylinder 0.95 10))
      (cube 7.34 1.56 10)
    )
    )
    (translate [0 1.91 10.7]
      (cube 12.3 4.18 18.6)
    )
    (translate [-5.62 -0.58 10.7]
      (cube 0.6 0.8 18.6)
    )
    (translate [5.62 -0.58 10.7]
      (cube 0.6 0.8 18.6)
    )
    (translate [0 4.5 20]
      (cube 2 5 5)
    )
  )
)

(def trrs-hole
  (union
    (translate [0 0 10]
      (binding [*fn* 100] (cylinder 3.2 20))
    )
    (translate [0 0.85 11.2]
      (cube 10.5 8 17.6)
    )
    (translate [0 4.5 20]
      (cube 2 5 5)
    )
  )
)

(def pi-cube
  (union
    (translate [5.7 24.55 -1.5]
      (cube 3 5.9 5)
    )
    (translate [5.7 23.525 2.5]
      (binding [*fn* 100] (cylinder 1 3))

    )
  )
)

(def pi-hole
  (difference
    (cube 24 55 10)
    (union
      pi-cube
      (mirror [0 1 0]
        pi-cube
      )
      (mirror [1 0 0]
        pi-cube
      )
      (rotate [0 0 pi]
        pi-cube
      )
    )
  )
)

(def ic-wall
  (difference
    (union
      (translate [0 -1.9 4.475]
        (cube 28.2 3.8 14.95)
        (translate [0 0.2 0]
        (cube 30.9 1.1 14.95))
      )
      (translate [-7.05 -12.8 5]
        (cube 14.1 18 10)
      )
      (translate [7.05 -12.8 5]
        (cube 14.1 18 10)
      )
      (translate [0 -50.8 4]
        (cube 28.2 58 8)
      )
    ) 
  )
)

(def ic-fixture
  (difference
    (union
        ic-wall
    )
    (translate [7 0 6]
      (rotate [(deg2rad 90) 0 0]
        usb-hole
      )
    )
    (translate [-7 0 6]
      (rotate [(deg2rad 90) 0 0]
        trrs-hole
      )
    )
    (translate [0 -50.8 4]
      pi-hole
    ) 
  )
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Hand rest case attachement ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def insert-shape
  (translate [0 2 6.4]
    (rotate [(deg2rad 90) 0 0]
      (binding [*fn* 30] (cylinder 2.7 5.5))
      (translate [0 2 -2.3]
        (cube 1 5 10)
      )
    )
  )
)
(def insert-socket
  (difference
  (translate [0 2 6.4]
    (rotate [(deg2rad 90) 0 0]
      (binding [*fn* 30] (cylinder 3.5 5.5))
    )
  )
  insert-shape
      (translate [0 0 10]
        (cube 1 15 10)
      )
  )
) 

(def wrist-attach-rest
  (translate [0 0 2.9]
    (union
      (translate [0 0 2.9]
        (rotate [(/ pi 2) 0 0]
          (binding [*fn* 100] (cylinder 4 20))))
      (cube 8 20 5.8)
    )
  )
)

(def seats-on-rest
  (union
    (translate [hook1-placement-x (- (+ hand-rest-position-y (/ hand-rest-width 2)) 10) 0]
      wrist-attach-rest
    )
    (translate [hook2-placement-x (- (+ hand-rest-position-y (/ hand-rest-width 2)) 10) 0]
      wrist-attach-rest
    )
  )
)

(defn joiner [length]
  (translate [0 0 2.9]
    (difference
      (union
        (translate [0 0 2.9]
          (rotate [(/ pi 2) 0 0]
            (binding [*fn* 100] (cylinder 4 length))))
        (cube 8 length 5.8)
      )
      (translate [0 (- length 10.2) 3.65]
        (rotate [(/ pi 2) 0 0]
          (binding [*fn* 100] (cylinder 2.7 length))
        )
      )
    )
  )
)

(def wrist-attach-case
  (union
    (translate [hook1-placement-x hook1-placement-y 0]
      insert-shape
    )
    (translate [hook2-placement-x hook2-placement-y 0]
      insert-shape
    )
  )
)
(def wrist-attach-case-socket
  (union
    (translate [hook1-placement-x hook1-placement-y 0]
      insert-socket
    )
    (translate [hook2-placement-x hook2-placement-y 0]
      insert-socket
    )
  )
)


(def length1  (* (- (+ hand-rest-position-y (/ hand-rest-width 2)) hook1-placement-y) -1) )
(def length2  (* (- (+ hand-rest-position-y (/ hand-rest-width 2)) hook2-placement-y) -1) )
   
(def joiners
  (union
    (translate [hook1-placement-x (+ hand-rest-position-y (+ (/ length1 2) (/ hand-rest-width 2))) 0]
      (joiner length1) )
    (translate [hook2-placement-x (+ hand-rest-position-y (+ (/ length2 2) (/ hand-rest-width 2))) 0]
      (joiner length2) )
  )
)

(def old-fix
  (difference
    (translate [0 2 3.75]
    (cube 5.3 4 7.5))
    insert-shape
  )
)

;(spit "things/test1.scad"
;      (write-scad
;        joiners
;      )
;)

;;;;;;;;;;;;;;;;;
;;; Hand rest ;;;
;;;;;;;;;;;;;;;;;

(def hand-rest-cube
    (extrude-linear {:height hand-rest-height :scale hand-rest-top :fn 100}
      (minkowski
        (square (- hand-rest-length (* hand-rest-radius 2)) (- hand-rest-width (* hand-rest-radius 2)))
        (binding [*fn* 100] (circle hand-rest-radius))
      )
    )
)

(def hand-rest-slope
  (translate [0 0 
              (+ 0.00 (- (/ hand-rest-height 2)
                (+
                  (*
                    (Math/tan (deg2rad hand-rest-y))
                    (/ (* hand-rest-top hand-rest-length) 2)
                  )
                  (*
                    (Math/tan (deg2rad hand-rest-x))
                    (/ (* hand-rest-top hand-rest-width) 2)
                  )
                )
               ))]
    (rotate [(deg2rad hand-rest-x) (deg2rad hand-rest-y) 0]
    (translate [0 0 (* 25 hand-rest-height)]
      (extrude-linear {:height (* 50 hand-rest-height) :scale 2 :fn 100}
        (square (* 50 hand-rest-length) (* 50 hand-rest-width))
      ) 
    )
    )
  )
)

(def hand-rest-insert
  (translate [0 0 (* -1 hand-rest-lip-height)]
    (intersection
      (extrude-linear {:height 1000}
        (project
          (translate [0 0 1000]
            (intersection
              (scale [(/ (- hand-rest-length (* 2 hand-rest-lip-width)) hand-rest-length)
              (/ (- hand-rest-width (* 2 hand-rest-lip-width)) hand-rest-width) 
              1] hand-rest-cube)
              hand-rest-slope
            )
          )
        )
      )
      hand-rest-slope
    )
  )
)

(def hand-rest-pads
  (union
    (translate [(- (/ hand-rest-length 2) pad-off)
                (- (/ hand-rest-width 2) pad-off)
                (+ (/ hand-rest-height -2) (/ pad-z 2))]
    (binding [*fn* 100] (cylinder pad-r pad-z))
    )
    (translate [(+ (/ hand-rest-length -2) pad-off)
                (- (/ hand-rest-width 2) pad-off)
                (+ (/ hand-rest-height -2) (/ pad-z 2))]
    (binding [*fn* 100] (cylinder pad-r pad-z))
    )
    (translate [(- (/ hand-rest-length 2) pad-off)
                (+ (/ hand-rest-width -2) pad-off)
                (+ (/ hand-rest-height -2) (/ pad-z 2))]
    (binding [*fn* 100] (cylinder pad-r pad-z))
    )
    (translate [(+ (/ hand-rest-length -2) pad-off)
                (+ (/ hand-rest-width -2) pad-off)
                (+ (/ hand-rest-height -2) (/ pad-z 2))]
    (binding [*fn* 100] (cylinder pad-r pad-z))
    )
  )
)

(def hand-rest
  (difference
    hand-rest-cube
    hand-rest-slope
    hand-rest-insert
    hand-rest-pads
  )
)

(def hand-rest-final
  (union
    (translate [hand-rest-position-x hand-rest-position-y (/ hand-rest-height 2)]
      hand-rest
    )
    seats-on-rest
    joiners
  )
)

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Wood Cutting Tool ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(def wood-piece
  (union
    (translate [0 0 -0.23]  
      (rotate [(deg2rad (* -1 hand-rest-x)) 0 0]
        (rotate [0 (deg2rad (* -1 hand-rest-y)) 0]
          (difference
            hand-rest-insert
            (scale [1.01 1.01 1]
            (translate [0 0 15]
              hand-rest-insert
            )
            )
          )
        )
      )
    )
  )
)

(def tool-1-negative
  (rotate [0 0 (deg2rad -3.55)]
    (union
      (rotate [0 (deg2rad -8) 0]
        (cube 10 100 11 :center false)
      )
      (cube 100 100 11 :center false)
      (translate [2 2 0]
        (binding [*fn* 100] (cylinder 4 20))
      )
    )
  )
)

(def tool-1
  (difference
    (cube 110 100 10 :center false)
    (translate [14 15 0]
      tool-1-negative
    )
  )
)

(def tool-2-negative
  (rotate [0 0 (deg2rad 0.3)]
    (union
      (rotate [(deg2rad -8) 0 0]
        (cube 100 10 11 :center false)
      )
      (translate [0 5 0]
        (cube 100 100 11 :center false) 
      )
      (translate [2 2 0]
        (binding [*fn* 100] (cylinder 4 20))
      )
    )
  )
)

(def tool-2
  (difference
    (cube 110 100 10 :center false)
    (translate [35 10 0]
      tool-2-negative 
    )
  )
)

(def tool-3-negative
  (rotate [0 0 (deg2rad 0.95)]
    (union
      (rotate [(deg2rad 8) 0 0]
        (cube 100 10 11 :center false)
      )
      (rotate [0 (deg2rad -15) (deg2rad 0.45)]
        (cube 10 100 11 :center false)
      )
      (rotate [0 0 (deg2rad 0.45)]
        (cube 100 100 11 :center false)
      )
      (translate [0 0 0]
        (cube 100 100 11 :center false) 
      )
      (translate [2 2 5]
        (binding [*fn* 100] (cylinder 6 20))
      )
    )
  )
)

(def tool-3
  (difference
    (cube 110 100 10 :center false)
    (translate [43.3 15 0]
      tool-3-negative
    )
  )
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Bottom plate generation ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def model-right-plate-before
  (project  
    (extrude-linear {:height 3}
      (difference
        (fill
          (cut
            case-walls
          )
        ) 
        (cut
          screw-insert-screw-holes
        )
      )
    )
  )
)


(def model-right-plate
  (project
    (extrude-linear {:height 3}
      (scale [0.996 0.996 0]
        (difference
          (fill
            (cut
              case-walls
            )
          )
          (cut
            case-walls
          )
          (cut
            screw-insert-screw-holes
          )
        )
      )
    )
  )
)

(defn plate-pad [offset]
  (translate offset
    (binding [*fn* 100] (cylinder pad-r pad-z))
  )
)

(def text-z 0.75)

(when (= thumb-style "default")
  (def plate-pad-br [-85.2 -71.6 (/ pad-z 2)])
  (def plate-pad-bm [-56 -95 (/ pad-z 2)])
)
(when (= thumb-style "mini")
  (def plate-pad-br [-80 -52 0])
  (def plate-pad-bm [-59 -81 0])
)
(when (= thumb-style "tightly")
  (def plate-pad-br [-85 -66 0])
  (def plate-pad-bm [-17 -48 0])
)

(when (and (= nrows 4) (= ncols 5))
  (def plate-pad-bl [22.7 -42.2 (/ pad-z 2)])
  (def plate-pad-tl [21 18.4 (/ pad-z 2)])
  (def plate-pad-tr [-59.3 30.5 (/ pad-z 2)])
  (def text-r [-30 -17 text-z])
  (def text-l [-70 -17 text-z])
)
(when (and (= nrows 4) (= ncols 6))
  (def plate-pad-bl [43.7 -42.2 (/ pad-z 2)])
  (def plate-pad-tl [50.8 11.9 (/ pad-z 2)])
  (def plate-pad-tr [-59.1 30.7 (/ pad-z 2)])
  (def text-r [-40 -17 text-z])
  (def text-l [-60 -17 text-z])
)
(when (and (= nrows 5) (= ncols 6))
  (def plate-pad-bl [43.7 -42.2 (/ pad-z 2)])
  (def plate-pad-tl [51.2 30.8 (/ pad-z 2)])
  (def plate-pad-tr [-54 50.3 (/ pad-z 2)])
  (def text-r [-40 -8 text-z])
  (def text-l [-60 -8 text-z])
)
(when (and (= nrows 6) (= ncols 6))
  (def plate-pad-bl [43.7 -42.2 (/ pad-z 2)])
  (def plate-pad-tl [44 55 (/ pad-z 2)])
  (def plate-pad-tr [-46 67 (/ pad-z 2)])
  (def text-r [-43 5 text-z])
  (def text-l [-58 5 text-z])
)

(def plate-pads-together
  (union
    (plate-pad plate-pad-br)
    (plate-pad plate-pad-bm)
    (plate-pad plate-pad-bl)
    (plate-pad plate-pad-tr)
    (plate-pad plate-pad-tl)
  )
)

(defn plate-text [offset]
  (rotate [0 0 pi]
    (translate offset
      (extrude-linear {:height 1.5}
        (text "K33B.com" :size 15 :font "Oxanium:style=ExtraBold")
      )
    )
  )
)

(defn plate-printed [pos]
  (mirror [pos 0 0]
  (rotate [pi 0 0]
    (difference
      (extrude-linear {:height 3 :center false}
        model-right-plate
      )
      (translate [0 0 0]
        (screw-insert-all-shapes 3.25 1.75 2)
      )
      ;(screw-insert-all-shapes 3 3 1)
      plate-pads-together
      (mirror [0 1 0]
        (mirror [pos 0 0]
          (if (= pos 0) (plate-text text-r)(plate-text text-l))
        )
      )
    )
  )
  )
)

;;;;;;;;;;;;;;;;
;;; Hotfixes ;;;
;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;
;;; Exporting ;;;
;;;;;;;;;;;;;;;;;

(def model-right 
  (difference
    (union
      key-holes
      key-holes-inner
      pinky-connectors
      extra-connectors
      connectors
      inner-connectors
      thumb-type
      thumb-connector-type
      (difference
        (union
          case-walls
          (translate [0 0 3]
            screw-insert-outers
          )
          wrist-attach-case-socket
        )
        usb-holder-space
        usb-holder-notch
        (translate [0 0 3]
          screw-insert-holes
        )
        wrist-attach-case
      )
    )
    (translate [0 0 -20]
      (cube 350 350 40)
    )
  )
)

(def model-left
  (mirror [1 0 0]
  (difference
    (union
      key-holes-left
      key-holes-inner
      pinky-connectors
      extra-connectors
      connectors
      inner-connectors
      thumb-type-left
      thumb-connector-type
      (difference
        (union
          case-walls
          (translate [0 0 3]
            screw-insert-outers
          )
          wrist-attach-case-socket
        )
        usb-holder-space
        usb-holder-notch
        (translate [0 0 3]
          screw-insert-holes
        )
        wrist-attach-case
      )
    )
    (translate [0 0 -20]
      (cube 350 350 40)
    )
  )
  )
)

(if (= thumb-style "default")[
  (def folder (str "things/6key/"nrows"x"ncols"/6key-"nrows"x"ncols"-"))
  (def img (str "things/img/6key-"nrows"x"ncols"-"))])

(if (= thumb-style "mini")[
  (def folder (str "things/5key/"nrows"x"ncols"/5key-"nrows"x"ncols"-"))
  (def img (str "things/img/5key-"nrows"x"ncols"-"))])

(if (= thumb-style "tightly")[
  (def folder (str "things/3key/"nrows"x"ncols"/3key-"nrows"x"ncols"-"))
  (def img (str "things/img/3key-"nrows"x"ncols"-"))])

(spit (str img"rest.scad")
  (write-scad
    (union
      hand-rest-final
      model-right
    )
  )
)

(spit (str img"right.scad")
  (write-scad
    (union
      model-right
    )
  )
)

(spit (str folder"right-ic.scad")
  (write-scad
    ic-fixture
  )
)
            
(spit (str folder"left-ic.scad")
  (write-scad
    (mirror [1 0 0]
      ic-fixture
    )
  )
)

(spit (str folder"right-rest.scad")
  (write-scad
    (union
      hand-rest-final
    )
  )
)

(spit (str folder"left-rest.scad")
  (write-scad
    (mirror [1 0 0]
      hand-rest-final
    )
  )
)

(spit (str folder"right.scad")
  (write-scad
    model-right
  )
)

(spit (str folder"left.scad")
  (write-scad
      model-left
  )
)

(spit (str folder"right-plate.scad")
  (write-scad
    (difference
      (plate-printed 0)
      ;(plate-text text-x text-y)
    )
  )
)

(spit (str folder"left-plate.scad")
  (write-scad
    (difference
      ;(mirror [1 0 0]
      (plate-printed 1)
      ;)
      ;(plate-text text-x text-y)
    )
  )
)

;(spit "things/6key/plate-laser-right.scad"
;  (write-scad
;    model-right-plate
;  )
;)

;(spit "things/6key/plate-laser-left.scad"
;  (write-scad
;    (mirror [-1 0 0]
;      model-right-plate
;    )
;  )
;)

(comment "
(spit "things/tool-1-left.scad"
  (write-scad
    (rotate [0 (deg2rad 0) 0]
      (mirror [0 1 0]
        tool-1
      )
    )
  )
)

(spit "things/tool-2-left.scad"
  (write-scad
    (rotate [0 (deg2rad 0) 0]
      (mirror [0 1 0]
        tool-2
      )
    )
  )
)

(spit "things/tool-3-left.scad"
  (write-scad
    (rotate [0 (deg2rad 0) 0]
      (mirror [0 1 0]
        tool-3
      )
    )
  )
)

(spit "things/tool-1-right.scad"
  (write-scad
    tool-1
  )
)

(spit "things/tool-2-right.scad"
  (write-scad
    tool-2
  )
)
(spit "things/tool-3-right.scad"
  (write-scad
    tool-3
  )
)
")

(defn -main [dum] 1)  ; dummy to make it easier to batch
