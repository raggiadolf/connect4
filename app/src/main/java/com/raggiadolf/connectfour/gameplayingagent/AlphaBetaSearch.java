package com.raggiadolf.connectfour.gameplayingagent;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AlphaBetaSearch{

    /* "You probably have a very subtle bug in your code." */

    /**
     * We keep track of the time by noting the time when the object is
     * instantiated, then whenever we check a new state, we check if we are
     * out of time, if we are, we throw an OutOfTimeException which the agent catches.
     */

    private int playclock;
    private long start;

    public AlphaBetaSearch(int playclock){
        this.playclock = playclock * 1000;
        this.start = (System.currentTimeMillis());
    }

    public Node AlphaBetaHard(int depth, State state, int alpha, int beta) throws OutOfTimeException {
        Node bestMove = new Node();
        Node reply;

        long timeUsed = System.currentTimeMillis() - this.start;

        if(timeUsed > (this.playclock - 100)) {
            throw new OutOfTimeException("Out of time!");
        }

        if(state.TerminalTest() || depth <= 0) {
            bestMove.setScore(state.hardEval());
            bestMove.setMove(state.getLastMove());
            return bestMove;
        }

        List<Integer> actions = state.LegalMoves();
        Collections.shuffle(actions);

        for(Integer action : actions) {
            state.DoMove(action);
            reply = AlphaBetaHard(depth - 1, state, -beta, -alpha);
            reply.setScore(-reply.getScore());
            state.UndoMove(action);

            if(reply.getScore() > bestMove.getScore()) {
                bestMove.setMove(action);
                bestMove.setScore(reply.getScore());
            }

            if(bestMove.getScore() > alpha) {
                alpha = bestMove.getScore();
            }

            if(alpha >= beta) break;
        }

        return bestMove;
    }

    /**
     * A second search function which utilises a different evaluation function
     * For testing purposes.
     */
    public Node AlphaBetaMedium(int depth, State state, int alpha, int beta) throws OutOfTimeException {
        Node bestMove = new Node();
        Node reply;

        long timeUsed = System.currentTimeMillis() - this.start;

        if(timeUsed > (this.playclock - 500)) {
            throw new OutOfTimeException("Out of time!");
        }

        if(state.TerminalTest() || depth <= 0) {
            bestMove.setScore(state.mediumEval());
            bestMove.setMove(state.getLastMove());
            return bestMove;
        }

        List<Integer> actions = state.LegalMoves();
        Collections.shuffle(actions);

        for(Integer action : actions) {
            state.DoMove(action);
            reply = AlphaBetaMedium(depth - 1, state, -beta, -alpha);
            reply.setScore(-reply.getScore());
            state.UndoMove(action);

            if(reply.getScore() > bestMove.getScore()) {
                bestMove.setMove(action);
                bestMove.setScore(reply.getScore());
            }

            if(bestMove.getScore() > alpha) {
                alpha = bestMove.getScore();
            }

            if(alpha >= beta) break;
        }

        return bestMove;
    }

    public Node AlphaBetaEasy(int depth, State state, int alpha, int beta) throws OutOfTimeException {
        Node randomMove = new Node();
        List<Integer> actions = state.LegalMoves();

        randomMove.setMove(actions.get(randInt(0, actions.size() - 1)));

        return randomMove;
    }

    public static int randInt(int min, int max) {
        Random rand = new Random(System.currentTimeMillis());

        return rand.nextInt((max - min) + 1) + min;
    }
}
