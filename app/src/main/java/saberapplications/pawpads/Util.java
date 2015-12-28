package saberapplications.pawpads;


import android.content.Intent;

/**
 * Created by blaze on 9/7/2015.
 */
public class Util {
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String EMAIL = "email";
    public static final String USER_NAME = "user_name";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public final static String SENDER_ID = "327302205372";
    public static String GCMREGID = "nothing yet";

    public static String base_url = "http://www.szablya.com/saberapps/gcm_demo/";
    public final static String pawpadsURL = "http://www.szablya.com/saberapps/pawpads/";
    public final static String register_url = base_url + "register.php";
    public final static String send_chat_url = base_url + "sendChatmessage.php";
    //TODO: verify php url
    public final static String update_user_url = base_url + "UpdateUserProfile.php";
    public static String DEVICE_USER = "";
    public static Intent SERVICE_INTENT = null;



//    // xmpp variables
//    //TODO: update this to my own server
//    public static final String SERVER ="http://52.33.238.201:9090";
//    public static final String DOMAIN = "52.33.238.201";
//    public static final String XMPP_PASSWORD = "Password";
//    public static final String XMPP_SECREAT_KEY = "DpIDyV2L";
//
//    public static final String SUFFIX_CHAT = "@" + DOMAIN;
//    public static final String SUFFIX_CHAT_GROUP = "@conference." + DOMAIN;

}
