package saberapplications.pawpads;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Dell on 3/19/2015.
 */
public class ChatObject implements Parcelable{

    String message;
    String type;
    Date dateTime;

    protected ChatObject(Parcel in) {
        message = in.readString();
        type = in.readString();
        dateTime=new Date(in.readLong());
    }

    public static final Creator<ChatObject> CREATOR = new Creator<ChatObject>() {
        @Override
        public ChatObject createFromParcel(Parcel in) {
            return new ChatObject(in);
        }

        @Override
        public ChatObject[] newArray(int size) {
            return new ChatObject[size];
        }
    };

    public String getType() {
        return type;
    }


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

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeString(type);
        dest.writeLong(dateTime.getTime());
    }
}
