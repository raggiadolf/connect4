package com.raggiadolf.connectfour.gameplayingagent;

/**
 * Nodes of the search tree that the search generates.
 * Holds the score for a particular move, and the move.
 */
public class Node {
    private Integer move;
    private int score;

    public Node() {
        this.move = null;
        this.score = Integer.MIN_VALUE + 1;
    }

    public void setScore(int score) { this.score = score; }

    public int getScore() { return this.score; }

    public void setMove(Integer move) { this.move = move; }

    public Integer getMove() { return this.move; }
}
