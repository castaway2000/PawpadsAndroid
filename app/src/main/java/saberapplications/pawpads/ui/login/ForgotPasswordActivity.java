package saberapplications.pawpads.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;

import java.util.List;

import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;


/**
 * Created by blaze on 3/24/2016.
 */
public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    //private static final int PERMISSION_REQUEST = 10000;
    Button buForgotpass;
    EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpass_activity);
        setTitle("PawPads | Reset Password");
        etEmail = (EditText) findViewById(R.id.etEmail);
        buForgotpass = (Button) findViewById(R.id.bForgotpass);
        buForgotpass.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        final String email = etEmail.getText().toString();
        switch (v.getId()) {
            case R.id.bForgotpass:
                if (!email.contains("@")) {
                    Toast toast = Toast.makeText(this, "Please input valid email", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                } else {
                    getNewPass(email);
                    startActivity(new Intent(this, LoginActivity.class));
                    Toast toast = Toast.makeText(this, "if you entered your email correctly " +
                            "you should receive an email shortly", Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }
        }
    }

    void getNewPass(final String email) {
        QBAuth.createSession(new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle params) {
                QBUsers.resetPassword(email, new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {

                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }

            @Override
            public void onError(QBResponseException responseException) {
                Util.onError(responseException, ForgotPasswordActivity.this);
            }

        });
    }
}



