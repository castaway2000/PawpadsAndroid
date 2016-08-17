package saberapplications.pawpads.ui.register;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


/**
 * Created by blaze on 10/21/2015.
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private static final int PERMISSION_REQUEST = 10000;
    Button bRegister;
    EditText etUsername, etPassword, etPasswordChk, etEmail;
    private GoogleApiClient mGoogleApiClient;
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("PawPads | Register");
        etUsername = (EditText) findViewById(R.id.etRegUsername);
        etPassword = (EditText) findViewById(R.id.etRegPassword);
        etPasswordChk = (EditText) findViewById(R.id.etRegPasswordChk);
        etEmail = (EditText) findViewById(R.id.etEmail);
        bRegister = (Button) findViewById(R.id.bRegister);
        bRegister.setOnClickListener(this);

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
        if (mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bRegister:
                String username = etUsername.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                String passwordCHK = etPasswordChk.getText().toString();

                //TODO: implement ssl encryption
                if (password.equals(passwordCHK) && email.contains("@") && username.length() != 0 && password.length() != 0 && password.length() >= 8) {

                    User user = new User(username, password, email, Util.GCMREGID);
                    if (checkPermissions()){
                        registerUser(user);
                    }
                    break;
                } else if (username.length() == 0) {
                    Toast toast = Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT);
                    toast.show();
                } else if (password.length() == 0) {
                    Toast toast = Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT);
                    toast.show();
                } else if (password.length() < 8) {
                    Toast toast = Toast.makeText(this, "Password is to short ( minimum 8 characters)", Toast.LENGTH_SHORT);
                    toast.show();
                } else if (!password.equals(passwordCHK)) {
                    Toast toast = Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT);
                    toast.show();
                } else if (!email.contains("@")) {
                    Toast toast = Toast.makeText(this, "Please input valid email", Toast.LENGTH_SHORT);
                    toast.show();
                }
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

        if (lastLocation==null){
            lastLocation=new Location("null");
        }
        Double lat = lastLocation.getLatitude();
        Double lng = lastLocation.getLongitude();
        ServerRequests serverRequests = new ServerRequests(this, lat, lng, null);

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
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Util.onError(e, RegisterActivity.this);
                            }

                        });

                    }

                    @Override
                    public void onError(QBResponseException responseException) {
                        Util.onError(responseException, RegisterActivity.this);
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
                    String username = etUsername.getText().toString();
                    String email = etEmail.getText().toString();
                    String password = etPassword.getText().toString();
                    User user = new User(username, password, email, Util.GCMREGID);
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
}
