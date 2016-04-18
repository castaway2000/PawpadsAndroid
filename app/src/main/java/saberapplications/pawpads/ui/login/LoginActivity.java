package saberapplications.pawpads.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.List;

import saberapplications.pawpads.R;
import saberapplications.pawpads.UserLocalStore;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.service.UserLocationService;
import saberapplications.pawpads.ui.home.MainActivity;
import saberapplications.pawpads.ui.register.RegisterActivity;

/**
 * Created by blaze on 10/21/2015.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    UserLocalStore userLocalStore;
    Button bLogin;
    EditText etUsername, etPassword;
    TextView registerLink, forgotpasswordLink;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("PawPads | Login");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);
        registerLink = (TextView) findViewById(R.id.tvRegisterLink);
        forgotpasswordLink = (TextView) findViewById(R.id.tvForgottenPassLink);
        userLocalStore = new UserLocalStore(this);

        bLogin.setOnClickListener(this);
        registerLink.setOnClickListener(this);
        forgotpasswordLink.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bLogin:
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();

                QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {

                    @Override
                    public void onSuccess(final QBSession session, Bundle params) {

                        QBUser qbUser = new QBUser();
                        qbUser.setLogin(username);
                        qbUser.setPassword(password);

                        QBUsers.signIn(qbUser, new QBEntityCallbackImpl<QBUser>() {
                            @Override
                            public void onSuccess(final QBUser user, Bundle params) {

                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
                                editor.putString(Util.QB_USER, username);
                                editor.putString(Util.USER_NAME, username);
                                editor.putString(Util.QB_PASSWORD, password);
                                editor.putInt(Util.QB_USERID, user.getId());
                                editor.apply();
                                UserLocationService.startService(user.getId());
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();

                            }

                            @Override
                            public void onError(List<String> errors) {
                                Util.onError(errors, LoginActivity.this);
                            }
                        });
                    }

                    @Override
                    public void onError(List<String> errors) {
                        Util.onError(errors, LoginActivity.this);
                    }
                });


                break;
            case R.id.tvRegisterLink:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.tvForgottenPassLink:
                startActivity(new Intent(this, ForgotPasswordActivity.class));
                break;
        }
    }
}
