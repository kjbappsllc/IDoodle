package hu.ait.keyshawn.idoodle.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

import hu.ait.keyshawn.idoodle.R;

/**
 * Created by mac on 5/16/17.
 */

public class GameUsersAdapter extends RecyclerView.Adapter<GameUsersAdapter.ViewHolder> {

    private List<String> userList;
    private List<String> userIDs;
    private String currentDrawerID = "";
    private String currentHostID = "";
    private DatabaseReference mData;
    private Context context;

    public GameUsersAdapter(Context context){
        this.context = context;
        userList = new ArrayList<>();
        userIDs = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.gameusers_row, parent, false);
        return new ViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String[] userElements = userList.get(position).split(",");
        String username = userElements[0];
        String points = userElements[1];
        holder.tvUsername.setText(username);
        holder.roundPoints.setText(points);

        if(userIDs.get(position).equals(currentDrawerID)){
            holder.ivDrawer.setVisibility(View.VISIBLE);
        }
        else{
            holder.ivDrawer.setVisibility(View.INVISIBLE);
        }

        if(userIDs.get(position).equals(currentHostID))
            holder.ivHostID.setVisibility(View.VISIBLE);
        else {
            holder.ivHostID.setVisibility(View.INVISIBLE);
        }
    }

    public void setCurrentDrawerID(String currentDrawerID){
        this.currentDrawerID = currentDrawerID;
        notifyDataSetChanged();
    }

    public void setCurrentHostID(String currentHostID){
        this.currentHostID = currentHostID;
        notifyDataSetChanged();
    }

    public void addUser(String ID, String userInfo){
        this.userIDs.add(0,ID);
        String[] userElements = userInfo.split(",");
        String username = userElements[0];
        this.userList.add(0,userInfo);
        notifyItemInserted(0);
        Toast.makeText(context, ""+ username + " has entered", Toast.LENGTH_SHORT).show();

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivHostID;
        private TextView tvUsername;
        private ImageView ivDrawer;
        private TextView roundPoints;

        public ViewHolder(View itemView) {
            super(itemView);

            ivHostID = (ImageView) itemView.findViewById(R.id.ivHostIcon);
            tvUsername = (TextView) itemView.findViewById(R.id.tvuserName);
            ivDrawer = (ImageView) itemView.findViewById(R.id.ivDrawer);
            roundPoints = (TextView) itemView.findViewById(R.id.userPoints);

        }
    }
}
