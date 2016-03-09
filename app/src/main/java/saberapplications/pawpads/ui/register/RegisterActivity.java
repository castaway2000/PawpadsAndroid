package saberapplications.pawpads.ui.register;


import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallbackImpl;
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
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {


    Button bRegister;
    EditText etUsername, etPassword, etPasswordChk, etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("PawPads | Register");
        etUsername = (EditText) findViewById(R.id.etRegUsername);
        etPassword = (EditText) findViewById(R.id.etRegPassword);
        etPasswordChk = (EditText) findViewById(R.id.etRegPasswordChk);
        etEmail = (EditText) findViewById(R.id.etEmail);
        bRegister = ( Button ) findViewById(R.id.bRegister);
        bRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.bRegister:
                String username = etUsername.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                String passwordCHK = etPasswordChk.getText().toString();

                //TODO: implement ssl encryption
                if(password.equals(passwordCHK) && email.contains("@") && username.length() != 0 && password.length() != 0 && password.length()>=8) {

                    User user = new User(username, password, email, Util.GCMREGID);
                    registerUser(user);
                    break;
                }
                else if(username.length() == 0){
                    Toast toast = Toast.makeText(this, "Please enter a username",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if (password.length() == 0){
                    Toast toast = Toast.makeText(this, "Please enter a password",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if (password.length() < 8){
                    Toast toast = Toast.makeText(this, "Password is to short ( minimum 8 characters)",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if (!password.equals(passwordCHK)){
                    Toast toast = Toast.makeText(this, "Passwords do not match",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if(!email.contains("@")){
                    Toast toast = Toast.makeText(this, "Please input valid email",Toast.LENGTH_SHORT);
                    toast.show();
                }
        }
    }

    private void registerUser(final User user){
        GPS gps = new GPS(this);
        Location loc = null;
        try {
            loc = new Location(gps.getLastBestLocation());

        }
        catch(NullPointerException e) {
            android.util.Log.w(this.toString(),
                    "GPS.getLastBestLocation() failed -- location services may be turned off");
            // TODO Better way to tell the user that something went wrong
            Toast.makeText(
                    this,
                    "Couldn't register. Turn on location services and try again.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }
        Double lat = loc.getLatitude();
        Double lng = loc.getLongitude();
        ServerRequests serverRequests = new ServerRequests(this, lat, lng, null);

        serverRequests.storeUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(final User returnedUser) {

                QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {

                    @Override
                    public void onSuccess(QBSession session, Bundle params) {
                        final QBUser qbUser = new QBUser(user.username, user.password);
                        //qbUser.setExternalId(returnedUser.regid);
                        qbUser.setEmail(user.email);

                        QBUsers.signUp(qbUser, new QBEntityCallbackImpl<QBUser>() {
                            @Override
                            public void onSuccess(QBUser user, Bundle args) {
                                finish();
                            }

                            @Override
                            public void onError(List<String> errors) {
                                Util.onError(errors,RegisterActivity.this);
                            }
                        });

                    }

                    @Override
                    public void onError(List<String> errors) {
                        Util.onError(errors,RegisterActivity.this);
                    }
                });




            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case GPS.PermissionRequestId:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO continue processing
                    android.util.Log.i(this.toString(), "ACCESS_FINE_LOCATION was granted");
                }
                else {
                    // Do nothing. User has denied the request for location data, so registration
                    // cannot continue.
                    android.util.Log.w(this.toString(), "ACCESS_FINE_LOCATION was denied");
                }
                break;
            default:
                break;
        }
    }
}
