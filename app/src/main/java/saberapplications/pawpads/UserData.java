package saberapplications.pawpads;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;

/**
 * Created by blaze on 9/9/2015.
 */
public class UserData extends Activity {
    Context mContext;
    public UserData(Context context) {
        this.mContext = context;
    }

    public String[] user;
    public String[] descr;
    public String[] geol;
    public int[] upics;

    public void getUserData() {
        //TODO: INTEGRATE CHAT HTTP REQUEST FOR JSON TASKS.
        //TODO: get photo, distance, description data from database

        final GPS gps = new GPS(mContext);
        float[] dat = gps.geolocation();
        String[] geoloc = {String.valueOf(dat[0]), String.valueOf(dat[0]), String.valueOf(dat[0]),
                           String.valueOf(dat[0]), String.valueOf(dat[0]), String.valueOf(dat[0]),
                           String.valueOf(dat[0]), String.valueOf(dat[0])};
        geol = geoloc;

        final int[] pics = {R.drawable.pic_11, R.drawable.pic_22, R.drawable.pic_33,
                            R.drawable.pic_11, R.drawable.pic_22, R.drawable.pic_33,
                            R.drawable.pic_44, R.drawable.pic_44};
        upics = pics;

        getList();
    }


    public void getList(){
        ServerRequests serverRequests = new ServerRequests(this.mContext);
        serverRequests.fetchListDataInBackground(null, new GetUserListCallback() {
            @Override
            public void done(UserList returnedUser) {

                //TODO: set geo returned relative to current location
                //UserData.this.geo = returnedUser.geo;

                //TODO: null photo handeling
                //Userdata.this.pics = returnedUser.pics;

                //TODO: make username, not actual name.
                UserData.this.user = returnedUser.name;

                //TODO: make description, not age
                UserData.this.descr = returnedUser.age;

                ((MainActivity) UserData.this.mContext).setListView(returnedUser);
            }
        });
    }
}
