package hu.ait.keyshawn.idoodle;

import android.app.Application;
import android.util.Log;

import hu.ait.keyshawn.idoodle.data.user;

/**
 * Created by mac on 5/12/17.
 */

public class MainApplication extends Application {
    user currentUser = new user();

    public user getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(user currentUser) {
        this.currentUser = currentUser;
        Log.d("user", currentUser.getUid());
    }
}
