package saberapplications.pawpads;

/**
 * Created by Dell on 3/19/2015.
 */
public class ChatObject {

    String message, type;

    public String getType() {
        return type;
    }

    public ChatObject(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }


}
