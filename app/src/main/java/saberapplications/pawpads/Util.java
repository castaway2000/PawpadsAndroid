package saberapplications.pawpads;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by blaze on 9/7/2015.
 */
public class Util {
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String EMAIL = "email";
    public static final String USER_NAME = "user_name";

    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public final static String SENDER_ID = "903183948435";

    public static String base_url = "http://www.szablya.com/saberapps/gcm_demo/";

    public final static String register_url = base_url + "register.php";
    public final static String send_chat_url = base_url + "sendChatmessage.php";

}
