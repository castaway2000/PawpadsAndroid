package saberapplications.pawpads;


import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by blaze on 9/7/2015.
 */
public class Util {
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static String APP_VERSION = "appVersion";
    public static final String USER_NAME = "user_name";
    public static final String IS_BLOCKED = "is_blocked";
    public static final String USER_LOCATION_LAT = "user_location_lat";
    public static final String USER_LOCATION_LONG = "user_location_long";
    public static final String AD_UNIT_ID = "ca-app-pub-5883625079032168/5482181931";

    //google play services
    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public final static String SENDER_ID = "PUT GOOGLE PLAY ID HERE";
    public static String GCMREGID = "nothing yet";
    //quickblox ID
    public static final String QB_APPID = "PUT YOURS HERE";
    public static final String QB_AUTH_KEY = "PUT YOURS HERE";
    public static final String QB_AUTH_SECRET = "PUT YOURS HERE";
    public static final String QB_ACCOUNT_KEY = "PUT YOURS HERE";

    //quickblox ID
    public static final String QB_APPID = "PUT YOURS HERE";
    public static final String QB_AUTH_KEY = "PUT YOURS HERE";
    public static final String QB_AUTH_SECRET = "PUT YOURS HERE";
    public static final String QB_ACCOUNT_KEY = "PUT YOURS HERE";

    public static final String STICKERS_API_KEY = "PUT YOUR IMOJI KEY HERE";
    public static final String IMOJI_SDK_CLIENT_ID = "PUT YOUR IMOJI CLIENT ID HERE";
    public static final String IMOJI_SDK_API_TOKEN = "PUT YOUR IMOJI API TOKEN HERE";

    public static String ACCURACY;
    public static int RANGE;

    public static boolean PUSH_NOTIFICIATIONS;
    public static boolean IM_ALERT;
    public static String UNIT_OF_MEASURE;

    public static int getRange() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(PawPadsApplication.getInstance());
        return defaultSharedPreferences.getInt(C.RANGE_KM, 60);
    }

    public static void setRange(int range) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(PawPadsApplication.getInstance()).edit();
        editor.putInt("range", range);
        editor.commit();
    }


    public static void onError(List<String> errors, Context context) {
        String msg = "";
        for (String error : errors) {
            msg = msg + error + "\n";
        }
        showAlert(context, msg);
    }

    public static void onError(Exception e, Context context) {
        Crashlytics.getInstance().logException(e);
        showAlert(context,e.getLocalizedMessage());
    }

    public static void onError(QBResponseException e, Context context) {
        Crashlytics.getInstance().logException(e);
        String message =  e.getLocalizedMessage();
        if(message.contains("Subscription with such UDID already exists")) return;
        if (e.getLocalizedMessage().contains("timeout")){
            showAlert(context,context.getString(R.string.error_timeout));
        }else {
            showAlert(context, e.getLocalizedMessage());
        }
    }
    public static void onError(String error, Context context) {
        showAlert(context,error);
    }

    public static void showAlert(Context context,String message){
        try {
            new AlertDialog.Builder(context, R.style.AppAlertDialogTheme)
                    .setMessage(message)
                    .setTitle("Error")
                    .setPositiveButton("OK", null)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     *
     * @param distanceTo in meters
     * @return formatted distance
     */
    public static  String formatDistance(float distanceTo) {

        if (Util.UNIT_OF_MEASURE.equals("MI")) {
            distanceTo = distanceTo * 3.2808f;
            //gps coordinates

            if (distanceTo > 5280) {
                return String.format("%.2f miles", distanceTo / 5280);
            } else {
                distanceTo = Math.round(distanceTo);
                return String.format("%.0f feet", distanceTo);
            }
        } else {
            //gps coordinates
            if (distanceTo > 1000) {
                return String.format("%.2f km", distanceTo / 1000);
            } else {
                distanceTo = Math.round(distanceTo);
                return String.format("%.0f meters", distanceTo);
            }
        }
    }


    public static String getUserName(QBUser user) {

        return user.getFullName()!=null ? user.getFullName() : user.getLogin();
    }

    public static int getCreatedChannelsCount() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(PawPadsApplication.getInstance());
        return defaultSharedPreferences.getInt(C.CREATED_CHANNELS_COUNT, 0);
    }

    public static void setCreatedChannelsCount(int channelsCount) {
        if(channelsCount < 0) channelsCount = 0;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(PawPadsApplication.getInstance()).edit();
        editor.putInt(C.CREATED_CHANNELS_COUNT, channelsCount);
        editor.apply();
    }

    public static Set<String> getFriendOutInvitesList() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(PawPadsApplication.getInstance());
        return defaultSharedPreferences.getStringSet(C.FRIEND_OUT_INVITES_LIST, new HashSet<String>());
    }

    public static void addFriendOutInviteToList(int userId) {
        Set<String> outInvites = getFriendOutInvitesList();
        outInvites.add(String.valueOf(userId));
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(PawPadsApplication.getInstance()).edit();
        editor.putStringSet(C.FRIEND_OUT_INVITES_LIST, outInvites);
        editor.apply();
    }


    public static Set<String> getFriendAcceptedList() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(PawPadsApplication.getInstance());
        return defaultSharedPreferences.getStringSet(C.FRIEND_ACCEPTED_LIST, new HashSet<String>());
    }

    public static void addFriendAcceptedList(int userId) {
        Set<String> outInvites = getFriendAcceptedList();
        outInvites.add(String.valueOf(userId));
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(PawPadsApplication.getInstance()).edit();
        editor.putStringSet(C.FRIEND_ACCEPTED_LIST, outInvites);
        editor.apply();
    }
    public static void removeFriendAcceptedList(int userId) {
        Set<String> outInvites = getFriendAcceptedList();
        outInvites.remove(String.valueOf(userId));
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(PawPadsApplication.getInstance()).edit();
        editor.putStringSet(C.FRIEND_ACCEPTED_LIST, outInvites);
        editor.apply();
    }

    public static void removeFriendOutInviteFromList(int userId) {
        Set<String> outInvites = getFriendOutInvitesList();
        outInvites.remove(String.valueOf(userId));
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(PawPadsApplication.getInstance()).edit();
        editor.putStringSet(C.FRIEND_OUT_INVITES_LIST, outInvites);
        editor.apply();
    }
}
