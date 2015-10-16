package com.raggiadolf.connectfour;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;

public class MainMenuActivity extends AppCompatActivity {
    MultiStateToggleButton m_difficultySelector;
    Difficulty m_difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        m_difficultySelector = (MultiStateToggleButton) this.findViewById(R.id.difficulty_selector);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
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

    public void startSinglePlayer(View view) {
        Intent intent = new Intent(this, SinglePlayerActivity.class);

        switch(m_difficultySelector.getValue()) {
            case 0:
                intent.putExtra("difficulty", "easy");
                break;
            case 1:
                intent.putExtra("difficulty", "medium");
                break;
            case 2:
                intent.putExtra("difficulty", "hard");
                break;
            default:
                intent.putExtra("difficulty", "easy");
                break;
        }

        startActivity(intent);
    }

    public void startMultiPlayer(View view) {
        Intent intent = new Intent(this, MultiPlayerActivity.class);
        startActivity(intent);
    }
}
