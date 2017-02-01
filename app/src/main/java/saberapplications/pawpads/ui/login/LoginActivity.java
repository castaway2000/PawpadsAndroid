package saberapplications.pawpads.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.UserLocalStore;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.ActivityLoginBinding;
import saberapplications.pawpads.ui.home.MainActivity;
import saberapplications.pawpads.ui.register.RegisterActivity;

/**
 * Created by blaze on 10/21/2015.
 */
public class LoginActivity extends AppCompatActivity {
    UserLocalStore userLocalStore;
    Button bLogin;
    EditText etUsername, etPassword;
    TextView registerLink, forgotpasswordLink;
    ImageView facebook, twitter;
    private LocationManager locationManager;
    private CallbackManager callbackManager;
    private TwitterAuthClient twitterAuthClient;
    public final ObservableBoolean isBusy = new ObservableBoolean(false);
    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setActivity(this);
        setTitle("PawPads | Login");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);
        registerLink = (TextView) findViewById(R.id.tvRegisterLink);

        Spanned sp = Html.fromHtml(getString(R.string.dont_have_account_register));
        registerLink.setText(sp);

        forgotpasswordLink = (TextView) findViewById(R.id.tvForgottenPassLink);
        userLocalStore = new UserLocalStore(this);


//FB setup
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            QBAuth.createSession();
                            QBUser user = QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, loginResult.getAccessToken().getToken(), null);
                            onSuccessLogin(user, null, loginResult.getAccessToken().getToken(), null);
                            isBusy.set(false);
                        } catch (QBResponseException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                task.execute();
            }

            @Override
            public void onCancel() {
                isBusy.set(false);
            }

            @Override
            public void onError(FacebookException error) {
                Util.onError(error, LoginActivity.this);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (twitterAuthClient != null) {
            twitterAuthClient.onActivityResult(requestCode, resultCode, data);
        }
        if (resultCode==RESULT_CANCELED){
            isBusy.set(false);
        }
    }

    private boolean isTwitterInstalled() {
        PackageManager pkManager = getPackageManager();
        try {
            PackageInfo pkgInfo = pkManager.getPackageInfo("com.twitter.android", 0);

            if (pkgInfo.packageName.equals("com.twitter.android")) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;

        }
        return false;
    }

    private void onSuccessLogin(QBUser user, String password, String token, String token_secret) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
        editor.putString(C.QB_USER, user.getLogin());
        editor.putString(C.USER_NAME, user.getFullName());
        editor.putInt(C.QB_USERID, user.getId());
        try {


            if (user.getTwitterId() != null) {
                editor.putString(C.AUTH_PROVIDER, C.TWITTER);
                editor.putString(C.AUTH_TOKEN, token);
                editor.putString(C.AUTH_TOKEN_SECRET, token_secret);
                editor.putString(C.PASSWORD, QBAuth.getBaseService().getToken());
            } else if (user.getFacebookId() != null) {
                editor.putString(C.AUTH_PROVIDER, C.FACEBOOK);
                editor.putString(C.AUTH_TOKEN, token);
                editor.putString(C.PASSWORD, QBAuth.getBaseService().getToken());
            } else {
                editor.putString(C.AUTH_PROVIDER, C.EMAIL);
                editor.putString(C.PASSWORD, password);
            }
        } catch (BaseServiceException e) {
            e.printStackTrace();
        }
        editor.apply();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void facebookLogin() {
        isBusy.set(true);
        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile"));
    }

    public void twitterLogin() {
        if (!isTwitterInstalled()) {
            Snackbar snackbar = Snackbar
                    .make(binding.coordinatorLayout, R.string.twitter_app_required, Snackbar.LENGTH_LONG);

            snackbar.show();
            return;
        }
        isBusy.set(true);
        twitterAuthClient = new TwitterAuthClient();

        TwitterLoginButton button;
        twitterAuthClient.authorize(LoginActivity.this, new Callback<TwitterSession>() {
            @Override
            public void success(final Result<TwitterSession> result) {
                final TwitterSession sessionData = result.data;
                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    Exception exception;

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {

                            QBAuth.createSession();
                            QBUser user = QBUsers.signInUsingSocialProvider(QBProvider.TWITTER, sessionData.getAuthToken().token, sessionData.getAuthToken().secret);
                            onSuccessLogin(user, null, sessionData.getAuthToken().token, sessionData.getAuthToken().secret);

                        } catch (QBResponseException e) {
                            e.printStackTrace();
                            exception = e;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        isBusy.set(false);
                        if (exception != null) {
                            Util.onError(exception, LoginActivity.this);
                        }
                    }
                };
                task.execute();

            }

            @Override
            public void failure(final TwitterException e) {
                Util.onError(e, LoginActivity.this);
            }
        });
    }

    public void emailLogin() {
        final String username = etUsername.getText().toString();
        final String password = etPassword.getText().toString();
        if (username == null || username.equals("")) {
            binding.etUsername.setError(getString(R.string.login_required));
            return;
        }
        if (password == null || password.equals("")) {
            binding.etPassword.setError(getString(R.string.password_required));
            return;
        }
        isBusy.set(true);
        QBAuth.createSession(new QBEntityCallback<QBSession>() {

            @Override
            public void onSuccess(final QBSession session, Bundle params) {

                QBUser qbUser = new QBUser();
                qbUser.setLogin(username);
                qbUser.setPassword(password);

                QBUsers.signIn(qbUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(final QBUser user, Bundle params) {
                        onSuccessLogin(user, password, null, null);
                        isBusy.set(false);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Util.onError(e, LoginActivity.this);
                        isBusy.set(false);
                    }


                });
            }

            @Override
            public void onError(QBResponseException responseException) {
                Util.onError(responseException, LoginActivity.this);
                isBusy.set(false);
            }

        });
    }

    public void register() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void restorePassword() {
        startActivity(new Intent(this, ForgotPasswordActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
