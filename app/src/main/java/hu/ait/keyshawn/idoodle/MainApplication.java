package hu.ait.keyshawn.idoodle;

import android.app.Application;
import android.util.Log;

import hu.ait.keyshawn.idoodle.data.User;

/**
 * Created by mac on 5/12/17.
 */

public class MainApplication extends Application {

    private User currentUser;

    @Override
    public void onCreate() {
        super.onCreate();
        currentUser = new User();

    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        Log.d("user", currentUser.getUid());
    }

    public void addGamePoints() {
        currentUser.addGamesPlayed();
    }

    public void addTotalPoints(int points){
        currentUser.addTotalPointsEarned(points);
    }
}
