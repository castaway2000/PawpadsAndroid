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
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.Date;

import saberapplications.pawpads.C;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.service.UserLocationService;
import saberapplications.pawpads.ui.home.SplashActivity;


/**
 * Created by Stas on 22.01.16.
 */
public abstract class BaseActivity extends AppCompatActivity
        implements LocationListener {
    private static final int RECREATE_SESSION = 2000;
    private static int openActivitiesCount = 0;

    protected boolean isExternalDialogOpened;

    private static Integer userId;
    protected static QBUser currentQBUser;

    //    private Location lastLocation;
    private boolean isActive;
//    protected static QBLocation qbLocation;
    protected boolean isReopened;

    protected QBPrivateChatManagerListener chatListener = new QBPrivateChatManagerListener() {
        @Override
        public void chatCreated(QBPrivateChat qbPrivateChat, final boolean createdLocally) {
            if (!createdLocally) {
                qbPrivateChat.addMessageListener(new QBMessageListener<QBPrivateChat>() {
                    @Override
                    public void processMessage(QBPrivateChat qbPrivateChat, final QBChatMessage qbChatMessage) {
                        onChatMessage(qbPrivateChat, qbChatMessage);
                    }

                    @Override
                    public void processError(QBPrivateChat qbPrivateChat, QBChatException e, QBChatMessage qbChatMessage) {
                        Util.onError(e, BaseActivity.this);
                    }

                });
            }
        }
    };


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
        isReopened=false;


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (isLoggedIn() && QBChatService.getInstance().isLoggedIn()) {
            try {
                onQBConnect(isReopened);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
        if (!isLoggedIn()) {
            Intent intent = new Intent(this, SplashActivity.class);
            intent.putExtra(C.RETURN_RESULT, true);
            startActivityForResult(intent, RECREATE_SESSION);
            return;
        }

        if (!QBChatService.getInstance().isLoggedIn()) {
            loginToChat();
            return;
        } else {

        }

        incrementActivityCount();

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
        QBChatService.getInstance().getPrivateChatManager().removePrivateChatManagerListener(chatListener);
        isReopened=true;
    }

    public void logOutChat() {
        if (QBChatService.getInstance().isLoggedIn()) {
            try {
                QBChatService.getInstance().logout();

            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }


    protected void loginToChat() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final QBUser qbUser = new QBUser(prefs.getString(Util.QB_USER, ""), prefs.getString(Util.QB_PASSWORD, ""));
        qbUser.setId(prefs.getInt(C.QB_USERID, 0));

        QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
            @Override
            public void onSuccess(Object o, Bundle bundle) {
                QBChatService.getInstance().startAutoSendPresence(60);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            QBPrivacyListsManager privacyListsManager = QBChatService.getInstance().getPrivacyListsManager();
                            try {
                                QBPrivacyList list = privacyListsManager.getPrivacyList("public");
                                if (list != null) {
                                    list.setDefaultList(true);
                                    list.setActiveList(true);
                                }
                                QBChatService.getInstance().getPrivateChatManager().addPrivateChatManagerListener(chatListener);
                                onQBConnect(isReopened);
                            } catch (Exception e) {
                                e.printStackTrace();
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
        });
    }


    public void onQBConnect(boolean isActivityReopened) throws Exception {

    }


    @Override
    public void onLocationChanged(Location location) {

    }


    public synchronized void incrementActivityCount() {

        openActivitiesCount++;
    }

    public synchronized void decrementActivityCount() {
        if (isExternalDialogOpened) return;
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

    public boolean isLoggedIn() {
        try {
            Date expDate = QBAuth.getBaseService().getTokenExpirationDate();
            String token = QBAuth.getBaseService().getToken();
            if (expDate == null) return false;
            return expDate.getTime() > System.currentTimeMillis() && token != null;
        } catch (BaseServiceException e) {
            e.printStackTrace();
            return false;
        }

    }

    public void onChatMessage(QBPrivateChat qbPrivateChat, final QBChatMessage qbChatMessage) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECREATE_SESSION) {
            if (resultCode == RESULT_OK) {
                try {
                   loginToChat();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                finish();
            }
        }
    }
}
