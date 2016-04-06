package saberapplications.pawpads;

/**
 * Created by Dell on 3/19/2015.
 */
public class ChatObject {

    String message;

    public String getType() {
        return type;
    }

    String type;
    public static final String SENT="sent";
    public static final String RECEIVED="received";

    public ChatObject(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }
}
