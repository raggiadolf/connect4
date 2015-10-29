package com.raggiadolf.connectfour;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class MultiPlayerFragmentActivityFragment extends Fragment {
    BoardView m_boardView;
    View v;

    public MultiPlayerFragmentActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_multi_player, container, false);
        m_boardView = (BoardView)v.findViewById(R.id.boardview);

        m_boardView.setMoveEventHandler(new OnMoveEventHandler() {
            @Override
            public void onMove(int action) {
                listener.onMove(action);
            }
        });

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Attach the fragment to a context, not used here, we use the deprecated version
     * that attaches the fragment to an activity
     * @param ctx
     */
    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        listener = (OnMoveListener) ctx;
    }

    /**
     * Attaches the fragment to an activity and assigns the activity as the listener.
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (OnMoveListener) activity;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Called when the user makes a move on the board
     */
    public interface OnMoveListener {
        void onMove(int action);
    }
    OnMoveListener listener;

    /**
     * Place a disc on the board
     * @param lastMove
     * @param lastRow
     * @param lastPlayerToken
     */
    public void updateDisplay(int lastMove, int lastRow, char lastPlayerToken) {
        m_boardView.placeDisc(lastMove, lastRow, lastPlayerToken);
    }

    /**
     * Can the user make a move on the board or not?
     * @param move
     */
    public void setCanMove(boolean move) {
        m_boardView.setCanMove(move);
    }

    /**
     * Set up the board from a string representation of a game state.
     * @param newBoard
     */
    public void setupBoard(String newBoard) {
        m_boardView.setupBoard(newBoard);
    }
}
