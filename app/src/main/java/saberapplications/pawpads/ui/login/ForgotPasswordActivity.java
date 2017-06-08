package saberapplications.pawpads.ui.login;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;

import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.BindableBoolean;
import saberapplications.pawpads.databinding.BindableString;
import saberapplications.pawpads.databinding.ForgotpassActivityBinding;


/**
 * Created by blaze on 3/24/2016.
 */
public class ForgotPasswordActivity extends AppCompatActivity {
    //private static final int PERMISSION_REQUEST = 10000;
    Button buForgotpass;
    EditText etEmail;
    ForgotpassActivityBinding binding;
    public  final BindableBoolean isBusy=new BindableBoolean(false);
    public final BindableString email = new BindableString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.forgotpass_activity);
        binding.setActivity(this);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.password_recovery);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.etEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                hideSoftKeyboard();
                if (actionId== EditorInfo.IME_ACTION_DONE){
                    //recover();
                    return true;
                }
                return false;
            }
        });

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void recover() {
        hideSoftKeyboard();
        if (email.isEmpty()) {
            binding.etEmail.setError(getString(R.string.email_required));
            return;
        }

        if (!Util.isEmailValid(email.get())) {
            showNotification(R.string.wrong_email_format);
            return;
        }
        isBusy.set(true);
        QBAuth.createSession(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle params) {
                QBUsers.resetPassword(email.get(), new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        isBusy.set(false);
                        new AlertDialog.Builder(ForgotPasswordActivity.this,R.style.AppAlertDialogTheme)
                                .setMessage("if you entered your email correctly you should receive an email shortly")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                })
                                .show();

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        isBusy.set(false);
                        if (e.getHttpStatusCode()==404){
                            Util.onError(getString(R.string.email_not_exists),ForgotPasswordActivity.this);
                        }else {
                            Util.onError(e,ForgotPasswordActivity.this);
                        }

                    }
                });
            }

            @Override
            public void onError(QBResponseException responseException) {
                Util.onError(responseException, ForgotPasswordActivity.this);
            }

        });



    }
    public void showNotification(int textId) {
        Toast.makeText(this,textId,Toast.LENGTH_LONG).show();
    }
    private void hideSoftKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}



