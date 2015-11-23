package saberapplications.pawpads;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by blaze on 10/21/2015.
 */
public class Register extends AppCompatActivity implements View.OnClickListener {

    Button bRegister;
    EditText etUsername, etPassword, etPasswordChk, etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("PawPads | Register");


        etUsername = (EditText) findViewById(R.id.etRegUsername);
        etPassword = (EditText) findViewById(R.id.etRegPassword);
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
                if(password == passwordCHK && email.contains("@") && username.length() != 0 && password.length() != 0) {
                    User user = new User(username, password, email);
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
                else if (password != passwordCHK){
                    Toast toast = Toast.makeText(this, "Passwords do not match",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if(!email.contains("@")){
                    Toast toast = Toast.makeText(this, "Please input valid email",Toast.LENGTH_SHORT);
                    toast.show();
                }
        }
    }

    private void registerUser(User user){
        GPS gps = new GPS(this);
        Location loc = new Location(gps.getLastBestLocation());
        Double lat = loc.getLatitude();
        Double lng = loc.getLongitude();
        ServerRequests serverRequests = new ServerRequests(this, lat, lng, null);
        serverRequests.storeUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                //TODO: start edit profile intent
                finish();
            }
        });
    }
}
