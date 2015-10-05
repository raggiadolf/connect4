package com.raggiadolf.connectfour;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.raggiadolf.connectfour.gameplayingagent.State;

public class MultiPlayerActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    BoardView m_boardView;
    State m_gameState;

    private static final String TAG = "Connect4";

    // Request code used to invoke sign in user interactions
    private static final int RC_SIGN_IN = 0;

    // Client used to interact with Google APIs
    private GoogleApiClient m_googleApiClient;

    // Are we currently resolving a connection failure?
    private boolean m_ResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean m_signinClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean m_autoStartSignInFlow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        // Create the Google Api Client with access to Plus and Games
        m_googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        setContentView(R.layout.activity_multi_player);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
        m_googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
        if(m_googleApiClient.isConnected()) {
            m_googleApiClient.disconnect();
        }
    }

    private void showSignInBar() {
        Log.d(TAG, "Showing sign in bar");
        findViewById(R.id.sign_in_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_out_bar).setVisibility(View.GONE);
    }

    private void showSignOutBar() {
        Log.d(TAG, "Showing sign out bar");
        findViewById(R.id.sign_in_bar).setVisibility(View.GONE);
        findViewById(R.id.sign_out_bar).setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_sign_in:
                Log.d(TAG, "Sign-in button clicked");
                m_signinClicked = true;
                m_googleApiClient.connect();
                break;
            case R.id.button_sign_out:
                Log.d(TAG, "Sign-out button clicked");
                m_signinClicked = false;
                Games.signOut(m_googleApiClient);
                m_googleApiClient.disconnect();
                showSignInBar();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_multi_player, menu);
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

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");
        showSignOutBar();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect");
        m_googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);
        if(m_ResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed ignoring connection failure; already resolving.");
            return;
        }

        if(m_signinClicked || m_autoStartSignInFlow) {
            m_autoStartSignInFlow = false;
            m_signinClicked = false;
            m_ResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, m_googleApiClient,
                    connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if(requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode=" + responseCode + ", intent= " + intent);
            m_signinClicked = false;
            m_ResolvingConnectionFailure = false;
            if(responseCode == RESULT_OK) {
                m_googleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, responseCode, R.string.signin_other_error);
            }
        }
    }
}
