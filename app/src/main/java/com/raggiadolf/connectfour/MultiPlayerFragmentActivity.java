package com.raggiadolf.connectfour;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.pkmmte.view.CircularImageView;
import com.raggiadolf.connectfour.gameplayingagent.State;

import java.util.ArrayList;
import java.util.List;

public class MultiPlayerFragmentActivity extends FragmentActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        OnInvitationReceivedListener, OnTurnBasedMatchUpdateReceivedListener, View.OnClickListener, MultiPlayerFragmentActivityFragment.OnMoveListener {

    State m_gameState;

    private static final String TAG = "Connect4";

    // For our intents
    private static final int RC_SIGN_IN = 9001;
    final static int RC_SELECT_PLAYERS  = 10000;
    final static int RC_LOOK_AT_MATCHES = 10001;
    final static int RC_REQUEST_ACHIEVEMENTS = 10002;
    final static int RC_LEADERBOARD = 10003;

    // Client used to interact with Google APIs
    private GoogleApiClient m_googleApiClient;

    // Are we currently resolving a connection failure?
    private boolean m_ResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean m_signinClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean m_autoStartSignInFlow = true;

    // Local convenience pointers
    private CircularImageView mOpponentImage;
    private TextView mOpponentDisplayName;
    private CircularImageView mUserImage;
    private TextView mUserDisplayName;
    private LinearLayout mGameOverMessage;
    private TextView mGameOverText;
    private Animation mAnimSlideIn;

    private AlertDialog m_alertDialog;

    // Should I be showing to turn API?
    public boolean isDoingTurn = false;
    public boolean isIngame = false;

    // This is the current match we're in; null if not loaded
    public TurnBasedMatch m_match;

    // This is the current match data after being unpersisted.
    // Do not retain references to match data once you have
    // taken an action on the match, such as takeTurn()
    public ConnectFourState mTurnData;

    // Used to show display pics and username for players
    private Participant mOpponent = null;
    private Participant mUser = null;
    private Integer mUserColor = null;

    // Simply used to catch an achievement and avoid
    // displaying it just before we start a fragment
    private boolean mStartedMatch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_player_fragment);

        // Create the Google Api Client with access to Plus and Games
        m_googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        // Setup signin and signout buttons
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        mOpponentImage = (CircularImageView) findViewById(R.id.opponentimage);
        mOpponentDisplayName = (TextView) findViewById(R.id.opponentdisplayname);
        mUserImage = (CircularImageView) findViewById(R.id.userimage);
        mUserDisplayName = (TextView) findViewById(R.id.userdisplayname);

        mGameOverMessage = (LinearLayout) findViewById(R.id.gameovermessage);
        mGameOverText = (TextView) findViewById(R.id.gameovertext);
        mAnimSlideIn = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in_from_left);

    }

    /**
     * The event handler for our game play fragment
     * @param action the action that was just made on the board(column placed into)
     */
    @Override
    public void onMove(int action) {
        List<Integer> legalMoves = m_gameState.LegalMoves();

        if (legalMoves.contains(action)) {
            m_gameState.DoMove(action);

            String nextParticipantId = getNextParticipantId();
            updateDisplay();
            // Create the next turn
            mTurnData.setLastCol(m_gameState.getLastMove());
            mTurnData.setLastRow(m_gameState.getLastRow());
            mTurnData.setLastPlayer(m_gameState.getPlayer());
            mTurnData.setTurnState(m_gameState.toString());

            // TODO: If draw?

            if (m_gameState.GoalTest()) { // The player just won
                mGameOverText.setText("You won!");
                mGameOverMessage.setBackgroundColor((m_gameState.getLastPlayerToken() == 'R') ? getResources().getColor(R.color.player1) : getResources().getColor(R.color.player2));
                mGameOverMessage.startAnimation(mAnimSlideIn);
                mGameOverMessage.setVisibility(View.VISIBLE);
                mGameOverMessage.bringToFront();

                // Register achievements
                Games.Achievements.unlock(m_googleApiClient, getResources().getString(R.string.TASTE_OF_BLOOD));
                Games.Achievements.increment(m_googleApiClient, getResources().getString(R.string.UP_AND_COMER), 1);
                Games.Achievements.increment(m_googleApiClient, getResources().getString(R.string.BEASTMODE), 1);
                Games.Leaderboards.submitScore(m_googleApiClient, getResources().getString(R.string.LEADERBOARD), 1);

                PendingResult<Leaderboards.LoadPlayerScoreResult> result = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(m_googleApiClient, getResources().getString(R.string.LEADERBOARD), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC);
                result.setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                    @Override
                    public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                        Games.Leaderboards.submitScore(m_googleApiClient, getResources().getString(R.string.LEADERBOARD), loadPlayerScoreResult.getScore().getRawScore() + 1);
                    }
                });

                // Setup the finish match results
                ParticipantResult myRes = new ParticipantResult(m_match.getParticipantId(Games.Players.getCurrentPlayerId(m_googleApiClient)), ParticipantResult.MATCH_RESULT_WIN, ParticipantResult.PLACING_UNINITIALIZED);
                ParticipantResult oppoRes = new ParticipantResult(getNextParticipantId(), ParticipantResult.MATCH_RESULT_LOSS, ParticipantResult.PLACING_UNINITIALIZED);
                List<ParticipantResult> results = new ArrayList<>();
                results.add(myRes);
                results.add(oppoRes);

                // Call finish match to notify the participants of the results
                Games.TurnBasedMultiplayer.finishMatch(m_googleApiClient, m_match.getMatchId(),
                        mTurnData.persist(), results)
                        .setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                            @Override
                            public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                                processResult(result);
                            }
                        });
            } else { // Take the turn the player just made
                Games.TurnBasedMultiplayer.takeTurn(m_googleApiClient, m_match.getMatchId(),
                        mTurnData.persist(), nextParticipantId).setResultCallback(
                        new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                            @Override
                            public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                                processResult(result);
                            }
                        }
                );
                mTurnData = null;
            }
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

    /**
     * Updates the display by placing a disc(which starts the dropped disc animation)
     */
    public void updateDisplay() {
        if(m_gameState.getLastMove() != null) {
            MultiPlayerFragmentActivityFragment fragment = (MultiPlayerFragmentActivityFragment) getFragmentManager().findFragmentByTag("gameplayfragment");
            fragment.updateDisplay(m_gameState.getLastMove(), m_gameState.getLastRow(), m_gameState.getLastPlayerToken());
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
        m_googleApiClient.connect();
    }

    /**
     * Handles the signin/signout buttons
     * @param view The button that was just clicked
     */
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.sign_in_button:
                m_signinClicked = true;
                m_match = null;
                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                m_googleApiClient.connect();
                break;
            case R.id.sign_out_button:
                m_signinClicked = false;
                Games.signOut(m_googleApiClient);
                if(m_googleApiClient.isConnected()) {
                    m_googleApiClient.disconnect();
                }
                setViewVisibility();
                break;
        }
    }

    /**
     * Called when the client is connected.
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        // Retrieve the TurnBasedMatch from the bundle
        if(bundle != null) {
            m_match = bundle.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if(m_match != null) {
                if(m_googleApiClient == null || !m_googleApiClient.isConnected()) {
                    Log.d(TAG, "Warning: accessing TurnBasedMatch when not connected");
                }

                updateMatch(m_match);
                return;
            }
        }
        setViewVisibility();

        // Might not be needed
        Games.Invitations.registerInvitationListener(m_googleApiClient, this);
        Games.TurnBasedMultiplayer.registerMatchUpdateListener(m_googleApiClient, this);
    }

    /**
     * Called if the clients connection is suspended, tries to reconnect
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect");
        m_googleApiClient.connect();
        setViewVisibility();
    }

    /**
     * Called when the connection failed
     * @param connectionResult
     */
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

        setViewVisibility();
    }

    /**
     * Displayes the inbox, will get back onActivityResult where
     * we figure out what we clicked on, using the RC_LOOK_AT_MATCHES constant.
     * @param view
     */
    public void onCheckGamesClicked(View view) {
        mGameOverMessage.setVisibility(View.GONE);
        Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(m_googleApiClient);
        startActivityForResult(intent, RC_LOOK_AT_MATCHES);
    }

    /**
     * Opens the create-game UI, will get back an onActivityResult where
     * we figure out what we clicked on, using the RC_SELECT_PLAYERS constant.
     * @param view
     */
    public void onStartMatchClicked(View view) {
        Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(m_googleApiClient,
                1, 1, true);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    /**
     * Opens up the defualt Achievements view
     * @param view
     */
    public void onCheckAchievementsClicked(View view) {
        startActivityForResult(Games.Achievements.getAchievementsIntent(m_googleApiClient), RC_REQUEST_ACHIEVEMENTS);
    }

    /**
     * Opens up the default Leaderboard view, we pass in the leaderboard ID declared in our value xml, since we only have
     * the one leaderboard for out app.
     * @param view
     */
    public void onLeaderboardClicked(View view) {
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(m_googleApiClient, getResources().getString(R.string.LEADERBOARD)), RC_LEADERBOARD);
    }

    /**
     * Here we handle the player returning from the calls to Look at Matches or Select Players(Start Match)
     * @param requestCode The code we passed to startActivityForResult()
     * @param responseCode Specified by the second activity, RESULT_OK if the operation was successful
     *                     or RESULT_CANCELED if the operation failed for some reason.
     * @param intent carries the result data
     */
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if(requestCode == RC_SIGN_IN) {
            m_signinClicked = false;
            m_ResolvingConnectionFailure = false;
            if(responseCode == RESULT_OK) {
                m_googleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, responseCode, R.string.signin_other_error);
            }
        }

        if(requestCode == RC_LOOK_AT_MATCHES) {
            // Returning from the 'Select match' dialog

            if (responseCode != Activity.RESULT_OK) {
                // User cancelled
                return;
            }

            // Fetch the match we just opened
            TurnBasedMatch match = intent.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);

            if (match != null) { // Start the new match
                m_gameState = null;
                mTurnData = null;
                isIngame = true;

                getFragmentManager().beginTransaction()
                        .add(R.id.gameplayfragment, new MultiPlayerFragmentActivityFragment(), "gameplayfragment")
                        .commit();

                // Force the fragment to be committed, commit was not committing fast enough
                // resulting in a NPE when we were trying to set up the board.
                getFragmentManager().executePendingTransactions();

                // Unlock the achievement for the player for accepting an invitation to a match.
                // This probably erroneously gives the player an achievement if he opens up a match he himself
                // started earlier. Fix in a future DLC.
                Games.Achievements.unlock(m_googleApiClient, getResources().getString(R.string.CHALLENGE_ACCEPTED));

                // Update the match with the info from the TurnBasedMatch object.
                updateMatch(match);
            }
        }

        if(requestCode == RC_SELECT_PLAYERS) {
            // Returned from 'Select players to invite' dialog
            if (responseCode != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            // Get the invitee list.
            final ArrayList<String> invitees =
                    intent.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            // Get auto-match criteria.
            Bundle autoMatchCriteria = null;
            int minAutoMatchPlayers = intent.getIntExtra(
                    Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = intent.getIntExtra(
                    Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                    .addInvitedPlayers(invitees)
                    .setAutoMatchCriteria(autoMatchCriteria)
                    .build();

            // Create and start the match.
            mStartedMatch = true;

            Games.TurnBasedMultiplayer
                    .createMatch(m_googleApiClient, tbmc)
                    .setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                        @Override
                        public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                            processResult(result);
                        }
                    });
            showSpinner();
        }
    }

    public void setViewVisibility() {
        boolean isSignedIn = (m_googleApiClient != null) && (m_googleApiClient.isConnected());

        if(!isSignedIn) { // Setup the login layout of the user is not signed in
            findViewById(R.id.login_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.matchup_layout).setVisibility(View.GONE);
            findViewById(R.id.gameplay_layout).setVisibility(View.GONE);

            if (m_alertDialog != null) {
                m_alertDialog.dismiss();
            }
            return;
        }


        // Display the name of the currently signed in user
        ((TextView) findViewById(R.id.name_field)).setText(Games.Players.getCurrentPlayer(
                m_googleApiClient).getDisplayName());
        findViewById(R.id.login_layout).setVisibility(View.GONE);


        if(isIngame) {

            // Hide the matchup layout and show the gameplay_layout
            findViewById(R.id.matchup_layout).setVisibility(View.GONE);
            findViewById(R.id.gameplay_layout).setVisibility(View.VISIBLE);

            MultiPlayerFragmentActivityFragment fragment = (MultiPlayerFragmentActivityFragment) getFragmentManager().findFragmentByTag("gameplayfragment");

            // Bold the name of the user who's turn it is to move.
            if (isDoingTurn) {
                fragment.setCanMove(true);
                mOpponentDisplayName.setAlpha(0.5f);
                mUserDisplayName.setAlpha(1f);
            } else {
                fragment.setCanMove(false);
                mOpponentDisplayName.setAlpha(1f);
                mUserDisplayName.setAlpha(0.5f);
            }

            mUserImage.setBorderColor(mUserColor);
            mOpponentImage.setBorderColor((mUserColor == getResources().getColor(R.color.player1)) ? getResources().getColor(R.color.player2) : getResources().getColor(R.color.player1));

            // Here we use an asynctask to download the users display pictures and display them above the game playing board
            // This should instead be done only once when the players of the game have been established. Doing it here repeatedly
            // is silly, but a quick and dirty solution when quick and dirty solutions are needed.
            if(mUser.getIconImageUrl() != null) {
                loadWebImage(mUserImage, mUser.getIconImageUrl());
            } else {
                mUserImage.setImageResource(R.drawable.anon);
            }
            mUserDisplayName.setText(mUser.getDisplayName());

            if (mOpponent != null) {
                if(mOpponent.getIconImageUrl() != null) {
                    loadWebImage(mOpponentImage, mOpponent.getIconImageUrl());
                } else {
                    mOpponentImage.setImageResource(R.drawable.anon);
                }
                mOpponentDisplayName.setText(mOpponent.getDisplayName());
            }
        } else { // Hide the gameplay layout and show the matchup, destroy the fragment if needed.
            findViewById(R.id.matchup_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.gameplay_layout).setVisibility(View.GONE);

            MultiPlayerFragmentActivityFragment fragment = (MultiPlayerFragmentActivityFragment) getFragmentManager().findFragmentByTag("gameplayfragment");
            if(fragment != null) {
                getFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
    }

    // Switch to gameplay view.
    public void setGameplayUI() {
        isDoingTurn = true;
        mUserColor = (m_gameState.getLastPlayerToken() == 'R') ? getResources().getColor(R.color.player2) : getResources().getColor(R.color.player1);
        setViewVisibility();
    }

    // Helpful dialogs

    public void showSpinner() {
        findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
    }

    public void dismissSpinner() {
        findViewById(R.id.progressLayout).setVisibility(View.GONE);
    }

    /**
     * Called when the user receives an invitation for a match, we use it to fade in a clickable
     * popup view informing the user of who invited him to a match.
     * @param invitation
     */
    @Override
    public void onInvitationReceived(Invitation invitation) {
        // Set up the popup view
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.game_invitation_popup, null);
        TextView popupText = (TextView) popupView.findViewById(R.id.popuptext);
        popupText.setText("An invitation has arrived from " + invitation.getInviter().getDisplayName() + ". Click here to view match.");
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT
        );

        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        if(!isFinishing()) { // We don't want to show the layout of the activity is destroying itself.
            popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 100);
            // Use a handler to hide the view after 5 seconds
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    popupWindow.dismiss();
                }
            }, 5000);
        }

        // Give the user an achievement for getting an invitation to a match.
        Games.Achievements.unlock(m_googleApiClient, getResources().getString(R.string.CHALLENGER_APPEARED));

    }

    @Override
    public void onInvitationRemoved(String s) {
        Toast.makeText(this, "An invitation was removed.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when a match the user is participating in is updated.
     * We could possibly take advantage of this for push notifications?
     * @param turnBasedMatch the match that was just updated.
     */
    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
        // Match updated, drop the disc into the new disc into the correct slot, update the state
        // and allow the user to make the next move.
        if(isIngame) {
            m_match = turnBasedMatch;

            updateMatch(m_match);
        } else {
            Log.d(TAG, "Not ingame");
        }
    }

    @Override
    public void onTurnBasedMatchRemoved(String s) {
        Toast.makeText(this, "A match was removed.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Start a turnbased multiplayer match
     * @param match the match to start
     */
    public void startMatch(TurnBasedMatch match) {
        // Set up the gamestate and the Turn data.
        m_gameState = new State();
        mTurnData = new ConnectFourState();
        mTurnData.setLastCol(m_gameState.getLastMove());
        mTurnData.setLastRow(m_gameState.getLastRow());
        mTurnData.setLastPlayer(m_gameState.getPlayer());
        mTurnData.setTurnState(m_gameState.toString());
        isIngame = true;

        // Launch the gameplay fragment
        getFragmentManager().beginTransaction()
                .add(R.id.gameplayfragment, new MultiPlayerFragmentActivityFragment(), "gameplayfragment")
                .commit();

        m_match = match;

        // Establish the player id's
        String playerId = Games.Players.getCurrentPlayerId(m_googleApiClient);
        String myParticipantId = m_match.getParticipantId(playerId);

        showSpinner();

        // Take the first turn
        Games.TurnBasedMultiplayer.takeTurn(m_googleApiClient, m_match.getMatchId(),
                null, myParticipantId).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                        processResult(result);
                    }
                }
        );
    }

    /**
     * The user requested a rematch from the endgame view, destroy the current fragment and
     * call the rematch method which establishes a rematch between the current players.
     * @param view
     */
    public void rematch(View view) {
        mGameOverMessage.setVisibility(View.GONE);
        MultiPlayerFragmentActivityFragment fragment = (MultiPlayerFragmentActivityFragment) getFragmentManager().findFragmentByTag("gameplayfragment");
        getFragmentManager().beginTransaction().remove(fragment).add(R.id.gameplayfragment, fragment).commit();
        rematch();
    }

    /**
     * Destroys the current fragment, sending the user back to the multiplayer view.
     * @param view
     */
    public void backToMainMenu(View view) {
        mGameOverMessage.setVisibility(View.GONE);
        isIngame = false;
        setViewVisibility();
    }

    /**
     * Establish a rematch by calling match.rematch and resetting the match and turndata.
     */
    public void rematch() {
        showSpinner();
        if(!m_match.canRematch()) {
            Log.d(TAG, "Can't rematch.. Shouldn't really be rematching.. still is?");
        }
        Games.TurnBasedMultiplayer.rematch(m_googleApiClient, m_match.getMatchId()).setResultCallback(
                new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                    @Override
                    public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                        processResult(result);
                    }
                }
        );
        m_match = null;
        isDoingTurn = false;
    }

    /**
     * Get the next participant. In this function, we assume that we are
     * round-robin, with all known players going before all automatch players.
     * This is not a requirement; players can go in any order. However, you can
     * take turns in any order.
     *
     * @return participantId of next player, or null if automatching
     */
    public String getNextParticipantId() {

        String playerId = Games.Players.getCurrentPlayerId(m_googleApiClient);
        String myParticipantId = m_match.getParticipantId(playerId);

        ArrayList<String> participantIds = m_match.getParticipantIds();

        int desiredIndex = -1;

        for (int i = 0; i < participantIds.size(); i++) {
            if (participantIds.get(i).equals(myParticipantId)) {
                desiredIndex = i + 1;
            }
        }

        if (desiredIndex < participantIds.size()) {
            return participantIds.get(desiredIndex);
        }

        if (m_match.getAvailableAutoMatchSlots() <= 0) {
            // You've run out of automatch slots, so we start over.
            return participantIds.get(0);
        } else {
            // You have not yet fully automatched, so null will find a new
            // person to play against.
            return null;
        }
    }
    /**
     * The main function which is called when players choose a match
     * from the inbox, or else create a match and want to start it.
     * @param match the match that we are currently participating in
     */
    public void updateMatch(TurnBasedMatch match) {
        m_match = match;

        int status = m_match.getStatus();
        int turnStatus = m_match.getTurnStatus();

        mOpponent = m_match.getDescriptionParticipant();
        mUser = m_match.getParticipant(match.getParticipantId(Games.Players.getCurrentPlayerId(m_googleApiClient)));

        switch (status) {
            case TurnBasedMatch.MATCH_STATUS_CANCELED:
                showWarning("Canceled!", "This game was canceled!");
                return;
            case TurnBasedMatch.MATCH_STATUS_EXPIRED:
                showWarning("Expired!", "This game is expired.  So sad!");
                return;
            case TurnBasedMatch.MATCH_STATUS_AUTO_MATCHING:
                showWarning("Waiting for auto-match...",
                        "We're still waiting for an automatch partner.");
                return;
            case TurnBasedMatch.MATCH_STATUS_COMPLETE:
                if(turnStatus == TurnBasedMatch.MATCH_TURN_STATUS_COMPLETE) { // We've already finished this match
                    return;
                }
                // TODO: Check for a draw
                mGameOverText.setText("You lost.");
                mGameOverMessage.setBackgroundColor((m_gameState.getLastPlayerToken() == 'W') ? getResources().getColor(R.color.player1) : getResources().getColor(R.color.player2));
                mGameOverMessage.startAnimation(mAnimSlideIn);
                mGameOverMessage.setVisibility(View.VISIBLE);
                mGameOverMessage.bringToFront();

                Games.Achievements.unlock(m_googleApiClient, getResources().getString(R.string.WINSOME_LOSESOME));

                // Call finishMatch to acknowledge that we have registered our result
                // and to complete the match.
                Games.TurnBasedMultiplayer.finishMatch(m_googleApiClient, m_match.getMatchId())
                        .setResultCallback(new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {
                            @Override
                            public void onResult(TurnBasedMultiplayer.UpdateMatchResult result) {
                                processResult(result);
                            }
                        });
                break;
        }

        // OK, it's active. Check on turn status.
        switch (turnStatus) {
            case TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN:
                mTurnData = ConnectFourState.unpersist(m_match.getData());
                if(mTurnData == null) {
                    // Log error?
                    Log.d("mTurnData", "Turn Data is null, possibly an error.");
                } else {
                    MultiPlayerFragmentActivityFragment fragment = (MultiPlayerFragmentActivityFragment) getFragmentManager().findFragmentByTag("gameplayfragment");
                    if(m_gameState == null) { // Reconstruct the state
                        if(mTurnData.getTurnState() != null) {
                            m_gameState = new State(mTurnData.getLastPlayer(), mTurnData.getTurnState(), mTurnData.getLastRow(), mTurnData.getLastCol());
                            fragment.setupBoard(m_gameState.toString());
                        } else {
                            m_gameState = new State();
                            fragment.setupBoard(m_gameState.toString());
                        }
                    } else { // Update the gamestate
                        if(mTurnData.getTurnState() != null) {
                            m_gameState.DoMove(mTurnData.getLastCol());
                            updateDisplay();
                        } else {
                            fragment.setupBoard(m_gameState.toString());
                        }
                    }
                    setGameplayUI();
                    return;
                }

                return;

            case TurnBasedMatch.MATCH_TURN_STATUS_THEIR_TURN:
                // Should return results.
                mTurnData = ConnectFourState.unpersist(m_match.getData());
                if(mTurnData == null) {
                    // Log error?
                    Log.d("mTurnData", "Turn Data is null, possibly an error.");
                } else {
                    if(m_gameState == null) { // Reconstruct the state
                        m_gameState = new State(mTurnData.getLastPlayer(), mTurnData.getTurnState(), mTurnData.getLastRow(), mTurnData.getLastCol());
                    }
                }
                MultiPlayerFragmentActivityFragment fragment = (MultiPlayerFragmentActivityFragment) getFragmentManager().findFragmentByTag("gameplayfragment");
                fragment.setupBoard(m_gameState.toString());
                mUserColor = (m_gameState.getLastPlayerToken() == 'R') ? getResources().getColor(R.color.player1) : getResources().getColor(R.color.player2);
                setViewVisibility();
                break;


            case TurnBasedMatch.MATCH_TURN_STATUS_INVITED:
                showWarning("Good inititative!",
                        "Still waiting for invitations.\n\nBe patient!");
        }

        mTurnData = null;

        setViewVisibility();
    }

    /**
     * Someone initated a match, start the match or update it, as applicable.
     * @param result
     */
    private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        dismissSpinner();

        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            Toast.makeText(this, "" + result.getStatus().getStatusCode(), Toast.LENGTH_LONG).show();
            return;
        }

        if (match.getData() != null) {
            // This is a game that has already started, so I'll just start
            updateMatch(match);
            return;
        }

        startMatch(match);
    }

    /**
     * Update the match.
     * @param result
     */
    public void processResult(TurnBasedMultiplayer.UpdateMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        dismissSpinner();

        if(mStartedMatch) {
            Games.Achievements.unlock(m_googleApiClient, getResources().getString(R.string.JOURNEY_STARTED));
        }

        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);

        if (isDoingTurn) {
            updateMatch(match);
            return;
        }

        setViewVisibility();
    }

    private void processResult(TurnBasedMultiplayer.CancelMatchResult result) {
        dismissSpinner();

        if (!checkStatusCode(null, result.getStatus().getStatusCode())) {
            return;
        }

        isDoingTurn = false;

        showWarning("Match", "This match is canceled.  All other players will have their game ended.");
    }

    private void processResult(TurnBasedMultiplayer.LeaveMatchResult result) {
        TurnBasedMatch match = result.getMatch();
        dismissSpinner();
        if (!checkStatusCode(match, result.getStatus().getStatusCode())) {
            return;
            }
        isDoingTurn = (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN);
        showWarning("Left", "You've left this match.");
        isIngame = false;
    }

    public void showErrorMessage(TurnBasedMatch match, int statusCode,
                                 int stringId) {

        showWarning("Warning", getResources().getString(stringId));
    }

    private boolean checkStatusCode(TurnBasedMatch match, int statusCode) {
        switch (statusCode) {
            case GamesStatusCodes.STATUS_OK:
                return true;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
                // This is OK; the action is stored by Google Play Services and will
                // be dealt with later.
                return true;
            case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                showErrorMessage(match, statusCode,
                        R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_already_rematched);
                break;
            case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
                showErrorMessage(match, statusCode,
                        R.string.network_error_operation_failed);
                break;
            case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
                showErrorMessage(match, statusCode,
                        R.string.client_reconnect_required);
                break;
            case GamesStatusCodes.STATUS_INTERNAL_ERROR:
                showErrorMessage(match, statusCode, R.string.internal_error);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
                showErrorMessage(match, statusCode,
                        R.string.match_error_inactive_match);
                break;
            case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
                showErrorMessage(match, statusCode,
                        R.string.match_error_locally_modified);
                break;
            default:
                showErrorMessage(match, statusCode, R.string.unexpected_status);
                Log.d(TAG, "Did not have warning or string to deal with: "
                        + statusCode);
        }

        return false;
    }

    /**
     * Helper function to download the image for the user.
     * Uses an async task to download the display pictuer, if it can't download it
     * it assigns a default picture as the display pic.
     * @param imageView
     * @param imageUrl
     */
    private void loadWebImage(final CircularImageView imageView, final String imageUrl) {
        Ion.with(this).load(imageUrl).asBitmap().setCallback(new FutureCallback<Bitmap>() {
            @Override
            public void onCompleted(Exception e, Bitmap result) {
                if (e == null) {
                    // Success
                    imageView.setImageBitmap(result);
                } else {
                    // Error
                    imageView.setImageResource(R.drawable.anon);
                }
            }
        });
    }

    // Generic warning/info dialog
    public void showWarning(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(title).setMessage(message);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                    }
                });

        // create alert dialog
        m_alertDialog = alertDialogBuilder.create();

        // show it
        m_alertDialog.show();
    }

    /**
     * Catch the back button and send the player to the main menu.
     */
    @Override
    public void onBackPressed() {
        if(isIngame) {
            isIngame = false;
            setViewVisibility();
        } else {
            Intent intent = new Intent(this, MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
