package saberapplications.pawpads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.Toast;

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
    public String regToken;

    @Override
    protected void onNewIntent(Intent intent) {

    }

    public void getUserData(){
        GPS gps = new GPS(this.mContext);
        Location loc = null;
        try {
            loc = new Location(gps.getLastBestLocation());
        }
        catch (NullPointerException e) {
            android.util.Log.w(this.mContext.toString(),
                    "GPS.getLastBestLocation() failed -- location services may be turned off");
            // TODO Better way to tell the user that something went wrong
            Toast.makeText(
                    this.mContext,
                    "Couldn't get user data. Turn on location services and try again.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }
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
