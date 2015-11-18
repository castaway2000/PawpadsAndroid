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
    public String[] upics;

//    public void getUserData() {
//        final GPS gps = new GPS(mContext);
//        float[] dat = gps.geolocation();
//        String[] geoloc = {String.valueOf(dat[0]), String.valueOf(dat[0]), String.valueOf(dat[0]),
//                           String.valueOf(dat[0]), String.valueOf(dat[0]), String.valueOf(dat[0]),
//                           String.valueOf(dat[0]), String.valueOf(dat[0])};
//        geol = geoloc;
//
//        final int[] pics = {R.drawable.pic_11, R.drawable.pic_22, R.drawable.pic_33,
//                            R.drawable.pic_11, R.drawable.pic_22, R.drawable.pic_33,
//                            R.drawable.pic_44, R.drawable.pic_44};
//        upics = pics;
//
//        getList();
//    }


    public void getUserData(){
        //TODO: get photo, distance, description data from database
        ServerRequests serverRequests = new ServerRequests(this.mContext);
        serverRequests.fetchListDataInBackground(null, new GetUserListCallback() {
            @Override
            public void done(UserList returnedUser) {

                //TODO: set geo returned relative to current location

                UserData.this.upics = returnedUser.pic;
                UserData.this.geol = returnedUser.distance;
                UserData.this.user = returnedUser.username;
                UserData.this.descr = returnedUser.profile;

                ((MainActivity) UserData.this.mContext).setListView(returnedUser);
            }
        });
    }
}
