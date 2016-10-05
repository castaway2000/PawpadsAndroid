package saberapplications.pawpads.ui.register;


import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.List;

import saberapplications.pawpads.GPS;
import saberapplications.pawpads.GetUserCallback;
import saberapplications.pawpads.R;
import saberapplications.pawpads.ServerRequests;
import saberapplications.pawpads.User;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.ActivityRegisterBinding;
import saberapplications.pawpads.databinding.BindableBoolean;
import saberapplications.pawpads.databinding.BindableString;


/**
 * Created by blaze on 10/21/2015.
 */
public class RegisterActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int PERMISSION_REQUEST = 10000;

    private GoogleApiClient mGoogleApiClient;
    private Location lastLocation;
    ActivityRegisterBinding binding;
    public final ObservableBoolean isBusy = new ObservableBoolean(false);
    public final BindableString username = new BindableString();
    public final BindableString email = new BindableString();
    public final BindableString password = new BindableString();
    public final BindableString passwordConfirmation = new BindableString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        binding.setActivity(this);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.registration);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.etRegPasswordChk.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId== EditorInfo.IME_ACTION_DONE){
                    register();
                    return true;
                }
                return false;
            }
        });


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
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST);
                return false;
            }

        }

        return true;
    }

    private void registerUser(final User user) {

        if (lastLocation == null) {
            lastLocation = new Location("null");
        }
        Double lat = lastLocation.getLatitude();
        Double lng = lastLocation.getLongitude();
        ServerRequests serverRequests = new ServerRequests(this, lat, lng, null);
        isBusy.set(true);
        serverRequests.storeUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(final User returnedUser) {

                QBAuth.createSession(new QBEntityCallback<QBSession>() {

                    @Override
                    public void onSuccess(QBSession session, Bundle params) {

                        final QBUser qbUser = new QBUser(user.username, user.password);
                        //qbUser.setExternalId(returnedUser.regid);
                        qbUser.setEmail(user.email);

                        QBUsers.signUp(qbUser, new QBEntityCallback<QBUser>() {
                            @Override
                            public void onSuccess(QBUser user, Bundle args) {
                                finish();
                                isBusy.set(false);
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Util.onError(e, RegisterActivity.this);
                                isBusy.set(false);
                            }

                        });

                    }

                    @Override
                    public void onError(QBResponseException responseException) {
                        Util.onError(responseException, RegisterActivity.this);
                        isBusy.set(false);
                    }

                });


            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    android.util.Log.i(this.toString(), "ACCESS_FINE_LOCATION was granted");

                    User user = new User(username.get(), password.get(), email.get(), Util.GCMREGID);
                    lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    registerUser(user);
                } else {
                    // Do nothing. User has denied the request for location data, so registration
                    // cannot continue.
                    android.util.Log.w(this.toString(), "ACCESS_FINE_LOCATION was denied");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void showSnack(int textId) {
        Snackbar snackbar = Snackbar
                .make(binding.coordinatorLayout, textId, Snackbar.LENGTH_LONG);
        snackbar.show();
    }



    public void register() {
        // validation
        boolean fieldsEmpty = false;
        if (username.isEmpty()) {
            fieldsEmpty = true;
            binding.etRegUsername.setError(getString(R.string.name_required));
        }
        if (email.isEmpty()) {
            fieldsEmpty = true;
            binding.etEmail.setError(getString(R.string.email_required));
        }

        if (password.isEmpty()) {
            fieldsEmpty = true;
            binding.etRegPassword.setError(getString(R.string.password_required));
        }
        if (passwordConfirmation.isEmpty()) {
            fieldsEmpty = true;
            binding.etRegPasswordChk.setError(getString(R.string.password_required));
        }
        if (fieldsEmpty) return;

        if (!Util.isEmailValid(email.get())) {
            showSnack(R.string.wrong_email_format);
            return;
        }
        if (password.get().length() < 8) {
            showSnack(R.string.password_to_short);
            return;
        }
        if (!password.equals(passwordConfirmation)) {
            showSnack(R.string.password_not_match);
        }

        User user = new User(username.get(), password.get(), email.get(), Util.GCMREGID);
        if (checkPermissions()) {
            registerUser(user);
        }
    }

}
