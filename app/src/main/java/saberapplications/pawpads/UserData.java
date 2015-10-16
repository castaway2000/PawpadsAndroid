package saberapplications.pawpads;

import android.app.Activity;
import android.content.Context;

/**
 * Created by blaze on 9/9/2015.
 */
public class UserData extends Activity {
    Context mContext;

    public UserData(Context context) {
        this.mContext = context;
    }

    private String getuserdataURL = "localhost:8080/getdata.php";
    public String[] user;
    public String[] descr;
    public String[] geol;
    public int[] upics;

    public void getUserData() {

//        DatabaseCall dc = new DatabaseCall();
//
//        //TODO: INTEGRATE CHAT HTTP REQUEST FOR JSON TASKS.
//        dc.JsonArrayRequest(getuserdataURL);

        final GPS gps = new GPS(mContext);
        float[] dat = gps.geolocation();
        String[] geoloc = {String.valueOf(dat[0]), String.valueOf(dat[0]), String.valueOf(dat[0]), String.valueOf(dat[0])};

        //TODO: get data from database
        final String[] username = {"Maria", "Steve", "Kristy", "Helen"};

        final String[] descrip = { /*getString(R.string.profile)*/ "garble" +
                "My parents dog is named Parmesan he is awesome! love him lots! if you want a " +
                "coffee let me know im always around.", "Im the man the legend!" +
                " hit me up if your in Sodo and want to get a nice beer.",
                "hot teacher incoming! im awesome i know it cause i teach it! read books! " +
                        "Travel the world! im new to this city trying to make some friends near me! chat me up!",
                "Life is short eat desert first! Dance and never let a moment slip by!"};

        user = username;
        descr = descrip;
        geol = geoloc;

        final int[] pics = {R.drawable.pic_11, R.drawable.pic_22, R.drawable.pic_33, R.drawable.pic_44};
        upics = pics;
    }
}
