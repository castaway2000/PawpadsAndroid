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
