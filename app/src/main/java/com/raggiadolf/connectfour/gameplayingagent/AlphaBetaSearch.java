package com.raggiadolf.connectfour.gameplayingagent;

import java.util.Collections;
import java.util.List;

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

    public Node AlphaBeta(int depth, State state, int alpha, int beta) throws OutOfTimeException {
        Node bestMove = new Node();
        Node reply;

        long timeUsed = System.currentTimeMillis() - this.start;

        if(timeUsed > (this.playclock - 100)) {
            throw new OutOfTimeException("Out of time!");
        }

        if(state.TerminalTest() || depth <= 0) {
            bestMove.setScore(state.eval());
            bestMove.setMove(state.getLastMove());
            return bestMove;
        }

        List<Integer> actions = state.LegalMoves();
        Collections.shuffle(actions);

        for(Integer action : actions) {
            state.DoMove(action);
            reply = AlphaBeta(depth - 1, state, -beta, -alpha);
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
    public Node AlphaBetaTest(int depth, State state, int alpha, int beta) throws OutOfTimeException {
        Node bestMove = new Node();
        Node reply;

        long timeUsed = System.currentTimeMillis() - this.start;

        if(timeUsed > (this.playclock - 500)) {
            throw new OutOfTimeException("Out of time!");
        }

        if(state.TerminalTest() || depth <= 0) {
            bestMove.setScore(state.testEval());
            bestMove.setMove(state.getLastMove());
            return bestMove;
        }

        List<Integer> actions = state.LegalMoves();
        Collections.shuffle(actions);

        for(Integer action : actions) {
            state.DoMove(action);
            reply = AlphaBetaTest(depth - 1, state, -beta, -alpha);
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
}
