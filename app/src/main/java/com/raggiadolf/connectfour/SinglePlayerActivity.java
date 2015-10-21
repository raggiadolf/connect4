package com.raggiadolf.connectfour;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.raggiadolf.connectfour.gameplayingagent.*;

import java.util.List;

public class SinglePlayerActivity extends AppCompatActivity {

    private Difficulty diff;
    private boolean mGameOver = false;
    private boolean mPlayersTurn = true;

    private BoardView m_boardView;
    private State m_gameState;

    private LinearLayout mGameOverMessage;
    private TextView mGameOverText;
    private Animation mAnimSlideIn;


    private static int playclock = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player);

        m_boardView = (BoardView) findViewById(R.id.boardview);
        mGameOverMessage = (LinearLayout) findViewById(R.id.gameovermessage);
        mGameOverText = (TextView) findViewById(R.id.gameovertext);
        mAnimSlideIn = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in_from_left);

        m_boardView.setMoveEventHandler(new OnMoveEventHandler() {
            @Override
            public void onMove(int action) {
                List<Integer> legalMoves = m_gameState.LegalMoves();

                if(legalMoves.contains(action) && !mGameOver) { // Check to see whether the move was valid
                    m_gameState.DoMove(action);
                    mPlayersTurn = true;
                    updateDisplay();
                    m_boardView.setCanMove(false);
                    if(!mGameOver) {
                        new AlphaBetaSearchTask().execute(m_gameState);
                    }
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
            // Player won!
            mGameOver = true;
            if(mPlayersTurn) {
                mGameOverText.setText("You won!");
                mGameOverMessage.setBackgroundColor(getResources().getColor(R.color.player2));
            } else {
                mGameOverText.setText("You lost.");
                mGameOverMessage.setBackgroundColor(getResources().getColor(R.color.player1));
            }
            // Slide in gameover
            mGameOverMessage.startAnimation(mAnimSlideIn);
            mGameOverMessage.setVisibility(View.VISIBLE);
        }
    }

    public void rematch(View view) {
        Intent intent = new Intent(this, SinglePlayerActivity.class);
        intent.putExtra("difficulty", diff.toString());
        startActivity(intent);
    }

    public void backToMainMenu(View view) {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
                            nextMove = abs.AlphaBetaEasy(i, searchState, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);
                            break;
                        case medium:
                            nextMove = abs.AlphaBetaMedium(i, searchState, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);
                            break;
                        case hard:
                            nextMove = abs.AlphaBetaHard(i, searchState, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);
                            break;
                        default:
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
            mPlayersTurn = false;
            updateDisplay();
            m_boardView.setCanMove(true);
        }
    }
}
