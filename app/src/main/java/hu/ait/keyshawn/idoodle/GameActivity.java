package hu.ait.keyshawn.idoodle;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.Chronometer;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import hu.ait.keyshawn.idoodle.View.DrawingView;
import hu.ait.keyshawn.idoodle.adapter.GameUsersAdapter;
import hu.ait.keyshawn.idoodle.adapter.MessagesAdapter;
import hu.ait.keyshawn.idoodle.constants.constants;
import hu.ait.keyshawn.idoodle.data.Game;
import hu.ait.keyshawn.idoodle.data.Gamestate;
import hu.ait.keyshawn.idoodle.data.Message;
import hu.ait.keyshawn.idoodle.data.User;

public class GameActivity extends AppCompatActivity {

    private DrawingView dvMain;
    public ImageView ivProjectedCanvas;
    public FloatingActionButton fab;
    public DatabaseReference mDatabase;
    public String currentDrawerID;
    public String currentGameID;
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
    public String hostUserID;
    public String gameState;
    int roundNumber = 1;
    String currentWord = "";
    List<Integer> playedWords = new ArrayList<>();
    public TextView tvHeaderViewRound;
    public HashMap<String , String> gameUsers = new HashMap<>();
    public List<String> gameUserIDS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if(getIntent().hasExtra(LobbyActivity.GAME_NAME)){
            setTitle(getIntent().getStringExtra(LobbyActivity.GAME_NAME));
            currentGameID = getCurrentUser().getCurrentGameID();
        }

        initDB();

        initGameDrawingEventLister();

        initGameUserListEventListener();

        initGameHostIDEventListener();

        initCurrentDrawerEventListener();

        initGameStateEventListener();

        initRoundNumberEventListener();

        initMessageEventListener();

        initCurrentWordListener();

        initUI();

    }

    private void initDB() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }
    public User getCurrentUser() {
        return ((MainApplication)getApplication()).getCurrentUser();
    }
    public DatabaseReference getCurrentGameReference() {return mDatabase.child(constants.db_Games).
            child(getCurrentUser().getCurrentGameID());}
    public DatabaseReference getCurrentCurrentUserReference() {return mDatabase.child(constants.db_Users).
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

            if(!currentWord.isEmpty()){
                String[] available = splitCurrentWord();

                for(String word : available){
                    if(etGuess.getText().toString().equals(word)){
                        sendSystemMessage("YOU GOT IT! The word was: " + available[0]);
                        addPlayerPoints();
                        shouldSendSystem = true;
                    }
                }

            }

            if(!shouldSendSystem) {
                Message newMessage = new Message(sender, message);

                String msgKey = getCurrentGameReference().
                        child(constants.db_Games_messages).push().getKey();

                getCurrentGameReference().
                        child(constants.db_Games_messages).
                        child(msgKey).setValue(newMessage);
            }
        }
        else {
            etGuess.setError("Please Enter Text");
        }
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
                child(currentDrawerID).runTransaction(new Transaction.Handler() {
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

    private void sendSystemMessage(String message) {
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
        tvHeaderViewRound.setText(getString(R.string.round, roundNumber));
    }

    public void initGameHostIDEventListener() {
        getCurrentGameReference().
                child(constants.db_Games_hostID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hostUserID = dataSnapshot.getValue(String.class);
                gmUsersAdapter.setCurrentHostID(hostUserID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initGameDrawingEventLister() {
        mDatabase.child(constants.db_Games).child(getCurrentUser().getCurrentGameID()).child(constants.db_Games_DrawingURL).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currentGameURL = dataSnapshot.getValue(String.class);

                if(!TextUtils.isEmpty(currentGameURL)){
                    try {
                        Glide
                                .with(GameActivity.this)
                                .load(currentGameURL)
                                .asBitmap()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .dontAnimate()
                                .into(new SimpleTarget<Bitmap>() {

                                    @Override
                                    public void onResourceReady(Bitmap arg0, GlideAnimation<? super Bitmap> arg1) {
                                        if(gameState.equals(Gamestate.GameStateToString(Gamestate.drawingPhase))) {
                                            ivProjectedCanvas.setImageBitmap(arg0);
                                        }
                                    }
                                });
                    }
                    catch (Exception e){}
                }
                else{
                    ivProjectedCanvas.setImageBitmap(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initGameUserListEventListener() {
        getCurrentGameReference().
                child(constants.db_Games_Userlist).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String ID = dataSnapshot.getKey();
                String userInfo = dataSnapshot.getValue(String.class);

                gameUsers.put(ID, userInfo);
                gameUserIDS.add(ID);
                gmUsersAdapter.addUser(ID, userInfo);
                rvUsers.scrollToPosition(0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String uID = dataSnapshot.getKey();
                String userInfo = dataSnapshot.getValue(String.class);
                gmUsersAdapter.updateUser(uID,userInfo);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String uID = dataSnapshot.getKey();
                String userInfo = dataSnapshot.getValue(String.class);

                int index = gameUserIDS.indexOf(uID);
                gameUserIDS.remove(index);
                gameUsers.remove(index);

                if(uID.equals(hostUserID)) {
                    Toast.makeText(GameActivity.this, "HOST HAS LEFT THE GAME", Toast.LENGTH_LONG).show();
                    leaveGame();
                    finish();
                }
                gmUsersAdapter.removeUser(uID, userInfo);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initCurrentDrawerEventListener() {
        mDatabase.child(constants.db_Games).child(getCurrentUser().getCurrentGameID()).child(constants.db_Games_currentDrawer).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentDrawerID = dataSnapshot.getValue(String.class);
                gmUsersAdapter.setCurrentDrawerID(currentDrawerID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initMessageEventListener() {
        getCurrentGameReference().child(constants.db_Games_messages).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message newMessage = dataSnapshot.getValue(Message.class);
                gmMessagesAdaper.addMessage(newMessage);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initCurrentWordListener() {
        getCurrentGameReference().
                child(constants.db_Games_currentWord).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    currentWord = dataSnapshot.getValue(String.class);
                    String [] available = splitCurrentWord();

                    if (getCurrentUser().getUid().equals(currentDrawerID)) {
                        tvWordDraw.setText(available[0]);
                        tvWordDraw.setVisibility(View.VISIBLE);
                        sendSystemMessage("Its your turn. Your word is: " + available[0]);
                    } else {
                        tvWordDraw.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initGameStateEventListener() {
        getCurrentGameReference()
                .child(constants.db_Games_gameState).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gameState = dataSnapshot.getValue(String.class);

                if(!TextUtils.isEmpty(gameState)) {
                    Gamestate gs = Gamestate.valueOf(gameState);

                    switch (gs) {
                        case preGamePhase:
                            doPreGamePhase();
                            break;

                        case drawingPhase:
                            doDrawingPhase();
                            break;

                        case endRoundPhase:
                            doEndRoundPhase();
                            break;

                        case endGamePhase:
                            doEndGamePhase();
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void doEndGamePhase() {
        stopIntermissionTimer();
        stopDrawingTimer();
        clearUI();
        tvTimer.setVisibility(View.GONE);

        updateGamesPlayedAndResetGame();
    }

    private void updateGamesPlayedAndResetGame() {
        getCurrentCurrentUserReference().
                child(constants.db_Users_gamesPlayed).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                    User currentUser = getCurrentUser();
                    currentUser.setGamesPlayed(currentValue + 1);
                    ((MainApplication)getApplication()).addGamePoints();
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if(hostUserID.equals(getCurrentUser().getUid())){
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

    private void doEndRoundPhase() {
        setUpNewRound();
        Log.d("gamestate", "ENDROUND");
    }

    private void doPreGamePhase() {
        if(hostUserID.equals(getCurrentUser().getUid())){
            btnStart.setVisibility(View.VISIBLE);
            tvWaiting.setVisibility(View.GONE);
        } else {
            btnStart.setVisibility(View.GONE);
            tvWaiting.setVisibility(View.VISIBLE);
        }

        Log.d("gamestate", "PREGAME");
    }

    private void doDrawingPhase() {
        btnStart.setVisibility(View.GONE);
        tvWaiting.setVisibility(View.GONE);
        tvTimer.setVisibility(View.VISIBLE);

        if(hostUserID.equals(getCurrentUser().getUid())) {
            getNewWord();
        }

        setUpCavnasUI();

        startDrawingTimer();

        Log.d("gamestate", "DRAWING");
    }

    private void setUpCavnasUI() {
        if(currentDrawerID.equals(getCurrentUser().getUid())){
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
        drawingTimer = new CountDownTimer(10000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                if((millisUntilFinished / 1000) < 11){
                    tvTimer.setTextColor(Color.RED);
                }
                tvTimer.setTextColor(Color.WHITE);
                tvTimer.setText(getString(R.string.countdown, millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if(getCurrentUser().getUid().equals(hostUserID)) {
                    String newGs = Gamestate.GameStateToString(Gamestate.endRoundPhase);
                    getCurrentGameReference().
                            child(constants.db_Games_gameState).setValue(newGs);
                }

            }
        }.start();
    }

    private void stopDrawingTimer() {
        drawingTimer.cancel();
    }

    private void stopIntermissionTimer(){
        intermissionTimer.cancel();
    }

    public void setUpNewRound() {
        if(hostUserID.equals(getCurrentUser().getUid())) {
            getCurrentGameReference().
                    child(constants.db_Games_roundNumber).setValue(roundNumber + 1);
        }

        clearUI();

        startIntermissionTimer();
    }

    private void startIntermissionTimer() {
        intermissionTimer = new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setTextColor(Color.WHITE);
                tvTimer.setText(getString(R.string.countdownIntermission, millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if(hostUserID.equals(getCurrentUser().getUid())) {
                    startRound();
                }
            }
        }.start();
    }

    private void clearUI() {
        if(currentDrawerID.equals(getCurrentUser().getUid())) {
            dvMain.clearDrawing();
        }

        clearMessages();
        dvMain.setVisibility(View.GONE);
        ivProjectedCanvas.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);
        tvWordDraw.setVisibility(View.INVISIBLE);

        getCurrentGameReference().
                child(constants.db_Games_DrawingURL).setValue("");
        ivProjectedCanvas.setImageBitmap(null);
        etGuess.setVisibility(View.VISIBLE);
        etGuess.setText("");
    }

    private void clearMessages() {
        getCurrentGameReference().child(constants.db_Games_messages).removeValue();
        gmMessagesAdaper.clear();
    }

    public void initRoundNumberEventListener() {
        if(!getCurrentUser().getCurrentGameID().isEmpty()) {
            getCurrentGameReference().
                    child(constants.db_Games_roundNumber).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        roundNumber = dataSnapshot.getValue(int.class);
                        if(roundNumber >= 6) {
                            if(hostUserID.equals(getCurrentUser().getUid())) {
                                getCurrentGameReference().
                                        child(constants.db_Games_roundNumber).
                                        setValue(1);

                                String newGs = Gamestate.GameStateToString(Gamestate.endGamePhase);
                                getCurrentGameReference().
                                        child(constants.db_Games_gameState).
                                        setValue(newGs);
                            }
                        }
                        if(roundNumber < 6) {
                            tvHeaderViewRound.setText(getString(R.string.round, roundNumber));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void leaveGame() {
        if(drawingTimer != null) {
            stopDrawingTimer();
        }

        if(intermissionTimer != null) {
            stopIntermissionTimer();
        }

        getCurrentGameReference().
                child(constants.db_Games_Userlist).
                child(getCurrentUser().getUid()).removeValue();

        mDatabase.child(constants.db_Users).
                child(getCurrentUser().getUid()).
                child("currentGameID").setValue("");

        User currentUser = getCurrentUser();
        currentUser.setCurrentGameID("");
        ((MainApplication)getApplication()).setCurrentUser(currentUser);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            leaveGame();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        leaveGame();
        finish();
        super.onDestroy();
    }

    private void startRound() {
        String newGs = Gamestate.GameStateToString(Gamestate.drawingPhase);

        if(gameUserIDS.size() >= 2){
            getNewDrawer(newGs);
            btnStart.setVisibility(View.GONE);
            tvWaiting.setVisibility(View.GONE);
            tvTimer.setVisibility(View.VISIBLE);
        }
        else {
            Toast.makeText(this, "Need at least 2 Players to Start Round", Toast.LENGTH_SHORT).show();
        }
    }

    private void getNewDrawer(final String newGs) {
       getCurrentGameReference().child(constants.db_Games_roundNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int roundNumber = dataSnapshot.getValue(int.class);
                int index = (roundNumber-1) % gameUserIDS.size();
                Log.d("rounds", "gameUserSize: " + gameUserIDS.size());
                Log.d("rounds", "roundNumber: " + roundNumber);
                Log.d("rounds", "index: " + index);
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
        int num = randomGen.nextInt(15)+1;
        while (playedWords.contains(num)){
            num = randomGen.nextInt(15)+1;
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

    private String[] splitCurrentWord() {
        return currentWord.split(",");
    }

    public void scrollMessageRecycler(int position){
        rvMessages.scrollToPosition(position);
    }
}
