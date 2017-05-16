package hu.ait.keyshawn.idoodle.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hu.ait.keyshawn.idoodle.LobbyActivity;
import hu.ait.keyshawn.idoodle.R;
import hu.ait.keyshawn.idoodle.data.game;

/**
 * Created by vickievictor on 5/15/17.
 */

public class LobbyAdapter extends RecyclerView.Adapter<LobbyAdapter.ViewHolder>{

    private List<game> gameList;
    private List<String> gameIDs;
    private Context context;

    public LobbyAdapter(Context context){
        this.context = context;
        gameIDs = new ArrayList<>();
        gameList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.lobby_row, parent, false);
        return new ViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.tvGameName.setText(gameList.get(position).getGameName());
        holder.tvRound.setText(context.getString(R.string.RoundNumber, gameList.get(position).getRoundNumber()));
        if (gameList.get(position).getUserList() == null){
            System.out.println("user list is null");
        }
        else {
            holder.tvUsers.setText(context.getString(R.string.UserNumber, gameList.get(position).getUserList().size()));
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LobbyActivity)context).initJoinGameDB(gameIDs.get(position), holder.tvGameName.getText().toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public void addGame(game newGame){
        gameList.add(0, newGame);
        gameIDs.add(0, newGame.getUid());
        notifyItemInserted(0);

    }

    public void removeGame(String id){

        int index = gameIDs.indexOf(id);

        if(index != -1) {
            gameList.remove(index);
            gameIDs.remove(index);
            notifyItemRemoved(index);
        }

    }

    public void updateGame(String id, game updated) {
        int index = gameIDs.indexOf(id);

        if(index != -1) {
            gameList.set(index, updated);
            gameIDs.set(index, id);
            notifyItemChanged(index);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvGameName;
        private TextView tvRound;
        private TextView tvUsers;
        private CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            tvGameName = (TextView)itemView.findViewById(R.id.tvGameName);
            tvRound = (TextView) itemView.findViewById(R.id.tvRound);
            tvUsers = (TextView) itemView.findViewById(R.id.tvUsers);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
        }
    }
}
