package saberapplications.pawpads;


import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import java.util.List;

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

    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public final static String SENDER_ID = "325095426674";
    public static String GCMREGID = "nothing yet";

    public static final String QB_APPID = "35252";
    public static final String QB_AUTH_KEY = "gb4f7kN3FLgap9A";
    public static final String QB_AUTH_SECRET = "sszVLheuYags2ZQ";
    public static final String QB_ACCOUNT_KEY = "S6mqpsBsKxfbSqZxGY4X";


    public static final String STICKERS_API_KEY = "94c58fc501b259bc84282c44cd278fdf";

    public static final String IMOJI_SDK_CLIENT_ID = "e517f2aa-0ca8-496d-bd22-18cd63edf3e0";
    public static final String IMOJI_SDK_API_TOKEN = "U2FsdGVkX1/vLM8AKeuOd1AgWow8+d7WThPxuO+VeUHZ4AiqoBv/4FxWE7kGpfsd";

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
        if(!message.contains("Subscription with such UDID already exists")){
            showAlert(context, e.getLocalizedMessage());
        }
    }
    public static void onError(String error, Context context) {
        showAlert(context,error);
    }

    private static void showAlert(Context context,String message){
        new AlertDialog.Builder(context,R.style.AppAlertDialogTheme)
                .setMessage(message)
                .setTitle("Error")
                .setPositiveButton("OK", null)
                .show();
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
}
