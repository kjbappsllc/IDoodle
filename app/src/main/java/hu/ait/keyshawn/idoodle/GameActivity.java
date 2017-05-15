package hu.ait.keyshawn.idoodle;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.api.BatchResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import hu.ait.keyshawn.idoodle.View.DrawingView;
import hu.ait.keyshawn.idoodle.constants.constants;
import hu.ait.keyshawn.idoodle.data.game;
import hu.ait.keyshawn.idoodle.data.gamestate;
import hu.ait.keyshawn.idoodle.data.user;

public class GameActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawingView dvMain;
    public ImageView ivProjectedCanvas;
    public FloatingActionButton fab;
    public DatabaseReference mDatabase;
    public user currentUser;
    public String currentDrawerID;
    public Button btnStart;
    public TextView tvWaiting;
    Chronometer chronTimer;
    public String hostUserID;
    public TreeMap<String , Integer> gameUsers = new TreeMap<>();
    public List<String> gameUserIDS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initUI();

        initDB();

        initGameDrawingEventLister();

        initGameUserListEventListener();

        initGameHostIDEventListener();

        initGameStateEventListener();

        initCurrentDrawerEventListener();
    }

    private void initDB() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        String gameKey = mDatabase.child(constants.db_Games).push().getKey();

        game newGame = new game(gameKey, "Test Game" );
        setTitle(newGame.getGameName());
        mDatabase.child(constants.db_Games).child(gameKey).setValue(newGame);

        currentUser = getCurrentUser();

        if(currentUser != null) {
            currentUser.setCurrentGameID(newGame.getUid());
            mDatabase.child(constants.db_Users).child(currentUser.getUid()).setValue(currentUser);
            mDatabase.child(constants.db_Games).child(currentUser.getCurrentGameID()).child(constants.db_Games_hostID).setValue(currentUser.getUid());
            mDatabase.child(constants.db_Games).child(gameKey).child(constants.db_Games_Userlist).child(currentUser.getUid()).setValue(0);
        }
    }

    private user getCurrentUser() {
        return ((MainApplication)getApplication()).getCurrentUser();
    }

    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dvMain = (DrawingView) findViewById(R.id.dvMainCanvas);
        ivProjectedCanvas = (ImageView) findViewById(R.id.ivProjectedCanvas);
        chronTimer = (Chronometer) findViewById(R.id.chronTimer);

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void initGameHostIDEventListener() {
        mDatabase.child(constants.db_Games).child(currentUser.getCurrentGameID()).child(constants.db_Games_hostID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hostUserID = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initGameDrawingEventLister() {
        mDatabase.child(constants.db_Games).child(currentUser.getCurrentGameID()).child(constants.db_Games_DrawingURL).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currentGameURL = dataSnapshot.getValue(String.class);

                if(!TextUtils.isEmpty(currentGameURL)){
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
                int numPoints = dataSnapshot.getValue(int.class);

                gameUsers.put(ID, numPoints);
                gameUserIDS.add(ID);
                checkUsers();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren()){
                    mDatabase.child(constants.db_Games).
                            child(getCurrentUser().getCurrentGameID()).removeValue();
                    Log.d("games", "USERLIST IS EMPTY");
                }
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
                    gamestate gs = gamestate.valueOf(gameState);

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

                            if(currentDrawerID.equals(getCurrentUser().getUid())){
                                ivProjectedCanvas.setVisibility(View.GONE);
                                dvMain.setVisibility(View.VISIBLE);
                                fab.setVisibility(View.VISIBLE);
                            } else {
                                ivProjectedCanvas.setVisibility(View.VISIBLE);
                                dvMain.setVisibility(View.GONE);
                                fab.setVisibility(View.GONE);
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
        for(String key: gameUsers.keySet()){
            Log.d("gameUsers", (key + " - " + gameUsers.get(key)));
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

    private void startGame() {
        String newGs = gamestate.GameStateToString(gamestate.drawingPhase);

        if(gameUsers.size() >= 1){
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

                int index = roundNumber % gameUsers.size();

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
