package hu.ait.keyshawn.idoodle.pager;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.ait.keyshawn.idoodle.R;
import hu.ait.keyshawn.idoodle.adapter.LobbyAdapter;

/**
 * Created by vickievictor on 5/15/17.
 */

public class FragmentLobbies extends Fragment{

    public static final String TAG = "FragmentLobbies";
    private LobbyAdapter lobbyAdapter;
    private RecyclerView lobbyRecycler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_lobbies,container,false);

        lobbyRecycler = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        if (lobbyRecycler != null){
            final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            lobbyRecycler.setLayoutManager(layoutManager);
            lobbyAdapter = new LobbyAdapter(getContext());
            lobbyRecycler.setAdapter(lobbyAdapter);
        }else{
            System.out.println("recycler is null");
        }

        return rootView;
    }
}
