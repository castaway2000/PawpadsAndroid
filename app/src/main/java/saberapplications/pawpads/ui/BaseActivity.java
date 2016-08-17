package saberapplications.pawpads.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.location.LocationListener;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.List;

import saberapplications.pawpads.Util;
import saberapplications.pawpads.service.UserLocationService;
import saberapplications.pawpads.ui.login.LoginActivity;


/**
 * Created by Stas on 22.01.16.
 */
public abstract class BaseActivity extends AppCompatActivity
        implements LocationListener {
    private static int openActivitiesCount = 0;


    protected static boolean isLoggedIn;
    private static Integer userId;

    //    private Location lastLocation;
    private boolean isActive;
//    protected static QBLocation qbLocation;

    BroadcastReceiver locationChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(UserLocationService.LOCATION);
            onLocationChanged(location);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create an instance of GoogleAPIClient.


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (isLoggedIn) {
            try {
                onQBConnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {

        isActive = true;
        super.onStart();
        incrementActivityCount();
        recreateSession();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationChanged, new IntentFilter(UserLocationService.LOCATION_CHANGED)
        );

    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationChanged);
        decrementActivityCount();

    }

    public void logOutChat() {
        if (QBChatService.isInitialized()) {
            try {
                QBChatService.getInstance().logout();
                isLoggedIn = false;
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
                            loginToChat();
                            UserLocationService.startService(userId);
                        } catch (Exception e) {

                        }

                    }

                    @Override
                    public void onError(QBResponseException responseException) {
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
                    QBPrivacyListsManager privacyListsManager = QBChatService.getInstance().getPrivacyListsManager();
                    try {
                        QBPrivacyList list = privacyListsManager.getPrivacyList("public");
                        if (list != null) {
                            list.setDefaultList(true);
                            list.setActiveList(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    onQBConnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else


            QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {
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
                }

                @Override
                public void onError(QBResponseException e) {
                    Util.onError(e, getBaseContext());
                }
            } );
        }




    public void onQBConnect() throws Exception {

    }


    @Override
    public void onLocationChanged(Location location) {

    }


    public synchronized void incrementActivityCount() {

        openActivitiesCount++;
    }

    public synchronized void decrementActivityCount() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                openActivitiesCount--;
                if (openActivitiesCount == 0) {
                    logOutChat();
                    //stoplocation updates
                    UserLocationService.stop();
                }
            }
        }, 500);
    }

    public Integer getUserId() {
        return userId;
    }

    protected boolean isActive() {
        return isActive;
    }
}
