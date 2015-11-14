package saberapplications.pawpads;

import android.content.Intent;
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
    EditText etName, etAge, etUsername, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("PawPads | Register");

        etName = (EditText) findViewById(R.id.etRegName);
        etAge = (EditText) findViewById(R.id.etRegAge);
        etUsername = (EditText) findViewById(R.id.etRegUsername);
        etPassword = (EditText) findViewById(R.id.etRegPassword);
        bRegister = ( Button ) findViewById(R.id.bRegister);

        bRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.bRegister:
                String name = etName.getText().toString();
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                int age = Integer.parseInt(etAge.getText().toString());

                User user = new User(name, age, username, password);


                registerUser(user);
                break;
        }

    }

    private void registerUser(User user){
        ServerRequests serverRequests = new ServerRequests(this);
        serverRequests.storeUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                finish();
            }
        });
    }

}
