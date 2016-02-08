package saberapplications.pawpads.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
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
public abstract class BaseActivity extends AppCompatActivity {
    public static int openActivitiesCount = 0;
    private LocationManager locationManager;

    @Override
    protected void onStart() {
        super.onStart();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        openActivitiesCount++;
//        if (!isConnected()) {
        recreateSession();
//        } else {
//            onQBConnect();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        logOut();

    }

    protected void recreateSession() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
        QBAuth.createSession(prefs.getString(Util.QB_USER, ""), prefs.getString(Util.QB_PASSWORD, ""),
                new QBEntityCallbackImpl<QBSession>() {
                    @Override
                    public void onSuccess(final QBSession result, Bundle params) {
                        String locationGPSProvider = LocationManager.GPS_PROVIDER;
                        Location lastKnownLocation = locationManager.getLastKnownLocation(locationGPSProvider);

                        double latitude = lastKnownLocation.getLatitude();
                        double longitude = lastKnownLocation.getLongitude();
                        QBLocation location = new QBLocation(latitude, longitude);
                        location.setUserId(result.getUserId());
                        QBLocations.createLocation(location, new QBEntityCallbackImpl<QBLocation>() {
                            @Override
                            public void onSuccess(QBLocation qbLocation, Bundle args) {
                                editor.putString(Util.USER_LOCATION_LAT, String.valueOf(qbLocation.getLatitude()));
                                editor.putString(Util.USER_LOCATION_LONG, String.valueOf(qbLocation.getLongitude()));
                                editor.putInt(Util.QB_USERID, result.getUserId());
                                editor.apply();
                            }

                            @Override
                            public void onError(List<String> errors) {

                            }
                        });

                        loginToChat();

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void onQBConnect() throws Exception;

}
