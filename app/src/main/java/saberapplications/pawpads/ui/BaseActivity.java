package saberapplications.pawpads.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
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
    private static int openActivitiesCount;

    @Override
    protected void onStart() {
        openActivitiesCount++;
        super.onStart();
        if (!isConnected()) {
            recreateSession();
        }else{
            onQBConnect();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        openActivitiesCount--;
        if (openActivitiesCount==0){
            logOut();

        }
    }

    private boolean isConnected(){
        try{
            return QBChatService.isInitialized() && QBAuth.getSession()!=null && QBAuth.getSession().getToken()!=null;
        }catch (Exception e){
            return  false;
        }
    }

    protected  void recreateSession(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        QBAuth.createSession(prefs.getString(Util.QB_USER, ""), prefs.getString(Util.QB_PASSWORD, ""),
                new QBEntityCallbackImpl<QBSession>() {
                    @Override
                    public void onSuccess(QBSession result, Bundle params) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
                        editor.putInt(Util.QB_USERID, result.getUserId());
                        editor.apply();
                        loginToChat();

                    }

                    @Override
                    public void onError(List<String> errors) {
                        startActivity(new Intent(getBaseContext(), LoginActivity.class));
                        finish();

                    }
                });

    }

    protected void loginToChat(){
        if (!QBChatService.isInitialized()) {
            QBChatService.init(getBaseContext());
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final QBUser qbUser = new QBUser(prefs.getString(Util.QB_USER,""),prefs.getString(Util.QB_PASSWORD,""));
        qbUser.setId(prefs.getInt(Util.QB_USERID, 0));

        QBChatService.getInstance().login(qbUser, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {
                try {
                    QBChatService.getInstance().startAutoSendPresence(60);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onQBConnect();
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
    protected void logOut(){
        try {
            QBChatService.getInstance().logout();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }
    public abstract void onQBConnect();

}
