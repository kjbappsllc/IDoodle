package hu.ait.keyshawn.idoodle;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import hu.ait.keyshawn.idoodle.data.Game;
import hu.ait.keyshawn.idoodle.firebase.FirebaseGameHandler;
import hu.ait.keyshawn.idoodle.view.DrawingView;
import hu.ait.keyshawn.idoodle.adapter.GameUsersAdapter;
import hu.ait.keyshawn.idoodle.adapter.MessagesAdapter;
import hu.ait.keyshawn.idoodle.constants.constants;
import hu.ait.keyshawn.idoodle.data.Gamestate;
import hu.ait.keyshawn.idoodle.data.Message;
import hu.ait.keyshawn.idoodle.data.User;

public class GameActivity extends AppCompatActivity {

    private DrawingView dvMain;
    public ImageView ivProjectedCanvas;
    public FloatingActionButton fab;
    public DatabaseReference mDatabase;
    public Button btnStart;
    public TextView tvWaiting;
    public EditText etGuess;
    public RecyclerView rvUsers;
    public RecyclerView rvMessages;
    public MessagesAdapter gmMessagesAdaper;
    public GameUsersAdapter gmUsersAdapter;
    public TextView tvTimer;
    public CountDownTimer drawingTimer;
    public CountDownTimer intermissionTimer;
    public TextView tvWordDraw;
    List<Integer> playedWords = new ArrayList<>();
    public TextView tvHeaderViewRound;
    public List<String> gameUserIDS = new ArrayList<>();
    public Game currentGame;
    FirebaseGameHandler firebaseGameHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if(getIntent().hasExtra(LobbyActivity.GAME_NAME)){
            String gmName = getIntent().getStringExtra(LobbyActivity.GAME_NAME);
            setTitle(gmName);
            currentGame = new Game(getCurrentUser().getCurrentGameID(), gmName);
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();

        firebaseGameHandler = new FirebaseGameHandler(this);

        initUI();
        firebaseGameHandler.initGameHostIDEventListener(currentGame,gmUsersAdapter);
        firebaseGameHandler.initGameUserListEventListener(currentGame,gmUsersAdapter,gameUserIDS);
        firebaseGameHandler.initCurrentDrawerEventListener(currentGame,gmUsersAdapter);
        firebaseGameHandler.initGameDrawingEventListener(currentGame,ivProjectedCanvas);
        firebaseGameHandler.initGameStateEventListener(currentGame);
        firebaseGameHandler.initRoundNumberEventListener(currentGame);
        firebaseGameHandler.initMessageEventListener(gmMessagesAdaper);
        firebaseGameHandler.initCurrentWordListener(currentGame);
    }


    public User getCurrentUser() {
        return ((MainApplication)getApplication()).getCurrentUser();
    }
    public DatabaseReference getCurrentGameReference() {return mDatabase.child(constants.db_Games).
            child(getCurrentUser().getCurrentGameID());}
    public DatabaseReference getCurrentUserReference() {return mDatabase.child(constants.db_Users).
            child(getCurrentUser().getUid());}


    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpDrawer(toolbar);
        findUIElements();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dvMain.clearDrawing();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRound();
            }
        });
    }

    private void findUIElements() {
        dvMain = (DrawingView) findViewById(R.id.dvMainCanvas);
        ivProjectedCanvas = (ImageView) findViewById(R.id.ivProjectedCanvas);
        etGuess = (EditText) findViewById(R.id.etGuess);
        etGuess.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(!TextUtils.isEmpty(etGuess.getText().toString())) {
                        String sender = getCurrentUser().getUsername();
                        String message = etGuess.getText().toString();
                        sendMessage(sender,message);
                        etGuess.setText("");
                        handled = true;
                    }
                }
                return handled;
            }
        });
        tvWordDraw = (TextView) findViewById(R.id.tvWordDraw);
        tvTimer = (TextView) findViewById(R.id.tvTimer);
        btnStart = (Button) findViewById(R.id.btnStart);
        tvWaiting = (TextView) findViewById(R.id.tvWaiting);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        rvMessages = (RecyclerView) findViewById(R.id.rvMessages);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvMessages.setHasFixedSize(true);
        rvMessages.setLayoutManager(layoutManager);
        gmMessagesAdaper = new MessagesAdapter(this);
        rvMessages.setAdapter(gmMessagesAdaper);

    }

    private void sendMessage(String sender, String message) {
        boolean shouldSendSystem = false;
        if(!TextUtils.isEmpty(etGuess.getText().toString())){
            if(!currentGame.getCurrentWord().isEmpty()){
                String[] available = splitCurrentWord();
                for(String word : available){
                    if(etGuess.getText().toString().toLowerCase().equals(word)){
                        sendSystemMessage("YOU GOT IT! The word was: " + available[0]);
                        addPlayerPoints();
                        shouldSendSystem = true;
                    }
                }
            }
            if(!shouldSendSystem) {
                sendGlobalMessage(sender, message);
            }
        }
        else {
            etGuess.setError("Please Enter Text");
        }
    }

    private void sendGlobalMessage(String sender, String message) {
        Message newMessage = new Message(sender, message);
        String msgKey = getCurrentGameReference().
                child(constants.db_Games_messages).push().getKey();
        getCurrentGameReference().
                child(constants.db_Games_messages).
                child(msgKey).setValue(newMessage);
    }

    private void addPlayerPoints() {
        getCurrentGameReference().
                child(constants.db_Games_Userlist).
                child(getCurrentUser().getUid()).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                String data = mutableData.getValue(String.class);
                Log.d("points", "Player: " + data);
                String[] parsedData = data.split(",");
                Integer points = Integer.valueOf(parsedData[1]);
                points += 1;

                mutableData.setValue(getString(R.string.userInfo,getCurrentUser().getUsername(),points));

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                addDrawerPoints();
            }
        });
    }

    private void addDrawerPoints(){
        getCurrentGameReference().
                child(constants.db_Games_Userlist).
                child(currentGame.getCurrentDrawer()).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                String data = mutableData.getValue(String.class);
                Log.d("points", "Drawer: " + data);
                String[] parsedData = data.split(",");
                Integer points = Integer.valueOf(parsedData[1]);
                points += 1;


                mutableData.setValue(getString(R.string.userInfo,parsedData[0],points));

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
            }
        });
    }

    public void sendSystemMessage(String message) {
        Message newMessage = new Message("SYSTEM", message);
        newMessage.isSystem = true;
        gmMessagesAdaper.addMessage(newMessage);
    }

    private void setUpDrawer(Toolbar toolbar) {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawerToggle.setDrawerIndicatorEnabled(false);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_users, this.getTheme());
        mDrawerToggle.setHomeAsUpIndicator(drawable);
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerVisible(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });

        drawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        setUpUIInsideDrawer(drawer);
    }

    private void setUpUIInsideDrawer(DrawerLayout drawer) {
        rvUsers = (RecyclerView) drawer.findViewById(R.id.rvUsers);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvUsers.setHasFixedSize(true);
        rvUsers.setLayoutManager(layoutManager);
        gmUsersAdapter = new GameUsersAdapter(this);
        rvUsers.setAdapter(gmUsersAdapter);

        TextView tvheaderText = (TextView) drawer.findViewById(R.id.headerView);
        tvheaderText.setText(getCurrentUser().getUsername());

        tvHeaderViewRound = (TextView) drawer.findViewById(R.id.tvHeaderViewRound);
        setHeaderText();
    }

    public void setCurrentWordUI(String[] available) {
        if (getCurrentUser().getUid().equals(currentGame.getCurrentDrawer())) {
            tvWordDraw.setText(available[0]);
            tvWordDraw.setVisibility(View.VISIBLE);
            sendSystemMessage("Its your turn. Your word is: " + available[0]);
        } else {
            tvWordDraw.setVisibility(View.INVISIBLE);
        }
    }

    public void doEndGamePhase() {
        stopIntermissionTimer();
        intermissionTimer = null;
        stopDrawingTimer();
        drawingTimer = null;
        clearUI();
        tvTimer.setVisibility(View.GONE);
        updateAndResetPoints();
    }

    private void updateAndResetPoints() {
        getCurrentGameReference().
                child(constants.db_Games_Userlist).
                child(getCurrentUser().getUid()).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                String data = mutableData.getValue(String.class);
                String[] parsedData = data.split(",");

                if(currentGame.getRoundNumber() >= 5) {
                    updatePointsInDB(Integer.valueOf(parsedData[1]));

                    ((MainApplication) getApplication()).
                            addTotalPoints(Integer.valueOf(parsedData[1]));
                }

                mutableData.setValue(getString(R.string.userInfo,getCurrentUser().getUsername(),0));
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                updateGamesPlayedAndResetGame();
            }
        });
    }

    private void updatePointsInDB(final int points) {
        getCurrentUserReference().
                child(constants.db_Users_totalPoints).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer current = mutableData.getValue(Integer.class);
                if(current == null){
                    mutableData.setValue(1);
                }
                else {
                    mutableData.setValue(current + points);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    private void updateGamesPlayedAndResetGame() {
        getCurrentUserReference().
                child(constants.db_Users_gamesPlayed).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);

                if(currentGame.getRoundNumber() >= 5) {
                    if (currentValue == null) {
                        mutableData.setValue(1);
                    } else {
                        mutableData.setValue(currentValue + 1);
                        ((MainApplication) getApplication()).addGamePoints();
                    }
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if(currentGame.getHostUserID().equals(getCurrentUser().getUid())){
                    String newGS = Gamestate.GameStateToString(Gamestate.preGamePhase);

                    getCurrentGameReference().
                            child(constants.db_Games_gameState).
                            setValue(newGS);

                    getCurrentGameReference().
                            child(constants.db_Games_currentDrawer).
                            setValue("");
                }
            }
        });
    }

    public void doEndRoundPhase() {
        setUpNewRound();
        Log.d("gamestate", "ENDROUND");
    }

    public void doPreGamePhase() {
        Log.d("GAMEDEBUG", currentGame.getHostUserID() + " nothin");
        if(currentGame.getHostUserID().equals(getCurrentUser().getUid())){
            btnStart.setVisibility(View.VISIBLE);
            tvWaiting.setVisibility(View.GONE);
        } else {
            btnStart.setVisibility(View.GONE);
            tvWaiting.setVisibility(View.VISIBLE);
        }

        Log.d("gamestate", "PREGAME");
    }

    public void doDrawingPhase() {
        btnStart.setVisibility(View.GONE);
        tvWaiting.setVisibility(View.GONE);
        tvTimer.setVisibility(View.VISIBLE);

        if(currentGame.getHostUserID().equals(getCurrentUser().getUid())) {
            getNewWord();
        }

        setUpCavnasUI();

        startDrawingTimer();

        Log.d("gamestate", "DRAWING");
    }

    private void setUpCavnasUI() {
        if(currentGame.getCurrentDrawer().equals(getCurrentUser().getUid())){
            ivProjectedCanvas.setVisibility(View.GONE);
            dvMain.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
            etGuess.setVisibility(View.INVISIBLE);
            etGuess.setHint("");
        } else {
            tvWordDraw.setVisibility(View.INVISIBLE);
            ivProjectedCanvas.setVisibility(View.VISIBLE);
            dvMain.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
            etGuess.setVisibility(View.VISIBLE);
            etGuess.setHint("Guess");
        }
    }

    private void startDrawingTimer() {
        Log.d("DEBUGGAME", "startedTimerDrawTimer");
        if(drawingTimer == null) {
            drawingTimer = new CountDownTimer(40000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    if ((millisUntilFinished / 1000) < 11) {
                        tvTimer.setTextColor(Color.RED);
                    } else {
                        tvTimer.setTextColor(Color.WHITE);
                    }

                    tvTimer.setText(getString(R.string.countdown, millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    if (getCurrentUser().getUid().equals(currentGame.getHostUserID())) {
                        String[] available = splitCurrentWord();
                        sendMessage("SYSTEM", "The correct word was: " + available[0]);
                        String newGs = Gamestate.GameStateToString(Gamestate.endRoundPhase);
                        getCurrentGameReference().
                                child(constants.db_Games_gameState).setValue(newGs);
                    }

                }
            }.start();
        }
    }

    private void stopDrawingTimer() {
        if(drawingTimer != null) {
            drawingTimer.cancel();
        }
    }

    private void stopIntermissionTimer(){
        if(intermissionTimer != null) {
            intermissionTimer.cancel();
        }
    }

    public void setUpNewRound() {
        drawingTimer = null;
        intermissionTimer = null;
        if(currentGame.getHostUserID().equals(getCurrentUser().getUid())) {
            getCurrentGameReference().
                    child(constants.db_Games_roundNumber).setValue(currentGame.getRoundNumber() + 1);
        }

        clearUI();

        startIntermissionTimer();
    }

    private void startIntermissionTimer() {
        Log.d("DEBUGGAME", "startedTimerIntTimer");
        if(intermissionTimer == null) {
            intermissionTimer = new CountDownTimer(10000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tvTimer.setTextColor(Color.WHITE);
                    tvTimer.setText(getString(R.string.countdownIntermission, millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    if (currentGame.getHostUserID().equals(getCurrentUser().getUid())) {
                        startRound();
                    }
                }
            }.start();
        }
    }

    private void clearUI() {
        if(currentGame.getCurrentDrawer().equals(getCurrentUser().getUid())) {
            dvMain.clearDrawing();
            Log.d("drawing", "Attempting to clear");
        }

        Log.d("drawing", "Attempting to clear UI");

        clearMessages();
        dvMain.setVisibility(View.GONE);
        ivProjectedCanvas.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);
        tvWordDraw.setVisibility(View.INVISIBLE);

        ivProjectedCanvas.setImageBitmap(null);
        etGuess.setVisibility(View.VISIBLE);
        etGuess.setText("");
    }

    private void clearMessages() {
        getCurrentGameReference().child(constants.db_Games_messages).removeValue();
        gmMessagesAdaper.clear();
    }

    public void setHeaderText() {
        tvHeaderViewRound.setText(getString(R.string.round, currentGame.getRoundNumber()));
    }

    public void leaveGame() {
        if(getCurrentUser().getUid().equals(currentGame.getHostUserID())){
            if (drawingTimer != null) {
                stopDrawingTimer();
                drawingTimer = null;
            }

            if (intermissionTimer != null) {
                stopIntermissionTimer();
                intermissionTimer = null;
            }

            Log.d("greattest", "Is Host User");

            getCurrentGameReference().
                    child(constants.db_Games_Userlist).
                    child(getCurrentUser().getUid()).removeValue();

            mDatabase.child(constants.db_Users).
                    child(getCurrentUser().getUid()).
                    child("currentGameID").setValue("");

            User currentUser = getCurrentUser();
            currentUser.setCurrentGameID("");
            ((MainApplication) getApplication()).setCurrentUser(currentUser);

            firebaseGameHandler.deinit();

        } else {

            if (drawingTimer != null) {
                stopDrawingTimer();
                drawingTimer = null;
            }

            if (intermissionTimer != null) {
                stopIntermissionTimer();
                intermissionTimer = null;
            }

            getCurrentGameReference().
                    child(constants.db_Games_Userlist).
                    child(getCurrentUser().getUid()).removeValue();

            mDatabase.child(constants.db_Users).
                    child(getCurrentUser().getUid()).
                    child("currentGameID").setValue("");

            User currentUser = getCurrentUser();
            currentUser.setCurrentGameID("");
            ((MainApplication) getApplication()).setCurrentUser(currentUser);

            firebaseGameHandler.deinit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            leaveGame();
        }
    }

    private void startRound() {
        String newGs = Gamestate.GameStateToString(Gamestate.drawingPhase);
        intermissionTimer = null;
        if(gameUserIDS.size() >= 2){
            getNewDrawer(newGs);
            btnStart.setVisibility(View.GONE);
            tvWaiting.setVisibility(View.GONE);
            tvTimer.setVisibility(View.VISIBLE);
        }
        else {
            newGs = Gamestate.GameStateToString(Gamestate.endGamePhase);
            getCurrentGameReference().
                    child(constants.db_Games_gameState).setValue(newGs);
            Toast.makeText(this, "Need at least 2 Players to Start Round", Toast.LENGTH_SHORT).show();
        }
    }

    private void getNewDrawer(final String newGs) {
       getCurrentGameReference().child(constants.db_Games_roundNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int roundNumber = dataSnapshot.getValue(int.class);
                int index = (roundNumber-1) % gameUserIDS.size();
                String nextUserID = gameUserIDS.get(index);

                getCurrentGameReference().child(constants.db_Games_currentDrawer).setValue(nextUserID);
                getCurrentGameReference().child(constants.db_Games_gameState).setValue(newGs);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getNewWord() {

        Random randomGen = new Random();
        int num = randomGen.nextInt(25)+1;
        while (playedWords.contains(num)){
            num = randomGen.nextInt(25)+1;
        }

        playedWords.add(num);

        mDatabase.child(constants.db_Words).
                child(Integer.toString(num)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String newWord = dataSnapshot.getValue(String.class);
                getCurrentGameReference().
                        child(constants.db_Games_currentWord).setValue(newWord);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String[] splitCurrentWord() {
        return currentGame.getCurrentWord().split(",");
    }

    public void scrollMessageRecycler(int position){
        rvMessages.scrollToPosition(position);
    }

    public void scrollUsersRecycler(){rvUsers.scrollToPosition(0);}

    public void removeFromBackstack() {finish();}

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Game newGame) {
        currentGame = newGame;
    }
}
