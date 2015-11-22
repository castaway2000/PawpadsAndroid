package saberapplications.pawpads;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by blaze on 10/21/2015.
 */
public class Register extends AppCompatActivity implements View.OnClickListener {

    Button bRegister;
    EditText etUsername, etPassword;

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
                String password = etPassword.getText().toString();

                //TODO: implement ssl encryption
                User user = new User(username, password);
                registerUser(user);
                break;
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
                finish();
            }
        });
    }
}
