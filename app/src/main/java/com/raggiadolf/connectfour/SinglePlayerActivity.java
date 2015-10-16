package com.raggiadolf.connectfour;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.raggiadolf.connectfour.gameplayingagent.*;

import java.util.List;

public class SinglePlayerActivity extends AppCompatActivity {

    private Difficulty diff;

    BoardView m_boardView;
    State m_gameState;

    private static int playclock = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player);

        m_boardView = (BoardView) findViewById(R.id.boardview);

        m_boardView.setMoveEventHandler(new OnMoveEventHandler() {
            @Override
            public void onMove(int action) {
                List<Integer> legalMoves = m_gameState.LegalMoves();

                if(legalMoves.contains(action)) { // Check to see whether the move was valid
                    m_gameState.DoMove(action);
                    updateDisplay();
                    m_boardView.setCanMove(false);
                    new AlphaBetaSearchTask().execute(m_gameState);
                }
            }
        });

        Intent intent = getIntent();
        diff = Difficulty.valueOf(intent.getStringExtra("difficulty"));

        m_gameState = new State();
        m_boardView.setupBoard(m_gameState.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_single_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateDisplay() {
        //m_boardView.setBoard(m_gameState.toString());
        m_boardView.placeDisc(m_gameState.getLastMove(), m_gameState.getLastRow(), m_gameState.getLastPlayerToken());
        if(m_gameState.GoalTest()) {
            Toast.makeText(getApplicationContext(), "Game over", Toast.LENGTH_SHORT).show();
        }
    }

    private class AlphaBetaSearchTask extends AsyncTask<State, Integer, Integer> {
        @Override
        protected Integer doInBackground(State... params) {
            State searchState = new State(params[0]);
            AlphaBetaSearch abs = new AlphaBetaSearch(playclock);
            Node nextMove = new Node();
            try {
                for(int i = 2; i < 42; i++) {
                    switch(diff) {
                        case easy:
                            Log.d("abs", "easy");
                            nextMove = abs.AlphaBetaEasy(i, searchState, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);
                            break;
                        case medium:
                            Log.d("abs", "medium");
                            nextMove = abs.AlphaBetaMedium(i, searchState, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);
                            break;
                        case hard:
                            Log.d("abs", "hard");
                            nextMove = abs.AlphaBetaHard(i, searchState, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);
                            break;
                        default:
                            Log.d("abs", "default");
                            nextMove = abs.AlphaBetaEasy(i, searchState, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);
                    }
                    publishProgress(i);
                }
                return nextMove.getMove();
            } catch(OutOfTimeException ex) {
                return nextMove.getMove();
            }
        }

        /**
         * Here we want to update some progressbar in the view to display that the game is in a 'thinking' state.
         * @param values
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer action) {
            m_gameState.DoMove(action);
            updateDisplay();
            m_boardView.setCanMove(true);
        }
    }
}
