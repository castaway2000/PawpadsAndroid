package saberapplications.pawpads.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.Date;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.ui.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    boolean returnResult;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_splash);
        if (getIntent() != null) {
            returnResult = getIntent().getBooleanExtra(C.RETURN_RESULT, false);
        }
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
        QBAuth.createSession(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                switch (prefs.getString(C.AUTH_PROVIDER,"")){
                    case C.EMAIL:
                        recreateSessionEmail();
                        break;
                    case C.FACEBOOK:
                        recreateSessionFB();
                        break;
                    case C.TWITTER:
                        recreateSessionTwitter();
                        break;
                    default:
                        onLoginFail();

                }
            }

            @Override
            public void onError(QBResponseException e) {
                Util.onError(e,SplashActivity.this);
            }
        });

    }

    protected void recreateSessionEmail() {
        QBUser qbUser=new QBUser(prefs.getString(C.QB_USER, ""), prefs.getString(C.PASSWORD, ""));
        QBUsers.signIn(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                onSuccessLogin();
            }

            @Override
            public void onError(QBResponseException e) {
                onLoginFail();
            }
        });


    }

    protected void recreateSessionFB() {
        String fbToken = prefs.getString(C.AUTH_TOKEN, "");
        //QBUser user = QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, loginResult.getAccessToken().getToken(), null);
        QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, fbToken, null, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                onSuccessLogin();
            }

            @Override
            public void onError(QBResponseException e) {
                onLoginFail();
            }
        });
    }
    private void recreateSessionTwitter(){
        String twitterToken=prefs.getString(C.AUTH_TOKEN,"");
        String twitterSectet=prefs.getString(C.AUTH_TOKEN_SECRET,"");
        QBUsers.signInUsingSocialProvider(QBProvider.TWITTER, twitterToken, twitterSectet, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                onSuccessLogin();
            }

            @Override
            public void onError(QBResponseException e) {
                onLoginFail();
            }
        });
    }

    private void onSuccessLogin() {
        if (returnResult) {
            setResult(RESULT_OK);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private void onLoginFail() {
        setResult(RESULT_CANCELED);
        startActivity(new Intent(getBaseContext(), LoginActivity.class));
        finish();
        LocalBroadcastManager.getInstance(SplashActivity.this).sendBroadcast(new Intent(C.CLOSE_ALL_APP_ACTIVITIES));

    }

}
