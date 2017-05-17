package hu.ait.keyshawn.idoodle.pager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.ait.keyshawn.idoodle.LobbyActivity;
import hu.ait.keyshawn.idoodle.MainApplication;
import hu.ait.keyshawn.idoodle.R;
import hu.ait.keyshawn.idoodle.data.User;

/**
 * Created by vickievictor on 5/15/17.
 */



public class FragmentUserDetails extends Fragment{

    private TextView tvUsername;
    private TextView tvGamesPlayed;
    private TextView tvTotalPoints;

    public static final String TAG = "FragmentUserDetails";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user_details,container,false);

        tvUsername = (TextView) rootView.findViewById(R.id.tvUsername);
        tvGamesPlayed = (TextView) rootView.findViewById(R.id.tvGamesPlayed);
        tvTotalPoints = (TextView) rootView.findViewById(R.id.tvTotalPoints);

        if ((tvUsername != null) && (tvTotalPoints != null) && (tvGamesPlayed != null) ){
            tvUsername.setText(getCurrentUser().getUsername());
            tvTotalPoints.setText(Integer.toString(getCurrentUser().getTotalPointsEarned()));
            tvGamesPlayed.setText(Integer.toString(getCurrentUser().getGamesPlayed()));
        }else{
            System.out.println("Something is null");
        }

        return rootView;
    }

    private User getCurrentUser() {
        return ((MainApplication)(getActivity()).getApplication()).getCurrentUser();
    }
}
