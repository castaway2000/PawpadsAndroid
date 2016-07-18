package saberapplications.pawpads;

import java.util.Date;

/**
 * Created by Dell on 3/19/2015.
 */
public class ChatObject {

    String message;

    public String getType() {
        return type;
    }

    String type;
    Date dateTime;
    public static final String SENT="sent";
    public static final String RECEIVED="received";

    public ChatObject(String message, String type,Date date) {
        this.message = message;
        this.type = type;
        this.dateTime=date;
    }

    public String getMessage() {
        return message;
    }
}
