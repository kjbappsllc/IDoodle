package hu.ait.keyshawn.idoodle;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kekstudio.dachshundtablayout.DachshundTabLayout;
import com.kekstudio.dachshundtablayout.indicators.DachshundIndicator;
import com.kekstudio.dachshundtablayout.indicators.LineMoveIndicator;

import org.greenrobot.eventbus.EventBus;

import hu.ait.keyshawn.idoodle.adapter.LobbyAdapter;
import hu.ait.keyshawn.idoodle.constants.constants;
import hu.ait.keyshawn.idoodle.data.Game;
import hu.ait.keyshawn.idoodle.data.Gamestate;
import hu.ait.keyshawn.idoodle.data.User;
import hu.ait.keyshawn.idoodle.pager.FragmentPager;

/**
 * Created by vickievictor on 5/15/17.
 */

public class LobbyActivity extends AppCompatActivity {

    public static final String GAME_NAME = "GAME_NAME";
    private DatabaseReference mDatabase;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        firebaseAuth = FirebaseAuth.getInstance();

        DachshundTabLayout tabLayout = (DachshundTabLayout) findViewById(R.id.tab_layout);

        LineMoveIndicator dachshundIndicator = new LineMoveIndicator(tabLayout);

        FragmentPager myFragmentPager =
                new FragmentPager(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(myFragmentPager);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setAnimatedIndicator(dachshundIndicator);

    }

    public User getCurrentUser() {
        return ((MainApplication) getApplication()).getCurrentUser();
    }

    public void showGameActivity(String gameName) {
        Intent intent = new Intent(LobbyActivity.this,
                GameActivity.class);
        intent.putExtra(GAME_NAME, gameName);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_newGame) {
            showAddGameDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showAddGameDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Create New Game");

        final EditText etGameName = new EditText(this);
        etGameName.setHint("Game Name");
        etGameName.setMaxLines(1);
        alertDialogBuilder.setView(etGameName);

        alertDialogBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(etGameName.getText().toString())) {
                    etGameName.setError("Please Enter Text");
                } else {
                    initNewGameDB(etGameName.getText().toString());
                }
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.show();
    }

    private void initNewGameDB(String gameName) {
        User currentUser = getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        String gameKey = mDatabase.child(constants.db_Games).push().getKey();

        Game newGame = new Game(gameKey, gameName);
        newGame.setGameState(Gamestate.GameStateToString(Gamestate.preGamePhase));
        newGame.getUserList().put(currentUser.getUid(),
                getString(R.string.userInfo, getCurrentUser().getUsername(), 0));
        newGame.setHostUserID(currentUser.getUid());

        EventBus.getDefault().post(newGame);
        mDatabase.child(constants.db_Games).child(gameKey).setValue(newGame);

        currentUser.setCurrentGameID(newGame.getUid());
        mDatabase.child(constants.db_Users).child(currentUser.getUid()).
                child(constants.db_Users_currentGameID).setValue(newGame.getUid());

        showGameActivity(newGame.getGameName());
    }

    public void initJoinGameDB(Game newGame, String gameName) {
        User currentUser = getCurrentUser();
        Resources res = getResources();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        EventBus.getDefault().post(newGame);

        if (currentUser != null) {
            currentUser.setCurrentGameID(newGame.getUid());
            mDatabase.child(constants.db_Users).child(currentUser.getUid()).
                    child(constants.db_Users_currentGameID).setValue(newGame.getUid());

            mDatabase.child(constants.db_Games).
                    child(newGame.getUid()).child(constants.db_Games_Userlist).
                    child(currentUser.getUid()).
                    setValue(res.getString(R.string.userInfo, getCurrentUser().getUsername(), 0));

            showGameActivity(gameName);
        }
    }

    @Override
    public void onBackPressed() {
        firebaseAuth.signOut();
        super.onBackPressed();
    }
}
