package saberapplications.pawpads;


import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.quickblox.core.exception.QBResponseException;

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
    public static final String USER_LOCATION_LAT= "user_location_lat";
    public static final String USER_LOCATION_LONG= "user_location_long";
    public static final String AD_UNIT_ID = "ca-app-pub-5883625079032168/4982654336";

    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public final static String SENDER_ID = "325095426674";
    public static String GCMREGID = "nothing yet";
      //notes: chat implemented with quickblox. these are my own not the contractors
//    public static final String QB_APPID="33547";
//    public static final String QB_AUTH_KEY="w8YNQSmstbNJ6AJ";
//    public static final String QB_AUTH_SECRET="ckSWjeEkjgNpdJ-";
    public static final String QB_APPID="35252";
    public static final String QB_AUTH_KEY="GHHUHJMGTcGN8EY";
    public static final String QB_AUTH_SECRET="NNSa7QQZDUzRvpB";
    public static final String QB_ACCOUNT_KEY="S6mqpsBsKxfbSqZxGY4X";
    public static final String QB_USER="qb_user";
    public static final String QB_PASSWORD="qb_password";
    public static final String QB_USERID ="qb_userid" ;
    public static final String STICKERS_API_KEY="94c58fc501b259bc84282c44cd278fdf";

    public static int ACCURACY;
    public static int RANGE;

    public static boolean PUSH_NOTIFICIATIONS;
    public static boolean IM_ALERT;
    public static String UNIT_OF_MEASURE;

    public static int getRange(){
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(PawPadsApplication.getInstance());
            return defaultSharedPreferences.getInt("range_km", 60);
    }
    public static void setRange(int range){
        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(PawPadsApplication.getInstance()).edit();
        editor.putInt("range",range);
        editor.commit();
    }


    public static void onError(List<String> errors,Context context) {
        String msg="";
        for(String error:errors){
            msg=msg+error+"\n";
        }
        new AlertDialog.Builder(context)
                .setMessage(msg)
                .setTitle("Error")
                .setPositiveButton("OK", null)
                .show();
    }
    public static void onError(Exception e,Context context) {

        new AlertDialog.Builder(context)
                .setMessage(e.getLocalizedMessage())
                .setTitle("Error")
                .setPositiveButton("OK", null)
                .show();
    }
    public static void onError(QBResponseException e, Context context) {

        new AlertDialog.Builder(context)
                .setMessage(e.getLocalizedMessage())
                .setTitle("Error")
                .setPositiveButton("OK", null)
                .show();
    }


}
