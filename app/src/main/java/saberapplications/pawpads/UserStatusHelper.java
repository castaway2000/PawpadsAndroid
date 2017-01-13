package saberapplications.pawpads;

import android.util.Log;

import com.quickblox.users.model.QBUser;

/**
 * Created by Roman Fomenko on 13.01.17.
 */

public class UserStatusHelper {
    public static final long OFFLINE_TIME = 1200000;
    public static final long AWAY_TIME = 600000;
    public static final int USER_OFFLINE = 1;
    public static final int USER_AWAY = 2;
    public static final int USER_ONLINE = 3;

    public static int getUserStatus(QBUser user) {
        Log.d("UserStatusHelper" , "getLastRequestAt = " + user.getLastRequestAt().getTime());
        long currentTime = System.currentTimeMillis();
        Log.d("UserStatusHelper" , "currentTime = " + currentTime);
        long userLastRequestAtTime = user.getLastRequestAt().getTime();
        Log.d("UserStatusHelper" , "getLastRequestAt = " + (currentTime - userLastRequestAtTime));
        // if user didn't do anything last 20 minutes
        if((currentTime - userLastRequestAtTime) > OFFLINE_TIME){
            // user is offline now
            return USER_OFFLINE;
        } else if((currentTime - userLastRequestAtTime) > AWAY_TIME){
            // user is away now
            return USER_AWAY;
        }
        // else online
        return USER_ONLINE;
    }
}
