package saberapplications.pawpads.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.location.QBLocations;
import com.quickblox.location.model.QBLocation;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.List;

import saberapplications.pawpads.Util;
import saberapplications.pawpads.ui.home.MainActivity;
import saberapplications.pawpads.ui.login.LoginActivity;


/**
 * Created by Stas on 22.01.16.
 */
public abstract class BaseActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    public static int openActivitiesCount = 0;

    private GoogleApiClient mGoogleApiClient;

    protected static boolean isLoggedIn;
    private Integer userId;

    public Location getLastLocation() {
        return lastLocation;
    }

    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }



    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        incrementActivityCount();
        recreateSession();
        if (isLoggedIn){
            try {
                onQBConnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
        mGoogleApiClient.disconnect();
        decrementActivityCount();

    }
    public void logOutChat(){
        if (QBChatService.isInitialized()) {
            try {
                QBChatService.getInstance().logout();
                isLoggedIn=false;
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void recreateSession() {
        if (isLoggedIn) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        QBAuth.createSession(prefs.getString(Util.QB_USER, ""), prefs.getString(Util.QB_PASSWORD, ""),
                new QBEntityCallbackImpl<QBSession>() {
                    @Override
                    public void onSuccess(final QBSession result, Bundle params) {
                        try {
                            userId = result.getUserId();
                            isLoggedIn = true;
                            updateUserLocation(null);
                            loginToChat();
                        } catch (Exception e) {

                        }

                    }

                    @Override
                    public void onError(List<String> errors) {
                        startActivity(new Intent(getBaseContext(), LoginActivity.class));
                        finish();

                    }
                });

    }

    protected void loginToChat() {
        if (!QBChatService.isInitialized()) {
            QBChatService.init(getBaseContext());
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final QBUser qbUser = new QBUser(prefs.getString(Util.QB_USER, ""), prefs.getString(Util.QB_PASSWORD, ""));
        qbUser.setId(prefs.getInt(Util.QB_USERID, 0));
        if (QBChatService.getInstance().isLoggedIn()) {
            try {
                if (QBChatService.getInstance() != null) {
                    onQBConnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else

        {
            QBChatService.getInstance().login(qbUser, new QBEntityCallbackImpl() {
                @Override
                public void onSuccess() {
                    try {
                        QBChatService.getInstance().startAutoSendPresence(60);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (QBChatService.getInstance() != null) {
                                        onQBConnect();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });


                    } catch (SmackException.NotLoggedInException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(List errors) {
                    Util.onError(errors, getBaseContext());
                }
            });
        }

    }

    protected void logOut() {
        try {
            if (QBChatService.isInitialized()) {
                QBChatService.getInstance().logout();
            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.clear();
            editor.apply();
            isLoggedIn=false;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void onQBConnect() throws Exception;

    @Override
    public void onConnected(Bundle bundle) {
        updateUserLocation(null);
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(120000);
        locationRequest.setFastestInterval(60000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, this);
        lastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        updateUserLocation(location);

    }

    protected void updateUserLocation(Location location){
        if (!mGoogleApiClient.isConnected()) return;
        if (!isLoggedIn) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (location==null){
            location=LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }
        if (location==null) return;
        if (userId==null) return;
        lastLocation=location;
        QBLocation qbLocation = new QBLocation(location.getLatitude(), location.getLongitude());
        qbLocation.setUserId(userId);
        QBLocations.createLocation(qbLocation, new QBEntityCallbackImpl<QBLocation>() {
            @Override
            public void onSuccess(QBLocation qbLocation, Bundle args) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
                editor.putString(Util.USER_LOCATION_LAT, String.valueOf(qbLocation.getLatitude()));
                editor.putString(Util.USER_LOCATION_LONG, String.valueOf(qbLocation.getLongitude()));
                editor.putInt(Util.QB_USERID, userId);
                editor.apply();
            }

            @Override
            public void onError(List<String> errors) {

            }
        });


    }
    public synchronized void incrementActivityCount(){
        openActivitiesCount++;
    }
    public synchronized void decrementActivityCount(){
        openActivitiesCount--;
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (openActivitiesCount==0){
                    logOutChat();
                }
            }
        },500);
    }

    public Integer getUserId() {
        return userId;
    }
}
