package saberapplications.pawpads.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.users.QBUsers;

import java.util.Date;

import saberapplications.pawpads.R;
import saberapplications.pawpads.ui.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Intent intent=new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    public boolean isLoggedIn(){
        try {
            Date expDate= QBUsers.getBaseService().getTokenExpirationDate();
            if (expDate==null) return false;
            return  expDate.getTime()>System.currentTimeMillis();
        } catch (BaseServiceException e) {
            e.printStackTrace();
            return false;
        }

    }
}
