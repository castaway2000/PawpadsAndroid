package saberapplications.pawpads.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.Date;

import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.service.UserLocationService;
import saberapplications.pawpads.ui.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

    }

    public boolean isLoggedIn() {
        try {
            Date expDate = QBUsers.getBaseService().getTokenExpirationDate();
            if (expDate == null) return false;
            return expDate.getTime() > System.currentTimeMillis();
        } catch (BaseServiceException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        recreateSession();
    }

    protected void recreateSession() {
        if (isLoggedIn()) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);


        QBAuth.createSession(prefs.getString(Util.QB_USER, ""), prefs.getString(Util.QB_PASSWORD, ""),
                new QBEntityCallback<QBSession>() {
                    @Override
                    public void onSuccess(final QBSession result, Bundle params) {
                        try {
                            loginToChat(result.getUserId());
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

    protected void loginToChat(final int userId) {
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

                        UserLocationService.startService(userId);
                        startActivity(new Intent(getBaseContext(), MainActivity.class));
                        finish();

                    } catch (Exception e) {
                        Util.onError(e,this);
                    }

                }
            } catch (Exception e) {
                Util.onError(e,this);
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
                                        if (QBChatService.getInstance() != null)
                                            UserLocationService.startService(userId);
                                        startActivity(new Intent(getBaseContext(), MainActivity.class));
                                        finish();
                                    } catch (
                                            Exception e
                                            )

                                    {
                                        Util.onError(e,SplashActivity.this);
                                    }

                                }


                            });
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Util.onError(e, getBaseContext());
                        }

                    }

            );
    }

}
