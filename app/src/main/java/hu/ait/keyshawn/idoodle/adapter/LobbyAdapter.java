package hu.ait.keyshawn.idoodle.adapter;

import android.content.Context;
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
    private Context context;

    public LobbyAdapter(Context context){
        this.context = context;
        gameList = new ArrayList<game>();

        gameList.add(new game("123","game #1"));
        gameList.add(new game("567","game #3"));

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.lobby_row, parent, false);
        return new ViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.tvGameName.setText(gameList.get(position).getGameName());
        holder.tvRoundNum.setText(Integer.toString(gameList.get(position).getRoundNumber()));
        if (gameList.get(position).getUserList() == null){
            System.out.println("user list is null");
        }
        holder.tvNumOfUsers.setText(Integer.toString(gameList.get(position).getUserList().size()));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LobbyActivity)context).showGameActivity
                        (gameList.get(holder.getAdapterPosition()).getGameName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public void addGame(String gameName){
        //add uid and gamename parameters to new game()
        gameList.add(0, new game());
        notifyItemInserted(0);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvGameName;
        private TextView tvRound;
        private TextView tvRoundNum;
        private TextView tvUsers;
        private TextView tvNumOfUsers;
        private CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            tvGameName = (TextView)itemView.findViewById(R.id.tvGameName);
            tvRound = (TextView) itemView.findViewById(R.id.tvRound);
            tvRoundNum = (TextView) itemView.findViewById(R.id.tvRoundNum);
            tvUsers = (TextView) itemView.findViewById(R.id.tvUsers);
            tvNumOfUsers= (TextView) itemView.findViewById(R.id.tvNumOfUsers);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
        }
    }
}
