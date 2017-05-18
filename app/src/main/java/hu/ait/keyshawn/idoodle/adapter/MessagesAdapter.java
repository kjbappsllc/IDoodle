package hu.ait.keyshawn.idoodle.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hu.ait.keyshawn.idoodle.GameActivity;
import hu.ait.keyshawn.idoodle.R;
import hu.ait.keyshawn.idoodle.data.Message;

/**
 * Created by mac on 5/18/17.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private List<Message> messages;
    private Context context;

    public MessagesAdapter(Context context){
        this.context = context;
        this.messages = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.messages_row, parent, false);
        return new ViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvmsgSender.setText(context.getString(R.string.msgSender,
                messages.get(position).getSender(),
                messages.get(position).getBody()));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size()-1);
        ((GameActivity)context).scrollMessageRecycler(messages.size()-1);
    }

    public void clear() {
        messages.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvmsgSender;

        public ViewHolder(View itemView) {
            super(itemView);

            tvmsgSender = (TextView) itemView.findViewById(R.id.tvmsgSender);

        }
    }
}
