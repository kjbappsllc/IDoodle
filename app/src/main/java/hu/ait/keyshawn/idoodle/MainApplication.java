package hu.ait.keyshawn.idoodle;

import android.app.Application;
import android.util.Log;

import hu.ait.keyshawn.idoodle.data.User;

/**
 * Created by mac on 5/12/17.
 */

public class MainApplication extends Application {

    User currentUser = new User();

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        Log.d("user", currentUser.getUid());
    }
}
