package saberapplications.pawpads;


import android.app.AlertDialog;
import android.content.Context;

import java.util.List;

/**
 * Created by blaze on 9/7/2015.
 */
public class Util {
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String EMAIL = "email";
    public static final String USER_NAME = "user_name";
    public static final String USER_LOCATION_LAT= "user_location_lat";
    public static final String USER_LOCATION_LONG= "user_location_long";
    public static final String USER_INFO= "user_info";
    public static final String USER_AVATAR_PATH= "user_avatar_path";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public final static String SENDER_ID = "325095426674";

    public static String GCMREGID = "nothing yet";

    public static String base_url = "http://www.szablya.com/saberapps/gcm_demo/";

    public final static String pawpadsURL = "http://www.szablya.com/saberapps/pawpads/";
    public final static String register_url = base_url + "register.php";
    public final static String send_chat_url = base_url + "sendChatmessage.php";

   //notes: chat implemented with quickblox. these are my own not the contractors
    public static final String QB_APPID="33547";
    public static final String QB_AUTH_KEY="w8YNQSmstbNJ6AJ";
    public static final String QB_AUTH_SECRET="ckSWjeEkjgNpdJ-";

    public static final String QB_USER="qb_user";
    public static final String QB_PASSWORD="qb_password";
    public static final String QB_USERID ="qb_userid" ;

    public static final String STICKERS_API_KEY="94c58fc501b259bc84282c44cd278fdf";

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
}
