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

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        listener = (OnMoveListener) ctx;
    }

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

    public interface OnMoveListener {
        public void onMove(int action);
    }
    OnMoveListener listener;

    public void updateDisplay(int lastMove, int lastRow, char lastPlayerToken) {
        m_boardView.placeDisc(lastMove, lastRow, lastPlayerToken);
    }

    public void setCanMove(boolean move) {
        m_boardView.setCanMove(move);
    }

    public void setupBoard(String newBoard) {
        m_boardView.setupBoard(newBoard);
    }
}
