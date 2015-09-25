package com.raggiadolf.connectfour.game;

/**
 * Just used to throw the agent out of the search if the time is up.
 */
public class OutOfTimeException extends Exception {
    public OutOfTimeException(String message) {
        super(message);
    }
}
