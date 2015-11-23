package saberapplications.pawpads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

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

    @Override
    protected void onNewIntent(Intent intent) {

    }

    public void getUserData(){
        GPS gps = new GPS(this.mContext);
        Location loc = new Location(gps.getLastBestLocation());
        Double lat = loc.getLatitude();
        Double lng = loc.getLongitude();
        UserLocalStore uls = new UserLocalStore(mContext);
        String username = uls.getLoggedInUser().username;

        ServerRequests serverRequests = new ServerRequests(this.mContext, lat, lng, username);
        serverRequests.fetchListDataInBackground(null, new GetUserListCallback() {
            @Override
            public void done(UserList returnedUser) {
                UserData.this.upics = returnedUser.pic;
                UserData.this.geol = returnedUser.distance;
                UserData.this.user = returnedUser.username;
                UserData.this.descr = returnedUser.profile;
                ((MainActivity) UserData.this.mContext).setListView(returnedUser);
            }
        });
    }
}
