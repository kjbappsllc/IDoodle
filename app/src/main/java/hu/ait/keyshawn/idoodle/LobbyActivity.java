package hu.ait.keyshawn.idoodle;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import hu.ait.keyshawn.idoodle.adapter.LobbyAdapter;
import hu.ait.keyshawn.idoodle.data.user;
import hu.ait.keyshawn.idoodle.pager.FragmentLobbies;
import hu.ait.keyshawn.idoodle.pager.FragmentPager;

/**
 * Created by vickievictor on 5/15/17.
 */

public class LobbyActivity extends AppCompatActivity {

    public static final String GAME_NAME = "GAME_NAME";
    private LobbyAdapter lobbyAdapter;
    private RecyclerView lobbyRecycler;
    private TextView tvUsername;
    private TextView tvGamesPlayed;
    private TextView tvTotalPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        FragmentPager myFragmentPager =
                new FragmentPager(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(myFragmentPager);

    }

    private user getCurrentUser() {
        return ((MainApplication)getApplication()).getCurrentUser();
    }

    public void showGameActivity(String gameName){
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

    public void showAddGameDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Create New Game");

        final EditText etGameName = new EditText(this);
        etGameName.setHint("Game Name");
        alertDialogBuilder.setView(etGameName);

        alertDialogBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                lobbyAdapter.addGame(etGameName.getText().toString());
                lobbyRecycler.scrollToPosition(0);
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
}
