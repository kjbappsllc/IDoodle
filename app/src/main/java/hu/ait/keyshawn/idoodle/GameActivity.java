package hu.ait.keyshawn.idoodle;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hu.ait.keyshawn.idoodle.View.DrawingView;
import hu.ait.keyshawn.idoodle.adapter.GameUsersAdapter;
import hu.ait.keyshawn.idoodle.constants.constants;
import hu.ait.keyshawn.idoodle.data.Gamestate;
import hu.ait.keyshawn.idoodle.data.User;

public class GameActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawingView dvMain;
    public ImageView ivProjectedCanvas;
    public FloatingActionButton fab;
    public DatabaseReference mDatabase;
    public String currentDrawerID;
    public Button btnStart;
    public TextView tvWaiting;
    public EditText etGuess;
    public RecyclerView rvUsers;
    public GameUsersAdapter gmUsersAdapter;
    Chronometer chronTimer;
    public String hostUserID;
    public HashMap<String , String> gameUsers = new HashMap<>();
    public List<String> gameUserIDS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if(getIntent().hasExtra(LobbyActivity.GAME_NAME)){
            setTitle(getIntent().getStringExtra(LobbyActivity.GAME_NAME));
        }

        initDB();

        initGameDrawingEventLister();

        initGameUserListEventListener();

        initGameHostIDEventListener();

        initCurrentDrawerEventListener();

        initGameStateEventListener();

        initUI();

    }

    private void initDB() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public User getCurrentUser() {
        return ((MainApplication)getApplication()).getCurrentUser();
    }

    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUpDrawer(toolbar);

        dvMain = (DrawingView) findViewById(R.id.dvMainCanvas);
        ivProjectedCanvas = (ImageView) findViewById(R.id.ivProjectedCanvas);
        chronTimer = (Chronometer) findViewById(R.id.chronTimer);
        etGuess = (EditText) findViewById(R.id.etGuess);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dvMain.clearDrawing();
            }
        });

        btnStart = (Button) findViewById(R.id.btnStart);
        tvWaiting = (TextView) findViewById(R.id.tvWaiting);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });
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
        navigationView.setNavigationItemSelectedListener(this);

        rvUsers = (RecyclerView) drawer.findViewById(R.id.rvUsers);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvUsers.setHasFixedSize(true);
        rvUsers.setLayoutManager(layoutManager);
        gmUsersAdapter = new GameUsersAdapter(this);
        rvUsers.setAdapter(gmUsersAdapter);

        TextView tvheaderText = (TextView) drawer.findViewById(R.id.headerView);
        tvheaderText.setText(getCurrentUser().getUsername());
    }

    public void initGameHostIDEventListener() {
        mDatabase.child(constants.db_Games).child(getCurrentUser().getCurrentGameID()).child(constants.db_Games_hostID).addValueEventListener(new ValueEventListener() {
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
                                        ivProjectedCanvas.setImageBitmap(arg0);
                                    }
                                });
                    }
                    catch (Exception e){
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initGameUserListEventListener() {
        mDatabase.child(constants.db_Games).
                child(getCurrentUser().getCurrentGameID()).
                child(constants.db_Games_Userlist).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String ID = dataSnapshot.getKey();
                String userInfo = dataSnapshot.getValue(String.class);

                gameUsers.put(ID, userInfo);
                gameUserIDS.add(ID);
                gmUsersAdapter.addUser(ID, userInfo);
                rvUsers.scrollToPosition(0);
                checkUsers();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String uID = dataSnapshot.getKey();
                String userInfo = dataSnapshot.getValue(String.class);

                int index = gameUserIDS.indexOf(uID);
                gameUserIDS.remove(index);
                gameUsers.remove(index);
                gmUsersAdapter.removeUser(uID, userInfo);
                checkUsers();
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

    public void initGameStateEventListener() {
        mDatabase.child(constants.db_Games)
                .child(getCurrentUser().getCurrentGameID())
                .child(constants.db_Games_gameState).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String gameState = dataSnapshot.getValue(String.class);

                if(!TextUtils.isEmpty(gameState)) {
                    Gamestate gs = Gamestate.valueOf(gameState);

                    switch (gs) {
                        case preGamePhase:
                            if(hostUserID.equals(getCurrentUser().getUid())){
                                btnStart.setVisibility(View.VISIBLE);
                                tvWaiting.setVisibility(View.GONE);
                            } else {
                                btnStart.setVisibility(View.GONE);
                                tvWaiting.setVisibility(View.VISIBLE);
                            }

                            Log.d("gs", "PREGAME");
                            break;

                        case drawingPhase:
                            btnStart.setVisibility(View.GONE);
                            tvWaiting.setVisibility(View.GONE);
                            chronTimer.setVisibility(View.VISIBLE);

                            if(currentDrawerID.equals(getCurrentUser().getUid())){
                                ivProjectedCanvas.setVisibility(View.GONE);
                                dvMain.setVisibility(View.VISIBLE);
                                fab.setVisibility(View.VISIBLE);
                                etGuess.setVisibility(View.INVISIBLE);
                                etGuess.setHint("");
                            } else {
                                ivProjectedCanvas.setVisibility(View.VISIBLE);
                                dvMain.setVisibility(View.GONE);
                                fab.setVisibility(View.GONE);
                                etGuess.setVisibility(View.VISIBLE);
                                etGuess.setHint("Guess");
                            }

                            Log.d("gs", "DRAWING");
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkUsers() {

        if(gameUserIDS.isEmpty()){
            mDatabase.child(constants.db_Games).
                    child(getCurrentUser().getCurrentGameID()).removeValue();
            Log.d("games", "USERLIST IS EMPTY");

        } else {
            for(String key: gameUsers.keySet()){
                Log.d("gameUsers", (key + " - " + gameUsers.get(key)));
            }
        }

    }

    private void leaveGame() {
        mDatabase.child(constants.db_Games).
                child(getCurrentUser().getCurrentGameID()).
                child(constants.db_Games_Userlist).
                child(getCurrentUser().getUid()).removeValue();

        mDatabase.child(constants.db_Users).
                child(getCurrentUser().getUid()).
                child("currentGameID").setValue("");
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
        super.onDestroy();
    }

    private void startGame() {
        String newGs = Gamestate.GameStateToString(Gamestate.drawingPhase);

        if(gameUsers.size() >= 2){
            getNewDrawer(newGs);
            btnStart.setVisibility(View.GONE);
            tvWaiting.setVisibility(View.GONE);
            chronTimer.setVisibility(View.VISIBLE);
        }
        else {
            Toast.makeText(this, "Need at least 2 Players to Start", Toast.LENGTH_SHORT).show();
        }
    }

    private void getNewDrawer(final String newGs) {
        mDatabase.child(constants.db_Games).child(getCurrentUser().getCurrentGameID()).child(constants.db_Games_roundNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int roundNumber = dataSnapshot.getValue(int.class);
                int index = roundNumber % gameUsers.size()-1;

                String nextUserID = gameUserIDS.get(index);

                mDatabase.child(constants.db_Games).child(getCurrentUser().getCurrentGameID()).child(constants.db_Games_currentDrawer).setValue(nextUserID);
                mDatabase.child(constants.db_Games).child(getCurrentUser().getCurrentGameID()).child(constants.db_Games_gameState).setValue(newGs);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
