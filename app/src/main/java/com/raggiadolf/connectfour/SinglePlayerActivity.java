package com.raggiadolf.connectfour;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.raggiadolf.connectfour.game.State;

public class SinglePlayerActivity extends AppCompatActivity {

    BoardView m_boardView;
    State m_gameState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player);

        m_boardView = (BoardView) findViewById(R.id.boardview);

        m_boardView.setMoveEventHandler(new OnMoveEventHandler() {
            @Override
            public void onMove(int action) {
                m_gameState.DoMove(action);
                updateDisplay();
            }
        });

        m_gameState = new State();
        m_boardView.setBoard(m_gameState.toString());
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
        m_boardView.setBoard(m_gameState.toString());
        if(m_gameState.GoalTest()) {
            Toast.makeText(getApplicationContext(), "Game over", Toast.LENGTH_SHORT).show();
        }
    }
}
