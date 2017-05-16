package hu.ait.keyshawn.idoodle.pager;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hu.ait.keyshawn.idoodle.R;
import hu.ait.keyshawn.idoodle.adapter.LobbyAdapter;
import hu.ait.keyshawn.idoodle.constants.constants;
import hu.ait.keyshawn.idoodle.data.game;

/**
 * Created by vickievictor on 5/15/17.
 */

public class FragmentLobbies extends Fragment {

    public static final String TAG = "FragmentLobbies";
    private LobbyAdapter lobbyAdapter;
    private RecyclerView lobbyRecycler;
    private DatabaseReference gamesRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_lobbies, container, false);

        lobbyRecycler = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        lobbyRecycler.setHasFixedSize(true);
        lobbyRecycler.setLayoutManager(layoutManager);
        lobbyAdapter = new LobbyAdapter(getContext());
        lobbyRecycler.setAdapter(lobbyAdapter);

        initGamesListener();

        return rootView;
    }

    public void initGamesListener() {
        gamesRef = FirebaseDatabase.getInstance().getReference();
        gamesRef.child(constants.db_Games).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                game newGame = dataSnapshot.getValue(game.class);
                lobbyAdapter.addGame(newGame);
                lobbyRecycler.scrollToPosition(0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                game updatedGame = dataSnapshot.getValue(game.class);
                lobbyAdapter.updateGame(updatedGame.getUid(), updatedGame);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                game removedGame = dataSnapshot.getValue(game.class);
                lobbyAdapter.removeGame(removedGame.getUid());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
