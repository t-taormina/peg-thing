(ns peg-thing.core
  (:gen-class))

(declare succesful-move prompt-move game-over query-rows)

(defn tri*
  "Generates lazy sequence of triangular numbers."
  ([]
   (tri* 0 1))
  ([sum n]
   (let [new-sum (+ sum n)]
     (cons new-sum (lazy-seq (tri* new-sum (inc n)))))))

(def tri (tri*))

(defn triangular?
  "Is the number triangular? e.g. 1, 3, 6, 10, 15, etc."
  [n]
  (= n (last (take-while #(>= n %) tri))))

(defn row-tri
  "The triangular number at the end of row n."
  [n]
  (last (take n tri)))

(defn row-num
  "Returns the row number the position belongs to: pos 1 in row 1,
  positions 2 and 3 in row 2, etc."
  [pos]
  (inc (count (take-while #(> pos %) tri))))

(defn connect
  "Form a mutual connetion between two positions."
  [board max-pos pos neighbor destination]
  (if (<= destination max-pos)
    (reduce (fn [new-board [p1 p2]]
              (assoc-in new-board [p1 :connections p2] neighbor))
            board
            [[pos destination] [destination pos]])
    board))

(defn connect-right
  [board max-pos pos]
  (let [neighbor (inc pos)
        destination (inc neighbor)]
    (if-not (or (triangular? neighbor) (triangular? pos))
      (connect board max-pos pos neighbor destination)
      board)))

(defn connect-down-left
  [board max-pos pos]
  (let [row (row-num pos)
        neighbor (+ row pos)
        destination (+ 1 row neighbor)]
    (connect board max-pos pos neighbor destination)))

(defn connect-down-right
  [board max-pos pos]
  (let [row (row-num pos)
        neighbor (+ 1 row pos)
        destination (+ 2 row neighbor)]
    (connect board max-pos pos neighbor destination)))

(defn add-pos
  "Pegs the position and performs connections."
  [board max-pos pos]
  (let [pegged-board (assoc-in board [pos :pegged] true)]
    (reduce (fn [new-board connection-creation-fn]
              (connection-creation-fn new-board max-pos pos))
            pegged-board
            [connect-right connect-down-left connect-down-right])))

(defn new-board
  "Creates a new board with the given number of rows."
  [rows]
  (let [initial-board {:rows rows}
        max-pos (row-tri rows)]
    (reduce (fn [board pos] (add-pos board max-pos pos))
            initial-board
            (range 1 (inc max-pos)))))

(defn pegged?
  "Does the position have a peg in it?"
  [board pos]
  (get-in board [pos :pegged]))

(defn remove-peg
  "Takes the peg out of a given position."
  [board pos]
  (assoc-in board [pos :pegged] false))

(defn place-peg
  "Puts a peg in a given position."
  [board pos]
  (assoc-in board [pos :pegged] true))

(defn move-peg
  "Take a peg out of p1 and place it in p2."
  [board p1 p2]
  (place-peg (remove-peg board p1) p2))

(defn valid-moves
  "Return a map of all valid moves for pos, where the key is the
  destination and the value is the jumped position."
  [board pos]
  (into {}
        (filter (fn [[destination jumped]]
                  (and (not (pegged? board destination))
                       (pegged? board jumped)))
                (get-in board [pos :connections]))))

(defn valid-move?
  "Return jumped position if the move from p1 to p2 is valid, return
  nil otherwise."
  [board p1 p2]
  (get (valid-moves board p1) p2))

(defn make-move
  "Move peg from p1 to p2, removing the jumped peg."
  [board p1 p2]
  (if-let [jumped (valid-move? board p1 p2)]
    (move-peg (remove-peg board jumped) p1 p2)))

(defn can-move?
  "Do any of the pegged positions have valid moves?"
  [board]
  (some (comp not-empty (partial valid-moves board))
        (map first (filter #(get (second %) :pegged) board))))

(def board (remove-peg(new-board 5) 5))
(pegged? board 5)
(valid-moves board 14)
(valid-move? board 14 5)

;;    Five row board
;; -----------------------
;;         1
;;       2   3
;;     4   5   6
;;   7   8   9   10
;; 11  12  13  14   15


(def my-map {1  {:pegged true, :connections {6 3, 4 2}},
             2  {:pegged true, :connections {9 5, 7 4}},
             3  {:pegged false, :connections {10 6, 8 5}},
             4  {:pegged false, :connections {13 8, 11 7, 6 5, 1 2}},
             5  {:pegged false, :connections {14 9, 12 8}},
             6  {:pegged false, :connections {15 10, 13 9, 4 5, 1 3}},
             7  {:pegged true, :connections {9 8, 2 4}},
             8  {:pegged true, :connections {10 9, 3 5}},
             9  {:pegged true, :connections {7 8, 2 5}},
             10 {:pegged true, :connections {8 9, 3 6}},
             11 {:pegged true, :connections {13 12, 4 7}},
             12 {:pegged true, :connections {14 13, 5 8}},
             13 {:pegged true, :connections {15 14, 11 12, 6 9, 4 8}},
             14 {:pegged true, :connections {12 13, 5 9}},
             15 {:pegged true, :connections {13 14, 6 10}},
             :rows 5})



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
