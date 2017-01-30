package saberapplications.pawpads;

import android.util.Log;

import com.quickblox.users.model.QBUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roman Fomenko on 13.01.17.
 */

public class UserStatusHelper {
    private static final long OFFLINE_TIME = 600000; // 10 min
    private static final long AWAY_TIME = 300000; // 5 min
    private static final int USER_OFFLINE = 1;
    private static final int USER_AWAY = 2;
    public static final int USER_ONLINE = 3;
    private static Map<Integer, Integer> mStatusUserList = new HashMap<>();
    private static Map<Integer, Long> mStatusByNewMessageChangedTimeList = new HashMap<>();

    public static int getUserStatus(QBUser user) {
        int userStatusByLastRequest = getUserStatusByLastRequestTime(user);
        if( !mStatusUserList.containsKey(user.getId())) {
            Log.d("UserStatusHelper", user.getId() + " " + userStatusByLastRequest);
            mStatusUserList.put(user.getId(), userStatusByLastRequest);
            mStatusByNewMessageChangedTimeList.put(user.getId(), 0L);
            return userStatusByLastRequest;
        } else {
            int existStatus = mStatusUserList.get(user.getId());
            long lastChangedStatusTime = mStatusByNewMessageChangedTimeList.get(user.getId());
            long currentTime = System.currentTimeMillis();
            long userLastRequestAtTime = user.getLastRequestAt().getTime();
            if(lastChangedStatusTime == 0L ||
                    (currentTime - lastChangedStatusTime) > (currentTime - userLastRequestAtTime)) {
                Log.d("UserStatusHelper", "lastChangedStatusTime is 0 or was a long time ago");
                mStatusUserList.put(user.getId(), userStatusByLastRequest);
                return userStatusByLastRequest;
            } else {
                if (existStatus >= userStatusByLastRequest) {
                    Log.d("UserStatusHelper", user.getId() + " exist-" + existStatus);
                    return getCheckedExistStatus(user);
                } else {
                    mStatusUserList.put(user.getId(), userStatusByLastRequest);
                    Log.d("UserStatusHelper", user.getId() + " byreq-" + userStatusByLastRequest);
                    return userStatusByLastRequest;
                }
            }
        }
    }

    private static int getUserStatusByLastRequestTime(QBUser user) {
        long currentTime = System.currentTimeMillis();
        long userLastRequestAtTime = user.getLastRequestAt().getTime();
        Log.d("UserStatusHelper", "getLastRequestAt = " + user.getLastRequestAt().getTime() +
                "  currentTime = " + currentTime +
                "  userLastRequestAtTime = " + (currentTime - userLastRequestAtTime));

        if ((currentTime - userLastRequestAtTime) > OFFLINE_TIME) {
            // user is offline now
            return USER_OFFLINE;
        } else if ((currentTime - userLastRequestAtTime) > AWAY_TIME) {
            // user is away now
            return USER_AWAY;
        } else {
            // else online
            return USER_ONLINE;
        }
    }

    private static int getCheckedExistStatus(QBUser user) {
        int checkedStatus;
        long lastChangedStatusTime = mStatusByNewMessageChangedTimeList.get(user.getId());
        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastChangedStatusTime) > OFFLINE_TIME) {
            // user is offline now
            checkedStatus = USER_OFFLINE;
        } else if ((currentTime - lastChangedStatusTime) > AWAY_TIME) {
            // user is away now
            checkedStatus = USER_AWAY;
        } else {
            // else online
            checkedStatus = USER_ONLINE;
        }
        mStatusUserList.put(user.getId(), checkedStatus);
        return checkedStatus;
    }

    public static void setUserStatusByNewMessage(int userId) {
        Log.d("UserStatusHelper", " new status for user " + userId);
        mStatusUserList.put(userId, USER_ONLINE);
        mStatusByNewMessageChangedTimeList.put(userId, System.currentTimeMillis());
    }
}
